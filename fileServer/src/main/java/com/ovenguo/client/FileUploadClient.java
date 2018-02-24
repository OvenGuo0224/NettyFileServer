package com.ovenguo.client;

import com.ovenguo.bean.FileUploadFile;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.io.File;

public class FileUploadClient {

    public void connect(int port, String host, final FileUploadFile fileUploadFile) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioServerSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<Channel>() {
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline().addLast(new ObjectEncoder());
                            channel.pipeline().addLast(new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(null)));
                            channel.pipeline().addLast(new FileUploadClientHandler(fileUploadFile));
                        }
                    });
            ChannelFuture future = bootstrap.connect(host, port).sync();
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        int port = 8080;
        if (args != null && args.length >0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        try {
            FileUploadFile uploadFile = new FileUploadFile();
            File file = new File("d:/1.txt");
            String fileMd5 = file.getName();
            uploadFile.setFile(file);
            uploadFile.setFile_md5(fileMd5);
            uploadFile.setStarPos(0);
            new FileUploadClient().connect(port, "127.0.0.1", uploadFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
