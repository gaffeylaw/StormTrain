package com.bigdata.integration.hbase;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.hbase.bolt.HBaseBolt;
import org.apache.storm.hbase.bolt.mapper.SimpleHBaseMapper;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 使用Storm完成词频统计功能
 */
public class LocalWordCountHBaseStormTopology {

    public static class DataSourceSpout extends BaseRichSpout {
        private SpoutOutputCollector collector;

        public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
            this.collector = collector;
        }

        /**
         * 业务：
         * 1） 读取指定目录的文件夹下的数据:/Users/rocky/data/storm/wc
         * 2） 把每一行数据发射出去
         */

        public static final String[] words = new String[]{"apple", "orange", "banana"};

        public void nextTuple() {
            Random random = new Random();
            String word = words[random.nextInt(words.length)];
            this.collector.emit(new Values(word));
            System.out.println("发送了一个单词:" + word);
            Utils.sleep(1000);
        }

        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("line"));
        }
    }


    /**
     * 对数据进行分割
     */
    public static class SplitBolt extends BaseRichBolt {

        private OutputCollector collector;

        public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
            this.collector = collector;
        }

        /**
         * 业务逻辑：
         * line： 对line进行分割，按照逗号
         */
        public void execute(Tuple input) {
            String word = input.getStringByField("line");
            this.collector.emit(new Values(word));
        }

        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("word"));
        }
    }


    /**
     * 词频汇总Bolt
     */
    public static class CountBolt extends BaseRichBolt {
        private OutputCollector collector;

        public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
            this.collector = collector;
        }


        Map<String, Integer> map = new HashMap<String, Integer>();

        /**
         * 业务逻辑：
         * 1）获取每个单词
         * 2）对所有单词进行汇总
         * 3）输出
         */
        public void execute(Tuple input) {
            // 1）获取每个单词
            String word = input.getStringByField("word");
            Integer count = map.get(word);
            if (count == null) {
                count = 0;
            }

            count++;

            // 2）对所有单词进行汇总
            map.put(word, count);

            // 3）输出
            this.collector.emit(new Values(word, map.get(word)));

        }

        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("word", "count"));
        }
    }

    public static void main(String[] args) {
        Config config = new Config();
        Map<String, Object> hbaseConf = new HashMap<String, Object>();
        hbaseConf.put("hbase.rootdir", "hdfs://hadoop001:8020/hbase");
        hbaseConf.put("hbase.zookeeper.quorum", "hadoop001:2181");
        config.put("hbase.conf", hbaseConf);

        // 通过TopologyBuilder根据Spout和Bolt构建Topology
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("DataSourceSpout", new DataSourceSpout());
        builder.setBolt("SplitBolt", new SplitBolt()).shuffleGrouping("DataSourceSpout");
        builder.setBolt("CountBolt", new CountBolt()).shuffleGrouping("SplitBolt");

        SimpleHBaseMapper mapper = new SimpleHBaseMapper()
                .withRowKeyField("word")
                .withColumnFields(new Fields("word"))
                .withCounterFields(new Fields("count"))
                .withColumnFamily("cf");

        HBaseBolt hbaseBolt = new HBaseBolt("WordCount", mapper).withConfigKey("hbase.conf");

        builder.setBolt("HBaseBolt", hbaseBolt).shuffleGrouping("CountBolt");

        // 创建本地集群
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("LocalWordCountHBaseStormTopology", config, builder.createTopology());
    }

}
