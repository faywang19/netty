package com.atguigu.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.util.CharsetUtil;

//自定义一个Handler，需要继承netty规定好的某个HandlerAdapter

public class NettyServerHandler extends ChannelInboundHandlerAdapter {


    //读取数据事件（读取客户端发送的消息）

    /**
     * @param ctx ChannelHandlerContext 上下文对象，含有（业务逻辑处理管道）管道pipeline，通道channel，地址
     * @param msg ：客户端发送的数据 默认Object
     * @throws Exception
     */
    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {

  /*      //解决方案1 用户程序自定义的普通任务；
        ctx.channel().eventLoop().execute(new Runnable() {
            public void run() {
                try{
                    Thread.sleep(5 * 1000);
                    ctx.writeAndFlush(Unpooled.copiedBuffer("hello,客户端~2", CharsetUtil.UTF_8));
                }catch (Exception e){
                    System.out.println("发生异常"+e.getMessage());
                }
            }
        });

        ctx.channel().eventLoop().execute(new Runnable() {
            public void run() {
                try{
                    Thread.sleep(5 * 1000);
                    ctx.writeAndFlush(Unpooled.copiedBuffer("hello,客户端~3", CharsetUtil.UTF_8));
                }catch (Exception e){
                    System.out.println("发生异常"+e.getMessage());
                }
            }
        });

        ctx.channel().eventLoop().schedule(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(5 * 1000);
                    ctx.writeAndFlush(Unpooled.copiedBuffer("hello,客户端~4", CharsetUtil.UTF_8));
                } catch (Exception e) {
                    System.out.println("发生异常" + e.getMessage());
                }
            }
        }, 5, TimeUnit.SECONDS);



        System.out.println("go  on...");*/

        System.out.println("服务器读取线程：" + Thread.currentThread().getName());
        System.out.println("Channle=" + ctx.channel());
        System.out.println("server ctx=" + ctx);
        System.out.println("看看Channel和pipeline的关系：");
        Channel channel = ctx.channel();
        ChannelPipeline pipeline = ctx.pipeline();//pipeline本质是一个双向链表，涉及出栈入栈




        //将msg转成一个ByteBuf,netty提供的，不是NIO的ByteBuffer
        ByteBuf buf = (ByteBuf) msg;
        System.out.println("客户端发送消息：" + buf.toString(CharsetUtil.UTF_8));
        System.out.println("客户端地址：" + ctx.channel().remoteAddress());


    }


    //数据读取完毕

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {

        //writeAndFlush是write+flush
        //将数据（发送时进行编码）写入到缓存，并刷新
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello,客户端~1", CharsetUtil.UTF_8));

    }

    //处理异常，需要关闭通道

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
