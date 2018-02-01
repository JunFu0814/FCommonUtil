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
 * -Xms5M -Xmx5M -XX:-UseGCOverheadLimit
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

    class KeyWord<K> extends SoftReference<K> {
        private K value;
        public KeyWord(K referent, ReferenceQueue<? super K> q,K value) {
            super(referent, q);
            this.value = value;
        }
        public K getValue() {
            return value;
        }

        /**
         * 1.finalize()是Object的protected方法，子类可以覆盖该方法以实现资源清理工作，GC在回收对象之前调用该方法。
         * 2.finalize方法在什么时候被调用?
         *  在垃圾回收的时候，某个对象要被回收的时候，会先进行一次标记，并且将该对象的finalize放到一个低优先级的线程中去执行,等到下一次垃圾回收的时候再把这个对象回收。
         *  jvm并不保证在垃圾回收之前能够执行他的finalize方法，甚至在执行finalize方法的线程发生了死循环，那其他的finalize方法都无法执行了。
         **/
        @Override
        protected void finalize() throws Throwable {
            try{
                lruList.remove(value);
                cache.remove(value);
            }finally {
                super.finalize();
                LOGGER.info(value +" finalize called ");
            }
        }
    }

    public void put(K key,V value){
        if(cache.containsKey(key)){
            //cache中已经存在当前的key,只更新它的ttl时间
            synchronized (lruList){
                lruList.put(key,System.currentTimeMillis());
                cache.put(key, value);
            }

        }else{
            synchronized (lruList){
                //referent byte[0]保证开销最小
                KeyWord keyWord = new KeyWord(new byte[0],referenceQueue,key);
                lruList.put(key,System.currentTimeMillis());
                cache.put(key,value);
            }

        }

        //LOGGER.info(key + "-----------"+value);
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
                    LOGGER.warn("TTL will remove "+key);
                    return null;
                }
            }
        }
        return value;
    }

    //统计gc回收的对象数量
    int count = 0 ;
    boolean flag = true;
    Thread thread  = new Thread(() -> {
        Object obj = null;
        while(true){
            obj = referenceQueue.poll();
            if(obj != null){
                synchronized (lruList) {
                    LOGGER.info(" GC Will remove " + ((KeyWord) obj).getValue());
                    cache.remove(((KeyWord) obj).getValue());
                    lruList.remove(((KeyWord) obj).getValue());

                    //当gc要爆的时候，手动将lrucache的size减少1/10，让其通过lru策略来释放出部分内存
                    if(flag){
                        if(lruList.size()<=128){
                            lruList.setCapacity(128);
                        }else{
                            int num = lruList.size();
                            lruList.setCapacity(num - num/8);
                            flag = false;
                        }

                    }
                    count++;
                }

            }else{
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    public class LRUList extends LinkedHashMap<K,Long> {
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
        public Long  put(K key, Long value) {
            return super.put(key, value);
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, Long> eldest) {
            if (size() > capacity) {
                synchronized (lruList) {
                    LOGGER.warn("LRU will remove " + eldest.getKey() +"---"+capacity);
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
