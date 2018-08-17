package com.bigdata.drpc;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;

import java.io.IOException;

/**
 * Created by luozhenfei1 on 2018/6/29.
 * RPC服务
 */
public class RPCServer {
    public static void main(String[] args) throws IOException {
        Configuration configuration = new Configuration();
        RPC.Builder builder = new RPC.Builder(configuration);

        //Java Builder模式
        RPC.Server server = builder.setProtocol(UserService.class)
                .setInstance(new UserServiceImpl())
                .setBindAddress("localhost")
                .setPort(9999)
                .build();

        server.start();

    }
}
