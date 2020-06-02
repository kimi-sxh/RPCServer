package com.nettyRpc.client.nettyClientScan;

import java.lang.annotation.*;

/**
 * Created by SUXH.
 *      NettyRpcClient注解标识是一个rpc客户端
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface NettyRpcClient {

}
