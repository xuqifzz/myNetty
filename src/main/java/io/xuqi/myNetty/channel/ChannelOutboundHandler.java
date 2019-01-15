package io.xuqi.myNetty.channel;


import java.net.SocketAddress;

public interface ChannelOutboundHandler  extends ChannelHandler{
    void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception;
    void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception;
}
