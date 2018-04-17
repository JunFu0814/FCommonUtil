package com.study.kafka;

import com.study.kafka.producer.KafkaProducerTool;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.Test;

import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProducerTest extends Thread{

	static ExecutorService pool = Executors.newFixedThreadPool(10);
	KafkaProducerTool producer;
	/**
	 * producer for kafka 1.0
	 * @throws Exception
	 */
	@Test
	public void testProducer() throws Exception {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "ssecbigdata03:9092");
		props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);//消息缓冲区大小
		props.put(ProducerConfig.LINGER_MS_CONFIG, 0);//send message without delay
		props.put(ProducerConfig.ACKS_CONFIG, "all");//对应partition的follower写到本地后才返回成功。
		props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "com.study.kafka.producer.partition.ItemPartitioner");
		props.put("key.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
		props.put("retries", 0);//发送消息失败不重试
		producer = KafkaProducerTool.getInstance(props, "kafka10test");
		/**
		 * 幂等producer ：producer.send的逻辑是幂等的，即发送相同的Kafka消息，broker端不会重复写入消息，kafka保证底层日志只持久化一次。
		 *               幂等性可以极大地减轻下游consumer系统实现消息去重的工作负担，但会有两点的限制：
		 *               1.不保证多分区的幂等性 2.不保证跨会话实现幂等性（同一个producer重启操作也不保证）
		 * 只启用幂等produce：enable.idempotence=true
		 */
		//props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

		/**
		 * 事务型的producer:允许一个应用发送消息到多个partitions或者topics,transactional保证原子性地写入到多个分区
		 * 使用transaction必须要保证retries != 0 && acks = all (所有follower副本确认接收到数据) && enable.idempotence=true
		 */
		//props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "my-transactional-id");

		Random random = new Random();
		try {
			while(true){
				int num = random.nextInt(20)%(20-0+1) + 0;
				String key = "15-WWW-80" + num +"|USA|1003";
				byte[] value = String.valueOf(num).getBytes("UTF-8");
				pool.submit(new ProducerTask(key,value));
				sleep(2000);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class ProducerTask implements Runnable{

		private String key;
		private byte[] value;

		public ProducerTask(String key,byte[] value){
			this.key = key;
			this.value = value;
		}

		@Override
		public void run() {
			producer.append(key, value);
			System.out.println(new Date() + " : "+  key+"");
		}
	}

}