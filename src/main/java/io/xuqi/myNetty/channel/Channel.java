package io.xuqi.myNetty.channel;

import java.net.SocketAddress;

//我这里没有unsafe,所以把unsafe的功能整进Channel了
public interface Channel {
    void register(EventLoop eventLoop, ChannelPromise promise);
    ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise);


    EventLoop eventLoop();
    ChannelPipeline pipeline();
    Unsafe unsafe();

    interface Unsafe{
        void bind(SocketAddress localAddress, ChannelPromise promise);
        void write(Object msg, ChannelPromise promise);


    }

}
