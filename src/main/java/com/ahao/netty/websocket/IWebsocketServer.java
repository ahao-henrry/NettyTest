package com.ahao.netty.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public interface IWebsocketServer {
    
    void handleWebsocketFrme(ChannelHandlerContext ctx, WebSocketFrame frame);
}
