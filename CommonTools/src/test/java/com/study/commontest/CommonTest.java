package com.study.commontest;

import org.junit.Test;

import java.util.*;
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
            System.out.println(blockingDeque.offer(i, 1000L, TimeUnit.MILLISECONDS));

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


    @Test
    public void testRebalance(){
        String[] pnum = {"p0","p1","p2","p3","p4","p5","p6","p7","p8","p9","p10",};
        String[] cnum = {"c1","c2","c3","c4"};
        List<String> pnumlist = Arrays.asList(pnum);
        List<String> cnumlist = Arrays.asList(cnum);
        System.out.println(rebalance(cnumlist,pnumlist));
    }

    /**
     * kafka  rebalance  ²ßÂÔ
     * @param consumerlist
     * @param partitionlist
     * @return
     */
    private Map<String, List<String>> rebalance(List<String> consumerlist, List<String> partitionlist){

        Map<String,List<String>> result = new HashMap();
        int pnum = partitionlist.size();
        int cnum = consumerlist.size();
        int N = pnum / cnum;
        int M = pnum % cnum;
        for(int i = 0 ; i < cnum ; i ++ ){
            List<String> value = new ArrayList();
            for(int j = 0 ; j < pnum ; j ++ ){
                if(j >= i*N && j < (i+1)*N ){
                    value.add(partitionlist.get(j));
                }
            }
            result.put(consumerlist.get(i), value);
        }
        if(M > 0){
            for(int i = 0 ; i < M ; i ++ ){
                result.get(consumerlist.get(i)).add(partitionlist.get( N * cnum + i));
            }
        }
        return result;
    }
}

