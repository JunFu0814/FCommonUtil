package com.study.distributedqueue;

import com.study.constants.Constants;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by lf52 on 2018/2/3.
 */
public class DetributeQueueTest {

    ExecutorService pool = Executors.newFixedThreadPool(5);

    @Test
    public void testQueueOffer(){
        // 连接zookeeper
        try {
            ZooKeeper zk = new ZooKeeper("10.16.46.170:2181", Constants.zk_sessionTimeout, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                }
            });
            DistributedConcurrentLinkedQueue queue = new DistributedConcurrentLinkedQueue(zk,"leotest",100);
            for(int i = 0;i < 100 ; i++){
                String data = "test"+ i;
                queue.offer(data.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Test
    public void testQueuePoll(){
        // 连接zookeeper
        try {
            ZooKeeper zk = new ZooKeeper("10.16.46.170:2181", Constants.zk_sessionTimeout, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                }
            });
            DistributedConcurrentLinkedQueue queue = new DistributedConcurrentLinkedQueue(zk,"leotest",20);
            System.out.println(new String(queue.poll()));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Test
    public void testQueueSize(){
        // 连接zookeeper
        try {
            ZooKeeper zk = new ZooKeeper("10.16.46.170:2181", Constants.zk_sessionTimeout, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                }
            });
            DistributedConcurrentLinkedQueue queue = new DistributedConcurrentLinkedQueue(zk,"leotest",20);
            System.out.println(queue.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testQueueClear() throws IOException {
        ZooKeeper zk = new ZooKeeper("10.16.46.170:2181", Constants.zk_sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
            }
        });
        DistributedConcurrentLinkedQueue queue = new DistributedConcurrentLinkedQueue(zk,"leotest",20);
        System.out.println(queue.clear());
    }

    @Test
    public void testQueueContains() throws IOException {
        ZooKeeper zk = new ZooKeeper("10.16.46.170:2181", Constants.zk_sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
            }
        });
        DistributedConcurrentLinkedQueue queue = new DistributedConcurrentLinkedQueue(zk,"leotest",20);
        System.out.println(queue.contains("test1".getBytes()));
    }

    @Test
    public void testQueue(){
        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue();
        for(int i = 0;i < 100  ; i++){
            queue.offer("test"+ i);
        }

        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    System.out.println(queue.poll());
                    //System.out.println(Thread.currentThread().getName() + " is running");
                } catch(Exception e) {

                }
            }
        };

        for (int i = 0; i < 100; i++) {
            Thread t = new Thread(runnable);
            t.start();
        }
    }



    @Test
    public void testQueueConcurrent() throws IOException, InterruptedException, ExecutionException {
        ZooKeeper zk = new ZooKeeper("10.16.46.170:2181", Constants.zk_sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
            }
        });
        int size = 50;
        DistributedConcurrentLinkedQueue queue = new DistributedConcurrentLinkedQueue(zk,"leotest",100);


        Callable task = new Callable<String>() {
            @Override
            public String call() throws Exception {
                try {
                    System.out.println(new String(queue.poll()));
                    //System.out.println(Thread.currentThread().getName() + " is running");
                    Thread.sleep(1000);
                } catch(Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

        };

        List<Future<String>> list = new ArrayList();
        for(int i = 0;i < size ; i++){
            list.add(pool.submit(task));
        }

        for(int i = 0;i < size ; i++){
            list.get(i).get();
        }

        Thread.sleep(10 * 1000);

    }




}
