package com.athena.dolly.websocket.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.athena.dolly.common.provider.AppContext;
import com.athena.dolly.web.aws.s3.S3Service;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

@Component
@Qualifier("websocketServerHandler")
@Sharable
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

	protected final Logger logger = LoggerFactory.getLogger(WebSocketServerHandler.class);
	
    private static final String WEBSOCKET_PATH = "/websocket";

    private WebSocketServerHandshaker handshaker;
    
    private Channel channel;

    public void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        } else {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
    	
        // Handle a bad request.
        if (!req.getDecoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }
        // Allow only GET methods.
        if (req.getMethod() != GET) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }

        // Handshake
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, false, Integer.MAX_VALUE);
        handshaker = wsFactory.newHandshaker(req);

        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
        }
        //req.release();
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {

    	synchronizeStorage(null, null, null);
    	
        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if ((frame instanceof TextWebSocketFrame)) {
            String request = ((TextWebSocketFrame) frame).text();
            ctx.channel().write(new TextWebSocketFrame(request));
        }
        if ((frame instanceof BinaryWebSocketFrame)) {
            try {
                byte[] bUserId = new byte[32];
                byte[] bAbsolutePath = new byte[32];
                byte[] bSize = new byte[32];
                frame.content().readBytes(bUserId);
                frame.content().readBytes(bAbsolutePath);
                frame.content().readBytes(bSize);

                System.out.println("bUserId : " + new String(bUserId));
                System.out.println("bAbsolutePath :" + new String(bAbsolutePath));
                System.out.println("bSize :" + new String(bSize));

                ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.parseInt(new String(bSize).trim()));

                RandomAccessFile targetFile = new RandomAccessFile("/tmp/ws/target.zip", "rw");
                frame.content().readBytes(byteBuffer);
                FileChannel inChannel = targetFile.getChannel();
                inChannel.position(0);
                byteBuffer.flip();
                inChannel.write(byteBuffer);
                inChannel.close();
                targetFile.close();
            } catch (FileNotFoundException ex) {
                logger.error("File Not Found", ex);
            } catch (IOException ex) {
                logger.error("IOException", ex);
            }
        }
    }

    private static void sendHttpResponse(
            ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {

        if (res.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            setContentLength(res, res.content().readableBytes());
        }

        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!isKeepAlive(req) || res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("WebSocket Handler", cause);
        ctx.close();
    }

    private static String getWebSocketLocation(FullHttpRequest req) {
        return "ws://" + req.headers().get(HOST) + WEBSOCKET_PATH;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
            logger.debug("FullHttpRequest received!");
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
            logger.debug("WebSocketFrame received!");
        } else {
            ReferenceCountUtil.release(msg);
        }
    }
    
    @Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {	
		logger.info("channelActive() has invoked.");
		channel = ctx.channel();
	}
    
    /**
     * 창재선생님, 여기 부분에 호출부분 넣어주세요.
     * @param userId User ID
     * @param path Directory path
     * @param localFile Temporary file which is in local tmp directory
     */
    protected void synchronizeStorage(String userId, String path, String localFile) {
    	S3Service s3Service = AppContext.getBean("s3Service", S3Service.class);
    	logger.debug(s3Service.listBuckets().toString());
    }
    
    public void sendMessageToClient(String msg) {
    	logger.debug("Send message [%s] to client", msg);
    	ChannelFuture future = channel.writeAndFlush(msg);
    }
}
