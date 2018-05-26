package com.etybeno.openrtb.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;


/**
 * Created by thangpham on 16/04/2018.
 */
public class OpenRTBChannelInitializer extends ChannelInitializer<SocketChannel> {

    private SslContext sslContext;

    public OpenRTBChannelInitializer(SslContext sslContext) {
        this.sslContext = sslContext;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        if (sslContext != null) {
            pipeline.addLast(sslContext.newHandler(ch.alloc()));
        }
        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("encoder", new HttpResponseEncoder());
        // Compress
        pipeline.addLast("deflater", new HttpContentCompressor(1));
        pipeline.addLast("handler", new BidChannelHandler());
    }
}
