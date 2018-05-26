package com.etybeno.openrtb.server;

import com.etybeno.common.config.ApplicationConfiguration;
import com.etybeno.common.util.HttpClientUtil;
import com.etybeno.common.util.StringPool;
import com.etybeno.common.util.StringUtil;
import com.etybeno.netty.util.NettyHttpUtil;
import com.etybeno.openrtb.model.BidRequestModel;
import com.etybeno.openrtb.util.Constant;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static io.netty.handler.codec.http.HttpHeaders.Names.USER_AGENT;

/**
 * Created by thangpham on 16/04/2018.
 */
public class UriMapper {

    private static Logger LOGGER = LogManager.getLogger(UriMapper.class);

    public static final String AUCTION_PATH = "/auction";
    //
    private static final BidRequestModel DEFAULT_BID = new BidRequestModel();
    private static List<String> BIDDER_LIST = null;
    private static double MIN_BID = 0;
    private static String NON_BID_RESPONSE = null;

    static {
        try {
            ApplicationConfiguration appConfig = ApplicationConfiguration._load();
            BIDDER_LIST = appConfig.getConfig().getList(String.class, Constant.ApplicationKey.BIDDER_LIST_KEY);
            MIN_BID = appConfig.getConfig().getDouble(Constant.ApplicationKey.MIN_BID_KEY);
            NON_BID_RESPONSE = appConfig.getConfig().getString(Constant.ApplicationKey.NON_BID_RESPONSE_KEY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static FullHttpResponse buildHttpResponse(String ipAddress, ChannelHandlerContext ctx,
                                                     HttpRequest request, String uri) throws IOException, ConfigurationException {
        if (uri.startsWith(AUCTION_PATH)) {
            QueryStringDecoder qdecoder = new QueryStringDecoder(uri);
            Map<String, List<String>> params = qdecoder.parameters();
            final String subid = NettyHttpUtil.getParamValue("subid", params);
            final String defaultUrl = NettyHttpUtil.getParamValue("durl", params);
            double minBid = StringUtil.safeParseDouble(NettyHttpUtil.getParamValue("mbid", params), MIN_BID);
            String ua = request.headers().get(USER_AGENT);
            //
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            List<Future<BidRequestModel>> list = new ArrayList<>();
            for (String bidder : BIDDER_LIST) {
                CallAPITask task = new CallAPITask(bidder, subid, ipAddress, ua);
                Future<BidRequestModel> future = executorService.submit(task);
                list.add(future);
            }
            Map<String, String> allBid = new HashMap();
            String redirect = defaultUrl;
            for (Future<BidRequestModel> call : list) {
                try {
                    BidRequestModel bidRequestModel = call.get();
                    if (bidRequestModel.getResultBid() >= minBid) {
                        minBid = bidRequestModel.getResultBid();
                        redirect = bidRequestModel.getResultUrl();
                    }
                    if (DEFAULT_BID != bidRequestModel)
                        allBid.put(String.valueOf(bidRequestModel.getResultBid()), bidRequestModel.getResultUrl());
                } catch (Exception e) { //Ignore this bid
                    LOGGER.error(String.format("Error when get receive call %s", StringUtil.GSON.toJson(call)), e);
                }
            }
            //shut down the executor service now
            executorService.shutdown();
            FullHttpResponse response;
            if(null == redirect || redirect.isEmpty()) response = NettyHttpUtil.theHttpContent(NON_BID_RESPONSE);
            else response = NettyHttpUtil.redirect(redirect);
            String headerOrigin = request.headers().get("Origin");
            if (!StringUtil.isNullOrEmpty(headerOrigin)) {
                response.headers().set("Access-Control-Allow-Origin", headerOrigin);
                response.headers().set("Access-Control-Allow-Methods", "GET");
                response.headers().set("Access-Control-Allow-Credentials", "true");
            }
            if (!allBid.isEmpty())
                LOGGER.info(String.format("Response at %s against %s",
                        redirect, StringUtil.GSON.toJson(allBid)));
            return response;
        }
        return NettyHttpUtil.theHttpContent(StringPool.NOT_SUPPORT);
    }
    private static class CallAPITask implements Callable<BidRequestModel> {
        private String url, subId, ua, ipAdress;

        CallAPITask(String url, String subId, String ipAdress, String ua) {
            this.url = url;
            this.subId = subId;
            this.ua = ua;
            this.ipAdress = ipAdress;
        }

        @Override
        public BidRequestModel call() throws Exception {
            String jsonData = null, formedUrl = null;
            try {
                String replace1 = StringUtils.replace(url, "{SUB_ID}", URLEncoder.encode(subId, StringPool.UTF_8));
                String replace2 = StringUtils.replace(replace1, "{IP}", URLEncoder.encode(ipAdress, StringPool.UTF_8));
                formedUrl = StringUtils.replace(replace2, "{USER_AGENT}", URLEncoder.encode(ua, StringPool.UTF_8));
                HttpClientUtil httpClientUtil = HttpClientUtil._load();
                jsonData = httpClientUtil.executeGet(formedUrl);
                return StringUtil.GSON.fromJson(jsonData, BidRequestModel.class);
            } catch (Exception ex) {
                return DEFAULT_BID;
            }
        }
    }

}
