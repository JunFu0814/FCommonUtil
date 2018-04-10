package com.study.lrucache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by lf52 on 2018/1/26.
 */
public class LRULinkedHashMap<K,V>  extends LinkedHashMap<K,V> {

    //存储数据容量,map中数据超过capacity执行过期策略
    private int capacity;

    public LRULinkedHashMap(int capacity){
        super(16, 0.75f, true);
        this.capacity = capacity;
    }

    /**
     * jdk1.8中当map调用get或者put方法的时候，如果K-V已经存在，会调用afterNodeAccess方法，
     * 会调用afterNodeAccess方法中，accessOrder为true时会先调用remove清除的当前首尾元素的指向关系，之后调用addBefore方法，将当前元素加入header之前。
     */
    public LRULinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder,int capacity) {
        super(initialCapacity, loadFactor, accessOrder);
        this.capacity = capacity;
    }

    /**
     * 重写removeEldestEntry方法
     *
     * 当有新元素加入Map的时候会调用Entry的addEntry方法，会调用removeEldestEntry方法
     *   1.默认的情况下removeEldestEntry方法只返回false表示元素永远不过期（在内存够用的情况下map会无限变大）。
     *   2.重写以后，当map达到capacity大小以后会返回true，依次执行过期策略保证map的大小不会无限变大。
     *
     * @param eldest
     * @return
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {

        //System.out.println(eldest.getKey() + "=" + eldest.getValue());

        if(size() > capacity) {
            return true;
        }

        return false;
    }

}
