package com.etybeno.detectclient.server;

import com.etybeno.common.config.ApplicationConfiguration;
import com.etybeno.common.model.Pager;
import com.etybeno.common.util.StringPool;
import com.etybeno.common.util.StringUtil;
import com.etybeno.detectclient.model.IpLocationModel;
import com.etybeno.detectclient.service.LocationService;
import com.etybeno.netty.util.NettyHttpUtil;
import com.etybeno.netty.util.StaticFileHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by thangpham on 21/05/2018.
 */
public class UriMapper {

    private static Logger LOGGER = LoggerFactory.getLogger(UriMapper.class);

    public static final String DETECT_UA_PATH = "/detect_ua";
    public static final String DETECT_IP_PATH = "/detect_ip";
    //

    static {
        try {
            ApplicationConfiguration appConfig = ApplicationConfiguration._load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static FullHttpResponse buildHttpResponse(String ipAddress, ChannelHandlerContext ctx,
                                                     HttpRequest request, String uri) throws IOException, ConfigurationException {
        FullHttpResponse fullHttpResponse = null;
        if (uri.startsWith(DETECT_UA_PATH)) {
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
            Map<String, List<String>> params = queryStringDecoder.parameters();
            ipAddress = NettyHttpUtil.getParamValue("ip", params, ipAddress);
            LocationService locationService = LocationService._load();
            IpLocationModel locationModel = locationService.getLocationFromIpAddress(ipAddress);
            fullHttpResponse = StaticFileHandler.staticFileResponse(
                    StringUtil.OBJECT_MAPPER.writeValueAsBytes(locationModel), StringPool.MIME_TYPE_JSON);
        } else if(uri.startsWith(DETECT_IP_PATH)) {
        }
        String headerOrigin = request.headers().get("Origin");
        if (fullHttpResponse != null &&  !StringUtil.isNullOrEmpty(headerOrigin)) {
            fullHttpResponse.headers().set("Access-Control-Allow-Origin", headerOrigin);
            fullHttpResponse.headers().set("Access-Control-Allow-Methods", "GET");
            fullHttpResponse.headers().set("Access-Control-Allow-Credentials", "true");
        }
        return fullHttpResponse;
    }

}
