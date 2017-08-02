package com.ahao.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    Logger logger = LoggerFactory.getLogger(getClass());
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("--****channelActive");
        ctx.writeAndFlush(Unpooled.copiedBuffer("this is a client repead...", CharsetUtil.UTF_8));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.info("--****channelRead; msg is : {}", msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        logger.info("--****channelReadComplete");
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.info("--****exceptionCaught; cause is : {}", cause);
        ctx.close();
    }

}
