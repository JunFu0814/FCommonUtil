package com.study.lrucache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * -Xms5M -Xmx5M -XX:-UseGCOverheadLimit -XX:SoftRefLRUPolicyMSPerMB=0
 * -XX:SoftRefLRUPolicyMSPerMB=N 这个参数比较有用的，官方解释是：Soft reference在虚拟机中比在客户集中存活的更长一些。
 * 其清除频率可以用命令行参数 -XX:SoftRefLRUPolicyMSPerMB=<N>来控制，这可以指定每兆堆空闲空间的 soft reference 保持
 * 存活（一旦它不强可达了）的毫秒数，这意味着每兆堆中的空闲空间中的 soft reference 会（在最后一个强引用被回收之后）
 * 存活1秒钟。注意，这是一个近似的值，因为  soft reference 只会在垃圾回收时才会被清除，而垃圾回收并不总在发生。
 * 系统默认为一秒，我觉得没必要等1秒，客户集中不用就立刻清除，改为 -XX:SoftRefLRUPolicyMSPerMB=0；
 *   softreference是否回收条件
 *   clock-timestamp >= freespace*SoftRefLRUPolicyMSPerMB
 *   clock：上次gc的时间
 */
public class LRUCache<K,V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LRUCache.class);

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
        referenceQueue = new ReferenceQueue();
        thread.setDaemon(true);
        thread.start();
        this.ttl = ttl;
        this.timeUnit = timeUnit;
    }

    private class KeyWord<K> extends SoftReference<K> {
        private K value;
        public KeyWord(K referent, ReferenceQueue<? super K> q,K value) {
            super(referent, q);
            this.value = value;
        }
        public K getValue() {
            return value;
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
        }
    }

    public void put(K key,V value){
        if(cache.containsKey(key)){
            synchronized (lruList){
                lruList.put(key,System.currentTimeMillis());
            }
            cache.put(key, value);

        }else{
            synchronized (lruList){
                KeyWord keyWord = new KeyWord(new byte[0],referenceQueue,key);
                lruList.put(key,System.currentTimeMillis());
            }
            cache.put(key,value);
        }
        //内存使用超过80%，通知系统尝试做gc操作
        if(((double)(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/Runtime.getRuntime().totalMemory())>0.8){
            System.gc();
        }
    }


    public V get(K key){
        V value = cache.get(key);
        if(value!=null){
            Long p =lruList.get(key);
            if(p !=null){
                boolean isDelete = System.currentTimeMillis() - p>timeUnit.toMillis(ttl)?true:false;
                if(isDelete){
                    synchronized (lruList) {
                        lruList.remove(key);
                        cache.remove(key);
                    }
                    LOGGER.info("TTL will remove "+key);
                    return null;
                }
            }
        }
        return value;
    }


    int count = 0 ;
    Thread thread  = new Thread(() -> {
        Object obj = null;
        while(true){
            try {
                obj = referenceQueue.remove();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(obj!=null) {
                synchronized (lruList) {
                    //gc方式移除cache中的数据
                    LOGGER.info(" GC Will remove " + ((KeyWord) obj).getValue());
                    cache.remove(((KeyWord) obj).getValue());
                    lruList.remove(((KeyWord) obj).getValue());
                    count++;
                }
            }
        }
    });

    private class LRUList extends LinkedHashMap<K,Long> {
        private int capacity;
        public LRUList(int capacity){
            super(16, 0.75f, true);
            this.capacity = capacity;
        }
        public LRUList(int initialCapacity, float loadFactor, boolean accessOrder,int capacity) {
            super(initialCapacity, loadFactor, accessOrder);
            this.capacity = capacity;
        }
        public void setCapacity(int capacity){
            this.capacity = capacity;
        }

        @Override
        public Long  put(K key, Long value) {
            return super.put(key, value);
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, Long> eldest) {
            if (size() > capacity) {
                synchronized (lruList) {
                    //cache size 达到capacity lru移除数据
                    LOGGER.info("LRU will remove " + eldest.getKey() +"---"+capacity);
                    cache.remove(eldest.getKey());
                    this.remove(eldest.getKey());
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


    public int getCount(){
        return count;
    }

}