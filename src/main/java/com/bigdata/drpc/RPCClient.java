package com.bigdata.drpc;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by luozhenfei1 on 2018/6/29.
 * RPC客户端
 */
public class RPCClient {
    public static void main(String[] args) throws IOException {
        Configuration configuration = new Configuration();
        long clientVersion = 1l;

        UserService userService = RPC.getProxy(UserService.class, clientVersion, new InetSocketAddress("localhost", 9999), configuration);
        userService.addUser("张三", 30);
        System.out.println("From Client Invoked: add user success......,name is ");
        RPC.stopProxy(userService);
    }
}
