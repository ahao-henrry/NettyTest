package com.ahao.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer {
    Logger logger = LoggerFactory.getLogger(getClass());
    private final int port;
    
    public NettyServer(int port) {
        this.port = port;
    }
    
    public void start() throws Exception {

        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(group).channel(NioServerSocketChannel.class)
                    .localAddress(port)
                    .childHandler(new ChannelInitializer<Channel>() {

                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline().addLast(new NettyServerHandler());
                        }
                    });
            ChannelFuture futrue = bootstrap.bind().sync();
            logger.info("--****server start on host : {}", bootstrap.localAddress(port));
            futrue.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }
    
    public static void main(String[] args) {
        try {
            new NettyServer(8080).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
