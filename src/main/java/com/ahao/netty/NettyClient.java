package com.ahao.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClient {

    private final int port;
    private final String host;
    
    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    public void connect() throws Exception {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .remoteAddress(host, port)
                    .handler(new ChannelInitializer<Channel>() {

                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline().addLast(new NettyClientHandler());
                        }
                    });
            ChannelFuture futrue = bootstrap.connect().sync();
            futrue.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }
    
    public static void main(String[] args) throws Exception {
        new NettyClient("127.0.0.1", 8080).connect();
    }
}
