package com.nettyRpc.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by SUXH on 2019/4/3.
 */

@RestController
public class UserController {


    @Autowired
    UserService userService;

    @RequestMapping(value = "/callRpc")
    public String callRpcTest(@RequestParam(value = "getParams") String getParams){
        //master
        userService.callRpc(getParams + " execute......");
        return "ok";
    }

    //33、44、66
}
