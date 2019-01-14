package io.xuqi.myNetty.channel;


public interface EventLoopGroup {
    EventLoop next();
    ChannelFuture register(Channel channel);
}
