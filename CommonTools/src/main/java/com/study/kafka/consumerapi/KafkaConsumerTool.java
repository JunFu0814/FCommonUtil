package com.study.kafka.consumerapi;

import com.study.kafka.handler.KafkaMsgHandler;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by lf52 on 2018/4/12.
 */
public class KafkaConsumerTool {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerTool.class);

    private List<String> subscribe;
    private Properties props;

    public KafkaConsumerTool(String topic,Properties props){
        this.subscribe = Arrays.asList(topic);
        this.props = props;
    }

    public KafkaConsumerTool(List<String> subscribe,Properties props){
        this.subscribe = subscribe;
        this.props = props;
    }

    /**
     * 自动控制offset
     * @param kafkaMsgHandler
     * @throws Exception
     */
    public void consume(final KafkaMsgHandler kafkaMsgHandler) throws Exception {

        KafkaConsumer<byte[], byte[]> consumer = new KafkaConsumer(props);
        consumer.subscribe(subscribe);

        while (true) {
            ConsumerRecords<byte[], byte[]> records = consumer.poll(100);
            for (ConsumerRecord<byte[], byte[]> record : records)
                kafkaMsgHandler.callback(record);
        }

    }

    /**
     * 手动控制offset消费
     * @param kafkaMsgHandler
     * @throws Exception
     */
    public void consumeOffsetControl(final KafkaMsgHandler kafkaMsgHandler) throws Exception {

        KafkaConsumer<byte[], byte[]> consumer = new KafkaConsumer(props);
        consumer.subscribe(subscribe);
        while (true) {
            ConsumerRecords<byte[], byte[]> records = consumer.poll(100);
            for (final TopicPartition partition : records.partitions()) {
                List<ConsumerRecord<byte[], byte[]>> partitionRecords = records.records(partition);
                if(kafkaMsgHandler.callback(partitionRecords)){
                    //确认提交offset的数据一定是业务逻辑中处理成功的
                    long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
                    consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(lastOffset + 1)));
                }
            }
        }

    }

    /**
     * consume in more partition
     * @param kafkaMsgHandler
     * @param topic
     * @param partitions
     * @param minBatchSize
     * @throws Exception
     */
    public void consume(final KafkaMsgHandler kafkaMsgHandler,String topic,Integer[] partitions,int minBatchSize) throws Exception {

        KafkaConsumer<byte[], byte[]> consumer = new KafkaConsumer(props);

        List<TopicPartition> topicPartitionList = new ArrayList(partitions.length);
        for (int partition : partitions){
            TopicPartition topicPartition = new TopicPartition(topic, partition);
            topicPartitionList.add(topicPartition);
        }
        consumer.assign(topicPartitionList);
        List<ConsumerRecord<byte[], byte[]>> buffer = new ArrayList<>();
        while (true) {
            ConsumerRecords<byte[], byte[]> records = consumer.poll(100);
            for (ConsumerRecord<byte[], byte[]> record : records){
                buffer.add(record);
            }
            if (buffer.size() >= minBatchSize) {
                kafkaMsgHandler.callback(buffer);
                consumer.commitSync();
                buffer.clear();
            }
        }

    }

    /**
     * consume in one partition
     * @param kafkaMsgHandler
     * @param topic
     * @param partition
     * @param minBatchSize
     * @throws Exception
     */
    public void consume(final KafkaMsgHandler kafkaMsgHandler,String topic,int partition,int minBatchSize) throws Exception {
        consume(kafkaMsgHandler, topic, (Integer[]) Arrays.asList(partition).toArray(), minBatchSize);
    }

    /**
     * 从指定offset开始消费
     * @param kafkaMsgHandler
     * @param topic
     * @param partition
     * @param minBatchSize
     * @param offset
     * @throws Exception
     */
    public void  consume(final KafkaMsgHandler kafkaMsgHandler,String topic,Integer partition,int minBatchSize,long offset) throws Exception {

        KafkaConsumer<byte[], byte[]> consumer = new KafkaConsumer(props);
        TopicPartition topicPartition = new TopicPartition(topic, partition);
        consumer.assign(Arrays.asList(topicPartition));
        consumer.seek(topicPartition, offset);
        List<ConsumerRecord<byte[], byte[]>> buffer = new ArrayList<>();
        while (true) {
            ConsumerRecords<byte[], byte[]> records = consumer.poll(100);
            for (ConsumerRecord<byte[], byte[]> record : records){
                buffer.add(record);
            }
            if (buffer.size() >= minBatchSize) {
                kafkaMsgHandler.callback(buffer);
                consumer.commitSync();
                buffer.clear();
            }
        }

    }




}
