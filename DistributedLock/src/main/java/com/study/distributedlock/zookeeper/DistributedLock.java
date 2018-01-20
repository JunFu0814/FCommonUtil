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
    public  void lock()  throws Exception;

    /**
     *获取锁，直到超时
     * @param time
     * @param unit
     * @return
     * @throws Exception
     */
    public  boolean lock(long time, TimeUnit unit)  throws Exception;

    /**
     * 释放锁
     * @throws Exception
     */
    public  void unlock()  throws Exception;
}