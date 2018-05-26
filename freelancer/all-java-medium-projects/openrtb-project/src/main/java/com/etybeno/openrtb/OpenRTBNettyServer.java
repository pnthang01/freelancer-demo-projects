package com.etybeno.openrtb;

import com.etybeno.common.util.HttpClientUtil;
import com.etybeno.openrtb.configuration.NettyServerConfiguration;
import com.etybeno.openrtb.server.OpenRTBChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.security.cert.CertificateException;

/**
 * Created by thangpham on 16/04/2018.
 */
public class OpenRTBNettyServer {

    private static Logger LOGGER = LogManager.getLogger(OpenRTBNettyServer.class);

    public static void main(String[] args) throws IOException, ConfigurationException, InterruptedException, CertificateException {
//        Map<String, String> mainConfig = StringUtil.parseMainArgs(args, new HashSet(Arrays.asList("dir_config")));
        //dir_config must end with directory separator
//        BaseConfiguration.setBaseConfig(mainConfig.get("dir_config"));
        HttpClientUtil._initialize(200, 5);
        OpenRTBNettyServer nettyServer = new OpenRTBNettyServer();
        nettyServer.run();
    }

    static final boolean SSL = System.getProperty("ssl") != null;
    private String HOST;
    private int PORT;

    public OpenRTBNettyServer() throws IOException, ConfigurationException {
        NettyServerConfiguration nettyConfig = NettyServerConfiguration._load();
        this.HOST = nettyConfig.getServer();
        this.PORT = nettyConfig.getPort();
    }

    public void run() throws InterruptedException, CertificateException, SSLException {
        // Configure SSL.
        final SslContext sslCtx;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }
        EventLoopGroup bossGroup = new NioEventLoopGroup(4);
        EventLoopGroup workerGroup = new NioEventLoopGroup(16);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);
            b.option(ChannelOption.SO_BACKLOG, Integer.MAX_VALUE);
            b.childOption(ChannelOption.TCP_NODELAY, false)
                    .childOption(ChannelOption.SO_KEEPALIVE, false)
                    .childHandler(new OpenRTBChannelInitializer(null));

            Channel ch = b.bind(PORT).sync().channel();
            LOGGER.info("Start OpenRTBNettyServer at " + HOST + ":" + PORT);
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
