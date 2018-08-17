package com.bigdata.drpc;

import org.apache.storm.Config;
import org.apache.storm.thrift.TException;
import org.apache.storm.utils.DRPCClient;

/**
 * Created by luozhenfei1 on 2018/6/29.
 */
public class RemoteDRPCClient {
    public static void main(String[] args) throws TException {
        Config config = new Config();

        //参数
        config.put(Config.STORM_NIMBUS_RETRY_TIMES, 3);
        config.put(Config.STORM_NIMBUS_RETRY_INTERVAL, 10);
        config.put(Config.STORM_NIMBUS_RETRY_INTERVAL_CEILING, 20);
        config.put(Config.DRPC_MAX_BUFFER_SIZE, 1048576);
        
        DRPCClient client = new DRPCClient(config, "hadoop001", 3772);
        String result = client.execute("addUser", "lisi");
        System.out.println("Client invoked:" + result);
    }
}
