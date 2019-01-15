package io.xuqi.myNetty.channel.nio;

import io.xuqi.myNetty.channel.*;
import io.xuqi.myNetty.channel.socket.nio.AbstractNioChannel;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

public class NioEventLoop implements EventLoop, Runnable {
    private Selector selector;
    private Thread thread;
    final private Queue<Runnable> taskQueue;
    NioEventLoop() {
        taskQueue = new LinkedBlockingQueue<>();
        try {
            selector =  Selector.open();
            //Netty里面是等有任务以后才初始化线程,我这里管不了这么多了,在构造函数直接启动线程
            thread= new Thread(this);
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public EventLoop next() {
        return this;
    }

    @Override
    public ChannelFuture register(Channel channel) {
        return register(new DefaultChannelPromise(channel,this));

    }

    public ChannelFuture register(final ChannelPromise promise){
        promise.channel().register(this,promise);
        return promise;
    }

    public Selector unwrappedSelector() {
        return selector;
    }

    @Override
    public void run() {
        while (true) {
            try {
                int readyChannels = selector.select(512);
                processSelectedKeysPlain(selector.selectedKeys());
                runAllTasks(); //执行异步提交的任务
                Thread.yield();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

    }

    protected static void safeExecute(Runnable task) {
        try {
            task.run();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    protected void runAllTasks(){
        for (;;) {
            Runnable task = taskQueue.poll();
            if(task == null)
                break;
            safeExecute(task);
        }
    }

    private void processSelectedKeysPlain(Set<SelectionKey> selectedKeys){
        if (selectedKeys.isEmpty()) {
            return;
        }
        Iterator<SelectionKey> i = selectedKeys.iterator();
        for (;;) {
            final SelectionKey k = i.next();
            final AbstractNioChannel a = (AbstractNioChannel)k.attachment();
            i.remove();
            a.unsafe().read();
            if (!i.hasNext()) {
                break;
            }
        }
    }

    @Override
    public boolean inEventLoop() {
        return Thread.currentThread() == thread;
    }

    @Override
    public void execute(Runnable task) {
        taskQueue.offer(task);
        selector.wakeup(); //唤醒selector, 以免它因无事可做而一直阻塞

    }
}
