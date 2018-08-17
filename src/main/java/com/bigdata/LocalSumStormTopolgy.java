package com.bigdata;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
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

/**
 * Created by luozhenfei1 on 2018/6/15.
 * 使用Storm实现累计求和操作
 */
public class LocalSumStormTopolgy {

    /*
    * Spout需要继承BaseRichSpout
    * 数据源需要产生数据并实现
    * */
    public static class DataSourceSpout extends BaseRichSpout {

        private SpoutOutputCollector collector;
        /*
        *初始化方法，只会被调用一次
        * */

        public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
            this.collector = collector;
        }

        int number = 0;

        /*
        * 产生数据，在生产上肯定是从消息队列中获取数据
        * 这个方法是死循环，会一直不停运行
        * */
        public void nextTuple() {
            this.collector.emit(new Values(++number));
            System.out.println("Spout" + number);

            //防止数据产生太快
            Utils.sleep(1000);
        }

        /*
        * 声明输出字段
        * */
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("num"));
        }
    }

    /*
    * 数据累计求和Bolt：接受数据并处理
    * */
    public static class SumBolt extends BaseRichBolt {

        /*
        * 初始化方法，只会被调用一次
        * */
        public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {

        }

        int sum = 0;

        /*
        * 其实也是一个死循环，职责：获取Spout发送过来的数据
        * */
        public void execute(Tuple input) {
            //Bolt中获取值可以根据index获取，也可以根据上一个环节的filed获取（建议使用）
            Integer value = input.getIntegerByField("num");
            sum += value;
            System.out.println("Bolt:sum = [" + sum + "]");
        }

        public void declareOutputFields(OutputFieldsDeclarer declarer) {

        }
    }

    public static void main(String[] args) {
        //TopologyBuilder根据Spout和Bolt来构建Topology
        //Storm中任何一个作业都是通过Topology的方式进行提交的
        //Topology中需要指定Spout和Bolt的执行顺序
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("DataSourceSpout", new DataSourceSpout());
        builder.setBolt("SumBolt", new SumBolt()).shuffleGrouping("DataSourceSpout");

        //创建一个本地的Storm集群：本地模式运行，不需要搭建Storm集群
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("LocalSumStormTopolgy", new Config(), builder.createTopology());
        //cluster.shutdown();
    }
}
