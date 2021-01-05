package com.atguigu.netty.simple;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer {

    public static void main(String[] args) throws InterruptedException {

        //创建BossGroup和WorkerGroup
        //bossGroup处理连接请求，业务交给workerGroup，无限循环
        //bossGroup和workerGroup含有的子线程NioEventLoop的个数默认cpu核数*2
        EventLoopGroup bossgroup = new NioEventLoopGroup(1);
        EventLoopGroup workergroup = new NioEventLoopGroup(8);


        try {
            //创建服务器端的启动对象，配置参数
            ServerBootstrap bootsrap = new ServerBootstrap();

            //使用链式编程进行设置
            bootsrap.group(bossgroup, workergroup)//设置两个线程组
                    .channel(NioServerSocketChannel.class)     //使用NioServerSocketChannel作为服务器的通道实现
                    .option(ChannelOption.SO_BACKLOG, 128)//设置线程队列等待连接的个数
                    .childOption(ChannelOption.SO_KEEPALIVE, true)//设置保持活动连接状态
                    .childHandler(new ChannelInitializer<SocketChannel>() {//创建一个通道测试对象（匿名对象）
                        //给pipeline设置处理器
                        protected void initChannel(SocketChannel ch) throws Exception {
                            System.out.println("客户socketChannel hashcode=" + ch.hashCode());//集合管理
                            //再推送消息时，可以将业务加入到各个channel对应的NIOEventloop的taskqueue或者scheduleQueue
                            ch.pipeline().addLast(new NettyServerHandler());
                        }
                    });//给workerGroup的EventLoop对应的管道设置处理器

            System.out.println("Server is ready...");

            //绑定端口并同步，生成了一个ChannelFuture对象
            //启动服务器（并绑定端口）
            final ChannelFuture cf = bootsrap.bind(6668).sync();

            //给cf注册监听器，监控关心的事件
            cf.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (cf.isSuccess()) {
                        System.out.println("监听端口6668成功");
                    } else {
                        System.out.println("监听端口6668失败");
                    }
                }
            });
            bootsrap.bind(6669).addListener((future -> {
                if (future.isSuccess()) {
                    System.out.println("监听6669成功");
                } else {
                    System.out.println("监听6669失败");
                }
            }));


            //对关闭通道进行监听
            cf.channel().closeFuture().sync();
        }finally {
            bossgroup.shutdownGracefully();
            workergroup.shutdownGracefully();
        }
    }
}
