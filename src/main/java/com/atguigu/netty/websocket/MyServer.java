package com.atguigu.netty.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class MyServer {

    public static void main(String[] args) throws InterruptedException {

        //创建两个线程组
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGrop = new NioEventLoopGroup(8);

        try{
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(bossGroup, workerGrop)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();

                            //基于Http协议，使用http的编码和解码器
                            pipeline.addLast(new HttpServerCodec());

                            //以块方式写，添加ChunkedWriteHandler处理器
                            pipeline.addLast(new ChunkedWriteHandler());

                            /*
                            http数据在传输过程中是分段，HttpObjectAggregator可以将多个断聚合
                            浏览器发送大量数据的时候，会发出多次http请求
                             */
                            pipeline.addLast(new HttpObjectAggregator(7000));

                            /*
                            对应WebSocket，数据以帧frame方式传递
                            看到webSocketFrame，下面有六个子类
                            浏览器请求时，ws://localhost:7000/hello  后面字符表示请求的uri
                            WebSocketServerProtocolHandler 核心功能把http协议升级为ws协议，保持长连接
                             */

                            pipeline.addLast(new WebSocketServerProtocolHandler("/hello"));

                            //自定义业务处理逻辑
                            pipeline.addLast(new MyTextWebSocketFrameHandler());

                        }
                    });

            //启动服务器
            ChannelFuture channelFuture = serverBootstrap.bind(7000).sync();
            channelFuture.channel().closeFuture().sync();

        }finally {
            workerGrop.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
