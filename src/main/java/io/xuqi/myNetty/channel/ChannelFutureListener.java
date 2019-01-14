package io.xuqi.myNetty.channel;

public interface ChannelFutureListener {
    void operationComplete(ChannelFuture future);
}
