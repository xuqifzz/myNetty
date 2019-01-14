package io.xuqi.standlone;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * i++ single thread nio http server
 */
public class MyHttpServer {

//
//    public static void main(String[] args) throws Exception{
//        //创建web服务实例,监听8899端口,网页根目录为D:/tmp/website
//        MyHttpServer httpServer = new MyHttpServer(8899,"D:/tmp/website");
//        //启动http服务
//        httpServer.start();
//    }

    final private int port;
    final private String rootDir;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    static private Map<String,String> mimeMapping;


    public MyHttpServer(int port, String rootDir)  {
        this.port = port;
        this.rootDir = rootDir;
    }

    /**
     * 单线程选择器
     */
    private void run() {
        try {
            while (true){
                //选择
                int readyChannels = selector.select(512);
                if (readyChannels == 0)
                    continue;
                final Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isAcceptable()) {
                        //处理新连接
                        doAcceptable();
                    } else if (key.isReadable()) {
                        //处理数据接收
                        doReadable(key);
                    }
                    keyIterator.remove();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void doService(SocketChannel socketChannel, ByteBuffer requestBuffer) throws IOException {
        try {
            //提取客户请求的资源路径
            String path = extractPath(requestBuffer);
            //打开路径对应的FileChannel
            FileChannel fileChannel = getFileChannel(path);
            //输出返回头,包括200以及ContentType
            outputResponseHeader(socketChannel,getContentType(path));
            //输出文件内容
            outputFileChannel(socketChannel,fileChannel);
        } catch (FileNotFoundException e) {
            //文件不存在则返回404
            response404(socketChannel);
        }

    }

    /**
     * 返回正常头
     */
    private void outputResponseHeader(SocketChannel socketChannel,String contentType) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 200 OK\r\n");
        if(!contentType.isEmpty()){
            sb.append("Content-Type: " + contentType + "\r\n");
        }
        sb.append("\r\n");
        outputByteBuffer(socketChannel,ByteBuffer.wrap(sb.toString().getBytes()));


    }
    /**
     * 输出文件内容
     */
    private void outputFileChannel(SocketChannel socketChannel,FileChannel fileChannel) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(10240);
        while (fileChannel.read(byteBuffer) > 0){
            byteBuffer.flip();
            outputByteBuffer(socketChannel,byteBuffer);
            byteBuffer.clear();
        }
        fileChannel.close();
    }
    /**
     * 返回404
     */
    private void response404(SocketChannel socketChannel) throws IOException {
        String response = "HTTP/1.1 404 Not Found\r\n\r\n";
        outputByteBuffer(socketChannel,ByteBuffer.wrap(response.getBytes()));

    }

    private void outputByteBuffer(SocketChannel socketChannel,ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()){
            socketChannel.write(buffer);
        }
    }


    /**
     * 因为这是个静态资源服务器,因此只需要解析出http头的第一行数据即可
     * 第一行一般是 一般是GET /path HTTP/1.1
     * 这里的处理是要提取出/path, 找到对应文件后,返回给浏览器
     */
    private void doReadable(SelectionKey key) {

        //获取关联SocketChannel
        SocketChannel socketChannel = (SocketChannel) key.channel();

        try {
            ByteBuffer requestBuffer = ByteBuffer.allocate(1024);
            while (socketChannel.isOpen() && socketChannel.read(requestBuffer) != -1) {
                // 只要有数据就可以了,因为我们要的数据就在第一行!
                if (requestBuffer.position() > 0) break;
            }
            requestBuffer.flip();
            //服务处理实现
            doService(socketChannel,requestBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            //服务完毕后,就关闭key以及对应通道,每个连接只传送一个文件
            cancelSelectionKey(key);
        }
    }

    /**
     * 从http头的第一行提取path
     */
    static private String extractPath(ByteBuffer requestBuffer){
        ByteBuffer buffer = ByteBuffer.allocate(128);
        byte b;
        while ((b =requestBuffer.get()) != 10 && buffer.position() < 100)
            buffer.put(b);
        buffer.flip();
        //提取http请求的第一行,一般是GET /path HTTP/1.1
        String firstLine = new String(buffer.array(),0,buffer.limit()-1);
        int idx = firstLine.indexOf("/");
        return firstLine.substring(idx,firstLine.indexOf(" ",idx));

    }

    private FileChannel getFileChannel(String path) throws FileNotFoundException {
        File file=new File(rootDir + path);
        if(file.isDirectory()){
            file = new File(file.getAbsolutePath() + "/index.html");
        }
        return new RandomAccessFile(file.getAbsolutePath(), "r").getChannel();

    }

    /**
     * 关闭key,同时关闭关联的SocketChannel
     */
    static private void cancelSelectionKey(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        key.cancel();
        try {
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 查阅mimeMapping来计算ContentType
     */
    static private String getContentType(String path){
        String suffix = path.substring(path.lastIndexOf(".") + 1).toLowerCase();
        return mimeMapping.getOrDefault(suffix,"");
    }


    /**
     * 处理接收新连接
     * 1. 接收新连接
     * 2. 设置非阻塞
     * 3. 向选择器注册OP_READ事件
     */
    private void doAcceptable() throws IOException {
        SocketChannel clientChannel = serverSocketChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
    }

    public void start() throws IOException {
        init();
        Thread thread = new Thread(this::run);
        thread.start();

    }


    /**
     * 1. 初始化 serverSocketChannel,监听指定端口
     * 2. 初始化 selector
     * 3. serverSocketChannel在selector注册OP_ACCEPT事件
     */
    private void  init() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        selector =  Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    static {
        //请自行补充
        mimeMapping = new HashMap<>();
        mimeMapping.put("html","text/html");
        mimeMapping.put("css","text/css");
        mimeMapping.put("json","application/json");
        mimeMapping.put("js","application/javascript");
        mimeMapping.put("jpg","image/jpeg");
        mimeMapping.put("png","image/png");

    }

}