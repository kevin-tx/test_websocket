package com.kevin.ws.client.wsclient;

/**
 * @author TX
 * @date 2020/9/3 13:41
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;
import java.net.URI;

public class ClientStart {
    private static Logger logger = LoggerFactory.getLogger(ClientStart.class);
    public static void main(String[] args){
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            MyClient client = new MyClient();
            container.connectToServer(client, new URI("ws://127.0.0.1:8813/wsproxy"));
            int turn = 0;
            while(turn++ < 10){
                String msg = "client send: 客户端消息 " + turn;
                client.send(msg);
                logger.info(msg);
//                Thread.sleep(1000);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            Thread.sleep(100000000000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
