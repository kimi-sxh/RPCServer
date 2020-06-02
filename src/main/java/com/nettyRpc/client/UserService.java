package com.nettyRpc.client;

import com.nettyRpc.client.nettyClientScan.NettyRpcClient;

/**
 * Created by suxh.
 *      定义业务方法
 */
@NettyRpcClient//标注是一个rpc客户端
public interface UserService {

    /**
     * <b>概要：</b>:
     *      rpc调用
     * <b>作者：</b>SUXH</br>
     * <b>日期：</b>2020/4/28 16:03 </br>
     * @param:
     * @return:
     */
    String callRpc(String param); //5
}
