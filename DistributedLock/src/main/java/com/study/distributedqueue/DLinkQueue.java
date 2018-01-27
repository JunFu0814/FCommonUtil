package com.study.distributedqueue;

import org.apache.zookeeper.KeeperException;

/**
 * Created by lf52 on 2018/1/25.
 *
 * 基于zk实现一个简单的分布式并发队列，适用于秒杀等场景。
 */
public interface DLinkQueue<E> {

     boolean offer(E e);

     E poll();

     boolean contains(E e);

     int size() throws KeeperException, InterruptedException;

}
