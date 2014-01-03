package com.athena.dolly.websocket.server;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

@Service
@Qualifier("websocketServer")
public class WebSocketServer {
	
	protected final Logger logger = LoggerFactory.getLogger(WebSocketServer.class);
	
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
					channel = b.bind(port).sync().channel().closeFuture().sync().channel();
					logger.info("=================================================================");
		            logger.info("Web socket server started at port " + port + '.');
		            logger.info("Open your browser and navigate to http://localhost:" + port + '/');
		            logger.info("=================================================================");
		            

				} catch (InterruptedException e) {
					e.printStackTrace();
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

}