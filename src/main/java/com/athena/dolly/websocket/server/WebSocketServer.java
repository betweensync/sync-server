package com.athena.dolly.websocket.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import com.athena.dolly.cloudant.ChangesEventListener;
import com.athena.dolly.common.provider.AppContext;

/**
 * 
 * @author jlee
 * @date Jan 5th, 2013
 * 
 */
@Service
@Qualifier("websocketServer")
public class WebSocketServer {

	protected final Logger logger = LoggerFactory
			.getLogger(WebSocketServer.class);

	@Value("#{contextProperties['websocket.port']}")
	private int port;

	@Inject
	@Named("bossGroup")
	private EventLoopGroup bossGroup;

	@Inject
	@Named("workerGroup")
	private EventLoopGroup workerGroup;

	@Inject
	private WebSocketServerInitializer initializer;

	private Channel channel;

	public WebSocketServer() {
		logger.debug("WebSocketServer created!!");
	}

	@PostConstruct
	public void start() throws Exception {
		new Thread() {
			@Override
			public void run() {
				try {
					ServerBootstrap b = new ServerBootstrap();
					b.group(bossGroup, workerGroup)
							.channel(NioServerSocketChannel.class)
							.handler(new LoggingHandler(LogLevel.WARN))
							.childHandler(initializer)
							.option(ChannelOption.SO_BACKLOG, 1024)
							.childOption(ChannelOption.SO_KEEPALIVE, true);

					// Bind and start to accept incoming connections.
					channel = b.bind(port).sync().channel();

					logger.info("=================================================================");
					logger.info("Server started at http://{}",
							channel.localAddress());
					logger.info("=================================================================");

					channel.closeFuture().sync();
				} catch (InterruptedException e) {
					logger.error("Server Interrupted", e);
				}
			}
		}.start();
	}

	@PreDestroy
	public void stop() {
		if (channel != null) {
			channel.close();
		}
	}

	public static void main(String[] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "context-common.xml" });

		AppContext.setApplicationContext(context);
		ChangesEventListener cel = new ChangesEventListener();
		cel.attachment();
	}
}
