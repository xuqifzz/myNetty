package io.xuqi.myNetty.channel;

public interface ChannelInboundInvoker {
    ChannelInboundInvoker fireChannelRead(Object msg);
}
