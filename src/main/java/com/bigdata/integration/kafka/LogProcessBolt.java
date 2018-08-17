package com.bigdata.integration.kafka;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

/**
 * Created by luozhenfei1 on 2018/7/12.
 */
public class LogProcessBolt extends BaseRichBolt {

    private OutputCollector collector;

    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    public void execute(Tuple input) {
        try {
            byte[] binaryByField = input.getBinaryByField("bytes");
            String value = new String(binaryByField);
            /*
            * 13688888888	116.272876,39.99243	[2018-07-12 16:50:32]
            * 解析日志
            * */
            String[] splits = value.split("\t");
            String phone = splits[0];
            String[] temp = splits[1].split(",");
            String longitude = temp[0];
            String latitude = temp[1];
            long time = DateUtils.getInstance().getTime(splits[2]);

            System.out.println(phone + "," + longitude + "," + latitude + "," + time);

            collector.emit(new Values(time, Double.parseDouble(longitude), Double.parseDouble(latitude)));
            this.collector.ack(input);
        } catch (Exception e) {
            this.collector.fail(input);
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("time", "longitude", "latitude"));
    }
}
