package com.etybeno.detectclient.server;

import com.etybeno.common.util.StringPool;
import com.etybeno.netty.util.NettyHttpUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by thangpham on 21/05/2018.
 */
public class DetectClientChannelHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger LOGGER = LogManager.getLogger(DetectClientChannelHandler.class.getName());

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.close();
        //Do nothing
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        long s = System.currentTimeMillis();
        FullHttpResponse response = null;
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            String uri = request.uri();
            String ipAddress = NettyHttpUtil.getRequestIP(ctx, request);
            try {
                response = UriMapper.buildHttpResponse(ipAddress, ctx, request, uri);
            } catch (Exception e) {
                LOGGER.error("Error when process request", e);
            }
        }

        if (response == null) response = NettyHttpUtil.theHttpContent(StringPool.NOT_SUPPORT);
        // Write the response.
        ChannelFuture future = ctx.writeAndFlush(response);
        ctx.close();
        //Close the non-keep-alive connection after the write operation is done.
        future.addListener(ChannelFutureListener.CLOSE);
        //
        long takes = System.currentTimeMillis() - s;
        if (takes > 600)
            LOGGER.info("actual times takes " + takes);
    }
}
