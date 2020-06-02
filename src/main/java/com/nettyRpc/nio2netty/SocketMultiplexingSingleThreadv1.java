package com.nettyRpc.nio2netty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class SocketMultiplexingSingleThreadv1 {

    //NIO  N：nonblocking     socket网络，内核机制
    //NIO  N  new  io    jdk    {channel，bytebuffer，selector（多路复用器！）}

    private ServerSocketChannel server = null;

    /** 选择器 */
    private Selector selector = null;
    int port = 9090;

    /**
     * <b>概要：</b>:
     *      初始化ServerSocketChannel并绑定端口及Selector
     * <b>作者：</b>SUXH</br>
     * <b>日期：</b>2020/4/26 17:27 </br>
     * @param:
     * @return:
     */
    public void initServer() {
        try {
            server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.bind(new InetSocketAddress(port));

            selector = Selector.open();
            SelectionKey serkey = server.register(selector, SelectionKey.OP_ACCEPT);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * <b>概要：</b>:
     *      启动服务
     * <b>作者：</b>SUXH</br>
     * <b>日期：</b>2020/4/26 17:28 </br>
     * @param:
     * @return:
     */
    public void start() {
        initServer();
        System.out.println("服务器启动了。。。。。");
        try {
            while (true) {
                while (selector.select(0) > 0) {  //问过内核了有没有事件，内核回复有！
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();   //从多路复用器  取出有效的key

                    Iterator<SelectionKey> iter = selectionKeys.iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();

                        if (key.isAcceptable()) {
                            acceptHandler(key);
                        } else if (key.isReadable()) {
                            readHandler(key);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * <b>概要：</b>:
     *      接受客户端连接并绑定selector
     * <b>作者：</b>SUXH</br>
     * <b>日期：</b>2020/4/26 17:29 </br>
     * @param:
     * @return:
     */
    public void acceptHandler(SelectionKey key) {
        try {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
            SocketChannel client = serverSocketChannel.accept();
            client.configureBlocking(false);

            ByteBuffer buffer = ByteBuffer.allocate(8192);
            client.register(selector, SelectionKey.OP_READ, buffer);

            System.out.println("-------------------------------------------");
            System.out.println("新客户端：" + client.getRemoteAddress());
            System.out.println("-------------------------------------------");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * <b>概要：</b>:
     *      读到buffer数据
     * <b>作者：</b>SUXH</br>
     * <b>日期：</b>2020/4/26 17:31 </br>
     * @param:
     * @return:
     */
    public void readHandler(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();

        buffer.clear();
        int read = 0;
        try {
            while (true) {
                read = client.read(buffer);
                if (read > 0) {
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        client.write(buffer);
                    }
//                    byte[] aaa  =  new byte[buffer.limit()];
//                    buffer.get(aaa);
//                    String b = new String(aaa);
//                    System.out.println(client.socket().getPort()+" : " + b );//打印客户端传过来的数据
                    buffer.clear();
                } else if (read == 0) {
                    break;
                }
                else {   //-1   close_wait   bug  死循环  CPU  100%
                    client.close();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SocketMultiplexingSingleThreadv1 service = new SocketMultiplexingSingleThreadv1();
        service.start();
    }
}
