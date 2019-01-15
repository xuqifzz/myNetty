package io.xuqi.myNetty.channel;


public interface ChannelPipeline extends ChannelInboundInvoker,ChannelOutboundInvoker {
    Channel channel();
    ChannelPipeline addLast(ChannelHandler handlers);
}
