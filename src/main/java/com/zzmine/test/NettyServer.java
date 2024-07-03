package com.zzmine.test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
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
                        //nioSocketChannel.pipeline().addLast(new FirstServerHandler());
                        nioSocketChannel.pipeline().addLast(new StringDecoder());
                        nioSocketChannel.pipeline().addLast(new ToUpperHandler());
                        nioSocketChannel.pipeline().addLast(new PrependAppendHandler());
                        nioSocketChannel.pipeline().addLast(new StringEncoder());
                        nioSocketChannel.pipeline().addLast(new LoggingOutboundHandler());
                        nioSocketChannel.pipeline().addLast(new SimpleOutboundHandler());
                        nioSocketChannel.pipeline().addLast(new SimpleChannelInboundHandler<>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {

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
                        if (future.isSuccess()) {
                            System.out.println("端口 [" + port + " ] 绑定成功！");
                        } else {
                            System.out.println("端口 [" + port + " ] 绑定失败。");
                            bind(serverBootstrap, port + 1);
                        }
                    }
                });
    }
}

// 入站处理器1: 将消息转换为大写
class ToUpperHandler extends io.netty.channel.ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof String) {
            String originalMessage = (String) msg;
            String upperCaseMessage = originalMessage.toUpperCase();
            ctx.fireChannelRead(upperCaseMessage);
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}

// 入站处理器2: 添加前缀和后缀
class PrependAppendHandler extends io.netty.channel.ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof String) {
            String message = (String) msg;
            String modifiedMessage = "[PREFIX] " + message + " [SUFFIX]";
            ctx.fireChannelRead(modifiedMessage);
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}

// 出站处理器1: 记录发送消息
class LoggingOutboundHandler extends io.netty.channel.ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("Sending: " + msg);
        super.write(ctx, msg, promise);
    }
}

// 出站处理器2: 处理并发送消息
class SimpleOutboundHandler extends io.netty.channel.SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println("Server received: " + msg);
        ctx.writeAndFlush(msg); // 接收消息后直接转发
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
