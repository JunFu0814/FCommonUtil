package com.study.distributedlock.manager;


import com.study.constants.Constants;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;


/**
 * Created by xiaojun on 2018/1/20.
 */
public class RedisManager {


    //JedisCluster内部使用了池化技术，每次使用完毕都会自动释放Jedis因此不需要关闭。
    private static JedisCluster jedis = null;

    public static JedisCluster getConnection()  {
        if(jedis == null){
            jedis = createConnection();
        }
        return jedis;
    }

    private static JedisCluster createConnection()  {
        JedisPoolConfig config = new JedisPoolConfig();
        // 设置最大连接数
        config.setMaxTotal(Constants.maxTotal);
        // 设置最大空闲数
        config.setMaxIdle(Constants.maxIdle);
        // 设置最大等待时间
        config.setMaxWaitMillis(Constants.maxWaitMillis);
        // 在borrow一个jedis实例时，是否需要验证，若为true，则所有jedis实例均是可用的
        config.setTestOnBorrow(true);

        HostAndPort host = new HostAndPort(Constants.redis_host,Constants.redis_port);

        return new JedisCluster(host,Constants.redisTimeOut,config);

    }



}
