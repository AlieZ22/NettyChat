package com.zzmine.test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class NettyServer {

    public static void main(String[] args) {
        ServerBootstrap serverBootstrap = new ServerBootstrap();     // 引导类，引导服务端的启动工作
        NioEventLoopGroup boss = new NioEventLoopGroup();      // 负责接收新连接的线程
        NioEventLoopGroup worker = new NioEventLoopGroup();    // 负责读取数据以及业务逻辑处理的线程
        serverBootstrap
                .group(boss, worker)       // 为引导类配置两大线程组
                .channel(NioServerSocketChannel.class)     // 指定IO模型
                .childHandler(new ChannelInitializer<NioSocketChannel>() {   // 连接处理逻辑
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) {
                        nioSocketChannel.pipeline().addLast(new StringDecoder());
                        nioSocketChannel.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, String msg) {
                                System.out.println(msg);
                            }
                        });
                    }
                });
        bind(serverBootstrap, 8000);
    }

    // 自动绑定递增的端口
    private static void bind(final ServerBootstrap serverBootstrap, final int port) {
        serverBootstrap.bind(port)
                .addListener(new GenericFutureListener<Future<? super Void>>() {      // 添加端口绑定监听器
                    @Override
                    public void operationComplete(Future<? super Void> future) throws Exception {
                        if(future.isSuccess()) {
                            System.out.println("端口 [" + port + " ] 绑定成功！");
                        } else {
                            System.out.println("端口 [" + port + " ] 绑定失败。");
                            bind(serverBootstrap, port + 1);
                        }
                    }
                });
    }
}
