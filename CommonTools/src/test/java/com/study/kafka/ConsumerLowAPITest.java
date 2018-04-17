package com.study.kafka;

import com.study.kafka.handler.KafkaMsgHandler;
import com.study.kafka.lowapi.KafkaLowAPIConsumer;
import kafka.message.MessageAndOffset;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lf52 on 2018/4/11.
 */
public class ConsumerLowAPITest {

    @Test
    public void test() throws Exception {

        KafkaLowAPIConsumer kafka = new KafkaLowAPIConsumer(100000,64*1024,100000);
        List<String> brokers = new ArrayList();
        brokers.add("ssecbigdata05");
        int partition = 0;
        int port = 9093;
        String topic = "testrepair";
        long maxReads = 100000;
        kafka.consume(maxReads, topic ,partition, brokers, port, new KafkaMsgHandler() {
            @Override
            public Boolean callback(Object object) throws Exception {
                MessageAndOffset messageAndOffset = (MessageAndOffset)object;
                ByteBuffer payload =messageAndOffset.message().payload();
                byte[] bytes = new byte[payload.limit()];
                payload.get(bytes);
                System.out.println(String.valueOf("now offset is : " + messageAndOffset.offset()) + "   &&   value is : " + new String(bytes, "UTF-8"));
                return true;
            }

        });
    }

}
