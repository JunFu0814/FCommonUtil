package com.study.distributedlock.redis.impl;

import com.study.distributedlock.manager.RedisManager;
import com.study.distributedlock.redis.DistributedLock;
import org.apache.log4j.Logger;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by xiaojun on 2018/1/20.
 */
public class RedisDistributedLock implements DistributedLock {

    private static final Logger logger = Logger.getLogger(RedisDistributedLock.class);

    /**
     * 加锁
     * @param lockName  锁的key
     * @param acquireTimeout  获取锁的超时时间
     * @param ttlTime   锁的超时时间(redis key ttl time)
     * @return
     */
    public String lock(String lockName, long acquireTimeout, long ttlTime) {
        JedisCluster conn = null;
        String retIdentifier = null;
        try {
            conn = RedisManager.getConnection();
            String identifier = UUID.randomUUID().toString();
            String lockKey = "DLock-" + lockName;

            int lockExpire = (int)(ttlTime / 1000);

            long end = System.currentTimeMillis() + acquireTimeout;
            //while true 循环，如果没有获得锁资源就会一直重试直到获取锁的操作超时（获取锁的超时时间，超过这个时间则放弃获取锁）
            while (System.currentTimeMillis() < end) {

                if (conn.setnx(lockKey, identifier) == 1) {
                    //成功获取到锁资源，返回标识
                    conn.expire(lockKey, lockExpire);
                    retIdentifier = identifier;
                    return retIdentifier;
                }

                // 返回-1代表key没有设置超时时间，为key设置一个超时时间
                if (conn.ttl(lockKey) == -1) {
                    conn.expire(lockKey, lockExpire);
                }

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

        } catch (JedisException e) {
            logger.error("get lock error", e);
        }
        return retIdentifier;
    }


    /**
     * 释放锁（删除key操作会watch，失败会回滚）
     * @param lockName 锁的key
     * @param identifier  释放锁的标识
     * @return
     */
    public boolean unlock(String lockName, String identifier) {

        JedisCluster conn = null;
        String lockKey = "DLock-" + lockName;
        boolean retFlag = false;
        try {
            conn = RedisManager.getConnection();
            while (true) {

                // 通过value值来判断是不是该锁，若是该锁，则删除，释放锁
                if (identifier.equals(conn.get(lockKey))) {
                    if(conn.del(lockKey) == 1){
                        retFlag = true;
                        break;
                    }

                }

            }
        } catch (JedisException e) {
            logger.error("release lock error", e);
        }
        return retFlag;
    }
}
