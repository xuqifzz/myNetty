package io.xuqi.myNetty.channel;

public interface EventLoop extends EventLoopGroup {
    boolean inEventLoop(); //判断当前是否已经在事件循环的线程中
    void execute(Runnable command); //提交任务到事件循环线程中运行
}
