package com.ahao.netty.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ahao.netty.util.PropertyConfigUtil;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.AttributeKey;

public class WebsocketServer implements IHttpServer,IWebsocketServer{

    public static void main(String[] args) {
        new WebsocketServer(8081).start();
    }
    
    private static final Logger logger = LoggerFactory.getLogger(WebsocketServer.class);
    private static PropertyConfigUtil property = 
            PropertyConfigUtil.getInstance("config.properties");
    
    private static final String HN_HTTP_CODEC = "HN_HTTP_CODEC";
    private static final String HN_HTTP_AGGREGATOR = "HN_HTTP_AGGREGATOR";
    private static final String HN_HTTP_CHUNK = "HN_HTTP_CHUNK";
    private static final String HN_SERVER = "HN_HTTP_LOGIC";
    private static final int MAX_CONTENT_LENGTH = 65536;
    
    private static final String WEBSOCKET_UPGRADE = "websocket";
    private static final String WEBSOCKET_CONNECTION = "Upgrade";
    private static final String WEBSOCKET_URL_ROOT_PATTERN = "ws://%s:%d";
    
    private static final AttributeKey<WebSocketServerHandshaker> ATTR_HANDSHAKER = 
            AttributeKey.newInstance("ATTR_KEY_CHANNELID");
    
    private String host;
    private int port;
    private final String WEBSOCKET_ROOT_URL;
    
    public WebsocketServer(int port) {  
    	this("localhost", port);  
    } 
    
    public WebsocketServer(String host, int port) {
    	String ip = property.getValue("host");
    	Integer porta = property.getIntValue("port");
    	logger.info("The host is :{}, port is :{}", ip, porta);
        this.host = (ip == null || "".equals("")) ? host : ip;
        this.port = porta == null ? port : porta;
        WEBSOCKET_ROOT_URL = String.format(WEBSOCKET_URL_ROOT_PATTERN, host, port);
    }
    
    private Map<ChannelId, Channel> channelMap = new ConcurrentHashMap<ChannelId, Channel>();
    
    public void start() {
        EventLoopGroup connectGroup = new NioEventLoopGroup();
        EventLoopGroup dataHandlerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(connectGroup, dataHandlerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<Channel>() {

                    @Override
                    protected void initChannel(Channel ch) throws Exception {

                        ChannelPipeline channelPipeline = ch.pipeline();
                        channelMap.put(ch.id(), ch);
                        logger.info("--****new channel : {}", ch);
                        ch.closeFuture().addListener(new ChannelFutureListener() {
                            
                            @Override
                            public void operationComplete(ChannelFuture future) throws Exception {

                                logger.info("--****channel close {}", future.channel());
                                channelMap.remove(future.channel().id());
                                logger.info("--****channel remain {}", channelMap);
                            }
                        });
                        
                        channelPipeline.addLast(HN_HTTP_CODEC, new HttpServerCodec());
                        channelPipeline.addLast(HN_HTTP_AGGREGATOR, new HttpObjectAggregator(MAX_CONTENT_LENGTH));
                        channelPipeline.addLast(HN_HTTP_CHUNK, new ChunkedWriteHandler());
                        channelPipeline.addLast(HN_SERVER, 
                                new WebsocketServerHandler(WebsocketServer.this, WebsocketServer.this));
                    }
                });
        
        try {
            ChannelFuture channelFuture = bootstrap.bind(host, port).addListener(new ChannelFutureListener() {
                
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {

                    logger.info("--****websocket start ....");
                }
            }).sync();
            channelFuture.channel().closeFuture().addListener(new ChannelFutureListener() {
                
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {

                    logger.info("--****server channel {} closed...", future.channel());
                }
            }).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            connectGroup.shutdownGracefully();
            dataHandlerGroup.shutdownGracefully();
        }
        logger.info("--****websocket server shutdown...");
    }
    
    @Override
    public void handleWebsocketFrme(ChannelHandlerContext ctx, WebSocketFrame frame) {

        if (frame instanceof TextWebSocketFrame) {
            String text = ((TextWebSocketFrame) frame).text();
            TextWebSocketFrame repFrame = new TextWebSocketFrame(text);
            logger.info("--****receive textwebsocketmsg from channel {}", ctx.channel());
            for (Channel ch : channelMap.values()) {
                if (ctx.channel().equals(ch)) {
                    continue;
                }
                ch.writeAndFlush(repFrame.retain());
                logger.info("--****write data[{}] to channel {} success...", text, ch);
            }
            return;
        } else if (frame instanceof PongWebSocketFrame) {
            logger.info("--****receive pongwebsocketmsg from channel {}", ctx.channel());
            return;
        } else if (frame instanceof PingWebSocketFrame) {
            logger.info("--****receive pingwebsocketmsg from channel {}", ctx.channel());
            ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        } else if (frame instanceof CloseWebSocketFrame) {
            logger.info("--****receive closewebsocketmsg from channel {}", ctx.channel());
            WebSocketServerHandshaker handshaker = ctx.channel().attr(ATTR_HANDSHAKER).get();
            if (handshaker == null) {
                logger.error("--****channel {} has no handshaker...", ctx.channel());
                return;
            }
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        
        logger.info("--****receive unknown binary frame...");
    }

    @Override
    public void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {

        if (isWebsocketUpgrade(request)) {
            logger.info("--****fetch a websocket requst...");
            String subProtocols = request.headers().get(HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL);
            logger.error("--****subProtocols is : {}", subProtocols);
            WebSocketServerHandshakerFactory handshakerFactory = 
                    new WebSocketServerHandshakerFactory(WEBSOCKET_ROOT_URL, subProtocols, false);
            WebSocketServerHandshaker handshaker = handshakerFactory.newHandshaker(request);
            if (handshaker == null) {
                logger.error("--****handshaker is : {}", handshaker);
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            } else {
                handshaker.handshake(ctx.channel(), request);
                ctx.channel().attr(ATTR_HANDSHAKER).set(handshaker);
            }
            return;
        }
        
        logger.info("--****this is a normal request...");
    }
    
    private boolean isWebsocketUpgrade(FullHttpRequest request) {
        HttpHeaders headers = request.headers();
        boolean isUpgrade = request.method().equals(HttpMethod.GET)
                && headers.get(HttpHeaderNames.UPGRADE).contains(WEBSOCKET_UPGRADE)
                && headers.get(HttpHeaderNames.CONNECTION).contains(WEBSOCKET_CONNECTION);
        return isUpgrade;
    }
	
}