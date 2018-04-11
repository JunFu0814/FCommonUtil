package com.study.kafka;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.util.Date;
import java.util.Properties;
import java.util.Random;

public class ProducerTest extends Thread{

	private final Producer<String, String> producer;
	
	public ProducerTest() {
		 Properties props = new Properties();   
         //Producer的配置
         props.setProperty("metadata.broker.list","ssecbigdata07:9093");    
         props.setProperty("serializer.class","kafka.serializer.StringEncoder");    
         props.put("request.required.acks","1"); 
         /*0：producer不等待来自broker同步完成的确认继续发送下一条（批）消息。此选项提供最低的延迟但最弱的耐久性保证（当服务器发生故障时某些数据会丢失，如leader已死，但producer并不知情，发出去的信息broker就收不到）。
           1：producer在leader已成功收到的数据并得到确认后发送下一条message。此选项提供了更好的耐久性为客户等待服务器确认请求成功（被写入死亡leader但尚未复制将失去了唯一的消息）。
          -1：producer在follower副本确认接收到数据后才算一次发送完成。 */
         ProducerConfig config = new ProducerConfig(props);    
         producer = new Producer<String, String>(config); 
	}
	//线程运行方法
	public void run() {
		Random random = new Random();
		try {    
           while(true){
        	     int num = random.nextInt(100)%(100-0+1) + 0;
            	 KeyedMessage<String, String> data = new KeyedMessage<String, String>("testrepair","the num is :",num+"");
            	 //发送消息
            	 producer.send(data);
            	 System.out.println(new Date() + "："+  num+"");
            	 sleep(2000);
			}       
       
         } catch (Exception e) {    
             e.printStackTrace();    
         }    
         producer.close(); 
	}
	
	 public static void main(String[] args) {    
		 ProducerTest producerThread = new ProducerTest();
		 producerThread.start();
            
     }   
}