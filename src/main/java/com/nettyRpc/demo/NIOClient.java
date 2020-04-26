package com.nettyRpc.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * <h3>概要:</h3>
 * TODO(请在此处填写概要)
 * <br>
 * <h3>功能:</h3>
 * <ol>
 * <li>TODO(这里用一句话描述功能点)</li>
 * </ol>
 * <h3>履历:</h3>
 * <ol>
 * <li>2020/4/23[SUXH] 新建</li>
 * </ol>
 */
public class NIOClient {

    /** 标识请求次数 */
    private static int flag = 0;

    /** 缓冲区大小 */
    private static final int BLOCK = 4096;

    /** 发送数据缓冲区 */
    private static ByteBuffer sendbuffer = ByteBuffer.allocate(BLOCK);

    /** 接受数据缓冲区 */
    private static ByteBuffer receivebuffer = ByteBuffer.allocate(BLOCK);

    /** 服务器端地址 */
    private final static InetSocketAddress SERVER_ADDRESS = new InetSocketAddress("localhost", NIOServer.PORT);

    public static void main(String[] args) throws IOException {
        // 打开socket通道
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);// 设置为非阻塞方式
        Selector selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT);//注册连接服务端socket动作
        socketChannel.connect(SERVER_ADDRESS);

        Set<SelectionKey> selectionKeys;
        Iterator<SelectionKey> iterator;
        SelectionKey selectionKey;
        SocketChannel client;//客户端channel可写可读
        String receiveText;
        String sendText;
        int count=0;

        while (true) {
            selector.select();//选择一组键，其相应的通道已为 I/O 操作准备就绪。此方法执行处于阻塞模式的选择操作。
            //返回此选择器的已选择键集。
            selectionKeys = selector.selectedKeys();
            iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                selectionKey = iterator.next();
                if (selectionKey.isConnectable()) {
                    System.out.println("client connect");
                    client = (SocketChannel) selectionKey.channel();
                    if (client.isConnectionPending()) {// 判断此通道上是否正在进行连接操作。完成套接字通道的连接过程。
                        client.finishConnect();
                        System.out.println("完成连接!");
                        sendbuffer.clear();
                        sendbuffer.put("Hello,Server".getBytes());
                        sendbuffer.flip();
                        client.write(sendbuffer);
                    }
                    client.register(selector, SelectionKey.OP_READ);
                } else if (selectionKey.isReadable()) {
                    client = (SocketChannel) selectionKey.channel();
                    //将缓冲区清空以备下次读取
                    receivebuffer.clear();
                    //读取服务器发送来的数据到缓冲区中
                    count=client.read(receivebuffer);
                    if(count>0){
                        receiveText = new String( receivebuffer.array(),0,count);
                        System.out.println("客户端接受服务器端数据--:"+receiveText);
                        client.register(selector, SelectionKey.OP_WRITE);
                    }

                } else if (selectionKey.isWritable()) {
                    sendbuffer.clear();
                    client = (SocketChannel) selectionKey.channel();
                    sendText = "message from client--" + (flag++);
                    sendbuffer.put(sendText.getBytes());
                    //将缓冲区各标志复位,因为向里面put了数据标志被改变要想从中读取数据发向服务器,就要复位
                    sendbuffer.flip();
                    client.write(sendbuffer);
                    System.out.println("客户端向服务器端发送数据--："+sendText);
                    client.register(selector, SelectionKey.OP_READ);
                }
            }
            selectionKeys.clear();
        }
    }
}


