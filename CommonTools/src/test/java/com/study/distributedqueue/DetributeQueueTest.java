package com.study.distributedqueue;

import com.study.constants.Constants;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Test;

import java.io.IOException;
import java.util.LinkedList;
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
            DistributedLinkedQueue queue = new DistributedLinkedQueue(zk,"leotest",100);
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
            DistributedLinkedQueue queue = new DistributedLinkedQueue(zk,"leotest",20);
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
            DistributedLinkedQueue queue = new DistributedLinkedQueue(zk,"leotest",20);
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
        DistributedLinkedQueue queue = new DistributedLinkedQueue(zk,"leotest",20);
        System.out.println(queue.clear());
    }

    @Test
    public void testQueueContains() throws IOException {
        ZooKeeper zk = new ZooKeeper("10.16.46.170:2181", Constants.zk_sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
            }
        });
        DistributedLinkedQueue queue = new DistributedLinkedQueue(zk,"leotest",20);
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
        DistributedLinkedQueue queue = new DistributedLinkedQueue(zk,"leotest",100);
        List<Future<Boolean>> list = new LinkedList<>();
        for(int i = 0;i < size ; i++){
            list.add(pool.submit(putTask(queue, "leotest" + i)));
        }
        list.forEach(future ->{
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
        //出队列
         if(queue.size() >=  50 ){
              for(int i = 0;i < queue.size() ; i++){
                  System.out.println(new String(queue.poll()));
              }
        }

        System.out.println("end");
    }

    private Callable<Boolean> putTask(DistributedLinkedQueue queue,String data) {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() {
                queue.offer(data.getBytes());
                return true;
            }
        };

    };


}
