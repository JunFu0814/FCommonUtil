package com.study.kafka.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * kafka 1.0 producer   1.支持事务操作  2.支持往指定partition发送消息
 *
 * producer是线程安全的，跨线程共享单个生产者实例通常比拥有多个实例更快。
 * 官方API ：http://kafka.apache.org/11/javadoc/index.html?org/apache/kafka/clients/producer/KafkaProducer.html
 */
public class KafkaProducerTool {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerTool.class);

    private BlockingDeque<ProducerRecord<byte[], byte[]> > msgBlockingDeque = null;
    private volatile static KafkaProducerTool kafkaProducerTool = null;

    private Producer<byte[], byte[]> producer;
    private String topic;
    //private Properties props = new Properties();
    private Properties props;


    private static int queueSize = 1000;

    public static KafkaProducerTool getInstance(String topic,Properties props,int queueSize) {
        if (kafkaProducerTool == null) {
            synchronized (KafkaProducerTool.class) {
                if (kafkaProducerTool == null) {
                    kafkaProducerTool = new KafkaProducerTool(topic , props,queueSize);
                }
                return kafkaProducerTool;
            }
        }
        return kafkaProducerTool;
    }

    public static KafkaProducerTool getInstance(Properties props, String topic) {
        if (kafkaProducerTool == null) {
            synchronized (KafkaProducerTool.class) {
                if (kafkaProducerTool == null) {
                    kafkaProducerTool = new KafkaProducerTool(topic , props,queueSize);
                }
                return kafkaProducerTool;
            }
        }
        return kafkaProducerTool;
    }


    /**
     * 发送消息
     * @param key
     * @param value
     * @return
     */
    public boolean append(String key,byte[] value) {
        try {
            ProducerRecord<byte[], byte[]> record = new ProducerRecord(topic,key.getBytes("UTF-8"),value);
            return msgBlockingDeque.offer(record, 10, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            LOGGER.error("Append Message To Queue Error",e);
        }
        return false;
    }

    /**
     * 指定parttion 发送消息
     * @param key
     * @param value
     * @param partition
     * @return
     */
    public boolean append(String key,byte[] value,int partition) {
        try {
            ProducerRecord<byte[], byte[]> record = new ProducerRecord(topic,partition,key.getBytes("UTF-8"),value);
            return msgBlockingDeque.offer(record, 10, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            LOGGER.error("Append Message To Queue Error", e);
        }
        return false;
    }

    private KafkaProducerTool(String topic,Properties props,int queueSize){
        this.topic = topic;
        this.props = props;
        msgBlockingDeque = new LinkedBlockingDeque<>(queueSize);
        initKafkaConfig();
    }

    private void initKafkaConfig() {
        producer = new KafkaProducer(props);
        Thread thread = new Thread(new SendMesToKafkaRunnable());
        thread.setDaemon(true);
        thread.setName("SendMesToKafkaRunnable");
        thread.start();
    }

    class SendMesToKafkaRunnable implements Runnable {
        @Override
        public void run() {

            while (true) {
                try {
                    try {
                        ProducerRecord<byte[], byte[]> record = msgBlockingDeque.poll(10, TimeUnit.MILLISECONDS);
                        if(record != null){
                            //send方法是async的（future模式），每次调用增加一条记录附加到缓存时会立即返回。可以允许producer把所有个别的记录集中在一起发送，以提高性能。
                            producer.send(record);
                        }
                    } catch (KafkaException e) {
                        LOGGER.error("Producer Send Message To Kafka Error",e);
                    }
                } catch (InterruptedException e) {
                    LOGGER.error("Poll From Queue Error",e);
                }
            }

        }
    }
}
