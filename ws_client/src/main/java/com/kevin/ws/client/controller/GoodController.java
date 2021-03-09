package com.kevin.ws.client.controller;

import com.kevin.ws.client.wsclient.MyClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author TX
 * @date 2020/5/28 14:57
 */

@RestController
public class GoodController {

    private MyClient myClient;

    public GoodController() {
//        myClient = new MyClient();
    }

    @RequestMapping(value="/ws/conn", method = RequestMethod.POST)
    public void wsconn(){
        myClient = new MyClient();
        myClient.conn();
    }

    @RequestMapping(value="/ws/disconn", method = RequestMethod.POST)
    public void wsdisconn(){
        myClient.disconn();
    }

    @RequestMapping(value="/ws/send", method = RequestMethod.POST)
    public void wssend(@RequestBody String data){
        myClient.send(data);
    }

    @RequestMapping(value="/ws/ping", method = RequestMethod.POST)
    public void wsping(){
        myClient.sendPing();
    }

    @RequestMapping(value="/ws/pong", method = RequestMethod.POST)
    public void wspong(){
        myClient.sendPong();
    }

    @RequestMapping(value="/ws/sendloop", method = RequestMethod.POST)
    public void wssendloop(@RequestBody String data){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(;;) {
                    myClient.send(data);
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
