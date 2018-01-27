package com.study.lrucache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by lf52 on 2018/1/27.
 *
 * VM Option
 * -Xmx10M -XX:-UseGCOverheadLimit
 */
public class LRUCacheTest {

    int size = 50000;
    ExecutorService pool = Executors.newFixedThreadPool(20);


    @Test
    public void testPut() throws InterruptedException {


        LRUCache lruCache = new LRUCache(size,30000, TimeUnit.MILLISECONDS);

        Cache<String,String> cache = CacheBuilder.newBuilder().weakKeys().weakValues().maximumSize(size)
                .expireAfterAccess(7, TimeUnit.DAYS)
                .removalListener((RemovalListener<String, String>) removalNotification -> {
                    System.out.println("buildCache will remove : "+removalNotification.getKey());
                }).build();


        long start = System.currentTimeMillis();
        List<Future<Boolean>> lrulist = new ArrayList(size);
        for(int i=0;i<size;i++){
            lrulist.add(pool.submit(new putTask(lruCache, new String("aaaaaaa" + i), new String("aaaaaa") + i)));
        }
        lrulist.forEach(item -> {
            try {
                item.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        long end = System.currentTimeMillis();
        System.out.println("LRUCache size: " + lruCache.getCache().size() + ", LRUCache costtime: " + (end - start));


       /* long start1 = System.currentTimeMillis();
        List<Future<Boolean>> cachelist = new ArrayList(size);
        for(int i=0;i<size;i++){
            cachelist.add(pool.submit(new putTask1(cache, new String("aaaaaaa" + i), new String("aaaaaa") + i)));
        }
        cachelist.forEach(item -> {
            try {
                item.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        long end1 = System.currentTimeMillis();

        System.out.println("cacheBuilder size: " + cache.size() + ", cacheBuilder costtime: " + (end1 - start1));*/




    }

    /**
     * test LRUCache, WeakHashmap , cacheBuilder foreach≤Ó“Ï
     */
    @Test
    public void testForeach() throws InterruptedException {

        LRUCache lruCache = new LRUCache(size,30000, TimeUnit.MILLISECONDS);

        Cache<String,String> cache = CacheBuilder.newBuilder().weakValues().maximumSize(size)
                .expireAfterAccess(30000, TimeUnit.MILLISECONDS)
                .removalListener((RemovalListener<String, String>) removalNotification -> {
                    System.out.println("cache will remove : "+removalNotification.getKey());
                }).build();


        for(int i=0;i<size;i++){
            lruCache.put(new String("aaaaaaa" + i), new String("aaaaaa") + i);
            cache.put(new String("aaaaaaa" + i), new String("aaaaaa") + i);
        }

        long start = System.currentTimeMillis();
        lruCache.getCache().forEach((k, v) ->
            System.out.println("key is : " + k + " , value is : " + v)
        );
        long end = System.currentTimeMillis();


        long start1 = System.currentTimeMillis();
        cache.asMap().forEach((k, v) ->
            System.out.println("key is : " + k + " , value is : " + v)
        );
        long end1 = System.currentTimeMillis();

        System.out.println("LRUCache size: " + lruCache.getCache().size() + ", LRUCache costtime: " + (end - start));
        System.out.println("cacheBuilder size: " + cache.size() + ", cacheBuilder costtime: " + (end1 - start1));


    }

    class putTask implements Callable<Boolean>{
        private LRUCache lruCache;
        private String key;
        private String value;

        public putTask(LRUCache lruCache,String key,String value){
            this.key = key;
            this.value = value;
            this.lruCache = lruCache;
        }
        @Override
        public Boolean call() throws Exception {
            lruCache.put(key,value);
            return true;
        }
    }

    class putTask1 implements Callable<Boolean>{
        private Cache cache;
        private String key;
        private String value;

        public putTask1(Cache cache,String key,String value){
            this.key = key;
            this.value = value;
            this.cache = cache;
        }
        @Override
        public Boolean call() throws Exception {
            cache.put(key,value);
            return true;
        }
    }


}
