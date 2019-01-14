package io.xuqi.myNetty.channel;

public interface ChannelPromise extends ChannelFuture{
    //这里只实现一个方法,就是设置操作成功, 因为我们暂时不考虑异常情况!
    ChannelPromise setSuccess();
}
