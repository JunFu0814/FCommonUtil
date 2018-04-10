package com.study.commontest;

import org.junit.Test;

import java.util.concurrent.*;

/**
 * Created by lf52 on 2018/2/24.
 */
public class CommonTest {


    BlockingDeque<Integer> blockingDeque = new LinkedBlockingDeque(10);
    ExecutorService pool = Executors.newFixedThreadPool(5);

    @Test
    public void testQueue() throws InterruptedException {
        for (int i = 0; i < 50 ; i++) {
            System.out.println(blockingDeque.offer(i,1000L, TimeUnit.MILLISECONDS));

        }
    }

    @Test
    public void testProduce(){

        for (int i = 0; ; i++) {
            blockingDeque.offer(i);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    @Test
    public void testConsumer() throws ExecutionException, InterruptedException {

        for (int i = 0; i< 5; i++) {
            Thread thread = new Thread(new TestTask());
            thread.setDaemon(true);
            thread.setName("TestTaskRunnable" + i);
            pool.execute(thread);
        }

    }

    class TestTask implements Runnable{

        @Override
        public void run(){
            System.out.println(Thread.currentThread().getName() + " : " + blockingDeque.poll());
        }
    }
}

