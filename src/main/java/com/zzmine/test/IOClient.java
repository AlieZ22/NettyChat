package com.zzmine.test;

import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;

// 简单IO客户端
public class IOClient {
    public static void main(String[] args) {
        new Thread(() -> {
            Socket socket = null;
            try {
                socket = new Socket("127.0.0.1", 8000);
                while(true){
                    socket.getOutputStream().write((new Date() + ": hello , my port is: " + socket.getLocalPort()).getBytes());
                    Thread.sleep(2000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
