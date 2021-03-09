package com.kevin.ws.client.wsclient;

/**
 * @author TX
 * @date 2020/9/3 13:39
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

@ClientEndpoint
public class MyClient {
    private static Logger logger = LoggerFactory.getLogger(MyClient.class);
    private Session session;
    private WebSocketContainer container = ContainerProvider.getWebSocketContainer();
    @OnOpen
    public void open(Session session){
        logger.info("Client WebSocket is opening...");
        this.session = session;
    }

    @OnMessage
    public void onMessage(String message){
        logger.info("Server send message: " + message);
    }

    @OnClose
    public void onClose(){
        logger.info("Websocket closed");
    }

    /**
     * 连接
     */
    public void conn(){
        try {
//            container.connectToServer(this, new URI("ws://10.10.1.73:7701/wsproxy"));
            container.connectToServer(this, new URI("ws://10.10.1.73:8813/wsproxy"));
//            container.connectToServer(this, new URI("wss://www.starvisioncloud.com/wsproxy"));
        } catch (DeploymentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void sendPing(){
        try {
            session.getAsyncRemote().sendPing(ByteBuffer.wrap(new byte[]{1}));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendPong(){
        try {
            session.getAsyncRemote().sendPing(ByteBuffer.wrap(new byte[]{2}));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 断开连接
     */
    public void disconn(){
        try {
            session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送客户端消息到服务端
     * @param message 消息内容
     */
    public void send(String message){
//        this.session.getAsyncRemote().sendText(message);
        try {
            this.session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}