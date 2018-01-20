package com.study.distributedlock.zookeeper.impl;


import com.study.distributedlock.zookeeper.DistributedLock;

import java.util.concurrent.TimeUnit;

/**
 * Created by lf52 on 2018/1/13.
 */
public class SimpleDistributedLockMutex implements DistributedLock {

    public void lock() throws Exception {
        
    }

    public boolean lock(long time, TimeUnit unit) throws Exception {
        return false;
    }

    public void unlock() throws Exception {

    }
}
