package com.study.kafka.lowapi;

import com.study.kafka.handler.KafkaMsgHandler;
import kafka.api.FetchRequestBuilder;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.common.ErrorMapping;
import kafka.common.TopicAndPartition;
import kafka.javaapi.*;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.javaapi.message.ByteBufferMessageSet;
import kafka.message.MessageAndOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by lf52 on 2018/4/10.
 *
 *  1.使用low api的代价：
      partition,broker,offset对你不再透明需要自己去管理这些，并且还要处理broker leader的切换。

    2.什么情况下去使用 lowapi：
      一个消息消息需要读取多次
      在一个处理topic流程中，一个consumer只消费一个partition的数据。
      管理实务来确保一个消息只会被消费一次(手动提交offset)。

    3.使用low api的步骤：
      必须知道读哪个topic的哪个partition
      找到负责该partition的broker leader，从而找到存有该partition副本的那个broker
      自己去写request并fetch数据
      还要注意需要识别和处理broker leader的改变
 */
public class KafkaLowAPIConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaLowAPIConsumer.class);
    private List<String> replicaBrokers;
    private int soTimeout;
    private int bufferSize;
    private int fetchSize;

    public KafkaLowAPIConsumer(){
        replicaBrokers = new ArrayList();
        this.soTimeout = 100000;
        this.bufferSize = 1024* 64;
        this.fetchSize = 100000;
    }

    public KafkaLowAPIConsumer(int soTimeout,int bufferSize,int fetchSize){
        replicaBrokers = new ArrayList();
        this.soTimeout = soTimeout;
        this.bufferSize = bufferSize;
        this.fetchSize = fetchSize;
    }

    /**
     * Finding the Lead Broker for a Topic and Partition
     * @param seedBrokers broker list
     * @param port
     * @param topic
     * @param partition
     * @return
     * 遍历每个broker，取出该topic的metadata，然后再遍历其中的每个partition metadata，如果找到我们要找的partition就返回
     * 根据返回的PartitionMetadata.leader().host()找到leader broker
     */
    public PartitionMetadata findLeader(List<String> seedBrokers, int port, String topic, int partition) {
        PartitionMetadata returnMetaData = null;
        loop:
        for (String seed : seedBrokers) { //遍历每个broker
            SimpleConsumer consumer = null;
            try{
                //param : host,port,soTimeOut,bufferSize,clientId
                consumer = new SimpleConsumer(seed, port, soTimeout, bufferSize, "leaderLookup");
                //生成只读的单一元素的List
                List<String> topics = Collections.singletonList(topic);
                //获取topic的meta信息
                TopicMetadataRequest request = new TopicMetadataRequest(topics);
                TopicMetadataResponse response = consumer.send(request);
                //取到Topic的Metadata
                List<TopicMetadata> metaData = response.topicsMetadata();
                //遍历每个partition metadata信息
                for (TopicMetadata topicMetadata : metaData){
                    List<PartitionMetadata> partitionList = topicMetadata.partitionsMetadata();
                    for(PartitionMetadata part : partitionList){
                        if(part.partitionId() == partition){
                            returnMetaData = part;
                            break loop; //找对应的partition就返回到就返回
                        }
                    }
                }

            }catch (Exception e){
                LOGGER.error("Error communicating with Broker [" + seed + "] to find Leader for [" + topic + ", " + partition + "] Reason: " + e);
            }finally {
                if(consumer != null){
                    consumer.close();
                }
            }

            if (returnMetaData != null) {
                replicaBrokers.clear();
                for (kafka.cluster.Broker replica : returnMetaData.replicas()) {
                    replicaBrokers.add(replica.host());
                }
            }
        }
        return returnMetaData;
    }


    /**
     * Finding Starting Offset for Reads
     * @param consumer
     * @param topic
     * @param partition
     * @param whichTime
     * @param clientId
     * @return
     * 获取最后一次提交的offset，作为下次消费的开始
     */
    public long getLastOffset(SimpleConsumer consumer, String topic, int partition, long whichTime, String clientId) {
        //TopicAndPartition是对topic和partition信息的封装
        TopicAndPartition topicAndPartition = new TopicAndPartition(topic, partition);
        Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = new HashMap();

        //build offset fetch request info，whichTime表示where to start reading data，两个取值。不一定起始的offset一定是0，因为messages会过期被删除
        requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(whichTime, 1));

        //param:requestInfo ,versionId,clientId
        OffsetRequest request = new OffsetRequest(requestInfo, kafka.api.OffsetRequest.CurrentVersion(),clientId);

        OffsetResponse response = consumer.getOffsetsBefore(request); //获取response

        if (response.hasError()) {
            LOGGER.error("Error fetching data Offset Data the Broker. Reason: " + response.errorCode(topic, partition));
            return 0;
        }

        //取到的一组offset,从第一个开始读
        long[] offsets = response.offsets(topic, partition);
        return offsets[0];
    }

    /**
     * Get New Leader Broker
     * @param oldLeader
     * @param topic
     * @param partition
     * @param port
     * @return
     * @throws Exception
     * 重新调用findLeader获取leader broker并且防止在切换过程中，取不到leader信息。
     * findNewLeader操作重试3次失败，直接抛出Exception退出
     *
     */
    public String findNewLeader(String oldLeader, String topic, int partition, int port) throws Exception {
        for (int i = 0; i < 3; i++) {
            boolean goToSleep;
            PartitionMetadata metadata = findLeader(replicaBrokers, port, topic, partition);
            if (metadata == null) {
                goToSleep = true;
            } else if (metadata.leader() == null) {
                goToSleep = true;
            } else if (oldLeader.equalsIgnoreCase(metadata.leader().host()) && i == 0) {
                //如果leader没有发生改变给ZooKeeper一些时间去恢复，并且borker在故障转移之前确实恢复，或者它是non-Broker问题，此时的leader也是有问题的。
                //这种情况下我们再重复一次findNewLeader操作，因此将i==0的情况也排除。
                goToSleep = true;
            } else {
                return metadata.leader().host();
            }
            if (goToSleep) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                }
            }
        }

        LOGGER.error("Unable to find new leader after Broker failure. Exiting");
        throw new Exception("Unable to find new leader after Broker failure. Exiting");
    }

    /**
     * 生成ClientName
     * @param topic
     * @param partition
     * @return
     */
    private String generateClientName(String topic,int partition){
      return "Client_" + topic + "_" + partition;
    }

    /**
     * Reading the Data && Error Handling
     * @param maxReads //一次读取数据的大小，如果producer在写入很大的message时，maxReads设置过小会返回an empty message set，这时需要加大这个值
     * @param topic
     * @param partition
     * @param seedBrokers
     * @param port
     * @param kafkaMsgHandler
     * @throws Exception
     */
    public void consume(long maxReads, String topic, int partition, List<String> seedBrokers, int port,final KafkaMsgHandler kafkaMsgHandler) throws Exception {

        PartitionMetadata metadata = findLeader(seedBrokers,port,topic,partition);
        if (metadata == null) {
            LOGGER.error("Can't find metadata for Topic and Partition. Exiting");
            return;
        }
        if (metadata.leader() == null) {
            LOGGER.error("Can't find Leader for Topic and Partition. Exiting");
            return;
        }

        String leadBroker = metadata.leader().host();
        String clientName = generateClientName(topic,partition);
        SimpleConsumer consumer = new SimpleConsumer(leadBroker, port, soTimeout, bufferSize, clientName);
        long readOffset = getLastOffset(consumer, topic, partition, kafka.api.OffsetRequest.EarliestTime(), clientName);

        int numErrors = 0;
        while (true) {
            if (consumer == null) {
                consumer = new SimpleConsumer(leadBroker, port, soTimeout, bufferSize, clientName);
            }
            //如果将大批量写入Kafka，则可能需要增加此fetchSize
            kafka.api.FetchRequest fetchRequest = new FetchRequestBuilder().clientId(clientName).addFetch(topic, partition, readOffset, fetchSize).build();
            FetchResponse fetchResponse = consumer.fetch(fetchRequest);

            //Error Handling:如果消费数据失败则用最新的offset并尝试更新leader broker
            if (fetchResponse.hasError()) {
                numErrors++;
                short code = fetchResponse.errorCode(topic, partition);
                LOGGER.warn("Error fetching data from the Broker:" + leadBroker + " Reason: " + code);
                if (numErrors > 5) break;

                // 处理offset非法的问题，用最新的offset
                if (code == ErrorMapping.OffsetOutOfRangeCode()) {
                    readOffset = getLastOffset(consumer, topic, partition, kafka.api.OffsetRequest.LatestTime(), clientName);
                    continue;
                }
                consumer.close();
                consumer = null;
                leadBroker = findNewLeader(leadBroker, topic, partition, port);
                continue;
            }

            numErrors = 0;
            long numRead = 0;
            //读取数据操作
            ByteBufferMessageSet mesageList = fetchResponse.messageSet(topic, partition);
            for (MessageAndOffset messageAndOffset : mesageList) {
                long currentOffset = messageAndOffset.offset();
                if (currentOffset < readOffset) {
                    LOGGER.warn("Found an old offset: " + currentOffset + " Expecting: " + readOffset);
                    continue;
                }
                readOffset = messageAndOffset.nextOffset();

                //读出数据之后的具体操作由用户自己去实现
                kafkaMsgHandler.callback(messageAndOffset);

                numRead++;
                maxReads--;
            }

            if (numRead == 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {

                }
            }

        }
        if (consumer != null) consumer.close();

    }

}
