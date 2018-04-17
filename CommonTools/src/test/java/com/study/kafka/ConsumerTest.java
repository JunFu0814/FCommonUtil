package com.study.kafka;

import com.study.kafka.consumerapi.KafkaConsumerTool;
import com.study.kafka.handler.KafkaMsgHandler;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;

/**
 * Created by lf52 on 2018/4/13.
 */
public class ConsumerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerTest.class);

    static ExecutorService pool = Executors.newFixedThreadPool(4);
    /**
     * 普通batch消费
     * @throws Exception
     */
    @Test
    public void test() throws Exception {
        Properties porp = initConfig("ssecbigdata03:9092");
        KafkaConsumerTool tool = new KafkaConsumerTool("kafka10test",porp);
        tool.consume(new KafkaMsgHandler() {
            @Override
            public Boolean callback(Object object) throws Exception {
                List<ConsumerRecord<byte[], byte[]>> recordlist = (List<ConsumerRecord<byte[], byte[]>>)object;
                System.out.println("record size : " + recordlist.size());
                recordlist.forEach(record -> {
                    try {
                        System.out.printf("offset = %d, key = %s, value = %s , partition = %s%n", record.offset(), new String(record.key(), "UTF-8"), new String(record.value(), "UTF-8"), record.partition());
                    } catch (UnsupportedEncodingException e) {
                        LOGGER.error(e.getMessage());
                    }
                });

                return true;
            }
        },5);

    }

    /**
     * batch消费：手动控制offset
     * @throws Exception
     */
    @Test
    public void test1() throws Exception {
        Properties porp = initConfig("ssecbigdata03:9092");
        KafkaConsumerTool tool = new KafkaConsumerTool("kafka10test",porp);
        tool.consume(new KafkaMsgHandler() {
            @Override
            public Boolean callback(Object object) throws Exception {


                List<ConsumerRecord<byte[], byte[]>> partitionRecords = (List<ConsumerRecord<byte[], byte[]>>)object;
                for (ConsumerRecord<byte[], byte[]> record : partitionRecords) {
                    try {
                        System.out.printf("offset = %d, key = %s, value = %s , partition = %s%n", record.offset(), new String(record.key(), "UTF-8"), new String(record.value(), "UTF-8"), record.partition());
                    } catch (UnsupportedEncodingException e) {
                        LOGGER.error(e.getMessage());
                    }
                }
                return true;
            }
        });

    }

    /**
     * 指定partition消费
     * @throws Exception
     */
    @Test
    public void test2() throws Exception {
        Properties porp = initConfig("ssecbigdata03:9092");
        KafkaConsumerTool tool = new KafkaConsumerTool("kafka10test",porp);
        tool.consume(new KafkaMsgHandler() {
            @Override
            public Boolean callback(Object object) throws Exception {


                List<ConsumerRecord<byte[], byte[]>> partitionRecords = (List<ConsumerRecord<byte[], byte[]>>)object;
                for (ConsumerRecord<byte[], byte[]> record : partitionRecords) {
                    try {
                        System.out.printf("offset = %d, key = %s, value = %s , partition = %s%n", record.offset(), new String(record.key(), "UTF-8"), new String(record.value(), "UTF-8"), record.partition());
                    } catch (UnsupportedEncodingException e) {
                        LOGGER.error(e.getMessage());
                    }
                }
                return true;
            }
        },"kafka10test",1,3);

    }

    /**
     * 一个consumer线程消费一个partition，如果一个consumer线程挂了对应的partition数据不会被消费(当前 topic 4个partition)
     * @throws Exception
     */
    @Test
    public void test3() throws Exception {
        String topic =  "kafka10test";
        List<Future<Boolean>> list = new ArrayList(4);
        Properties porp = initConfig("ssecbigdata03:9092");
        for (int i = 0 ; i < 4 ; i ++){
            int parition = i;
            Future<Boolean> future = pool.submit(new Callable<Boolean>() {
               @Override
               public Boolean call() throws Exception {
                   KafkaConsumerTool tool = new KafkaConsumerTool("kafka10test",porp);
                   tool.consume(new KafkaMsgHandler() {
                       @Override
                       public Boolean callback(Object object) throws Exception {


                           List<ConsumerRecord<byte[], byte[]>> partitionRecords = (List<ConsumerRecord<byte[], byte[]>>)object;
                           for (ConsumerRecord<byte[], byte[]> record : partitionRecords) {
                               try {
                                   System.out.printf(Thread.currentThread().getName() + " --> offset = %d, key = %s, value = %s , partition = %s%n", record.offset(), new String(record.key(), "UTF-8"), new String(record.value(), "UTF-8"), record.partition());
                               } catch (UnsupportedEncodingException e) {
                                   e.printStackTrace();
                                   LOGGER.error(e.getMessage());
                               }
                           }
                           return true;
                       }
                   },topic,parition,1);
                   return true;
               }
           });
            list.add(future);
       }
        list.forEach(future -> {
            try {
                future.get();
            } catch (Exception e) {
                LOGGER.error("Error", e);
            }
        });
    }


    private Properties initConfig(String brokers){
        Properties props = new Properties();
        props.put("bootstrap.servers", brokers);
        //手动提交offset
        props.put("enable.auto.commit", "false");
        props.put("session.timeout.ms", "30000");
        props.put("max.poll.records", 100); //每次poll最多获取100条数据
        props.put("group.id", "0");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        return props;
    }
}
