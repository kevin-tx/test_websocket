package ws.server.wsserver;

/**
 * @author TX
 * @date 2020-09-04 16:28
 * @description 带websockt消息缓冲池的服务
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ServerEndpoint(value = "/wsproxy", configurator = CustomSpringConfigurator.class)
@Component
public class WebSocketServer2 {
    private Logger logger = LoggerFactory.getLogger(WebSocketServer.class);
    private WsMsgPool wsMsgPool = new WsMsgPool();
    //处理websocket消息的线程池
    private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(8);
    //websocket任务分发线程
    private Thread tWsMsgDispatcher = null;

    @PostConstruct
    public void init() {
        //websockt任务消费线程
        tWsMsgDispatcher = new Thread(() -> {
            while (true){
                MsgTask task = wsMsgPool.take();
                if(task == null){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    fixedThreadPool.execute(task);
                }
            }
        }, "tWsMsgDispatcher");
        tWsMsgDispatcher.start();
    }

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
    public void onMessage(String message, Session session) {
        try {
            String msg = "receive from (" + WebsocketUtil.getRemoteAddress(session) + "):" + message;
            logger.info(msg);
            wsMsgPool.add(message, session);
        } catch (Exception ex) {
            logger.error("",ex);
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
            synchronized (session) {
                ByteBuffer bf = ByteBuffer.wrap(message);
                session.getBasicRemote().sendBinary(bf);
                logger.info("send to (" + WebsocketUtil.getRemoteAddress(session) + ") -- " + message);
            }
        } catch (Exception ex) {
            logger.error("Send ws error", ex);
            throw ex;
        }
    }


    //websocket消息的缓冲池
    class WsMsgPool{
        private Deque<MsgTask> msgPool = new LinkedList<>();
//        private Deque<TdRequest<String>> msgPool = new LinkedBlockingDeque<>();
        private final int maxSize = 10000;
        public int size(){
            return msgPool.size();
        }
        public void add(String message, Session session){

            synchronized (msgPool) {
                if(msgPool.size() >= maxSize) {
                    try {
                        logger.debug("[add over]WsMsgPool size:" + msgPool.size());
                        sendMessage(session, "-------------------------------over size");
                    } catch (Exception ex) {
                    }
                    return;
                }
                msgPool.addFirst(new MsgTask(message, session));
                logger.debug("[add]WsMsgPool size:" + msgPool.size());
            }
        }

        public MsgTask take(){
            synchronized (msgPool) {
                if(msgPool.size()>0) {
                    MsgTask last = msgPool.removeLast();
                    logger.debug("[take]WsMsgPool size:" + msgPool.size());
                    return last;
                }
            }
            return null;
        }
    }

    //消息处理任务类
    class MsgTask implements Runnable {

        private String request;
        private Session session;

        public MsgTask(String request, Session session) {
            this.request = request;
            this.session = session;
        }

        @Override
        public void run() {
            try {
                logger.info("------working start: " + request);
//                Thread.sleep(2000);
                logger.info("------working end: " + request);
                sendMessage(session, request);
            } catch (Exception e) {
                logger.error("",e);
            }
        }
    }
}