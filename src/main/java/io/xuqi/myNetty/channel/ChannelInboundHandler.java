package io.xuqi.myNetty.channel;

public interface ChannelInboundHandler  extends ChannelHandler {
    void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception;
}
