package io.xuqi.myNetty.channel;

public interface ChannelHandlerContext extends  ChannelInboundInvoker, ChannelOutboundInvoker{
    ChannelHandler handler();
}
