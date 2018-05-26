package com.etybeno.detectclient.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslContext;

/**
 * Created by thangpham on 21/05/2018.
 */
public class DetectClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    private SslContext sslContext;

    public DetectClientChannelInitializer(SslContext sslContext) {
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
        pipeline.addLast("handler", new DetectClientChannelHandler());
    }
}
