package ws.server.wsserver;

/**
 * @author TX
 * @date 2020-09-04 16:28
 * @description
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.*;

//@ServerEndpoint(value = "/wsproxy", configurator = CustomSpringConfigurator.class)
//@Component
public class WebSocketServer {

    private Logger logger = LoggerFactory.getLogger(WebSocketServer.class);
    ExecutorService fixedThreadPool = Executors.newFixedThreadPool(20);

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        logger.info("new ws open：" + WebsocketUtil.getRemoteAddress(session));
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        logger.info("ws close: " + WebsocketUtil.getRemoteAddress(session));
    }

    /**
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        logger.error("on Error: " + WebsocketUtil.getRemoteAddress(session), error);
    }


    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) throws IOException, InterruptedException {
        try {
            String msg = "receive from (" + WebsocketUtil.getRemoteAddress(session) + "):" + message;
            logger.info(msg);
            fixedThreadPool.execute(new MsgTask(msg, session));
        }
        catch (Exception ex){
            logger.error("",ex);
        }
    }

    class MsgTask implements Runnable {

        private String message;
        private Session session;

        public MsgTask(String message, Session session) {
            this.message = message;
            this.session = session;
        }

        @Override
        public void run() {
            try {
                logger.info("------working start: " + message);
//                Thread.sleep(2000);
                logger.info("------working end: " + message);
                sendMessage(session, message);
            } catch (Exception e) {
                logger.error("",e);
            }
        }
    }

    /**
     * 服务端下发消息
     *
     * @param session
     * @param message
     * @throws IOException
     */
    public void sendMessage(Session session, String message) throws IOException {
        try {
            synchronized (session) {
                session.getBasicRemote().sendText(message);
//            session.getAsyncRemote().sendText(message);
                logger.info("send to (" + WebsocketUtil.getRemoteAddress(session) + ") -- " + message);
            }
        } catch (Exception ex) {
            logger.error("Send ws error", ex);
            throw ex;
        }
    }

    /**
     * 服务端下发二进制字节消息
     *
     * @param session
     * @param message
     * @throws IOException
     */
    public void sendMessageByte(Session session, byte[] message) throws IOException {
        try {
            ByteBuffer bf = ByteBuffer.wrap(message);
            session.getBasicRemote().sendBinary(bf);
            logger.info("send to (" + WebsocketUtil.getRemoteAddress(session) + ") -- " + message);
        } catch (Exception ex) {
            logger.error("Send error", ex);
            throw ex;
        }
    }
}