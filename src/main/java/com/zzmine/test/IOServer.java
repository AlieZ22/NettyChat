package com.zzmine.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

// 简单IO服务端
public class IOServer {
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(8000);

        // 接收新连接线程
        new Thread(() -> {
            while(true){
                try {
                    // 1.阻塞方法，获取新连接
                    Socket socket = serverSocket.accept();
                    // 2.为每个新连接创建新线程，负责读取数据
                    new Thread(() -> {
                        try {
                            int len;
                            byte[] data = new byte[1024];
                            InputStream inputStream = null;
                            inputStream = socket.getInputStream();
                            // 3.字节流方式读取数据
                            while((len = inputStream.read(data)) != -1){
                                System.out.println(new String(data, 0, len));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
