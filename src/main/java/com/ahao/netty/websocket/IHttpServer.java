package com.ahao.netty.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public interface IHttpServer {

    void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request);
}
