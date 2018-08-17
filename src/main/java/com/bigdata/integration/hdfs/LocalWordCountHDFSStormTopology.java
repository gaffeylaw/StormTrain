package com.bigdata.integration.hdfs;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.hdfs.bolt.HdfsBolt;
import org.apache.storm.hdfs.bolt.format.DefaultFileNameFormat;
import org.apache.storm.hdfs.bolt.format.DelimitedRecordFormat;
import org.apache.storm.hdfs.bolt.format.FileNameFormat;
import org.apache.storm.hdfs.bolt.format.RecordFormat;
import org.apache.storm.hdfs.bolt.rotation.FileRotationPolicy;
import org.apache.storm.hdfs.bolt.rotation.FileSizeRotationPolicy;
import org.apache.storm.hdfs.bolt.sync.CountSyncPolicy;
import org.apache.storm.hdfs.bolt.sync.SyncPolicy;
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

import java.util.Map;
import java.util.Random;

/**
 * 使用Storm完成词频统计功能
 */
public class LocalWordCountHDFSStormTopology {

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

        public static final String[] words = new String[]{"aple", "orange", "banana"};

        public void nextTuple() {
            Random random = new Random();
            String word = words[random.nextInt(words.length)];
            this.collector.emit(new Values(word));
            System.out.println("发送了一个单词:" + word);
            Utils.sleep(200);
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

    public static void main(String[] args) {

        // 通过TopologyBuilder根据Spout和Bolt构建Topology
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("DataSourceSpout", new DataSourceSpout());
        builder.setBolt("SplitBolt", new SplitBolt()).shuffleGrouping("DataSourceSpout");

        // use "|" instead of "," for field delimiter
        RecordFormat format = new DelimitedRecordFormat()
                .withFieldDelimiter("|");

        // sync the filesystem after every 1k tuples
        SyncPolicy syncPolicy = new CountSyncPolicy(1000);

        // rotate files when they reach 5MB
        FileRotationPolicy rotationPolicy = new FileSizeRotationPolicy(5.0f, FileSizeRotationPolicy.Units.MB);

        FileNameFormat fileNameFormat = new DefaultFileNameFormat()
                .withPath("/foo/");

        HdfsBolt bolt = new HdfsBolt()
                .withFsUrl("hdfs://hadoop001:8020")
                .withFileNameFormat(fileNameFormat)
                .withRecordFormat(format)
                .withRotationPolicy(rotationPolicy)
                .withSyncPolicy(syncPolicy);

        builder.setBolt("HdfsBolt", bolt).shuffleGrouping("SplitBolt");


        // 创建本地集群
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("LocalWordCountHDFSStormTopology",
                new Config(), builder.createTopology());
    }

}
