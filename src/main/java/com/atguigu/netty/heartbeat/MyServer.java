package com.atguigu.netty.heartbeat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class MyServer {
    public static void main(String[] args) throws InterruptedException {

        //创建两个线程组
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try{
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            //加入IdleStateHandler
                           /* *
                             *IdleStateHandler 处理空闲状态
                             * long readerIdleTime：多长时间未读，就会发送一个心跳检测包检测是否连接
                             * long writeIdleTime:多长时间未写
                             * long allIdleTime:多长时间没有读写
                             * triggers an{@link io.netty.handler.timeout.IdleStateEvent} when a
                             * {@link io.netty.channel.Channel}has not performed read,write,or both
                             * operation for a while.
                             * 当IdleStateEvent触发后，就会传递给管道的下一个handler去处理
                             * 通过调用（触发）下一个handler的userEventTriggered，在该方法中去处理IdleStateEvent
                             * 读空闲，写空闲，读写空闲
                             **/
                            pipeline.addLast(new IdleStateHandler(13, 5, 2, TimeUnit.SECONDS));

                            //加入一个对空闲检测进一步处理的handler
                            pipeline.addLast(new MyServerHandler());
                        }
                    });
            //启动服务器
            ChannelFuture channelFuture = serverBootstrap.bind(7000).sync();
            channelFuture.channel().closeFuture().sync();

        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
