package com.study.kafka.producer.partition;


import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.utils.Utils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * 1.kafka 1.0 默认分区策略是：
 *   1）如果在kafka message中指定了分区，则使用该分区
 *   2）如果未指定分区但kafka message中存在key，则根据key的hash结果选择一个分区
 *   3）如果未指定分区且kafka message中不存在key，则以循环方式选择分区
 *
 * 2.我们也可以自定义分区策略，满足自己爹业务需求。如，包含指定字符串的key落在指定的分区上，可以用来优先消费。
 *
 */
public class ItemPartitioner implements Partitioner {

    private final ConcurrentMap<String, AtomicInteger> topicCounterMap = new ConcurrentHashMap();


    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {

        List partitions = cluster.partitionsForTopic(topic);
        int numPartitions = partitions.size();

        if(keyBytes == null) {
            //kafka msg 中不包含key，使用分区默认的逻辑。
            int nextValue = this.nextValue(topic);
            List availablePartitions = cluster.availablePartitionsForTopic(topic);
            if(availablePartitions.size() > 0) {
                int part = Utils.toPositive(nextValue) % availablePartitions.size();
                return ((PartitionInfo)availablePartitions.get(part)).partition();
            } else {
                //没有分区可用，任意给出一个不可用的分区
                return Utils.toPositive(nextValue) % numPartitions;
            }
        } else {
            //todo 可以在这自定义分区策略
            return Utils.toPositive(Utils.murmur2(keyBytes)) % numPartitions;
        }
    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> map) {

    }

    private int nextValue(String topic) {
        AtomicInteger counter = this.topicCounterMap.get(topic);
        if(null == counter) {
            counter = new AtomicInteger(ThreadLocalRandom.current().nextInt());
            AtomicInteger currentCounter = this.topicCounterMap.putIfAbsent(topic, counter);
            if(currentCounter != null) {
                counter = currentCounter;
            }
        }

        return counter.getAndIncrement();
    }

}
