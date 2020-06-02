package com.nettyRpc.nio2netty;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * <b>概要：</b>:
 *      oio 堵塞socket通信
 *          测试：nc 192.168.79.1 9090
 * <b>作者：</b>SUXH</br>
 * <b>日期：</b>2020/4/26 15:29 </br>
 * @param:
 * @return:
 */
public class SocketIO {

    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(9090);

        System.out.println("step1: new ServerSocket(9090) ");


        Socket client = server.accept();  //阻塞1
        System.out.println("step2:client\t"+client.getPort());

        while(true){
            InputStream in = client.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            System.out.println(reader.readLine());  //阻塞2
        }
    }


}
