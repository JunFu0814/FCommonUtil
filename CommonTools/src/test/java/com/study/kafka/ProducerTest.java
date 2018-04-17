package com.study.kafka;

import com.study.kafka.producer.KafkaProducerTool;
import org.junit.Test;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProducerTest extends Thread{

	KafkaProducerTool producer = KafkaProducerTool.getInstance("ssecbigdata03:9092","kafka10test");

	static ExecutorService pool = Executors.newFixedThreadPool(10);

	/**
	 * producer for kafka 1.0
	 * @throws Exception
	 */
	@Test
	public void testProducer() throws Exception {

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