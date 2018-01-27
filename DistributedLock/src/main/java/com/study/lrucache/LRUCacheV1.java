package com.study.lrucache;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jc6a on 2018/1/25.
 */
public class LRUCacheV1<K,V> {


    private ConcurrentHashMap<K,V> cache ;
    private ReferenceQueue referenceQueue;
    private LRUList lruList;
    public LRUCacheV1(int maxCapacity){
        lruList = new LRUList(maxCapacity);
        referenceQueue = new ReferenceQueue();
        cache = new ConcurrentHashMap<>();
        thread.start();
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
        synchronized (lruList){
            lruList.put(new KeyWord(new byte[1],referenceQueue,key), (V) new byte[1]);
        }
        cache.put(key,value);

    }

    public V get(K key){
        return cache.get(key);
    }



    Thread thread  = new Thread(() -> {
        Object obj = null;
        while(true){
            obj = referenceQueue.poll();
            if(obj != null){
                synchronized (lruList) {
                    System.out.println("-------------------------------------------------------" + ((KeyWord) obj).getValue());
                    cache.remove(((KeyWord) obj).getValue());
                    //todo 这里会存在一些问题，lruList中key是以SoftReference的方式存放，remove操作并不能key去移除对应的value，弱键垃圾回收仅依赖恒等式（==）
                    lruList.remove(obj);
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

    class LRUList extends LinkedHashMap<KeyWord,V>{
        private int capacity;
        public LRUList(int capacity){
            super(16, 0.75f, true);
            this.capacity = capacity;
        }
        public LRUList(int initialCapacity, float loadFactor, boolean accessOrder,int capacity) {
            super(initialCapacity, loadFactor, accessOrder);
            this.capacity = capacity;
        }

        @Override
        public V put(KeyWord key, V value) {
            return super.put(key, value);
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<KeyWord, V> eldest) {
            if(size()>capacity){
                System.out.println(eldest.getKey().getValue());
                cache.remove(eldest.getKey().getValue());
                return true;
            }
            return false;
        }
    }

    public Map<K,V> getCache(){
        return cache;
    }
    public void toString1(){
        System.out.println(lruList.toString());
    }

    public void test(){
        LRUList lruList = new LRUList(20);
        lruList.put(new KeyWord(new byte[1],referenceQueue,new byte[1]), (V) new byte[1]);
        System.out.println(lruList.get(new KeyWord(new byte[1],referenceQueue,new byte[1])));


    }
    public static void main(String[] args) {
        LRUCache lruCache = new LRUCache(150000);
        for(int i=0;i<150000;i++){
            lruCache.put(new String("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"+1),new String("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa123"));
        }
        System.out.println("end");
        System.out.println(lruCache.getCache().size());
    }

}
