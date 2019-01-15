package io.xuqi.myNetty.channel;

import java.net.SocketAddress;

public interface ChannelOutboundInvoker {
    ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise);
    ChannelFuture write(Object msg);
}
