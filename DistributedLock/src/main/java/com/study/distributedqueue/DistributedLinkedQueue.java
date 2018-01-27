package com.study.distributedqueue;


import com.study.constants.Constants;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

/**
 * Created by lf52 on 2018/1/25.
 */
public class DistributedLinkedQueue<E> implements DLinkQueue<E> {

    private static final int MAX_CAPACITY = 2000;

    private ZooKeeper zk;
    private int capacity;
    private String queueName;


    private DistributedLinkedQueue(ZooKeeper zk,String queueName,Integer capacity){

         //考虑到zk上创建过多的node会影响其性能，限制queue的大小最大为2000
         if (capacity > MAX_CAPACITY){
             throw new IllegalArgumentException("Illegal Capacity: "+ capacity);
         }
         this.zk = zk;
         this.queueName = queueName;
         this.capacity = capacity;

    }

    public boolean offer(E e) {

        if (e == null)
            throw new NullPointerException();

        //队列满了，则入队失败

        return false;
    }

    public E poll() {
        return null;
    }

    public boolean contains(E e) {
        return false;
    }

    public int size() throws KeeperException, InterruptedException {

        List<String> nodes = zk.getChildren(Constants.zk_rootQueue,false);

        return 0;
    }
}
