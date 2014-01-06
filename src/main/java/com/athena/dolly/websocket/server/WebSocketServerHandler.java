package com.athena.dolly.websocket.server;

import com.athena.dolly.common.provider.AppContext;
import com.athena.dolly.web.aws.s3.S3Service;
import com.athena.dolly.websocket.server.message.SyncMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
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
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author Jay Lee
 * @date Jan 5th, 2013
 *
 * @todo 1. ContiunationFrame for frame segmentation 2. Full-fledged
 * authentication framework
 *
 */
@Component
@Qualifier("websocketServerHandler")
@Sharable
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    protected final Logger logger = LoggerFactory.getLogger(WebSocketServerHandler.class);

    @Value("#{contextProperties['websocket.path']}")
    private static String WEBSOCKET_PATH;

    @Value("#{contextProperties['websocket.tmp.dir']}")
    private String TMP_FILE_ROOT_PATH;

    private WebSocketServerHandshaker handshaker;
    private ConcurrentMap<String, Channel> channelClientMap;

    private static final AttributeKey<String> USERID = AttributeKey.valueOf("channel.userid");

    private static final int HEADERSIZE_USERID = 32;
    private static final int HEADERSIZE_ABSOLUTEPATH = 128;
    private static final int HEADERSIZE_FILESIZE = 32;

    public WebSocketServerHandler() {
        this.channelClientMap = new ConcurrentHashMap(1024);
        logger.debug("WebSocketServerHandler created!!!");
    }

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

    private void handleHttpRequest(final ChannelHandlerContext ctx, final FullHttpRequest req) throws Exception {
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

        // WebSocket Handshake
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, false, Integer.MAX_VALUE);
        handshaker = wsFactory.newHandshaker(req);

        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
        } else {
            final ChannelFuture f = handshaker.handshake(ctx.channel(), req);
            f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        HttpHeaders header = req.headers();
                        String userid = header.get("WebSocketUSERID");
                        ctx.channel().attr(USERID).set(userid);
                        channelClientMap.put(userid, ctx.channel());
                        logger.debug("User {} is logged in. Current Session Count : {}", userid, channelClientMap.size());
                    }
                }
            });
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if ((frame instanceof TextWebSocketFrame)) {
            handleTextMessageFromClient(ctx, frame);
            return;
        }
        if ((frame instanceof BinaryWebSocketFrame)) {
            handleBinaryMessageFromClient(ctx, frame);
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
        logger.debug("Client '{}' is connected!", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(ctx.channel().attr(USERID).get() != null) channelClientMap.remove(ctx.channel().attr(USERID).get());
        logger.debug("User {}/{} is logged out. Current Session Count : {}", ctx.channel().attr(USERID).get(), ctx.channel().remoteAddress(), channelClientMap.size());
    }

    private void handleBinaryMessageFromClient(ChannelHandlerContext ctx, WebSocketFrame frame) {
        byte[] bUserId = new byte[HEADERSIZE_USERID];
        byte[] bAbsolutePath = new byte[HEADERSIZE_ABSOLUTEPATH];
        byte[] bSize = new byte[HEADERSIZE_FILESIZE];
        String userId, absolutePath;
        Integer fileSize;

        try {
            frame.content().readBytes(bUserId);
            frame.content().readBytes(bAbsolutePath);
            frame.content().readBytes(bSize);
            userId = new String(bUserId, "UTF8").trim();
            absolutePath = new String(bAbsolutePath, "UTF8").trim();
            fileSize = Integer.parseInt(new String(bSize, "UTF8").trim());

            logger.debug("bUserId : {}, bAbsolutePath : {}, bSize : {}", userId, absolutePath, fileSize);

            if (userId.length() > 0 && absolutePath.length() > 0 && fileSize > 0) {
                ByteBuffer byteBuffer = ByteBuffer.allocate(fileSize);
                String tmpFileAbsolutePath = TMP_FILE_ROOT_PATH + "/" + userId + absolutePath;
                String tmpFilePath = tmpFileAbsolutePath.substring(0, tmpFileAbsolutePath.lastIndexOf("/"));

                // 경로 만들기
                if (new File(tmpFilePath).mkdirs()) {
                    logger.debug("Directory is created : {}", tmpFilePath);
                }

                RandomAccessFile targetFile = new RandomAccessFile(tmpFileAbsolutePath, "rw");
                frame.content().readBytes(byteBuffer);
                FileChannel inChannel = targetFile.getChannel();
                inChannel.position(0);
                byteBuffer.flip();
                inChannel.write(byteBuffer);
                inChannel.close();
                targetFile.close();
                logger.debug("File is written : {}", tmpFileAbsolutePath);

                synchronizeStorage(userId, absolutePath, tmpFileAbsolutePath);
            } else {
                throw new RuntimeException("WebSocketFrame is corrupted.");
            }
            // TODO : Request client to retry.
        } catch (FileNotFoundException ex) {
            logger.error("File Not Found", ex);
        } catch (IOException ex) {
            logger.error("IOException", ex);
        }
    }

    private void handleTextMessageFromClient(ChannelHandlerContext ctx, WebSocketFrame frame) {
        ObjectMapper mapper = new ObjectMapper();
        String request = ((TextWebSocketFrame) frame).text();

        try {
            SyncMessage clientMessage = mapper.readValue(request, SyncMessage.class);

            // TODO : Completely revamp the login process
            if (WebSocketResource.OPCODE_CONNECT.equals(clientMessage.getOpcode())) {
//                ctx.channel().attr(USERID).set(clientMessage.getUserid());
//                channelClientMap.put(clientMessage.getUserid(), ctx.channel());
//                clientMessage.setRet("OK");
//                ctx.channel().write(new TextWebSocketFrame(mapper.writeValueAsString(clientMessage)));
//                logger.debug("User {} is logged in. Current Session Count : {}", clientMessage.getUserid(), channelClientMap.size());
            }
            if (WebSocketResource.OPCODE_TRANSFER_DELETE.equals(clientMessage.getOpcode())) {

            }
            if (WebSocketResource.OPCODE_TRANSFER_INSERT.equals(clientMessage.getOpcode())) {

            }
        } catch (IOException ex) {
            logger.debug("TextWebSocketFrame can't be handled(invalid json). Echo-ed request.", ex);
            ctx.channel().write(new TextWebSocketFrame(request));
        }
    }

    /**
     * 창재선생님, 여기 부분에 호출부분 넣어주세요.
     *
     * @param userId User ID
     * @param path Directory path
     * @param localFile Temporary file which is in local tmp directory
     */
    protected void synchronizeStorage(String userId, String path, String localFile) {
        S3Service s3Service = AppContext.getBean("s3Service", S3Service.class);
        s3Service.putObject(userId, path.substring(1, path.length()), localFile);
        logger.debug(s3Service.listBuckets().toString());
    }

    public void sendMessageToClient(JsonNode msg) {
    	
    	JsonNode opcode = msg.get("deleted");
    	
    	if(opcode != null && !"true".equals(opcode.textValue())) {
            logger.info("sendMessageToClient", msg.textValue());
            ObjectMapper mapper = new ObjectMapper();
            String userid = msg.get("userid").textValue();
            String absolutePath = msg.get("absolutePath").textValue();
            ArrayList<String> fileList = new ArrayList<String>();
            fileList.add(absolutePath);
            SyncMessage msgToClient = new SyncMessage();
            msgToClient.setUserid(userid);
            msgToClient.setOpcode(WebSocketResource.OPCODE_TRANSFER_INSERT);
            msgToClient.setsFileList(fileList);

            try {
                Channel clientChannel = channelClientMap.get(userid);
                if (clientChannel != null && clientChannel.isOpen()) {
                    clientChannel.writeAndFlush(new TextWebSocketFrame(mapper.writeValueAsString(msgToClient)));
                }
            } catch (JsonProcessingException ex) {
                logger.info("Json Processing Error", ex);
            }
    	}
    	
    	

    }
}
