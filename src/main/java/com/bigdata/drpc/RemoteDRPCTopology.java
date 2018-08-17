package com.bigdata.drpc;


import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.drpc.LinearDRPCTopologyBuilder;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

/**
 * Created by luozhenfei1 on 2018/6/29.
 */
public class RemoteDRPCTopology {
    public static class MyBolt extends BaseRichBolt {

        private OutputCollector outputCollector;

        public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
            this.outputCollector = collector;
        }

        public void execute(Tuple input) {
            Object requestId = input.getValue(0);//请求的id
            String name = input.getString(1);//请求的参数

            /*
            * TODO:业务逻辑处理
            * */
            String result = "add user:" + name;
            this.outputCollector.emit(new Values(requestId, result));
        }

        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("id", "result"));
        }
    }

    public static void main(String[] args) throws InvalidTopologyException, AuthorizationException, AlreadyAliveException {
        LinearDRPCTopologyBuilder builder = new LinearDRPCTopologyBuilder("addUser");
        builder.addBolt(new MyBolt());

        StormSubmitter.submitTopology("drpc-topology", new Config(), builder.createRemoteTopology());

    }
}