package com.study.distributedlock.manager;


import org.apache.hadoop.conf.Configuration;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;


/**
 * Created by xiaojun on 2018/1/20.
 */
public class RedisManager {

    private static Configuration conf = new Configuration();

    //JedisCluster内部使用了池化技术，每次使用完毕都会自动释放Jedis因此不需要关闭。
    private static JedisCluster jedis = null;

    public static JedisCluster getConnection()  {
        if(jedis == null){
            jedis = createConnection();
        }
        return jedis;
    }

    private static JedisCluster createConnection()  {
        conf.addResource("jedis.xml");
        JedisPoolConfig config = new JedisPoolConfig();
        // 设置最大连接数
        config.setMaxTotal(conf.getInt("maxTotal", 100));
        // 设置最大空闲数
        config.setMaxIdle(conf.getInt("maxIdle", 8));
        // 设置最大等待时间
        config.setMaxWaitMillis(conf.getInt("maxWaitMillis",1000 * 100));
        // 在borrow一个jedis实例时，是否需要验证，若为true，则所有jedis实例均是可用的
        config.setTestOnBorrow(true);

        HostAndPort host = new HostAndPort(conf.get("host"),conf.getInt("port", 6379));

        return new JedisCluster(host,conf.getInt("timeOut",3000),config);

    }



}
