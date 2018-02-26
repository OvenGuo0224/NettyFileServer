package com.ovenguo.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class HttpFileServer {
    private final int port = 80;
    private final String localDir = "d:/";

    public void run() throws Exception {
        EventLoopGroup acceptorGroup = new NioEventLoopGroup();
        EventLoopGroup clientGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBookstrap = new ServerBootstrap();
            serverBookstrap.group(acceptorGroup, clientGroup).channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel sc) throws Exception {
                            sc.pipeline().addLast("http-decoder", new HttpRequestDecoder());
                            sc.pipeline().addLast("http-aggregator", new HttpObjectAggregator(1024
                                    * 1024));
                            sc.pipeline().addLast("http-encoder", new HttpResponseEncoder());
                            sc.pipeline().addLast("http-handler", new HttpRequestHandler(localDir));
                        }
                    });
            ChannelFuture channelFuture = serverBookstrap.bind(port).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            acceptorGroup.shutdownGracefully();
            clientGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new HttpFileServer().run();
    }

}
