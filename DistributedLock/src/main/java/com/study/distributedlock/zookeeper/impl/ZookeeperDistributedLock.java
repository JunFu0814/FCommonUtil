package com.study.distributedlock.zookeeper.impl;


import com.study.constants.Constants;
import com.study.distributedlock.zookeeper.DistributedLock;
import org.apache.log4j.Logger;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by lf52 on 2018/1/13.
 */
public class ZookeeperDistributedLock implements DistributedLock {

    private static final Logger logger = Logger.getLogger(ZookeeperDistributedLock.class);

    private ZooKeeper zk = null;

    // zk上锁的名称（竞争的资源）
    private String lockName;
    // 等待的前一个锁
    private String WAIT_LOCK;
    // 当前锁
    private String CURRENT_LOCK;
    // 计数器
    private CountDownLatch countDownLatch;



    public ZookeeperDistributedLock(String config, String lockName) {
        this.lockName = lockName;
        try {

            // 连接zookeeper
            zk = new ZooKeeper(config, Constants.zk_sessionTimeout, new Watcher() {
                public void process(WatchedEvent event) {
                    if (countDownLatch != null) {
                        countDownLatch.countDown();
                    }
                }
            });

            Stat stat = zk.exists(Constants.zk_rootLock, false);
            if (stat == null) {
                // 如果根节点不存在，则创建根节点
                zk.create(Constants.zk_rootLock, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }

        } catch (Exception e) {
            logger.error("Init ZookeeperDistributedLock Fail",e);
        }
    }


    public void lock() {
        try {
            if (this.tryLock()) {
                logger.info(Thread.currentThread().getName() + " " + lockName + " get lock : " + CURRENT_LOCK);
                return;
            } else {
                waitForLock(WAIT_LOCK, Constants.zk_sessionTimeout);
            }
        } catch (Exception e) {
            logger.error("get lock error",e);
        }
    }

    public boolean tryLock() {
        try {
            if (lockName.contains("--")) {
                throw new RuntimeException("LockName Can Not Contain '--' ");
            }
            // 创建临时有序节点
            CURRENT_LOCK = zk.create(Constants.zk_rootLock + "/" + lockName + "--", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            // 取所有子节点
            List<String> childNodes =zk.getChildren(Constants.zk_rootLock, false);
            // 取出所有lockName的锁(按锁名称从小到大排序)
            List<String> lockObjects = new LinkedList<String>();
            for (String node : childNodes) {
                String _node = node.split("--")[0];
                if (_node.equals(lockName)) {
                    lockObjects.add(node);
                }
            }
            Collections.sort(lockObjects);

            if (CURRENT_LOCK.equals(Constants.zk_rootLock + "/" + lockObjects.get(0))) {
                return true;
            }
            // 若不是最小节点，则找到自己的前一个节点
            String prevNode = CURRENT_LOCK.substring(CURRENT_LOCK.lastIndexOf("/") + 1);
            WAIT_LOCK = lockObjects.get(Collections.binarySearch(lockObjects, prevNode) - 1);

        } catch (Exception e) {
            logger.error("get lock error",e);
        }

        return false;
    }

    public boolean tryLock(long time, TimeUnit unit) {
        try {
            if (this.tryLock()) {
                return true;
            }
            return waitForLock(WAIT_LOCK, time);
        } catch (Exception e) {
            logger.error("try get lock", e);
        }
        return false;
    }

    public void unlock() {
        logger.info("release lock " + CURRENT_LOCK);
        try {
            zk.delete(CURRENT_LOCK, -1);
            CURRENT_LOCK = null;
        } catch (Exception e) {
            logger.error("unlock error",e);
        }finally {
            try {
                zk.close();
            } catch (InterruptedException e) {
                logger.error("zkClient close error", e);
            }
        }
    }

    private boolean waitForLock(String prev, long waitTime) throws KeeperException, InterruptedException {
        Stat stat = zk.exists(Constants.zk_rootLock + "/" + prev, true);

        if (stat != null) {
            logger.info(Thread.currentThread().getName() + " wait for lock: " + Constants.zk_rootLock  + "/" + prev);
            this.countDownLatch = new CountDownLatch(1);
            this.countDownLatch.await(waitTime, TimeUnit.MILLISECONDS);
            this.countDownLatch = null;
            logger.info(Thread.currentThread().getName() + " get lock");
        }
        return true;
    }
}
