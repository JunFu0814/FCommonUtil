package com.study.lrucache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by jc6a on 2018/1/25.
 */
public class LRUCache<K,V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LRUCache.class);


    //存储KeyWord并记录index，之后会根据index删除
    private List<KeyWord> list;
    //数据存储介质，线程安全
    private ConcurrentHashMap<K,V> cache ;
    //保证LRU
    private LRUList lruList;
    //cache中数据的ttl过期时间
    private int ttl;
    //ttl的单位
    private TimeUnit timeUnit;
    //SoftReference软引用的引用队列
    private ReferenceQueue referenceQueue;

    public LRUCache(int maxCapacity){
        this(maxCapacity,7,TimeUnit.DAYS);
    }

    public LRUCache(int maxCapacity, int ttl, TimeUnit timeUnit){
        cache = new ConcurrentHashMap<>();
        lruList = new LRUList(maxCapacity);
        list = new LinkedList<>();
        referenceQueue = new ReferenceQueue();
        thread.setDaemon(true);
        thread.start();
        this.ttl = ttl;
        this.timeUnit = timeUnit;
    }

    class KeyWord<K> extends SoftReference<K> {
        private K value;
        public KeyWord(K referent, ReferenceQueue<? super K> q,K value) {
            super(referent, q);
            this.value = value;
        }
        public K getValue() {
            return value;
        }
    }

    public void put(K key,V value){
        if(cache.containsKey(key)){
            //cache中已经存在当前的key,只更新它的ttl时间
            synchronized (list){
                Pair<Integer,Long> pair = lruList.get(key);
                int index = pair.getKey();
                lruList.put(key,new Pair<>(index,System.currentTimeMillis()));
            }
            cache.put(key, value);
        }else{
            synchronized (list){
                //referent byte[0]保证开销最小
                list.add(new KeyWord(new byte[0],referenceQueue,key));
                //index 作用记录KeyWord在list中位置，用于移除操作
                int index = list.size()-1;
                lruList.put(key,new Pair<>(index,System.currentTimeMillis()));
            }
            cache.put(key,value);
        }
    }


    public V get(K key){
        V value = cache.get(key);
        if(value!=null){
            Pair<Integer,Long> p =lruList.get(key);
            if(p !=null){
                long keyTime = p.getValue();
                boolean isDelete = System.currentTimeMillis() - keyTime>timeUnit.toMinutes(ttl)?true:false;
                if(isDelete){
                    synchronized (list) {
                        list.remove((int) lruList.get(key).getKey());
                        lruList.remove(key);
                        cache.remove(key);
                    }
                    LOGGER.warn("TTL will remove "+key);
                    return null;
                }
            }
        }
        return value;
    }



    Thread thread  = new Thread(() -> {
        Object obj = null;
        while(true){
            obj = referenceQueue.poll();
            if(obj != null){
                synchronized (list) {
                        LOGGER.info(" GC Will remove " + ((KeyWord) obj).getValue());
                        cache.remove(((KeyWord) obj).getValue());
                        lruList.remove(((KeyWord) obj).getValue());
                        list.remove(obj);
                }

            }else{
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    class LRUList extends LinkedHashMap<K,Pair<Integer,Long>>{
        private int capacity;
        public LRUList(int capacity){
            super(16, 0.75f, true);
            this.capacity = capacity;
        }
        public void setCapacity(int capacity){
            this.capacity = capacity;
        }
        public LRUList(int initialCapacity, float loadFactor, boolean accessOrder,int capacity) {
            super(initialCapacity, loadFactor, accessOrder);
            this.capacity = capacity;
        }

        @Override
        public Pair<Integer,Long>  put(K key, Pair<Integer,Long> value) {
            return super.put(key, value);
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, Pair<Integer,Long>> eldest) {
            if (size() > capacity) {
                System.out.println(size() + "------" + capacity);
                synchronized (list) {
                    LOGGER.warn("LRU will remove " + eldest.getKey());
                    cache.remove(eldest.getKey());
                    list.remove((int) eldest.getValue().getKey());
                }
                return true;
            }
            return false;
        }
    }

    public Map<K,V> getCache(){
        return cache;
    }

    public LRUList getLruList(){
        return lruList;
    }

    public List getList(){
        return list;
    }

    public static void main(String[] args) throws InterruptedException {
        LRUCache lruCache = new LRUCache(150000,3000,TimeUnit.MILLISECONDS);
        for(int i=0;i<150000;i++){
            lruCache.put(new String("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"+i),new String("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa123"));
            Thread.sleep(5000);
            lruCache.get(new String("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"+i));
        }
        System.out.println("end");
        System.out.println(lruCache.getCache().size());
        System.out.println(lruCache.getLruList().size());
        System.out.println(lruCache.getList().size());


    }

}
