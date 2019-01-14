package io.xuqi.myNetty.bootstrap;


import io.xuqi.myNetty.channel.*;

import java.net.InetSocketAddress;


//服务端启动器
public class ServerBootstrap {
    private  Class<? extends Channel> channelClass;
    private EventLoopGroup parentGroup;
    private EventLoopGroup childGroup;
    private ChannelHandler childHandler;

    public ChannelFuture bind(int port){
        final ChannelFuture regFuture = initAndRegister();
        final Channel channel = regFuture.channel();
        final ChannelPromise promise = new DefaultChannelPromise(channel,channel.eventLoop());
        regFuture.addListener((future) -> {
            channel.bind(new InetSocketAddress(port),promise);
        });
        return promise;

    }

    public ServerBootstrap group(EventLoopGroup parentGroup, EventLoopGroup childGroup) {
        this.parentGroup = parentGroup;
        this.childGroup = childGroup;
        return this;
    }
    public ServerBootstrap channel(Class<? extends Channel> channelClass) {
        this.channelClass = channelClass;
        return this;
    }

    public ServerBootstrap childHandler(ChannelHandler childHandler){
        this.childHandler = childHandler;
        return this;
    }

    private ChannelFuture initAndRegister(){
        try {
            //创建NioServerSocketChannel
            Channel channel = channelClass.newInstance();
            init(channel);
            //注册到主EventLoopGroup
            return parentGroup.register(channel);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    private void init(Channel channel){
        channel.setHandler(new ServerBootstrapAcceptor(childGroup,childHandler));

    }

    private static class ServerBootstrapAcceptor implements ChannelHandler{
        private final EventLoopGroup childGroup;
        private final ChannelHandler childHandler;
        ServerBootstrapAcceptor(EventLoopGroup childGroup,ChannelHandler childHandler){
            this.childGroup = childGroup;
            this.childHandler = childHandler;
        }
        @Override
        public void channelRead(Object msg) {
            final Channel child = (Channel)msg;
            child.setHandler(childHandler);
            //在此处将接收到的NioSocketChannel注册到childGroup
            childGroup.register(child);
        }
    }
}
