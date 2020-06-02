package com.nettyRpc.client.nettyClientScan;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

/**
 * Created by SUXH.
 *      注入bean
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class NettyRpcClientFactoryBean implements FactoryBean<Object>{

    private Class<?> type;

    /**
     * <b>概要：</b>:
     *      容器启动会调用该方法获取bean对象
     * <b>作者：</b>SUXH</br>
     * <b>日期：</b>2020/4/28 18:04 </br>
     * @return 返回代理对象
     */
    @Override
    public Object getObject() throws Exception {
        //创建代理对象
        return Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, new NettyRpcInvocationHandler(type));
    }

    @Override
    public Class<?> getObjectType() {
        return this.type;
    }

}