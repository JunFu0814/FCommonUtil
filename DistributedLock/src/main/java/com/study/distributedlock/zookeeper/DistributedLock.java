package com.study.distributedlock.zookeeper;

import java.util.concurrent.TimeUnit;

/**
 * Created by lf52 on 2018/1/13.
 */
public interface DistributedLock {
    /**
     * 获取锁，如果锁被占用就一直等待
     * @throws Exception
     */
    public  void lock();

    /**
     * 尝试获取锁，立刻返回
     * @return
     */
    public  boolean tryLock();

    /**
     * 尝试获取锁，直到超时
     * @param time
     * @param unit
     * @return
     * @throws Exception
     */
    public  boolean tryLock(long time, TimeUnit unit);

    /**
     * 释放锁
     * @throws Exception
     */
    public  void unlock();
}