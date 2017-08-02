package com.ahao.netty.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public class WebsocketServerHandler extends SimpleChannelInboundHandler<Object>{
    
    private static final Logger logger = LoggerFactory.getLogger(WebsocketServer.class);

    private IHttpServer httpServer;
    
    private IWebsocketServer websocketServer;
    
    public WebsocketServerHandler(IHttpServer httpServer, IWebsocketServer websocketServer) {
        super();
        this.httpServer = httpServer;
        this.websocketServer = websocketServer;
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof FullHttpRequest) {
            httpServer.handleHttpRequest(ctx, (FullHttpRequest) msg);
            return;
        } else if (msg instanceof WebSocketFrame) {
            websocketServer.handleWebsocketFrme(ctx, (WebSocketFrame) msg);
            return;
        } 
        
        logger.info("--****fetch a unknown msg : {}", msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

}