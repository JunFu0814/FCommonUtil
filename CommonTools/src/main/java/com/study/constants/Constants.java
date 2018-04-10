package com.study.constants;

import org.apache.hadoop.conf.Configuration;

/**
 * Created by lf52 on 2018/1/25.
 */
public class Constants {

    private static Configuration conf = new Configuration();

    static{
        conf.addResource("conf.xml");
    }

    public static final String redis_host = conf.get("redis_host");
    public static final Integer redis_port = conf.getInt("redis_port", 6379);
    // redis最大连接数
    public static final Integer maxTotal = conf.getInt("maxTotal", 100);
    // redis最大空闲数
    public static final Integer maxIdle = conf.getInt("maxIdle", 8);
    // redis最大等待时间
    public static final Integer maxWaitMillis = conf.getInt("maxWaitMillis", 1000 * 100);
    // redis time out
    public static final Integer redisTimeOut = conf.getInt("timeOut",3000);
    // zk sessionTimeout
    public static final Integer zk_sessionTimeout = conf.getInt("zk_sessionTimeout",30000);
    // zk rootLock
    public static final String zk_rootLock = conf.get("zk_rootLock");
    // zk rootqueue
    public static final String zk_rootQueue = conf.get("zk_rootQueue");


}
