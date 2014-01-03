package com.athena.dolly.websocket.server;


import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;


@Component
public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {
	
	@Inject
	private WebSocketServerHandler handler;
	
    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("codec-http", new HttpServerCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(65536)); // == Netty 3.0 HttpChunkAggregator
        //pipeline.addLast("aggregator-websocket", new WebSocketFrameAggregator(65536));
        pipeline.addLast("handler", handler);
        
        //WebSocketFrameAggregator
    }
}