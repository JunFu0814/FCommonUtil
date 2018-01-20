package com.study.distributedlock.redis;

/**
 * Created by xiaojun on 2018/1/20.
 */
public interface DistributedLock {

    /**
     * 加锁
     * @param lockName  锁的key
     * @param acquireTimeout  获取超时时间
     * @param timeout   锁的超时时间
     * @return 锁标识
     */
    public  String lock(String lockName, long acquireTimeout, long timeout);

    /**
     * 释放锁
     * @param lockName 锁的key
     * @param identifier    释放锁的标识
     * @throws Exception
     */
    public boolean unlock(String lockName, String identifier);
}