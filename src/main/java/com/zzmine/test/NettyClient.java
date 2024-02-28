package com.zzmine.test;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.AttributeKey;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class NettyClient {
    private static final int MAX_RETRY = 5;

    public static void main(String[] args) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();     // 客户端启动引导类
        NioEventLoopGroup group = new NioEventLoopGroup();   // 对应于IOClient中的主线程
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .attr(AttributeKey.newInstance("clientName"), "nettyClient")
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        channel.pipeline().addLast(new StringEncoder());
                    }
                });
        connet(bootstrap, "127.0.0.1", 8000, MAX_RETRY);
//        while (true && channel!=null){
//            channel.writeAndFlush(new Date() + ": hello server.");
//            Thread.sleep(2000);
//        }
    }

    // 考虑在网络不好的情况下，自动断线重连
    private static void connet(Bootstrap bootstrap, String host, int port, int retry){
        bootstrap.connect(host, port).addListener(future -> {
            if (future.isSuccess()){
                System.out.println("连接成功！");
            } else if (retry == 0){
                System.err.println("重试次数已用完，放弃连接！");
            } else {
                // 指数退避的方式依次增加重连间隔
                int order = (MAX_RETRY - retry) + 1;   // 第几次重连
                int delay = 1 << order;
                System.err.println(new Date() + ": 连接失败，第" + order + "次重连......");
                bootstrap.config().group().schedule(()->connet(bootstrap,host,port,retry-1), delay, TimeUnit.SECONDS);
            }
        });
    }
}
