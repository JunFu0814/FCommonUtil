package com.study.distributedlock.redis;

import com.study.distributedlock.redis.impl.RedisDistributedLock;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lun on 2018/1/20.
 */
public class DLockTest {

    ExecutorService pool = Executors.newFixedThreadPool(10);
    /**
     * 模拟一个秒杀的场景 50个线程秒杀500个商品
     */

    @Test
    public void testwithOutLock() throws InterruptedException {
        LockTask task = new LockTask();
        for(int i = 0 ;i < 50 ; i ++){
            Thread t = new Thread(task);
            t.start();
        }
        Thread.sleep(1000*10);
    }

    @Test
    public void testwithLock() throws InterruptedException {
        RedisDistributedLock lock = new RedisDistributedLock();
        LockTask1 task = new LockTask1(lock);
        for(int i = 0 ;i < 50 ; i ++){
            Thread t = new Thread(task);
            t.start();
        }

        Thread.sleep(1000*10);
    }

   class LockTask implements Runnable{

       int n = 500;
       public void run() {
           System.out.println(Thread.currentThread().getName() + " get lock");
           System.out.println(--n);
       }
   }

    class LockTask1 implements Runnable{

        private RedisDistributedLock lock;
        private LockTask1(RedisDistributedLock lock){
           this.lock = lock;
        }

        int n = 500;
        public void run() {
            String indentifier = lock.lock("leotest",5000,1000);
            System.out.println(Thread.currentThread().getName() + " get lock");
            System.out.println(--n);
            lock.unlock("leotest",indentifier);
        }
    }

    @Test
    public void testdel() throws InterruptedException {
        Jedis jedis1 = new Jedis("10.16.46.192",8018);
        System.out.println(jedis1.del("leoA"));
    }

    @Test
    public void testinsert() throws InterruptedException {
        Jedis jedis = new Jedis("10.16.46.172",8010);
        System.out.println(jedis.set("leoA", "666"));
    }

    @Test
    public void testmigrate(){
        Jedis jedis = new Jedis("10.16.46.172",8010);
        Jedis jedis1 = new Jedis("10.16.46.192",8018);
        System.out.println(jedis.migrate("10.16.46.192", 8018, "leoA", 0, 1000));
        System.out.println(jedis.get("leoA"));
        System.out.println(jedis1.get("leoA"));
    }

    /**
     * 使用tcp连接到redis发送命令读取数据（BIO）
     */
    @Test
    public void testSocketConnectRedis(){
        //String command = "*2\r\n$3\r\nget\r\n$5\r\nhello\r\n";
        //String command = "*1\r\n$6\r\ndbsize\r\n";
        //String command = "*1\r\n$4\r\ninfo\r\n";
        //pipeline
        String command = "*2\r\n$3\r\nget\r\n$5\r\nhello\r\n*1\r\n$6\r\ndbsize\r\n";

        BufferedOutputStream out = null;
        BufferedReader bufferedReader = null;
        Socket socket = new Socket();
        try {

            socket.setReuseAddress(true);
            socket.setKeepAlive(true); // Will monitor the TCP connection is
            socket.setTcpNoDelay(true); // Socket buffer Whetherclosed, to
            socket.setSoLinger(true, 0); // Control calls close () method,
            socket.connect(new InetSocketAddress("10.16.46.172", 8009), 3000);
            socket.setSoTimeout(30000);
            // 向客户端回复信息
            out = new BufferedOutputStream(socket.getOutputStream());
            out.write(command.getBytes());
            out.flush();

            InputStreamReader read = new InputStreamReader(socket.getInputStream());
            bufferedReader = new BufferedReader(read,10000);

            String line = null;
            while ((line = bufferedReader.readLine()) != null){
                System.out.println(line);
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
                bufferedReader.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

}
