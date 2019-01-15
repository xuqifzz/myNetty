package io.xuqi.myNetty.channel.socket.nio;
import io.xuqi.myNetty.channel.Channel;
import io.xuqi.myNetty.channel.ChannelPromise;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

//负责接收客户端发来的数据
public class NioSocketChannel extends AbstractNioChannel{
    private ByteBuffer buffer = ByteBuffer.allocate(1024);
    NioSocketChannel(Channel parent, SocketChannel socket){
        super(parent,socket, SelectionKey.OP_READ);
    }


    @Override
    protected SocketChannel javaChannel() {
        return (SocketChannel) super.javaChannel();
    }


    @Override
    public Object doReadMessages() {
        try {
            buffer.clear();
            javaChannel().read(buffer);
            buffer.flip();
            if(buffer.limit() > 0){
                byte[] result = new byte[buffer.limit()];
                buffer.get(result);
                return result;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    void doWrite(Object msg, ChannelPromise promise) {
        //此处简单处理
        try {
            ByteBuffer buffer = (ByteBuffer)msg;
            javaChannel().write(buffer);
            promise.setSuccess();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
        //暂时没有实现客户端的端口绑定
    }


}
