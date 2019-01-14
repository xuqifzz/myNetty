package io.xuqi.myNetty.channel;

import java.util.ArrayList;
import java.util.List;

public class DefaultChannelPromise implements ChannelPromise {
    private final Channel channel;
    private final EventLoop eventLoop;
    private List<ChannelFutureListener> listeners = new ArrayList<>();

    //是否成功的标志位
    private volatile boolean isSuccess = false;

    public DefaultChannelPromise(Channel channel,EventLoop eventLoop){
        this.channel = channel;
        this.eventLoop = eventLoop;
    }

    @Override
    public Channel channel() {
        return this.channel;
    }

    @Override
    public boolean isDone() {
        return isSuccess;
    }

    //阻塞等待操作完成
    @Override
    public ChannelPromise await() throws InterruptedException {
        if (isDone()) {
            return this;
        }
        //当操作没有完成时,挂起当前线程
        synchronized (this) {
            while (!isDone()) {
                wait();
            }
        }
        return this;
    }

    //添加监听器
    @Override
    public ChannelFuture addListener(ChannelFutureListener listener) {
        synchronized (this){
            listeners.add(listener);
        }
        //检查一下是不是早就完成了
        if (isDone()) {
            //通知所有监听器
            notifyListeners();
        }

        return this;
    }

    //设置操作完成
    @Override
    public ChannelPromise setSuccess() {
        isSuccess = true;
        synchronized (this){
            //唤醒所有被await挂起的线程
            notifyAll();
        }
        //调用所有监听器
        notifyListeners();
        return this;
    }

    private void notifyListeners(){
        if(eventLoop.inEventLoop()){
            notifyListenersNow();
        }else {
            eventLoop.execute(()-> notifyListenersNow());
        }
    }

    private void notifyListenersNow(){
        List<ChannelFutureListener> list = new ArrayList<>();

        synchronized (this){
            list.addAll(listeners);
            listeners.clear();
        }
        for (ChannelFutureListener listener : list){
            listener.operationComplete(this);
        }

    }

}
