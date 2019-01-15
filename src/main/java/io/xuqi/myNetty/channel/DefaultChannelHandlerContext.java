package io.xuqi.myNetty.channel;

public class DefaultChannelHandlerContext  extends AbstractChannelHandlerContext{
    private final ChannelHandler handler;
    DefaultChannelHandlerContext(DefaultChannelPipeline pipeline, EventLoop eventLoop, String name, ChannelHandler handler) {
        super(pipeline, eventLoop, name, handler instanceof ChannelInboundHandler, handler instanceof ChannelOutboundHandler);
        this.handler = handler;
    }

    @Override
    public ChannelHandler handler() {
        return handler;
    }

}
