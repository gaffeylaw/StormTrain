package com.bigdata.integration.kafka;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.jdbc.bolt.JdbcInsertBolt;
import org.apache.storm.jdbc.common.ConnectionProvider;
import org.apache.storm.jdbc.common.HikariCPConnectionProvider;
import org.apache.storm.jdbc.mapper.JdbcMapper;
import org.apache.storm.jdbc.mapper.SimpleJdbcMapper;
import org.apache.storm.kafka.BrokerHosts;
import org.apache.storm.kafka.KafkaSpout;
import org.apache.storm.kafka.SpoutConfig;
import org.apache.storm.kafka.ZkHosts;
import org.apache.storm.shade.com.google.common.collect.Maps;
import org.apache.storm.topology.TopologyBuilder;

import java.util.Map;
import java.util.UUID;

/**
 * Created by luozhenfei1 on 2018/7/12.
 * Kafka整合Storm测试
 */
public class StormKafkaTopic {

    public static void main(String[] args) {

        TopologyBuilder builder = new TopologyBuilder();

        //topic名称
        String topicName = "project_topic";

        //指定存储路径
        String zkRoot = "/" + topicName;
        String id = UUID.randomUUID().toString();
        BrokerHosts hosts = new ZkHosts("hadoop001");
        SpoutConfig spoutConfig = new SpoutConfig(hosts, topicName, zkRoot, id);

        //设置读取偏移量
        spoutConfig.startOffsetTime = kafka.api.OffsetRequest.LatestTime();
        KafkaSpout kafkaSpout = new KafkaSpout(spoutConfig);

        String SPOUT_ID = KafkaSpout.class.getSimpleName();
        builder.setSpout(SPOUT_ID, kafkaSpout);

        String BOLT_ID = LogProcessBolt.class.getSimpleName();
        builder.setBolt(BOLT_ID, new LogProcessBolt()).shuffleGrouping(SPOUT_ID);

        Map hikariConfigMap = Maps.newHashMap();
        hikariConfigMap.put("dataSourceClassName", "com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        hikariConfigMap.put("dataSource.url", "jdbc:mysql://hadoop001/storm");
        hikariConfigMap.put("dataSource.user", "root");
        hikariConfigMap.put("dataSource.password", "root");
        ConnectionProvider connectionProvider = new HikariCPConnectionProvider(hikariConfigMap);

        String tableName = "stat";
        JdbcMapper simpleJdbcMapper = new SimpleJdbcMapper(tableName, connectionProvider);

        JdbcInsertBolt userPersistanceBolt = new JdbcInsertBolt(connectionProvider, simpleJdbcMapper)
                .withTableName(tableName)
                .withQueryTimeoutSecs(30);

        builder.setBolt("JdbcInsertBolt", userPersistanceBolt).shuffleGrouping(BOLT_ID);

        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology(StormKafkaTopic.class.getSimpleName(), new Config(), builder.createTopology());
    }
}
