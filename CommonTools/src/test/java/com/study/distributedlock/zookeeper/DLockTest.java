package com.study.distributedlock.zookeeper;

import com.study.distributedlock.zookeeper.impl.ZookeeperDistributedLock;


public class DLockTest {

    static int n = 500;


    public static void main(String[] args) {

        Runnable runnable = new Runnable() {
            public void run() {
                DistributedLock lock = null;
                try {
                    lock = new ZookeeperDistributedLock("xx.xx.xx.xx:xxxx", "leotest");
                    lock.lock();
                    System.out.println(--n);
                    System.out.println(Thread.currentThread().getName() + " is running");
                } finally {
                    if (lock != null) {
                        lock.unlock();
                    }
                }
            }
        };

        for (int i = 0; i < 50; i++) {
            Thread t = new Thread(runnable);
            t.start();
        }
    }
}
