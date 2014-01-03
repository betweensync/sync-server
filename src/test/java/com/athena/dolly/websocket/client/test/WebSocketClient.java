/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
//The MIT License
//
//Copyright (c) 2009 Carl Bystršm
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in
//all copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//THE SOFTWARE.
package com.athena.dolly.websocket.client.test;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class WebSocketClient {

	private final URI uri;
	
	private Channel ch;
	private EventLoopGroup group;

	public WebSocketClient(URI uri) {
		this.uri = uri;
	}
	
	private void initialize() throws InterruptedException {
        group = new NioEventLoopGroup();
        
        Bootstrap bootstrap = new Bootstrap();
        String protocol = uri.getScheme();
        if (!"ws".equals(protocol)) {
            throw new IllegalArgumentException("Unsupported protocol: " + protocol);
        }

        HttpHeaders customHeaders = new DefaultHttpHeaders();
        customHeaders.add("MyHeader", "MyValue");

        // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
        // If you change it to V00, ping is not supported and remember to change
        // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
        final WebSocketClientHandler handler =
                new WebSocketClientHandler(
                        WebSocketClientHandshakerFactory.newHandshaker(
                                uri, WebSocketVersion.V13, null, false, customHeaders));

        bootstrap.group(group)
         .channel(NioSocketChannel.class)
         .handler(new ChannelInitializer<SocketChannel>() {
             @Override
             public void initChannel(SocketChannel ch) throws Exception {
                 ChannelPipeline pipeline = ch.pipeline();
                 pipeline.addLast("http-codec", new HttpClientCodec());
                 pipeline.addLast("aggregator", new HttpObjectAggregator(8192));
                 pipeline.addLast("ws-handler", handler);
             }
         });

        System.out.println("WebSocket Client connecting");
        ch = bootstrap.connect(uri.getHost(), uri.getPort()).sync().channel();
        handler.handshakeFuture().sync();
		
	}

	public void sendFile(File file) throws Exception {
        // Send 10 messages and wait for responses
        System.out.println("WebSocket Client sending file ->" + file.getAbsolutePath());
        //for (int i = 0; i < 10; i++) {
        //    ch.writeAndFlush(new TextWebSocketFrame("Message #" + i));
        //}
        
        ch.writeAndFlush(new TextWebSocketFrame(file.getAbsolutePath()));
        
        // Binary File Data Send
        byte[] buf = new byte[65536];
        int len = 0;
        
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        
        while ((len = bis.read(buf)) != -1) {
        	ch.write(new BinaryWebSocketFrame(Unpooled.copiedBuffer(buf, 0, len)));
        }
        
        ch.flush();
        System.out.println("File send succeed");
        
        bis.close();
        
        // Ping
        System.out.println("WebSocket Client sending ping");
        ch.writeAndFlush(new PingWebSocketFrame(Unpooled.copiedBuffer(new byte[]{1, 2, 3, 4, 5, 6})));


        
    }

	private void shutdown() throws InterruptedException {
        // Close
        System.out.println("WebSocket Client sending close");
        ch.writeAndFlush(new CloseWebSocketFrame());

        // WebSocketClientHandler will close the connection when the server
        // responds to the CloseWebSocketFrame.
        ch.closeFuture().sync();
		group.shutdownGracefully();
	}
		

	public static void main(String[] args) throws Exception {
		URI uri;
		if (args.length > 0) {
			uri = new URI(args[0]);
		} else {
			uri = new URI("ws://localhost:7700/websocket");
		}
		WebSocketClient client = new WebSocketClient(uri);
		client.initialize();
		
		File directory = new File("C:/Private");
		Collection<File> files = FileUtils.listFiles(directory, TrueFileFilter.TRUE, TrueFileFilter.TRUE);
		
		for (Iterator<File> iterator = files.iterator(); iterator.hasNext();) {
            File file = (File) iterator.next();
            client.sendFile(file);
        }
		//client.sendFile(new File("C:/Temp/netty-master.zip"));
		client.shutdown();
	}

	
}
