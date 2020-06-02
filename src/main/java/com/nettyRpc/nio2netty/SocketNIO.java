package com.nettyRpc.nio2netty;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

/**
 * <b>概要：</b>:
 *      支持多客户端非堵塞socket通信
 *          测试：
 *          终端1：linux shell:nc 192.168.79.1 9090
 *          终端2：linux shell:nc 192.168.79.1 9090
 * <b>作者：</b>SUXH</br>
 * <b>日期：</b>2020/4/26 15:40 </br>
 * @param:
 * @return:
 */
public class SocketNIO {
    public static void main(String[] args) throws Exception {

        LinkedList<SocketChannel> clients = new LinkedList<>();

        ServerSocketChannel ss = ServerSocketChannel.open();
        ss.bind(new InetSocketAddress(9090));
        ss.configureBlocking(false); //重点

        while(true){
            Thread.sleep(1000);
            SocketChannel client = ss.accept(); //不会阻塞
            if(client == null ){
                System.out.println("null.....");
            }else{
                client.configureBlocking(false);
                int port = client.socket().getPort();
                System.out.println("client...port: "+port);
                clients.add(client);
            }

            ByteBuffer buffer = ByteBuffer.allocateDirect(4096);  //可以在堆里   堆外

            for (SocketChannel c : clients) {   //串行化！！！！  多线程！！
                int num = c.read(buffer);  // >0  -1  0   //不会阻塞
                if(num>0){
                    buffer.flip();
                    byte[] aaa  =  new byte[buffer.limit()];
                    buffer.get(aaa);

                    String b = new String(aaa);
                    System.out.println(c.socket().getPort()+" : " + b );
                    buffer.clear();
                }


            }
        }
    }

}
