package com.atguigu.netty.groupchat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.beans.SimpleBeanInfo;
import java.text.SimpleDateFormat;

public class GroupChatServerHandler extends SimpleChannelInboundHandler {

    //

    //

    //定义一个channel组，管理所有的channel
    //GlobalEventExecutor.INSTANCE  是一个全局的事件执行器，一个单例
    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    //handlerAdded表示连接建立，一旦连接，第一个被执行
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        //将该客户加入聊天的信息推送给其他在线的客户端
        //该方法会将channelGroup中所有的channel遍历，并发送消息，不需要自己遍历

        channelGroup.writeAndFlush("[客户端]" + channel.remoteAddress() + "加入聊天" + sdf.format(new java.util.Date()) + "\n");

        channelGroup.add(channel);
    }


    @Override
    //断开连接，将xx客户离开信息推送给当前在线的客户
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        channelGroup.writeAndFlush("[客户端]" + channel.remoteAddress() + "离开了\n");
        System.out.println("channelGroup size=" + channelGroup.size());
    }


    @Override
    //监听上限
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress()+"上线了~");
    }


    @Override
    //监听下线
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress()+"离线了~");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        //获取到当前channel
        Channel channel = ctx.channel();
        //遍历，根据channel不同情况回送不同消息
        channelGroup.forEach(channel1 -> {
            if (channel != channel1) {
                channel1.writeAndFlush("[客户]" + channel.remoteAddress() + "发送了消息：" + msg
                        + "\n");
            } else {
                channel1.writeAndFlush("[自己]发送了消息" + msg + "\n");
            }
        });
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //关闭通道
        ctx.close();
    }
}
