package com.atguigu.netty.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.net.URI;

/*
说明：
1、SimpleChannelInboundHandler是ChannelInboundHandlerAdapter的子类
2、HttpObject 客户端和服务器相互通讯的数据被封装成HTTPObject
 */

public class TestHttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {

//   channelRead0读取客户端数据
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {

        System.out.println("通过ctx获取channel="+ctx.channel());
        System.out.println("通过pipeline获取channel="+ctx.pipeline().channel());
        System.out.println("通过ctx获取pipeline=" + ctx.pipeline());
        System.out.println("通过channel获取pipeline="+ctx.channel().pipeline());
        System.out.println("当前ctx的handler=" + ctx.handler());

        //判断msg是不是httprequest请求
        if (msg instanceof HttpRequest) {

            System.out.println("ctx类型=" + ctx.getClass());
            System.out.println("pipeline hashcode" + ctx.pipeline().hashCode() + "TestHttpServerHandler hash=" + this.hashCode());


            System.out.println("msg类型=" + msg.getClass());
            System.out.println("客户端地址" + ctx.channel().remoteAddress());

            //获取到
            HttpRequest httpRequest = (HttpRequest) msg;
            //获取uri
            URI uri = new URI(httpRequest.uri());

            if ("/favicon.ico".equals(uri.getPath())) {
                System.out.println("请求了favicon，不做响应");
                return;
            }

            //回复信息给浏览器（http协议）

            ByteBuf content = Unpooled.copiedBuffer("hello,我是服务器", CharsetUtil.UTF_8);

            //构造一个http响应，即httpresponse
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);

            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=utf-8");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());

            //将构建好的response返回
            ctx.writeAndFlush(response);
        }
    }
}
