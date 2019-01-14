package io.xuqi.myNetty.channel;

public interface ChannelFuture {
    //返回关联的channel
    Channel channel();
    //判断操作是否完成
    boolean isDone();
    //等待操作完成
    ChannelFuture await() throws InterruptedException;
    //添加监听器,监听操作完成
    ChannelFuture addListener(ChannelFutureListener listener);
}
