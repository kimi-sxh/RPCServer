package com.nettyRpc.nio2netty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class SocketMultiplexingThreads {

    private ServerSocketChannel server = null;
    private Selector selector1 = null;
    private Selector selector2 = null;
    private Selector selector3 = null;
    int port = 9090;



    public void initServer() {
        try {
            server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.bind(new InetSocketAddress(port));

            selector1 = Selector.open();
            selector2 = Selector.open();
            selector3 = Selector.open();

            server.register(selector1, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SocketMultiplexingThreads service = new SocketMultiplexingThreads();
        service.initServer();
        NioThread T1 = new NioThread(service.selector1, 2);

        NioThread T2 = new NioThread(service.selector2);
        NioThread T3 = new NioThread(service.selector3);

        T1.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        T2.start();
        T3.start();

        System.out.println("服务器启动了。。。。。");

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

class NioThread extends Thread {

    /** 当前多路复用 */
    Selector selector = null;

    /** worker数量 */
    int selectors = 0;

    /** 标识哪个worker */
    int id = 0;

    /** 当前selector 是否为boss */
    boolean  boss =  false;

    /** SocketChannel队列 几个worker就几个queue */
    static BlockingQueue<SocketChannel>[] queue;

    /** 线程安全整型用来计数 */
    static AtomicInteger idx = new AtomicInteger();

    NioThread(Selector sel, int n) {// boss
        this.selector = sel;
        this.selectors = n;  //2
        boss = true ;

        queue = new LinkedBlockingQueue[selectors];
        for (int i = 0; i < n; i++) {
            queue[i] = new LinkedBlockingQueue<>();
        }
        System.out.println("Boss 启动");
    }

    NioThread(Selector sel) {//worker
        this.selector = sel;
        id = idx.getAndIncrement() % selectors;
        System.out.println("worker: " + id + " 启动");
    }

    @Override
    public void run() {
        try {
            while (true) {

                while (selector.select(10) > 0) {
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iter = selectionKeys.iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();
                        if (key.isAcceptable()) {
                            acceptHandler(key);//第一步调用
                        } else if (key.isReadable()) {
                            readHandler(key);//第三步
                        }
                    }
                }
                if (!boss && !queue[id].isEmpty()) {   //第二步：boss  不参与的  你有3个线程，boss不参与，只有work根据分配，分别注册自己的client
                    ByteBuffer buffer = ByteBuffer.allocate(8192);
                    SocketChannel client = queue[id].take();
                    client.register(selector, SelectionKey.OP_READ, buffer);
                    System.out.println("-------------------------------------------");
                    System.out.println("新客户端：" + client.socket().getPort() + "分配到worker：" + (id));
                    System.out.println("-------------------------------------------");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void acceptHandler(SelectionKey key) {
        try {
            ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
            SocketChannel client = ssc.accept();
            client.configureBlocking(false);

            int num = idx.getAndIncrement() % selectors;  //0,1

            queue[num].add(client);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * <b>概要：</b>:
     *      读取可用selector并读取通道信息，并将消息输出到客户端
     * <b>作者：</b>SUXH</br>
     * <b>日期：</b>2020/4/26 15:55 </br>
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
                    buffer.clear();
                } else if (read == 0) {
                    break;
                } else {
                    client.close();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}



