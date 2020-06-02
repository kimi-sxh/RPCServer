package com.nettyRpc;


import com.nettyRpc.client.nettyClientScan.EnableNettyRpcClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Created by SUXH.
 */

@SpringBootApplication
@EnableNettyRpcClient(basePackages = {"com.nettyRpc"})
public class NettyRpcSpringBootApplication implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(NettyRpcSpringBootApplication.class);
    }

    /**
     * <b>概要：</b>:
     *      通过实现WebServerFactoryCustomizer 设置启动端口
     * <b>作者：</b>SUXH</br>
     * <b>日期：</b>2020/4/28 16:10 </br>
     * @param:
     * @return:
     */
    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
        factory.setPort(9393);
    }
}