package com.study.distributedlock.redis;

import com.study.distributedlock.manager.RedisManager;
import com.study.distributedlock.redis.impl.RedisDistributedLock;
import org.junit.Test;
import redis.clients.jedis.JedisCluster;

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
        JedisCluster conn = RedisManager.getConnection();
        System.out.println(conn.set("leoA","555"));
        System.out.println(conn.del("leoA1"));
    }
}
