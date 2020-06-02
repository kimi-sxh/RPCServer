package com.nettyRpc.server;


import com.nettyRpc.client.UserService;

/**
 * Created by SUXH.
 */
public class UserServiceImpl implements UserService {

    @Override
    public String callRpc(String param) {
        //打印请求参数
        System.out.println(param);
        return param;
    }
}
