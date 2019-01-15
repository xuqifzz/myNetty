package io.xuqi.application;

import io.xuqi.myNetty.bootstrap.ServerBootstrap;

import io.xuqi.myNetty.channel.ChannelHandler;
import io.xuqi.myNetty.channel.ChannelInboundHandler;
import io.xuqi.myNetty.channel.EventLoopGroup;
import io.xuqi.myNetty.channel.nio.NioEventLoopGroup;
import io.xuqi.myNetty.channel.socket.nio.NioServerSocketChannel;

import java.nio.ByteBuffer;

public class Server {

    public static void main(String[] args) throws Exception{
//        ChannelHandler handler = (o) -> {
//            byte[] bytes = (byte[])o;
//            String msg = new String(bytes);
//            System.out.println("接收到消息:" + msg);
//        };
        ChannelInboundHandler handler = (ctx,msg) -> {
            byte[] bytes = (byte[])msg;
            String str = new String(bytes);
            System.out.println("接收到消息:" + str);
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            ctx.write(buffer).addListener((future) -> {
                System.out.println("回复成功");
            });
        };
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(1);
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(handler);
        //这里改成异步调用
        b.bind(6666).addListener((future)->{
            System.out.println("绑定端口成功");
        });

        for (;;) Thread.sleep(1000);

    }
}
