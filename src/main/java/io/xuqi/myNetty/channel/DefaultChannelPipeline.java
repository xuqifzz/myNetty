package io.xuqi.myNetty.channel;

import java.net.SocketAddress;

public class DefaultChannelPipeline implements ChannelPipeline {

    final AbstractChannelHandlerContext head;
    final AbstractChannelHandlerContext tail;
    final Channel channel;

    public DefaultChannelPipeline(Channel channel) {
        this.channel = channel;
        tail = new TailContext(this);
        head = new HeadContext(this);
        head.next = tail;
        tail.prev = head;
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
        return tail.bind(localAddress,promise);
    }

    @Override
    public ChannelFuture write(Object msg) {
        return tail.write(msg);
    }


    @Override
    public Channel channel() {
        return channel;
    }

    @Override
    public ChannelPipeline addLast(ChannelHandler handler) {
        final AbstractChannelHandlerContext newCtx;
        synchronized (this){
            newCtx = new DefaultChannelHandlerContext(this, null, null, handler);
            AbstractChannelHandlerContext prev = tail.prev;
            newCtx.prev = prev;
            newCtx.next = tail;
            prev.next = newCtx;
            tail.prev = newCtx;

        }
        return this;
    }

    @Override
    public final ChannelPipeline fireChannelRead(Object msg) {
        AbstractChannelHandlerContext.invokeChannelRead(head, msg);
        return this;
    }


    final class TailContext extends AbstractChannelHandlerContext   implements ChannelInboundHandler{

        TailContext(DefaultChannelPipeline pipeline) {
            super(pipeline, null, "TailContext", true, false);
        }
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        }
        @Override
        public ChannelHandler handler() {
            return this;
        }
    }

    final class HeadContext extends AbstractChannelHandlerContext  implements ChannelOutboundHandler, ChannelInboundHandler{

        HeadContext(DefaultChannelPipeline pipeline){
            super(pipeline, null, "HeadContext", true, true);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ctx.fireChannelRead(msg);
        }
        @Override
        public ChannelHandler handler() {
            return this;
        }

        @Override
        public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
            channel.unsafe().bind(localAddress,promise);
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            channel.unsafe().write(msg,promise);
        }

    }
}
