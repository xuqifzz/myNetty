package io.xuqi.myNetty.channel;

import java.net.SocketAddress;

abstract class AbstractChannelHandlerContext implements ChannelHandlerContext{
    private final boolean inbound;
    private final boolean outbound;
    private final DefaultChannelPipeline pipeline;
    private final String name;
    private final EventLoop eventLoop;

    volatile AbstractChannelHandlerContext next;
    volatile AbstractChannelHandlerContext prev;

    AbstractChannelHandlerContext(DefaultChannelPipeline pipeline,
                                  EventLoop eventLoop,
                                  String name,
                                  boolean inbound,
                                  boolean outbound){
        this.pipeline = pipeline;
        this.eventLoop = eventLoop;
        this.inbound = inbound;
        this.outbound = outbound;
        this.name = name;
    }

    public EventLoop eventLoop() {
        if (eventLoop == null) {
            return pipeline.channel().eventLoop();
        } else {
            return eventLoop;
        }
    }
    @Override
    public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
        final AbstractChannelHandlerContext next = findContextOutbound();
        EventLoop eventLoop = next.eventLoop();
        if(eventLoop.inEventLoop()){
            next.invokeBind(localAddress, promise);
        }else {
            safeExecute(eventLoop, ()->{next.invokeBind(localAddress, promise);}, promise, null);
        }
        return promise;
    }

     @Override
     public ChannelFuture write(Object msg) {
         AbstractChannelHandlerContext next = findContextOutbound();
         ChannelPromise promise = new DefaultChannelPromise(pipeline.channel(),eventLoop());
         EventLoop eventLoop = next.eventLoop();
         if (eventLoop.inEventLoop()) {
             next.invokeWrite(msg, promise);
         }else {
             safeExecute(eventLoop,()-> {next.invokeWrite(msg, promise);},promise,msg);
         }

         return promise;
     }

    private void invokeWrite(Object msg, ChannelPromise promise) {
        try {
            ((ChannelOutboundHandler) handler()).write(this, msg, promise);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

     private void invokeBind(SocketAddress localAddress, ChannelPromise promise) {
        try {
            ((ChannelOutboundHandler) handler()).bind(this, localAddress, promise);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }



    private AbstractChannelHandlerContext findContextOutbound() {
        AbstractChannelHandlerContext ctx = this;
        do {
            ctx = ctx.prev;
        } while (!ctx.outbound);
        return ctx;
    }
    private AbstractChannelHandlerContext findContextInbound() {
        AbstractChannelHandlerContext ctx = this;
        do {
            ctx = ctx.next;
        } while (!ctx.inbound);
        return ctx;
    }

    private static boolean safeExecute(EventLoop eventLoop, Runnable runnable, ChannelPromise promise, Object msg) {
        try {
            eventLoop.execute(runnable);
            return true;
        } catch (Throwable cause) {
            cause.printStackTrace();
            return false;
        }
    }


     @Override
     public ChannelHandlerContext fireChannelRead(final Object msg) {
         invokeChannelRead(findContextInbound(), msg);
         return this;
     }
    private void invokeChannelRead(Object msg) {
        try {
            ((ChannelInboundHandler) handler()).channelRead(this, msg);
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }
    static void invokeChannelRead(final AbstractChannelHandlerContext next, Object msg) {
        EventLoop eventLoop = next.eventLoop();
        if (eventLoop.inEventLoop()) {
            next.invokeChannelRead(msg);
        } else {
            eventLoop.execute(()-> next.invokeChannelRead(msg));
        }
    }
 }
