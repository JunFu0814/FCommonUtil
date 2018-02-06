package com.study.distributedqueue;


import com.study.constants.Constants;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by lf52 on 2018/1/25.
 *
 * 基于zk实现一个简单的分布式并发队列，适用于秒杀等场景。
 */
public class DistributedConcurrentLinkedQueue implements DLinkQueue {

    private static final Logger logger = Logger.getLogger(DistributedConcurrentLinkedQueue.class);

    private static final int MAX_CAPACITY = 2000;

    private ZooKeeper zk;
    private int capacity;
    private String queueName;


    public DistributedConcurrentLinkedQueue(ZooKeeper zk, String queueName, Integer capacity){

         //考虑到zk上创建过多的node会影响其性能，限制queue的大小最大为2000
         if (capacity > MAX_CAPACITY){
             throw new IllegalArgumentException("Illegal Capacity: "+ capacity);
         }
         this.zk = zk;
         this.queueName = queueName;
         this.capacity = capacity;

        try {
            Stat stat = zk.exists(Constants.zk_rootQueue, null);
            if (stat == null){
                //如果根节点不存在则创建
                zk.create(Constants.zk_rootQueue, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            logger.error("Create Node Error", e);
        }

    }

    @Override
    public boolean offer(byte[] e) {

        if (e == null)
            throw new NullPointerException();

        try {
            Stat stat = zk.exists(Constants.zk_rootQueue + "/" + queueName, null);
            if (stat == null){
                zk.create(Constants.zk_rootQueue+ "/" + queueName, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }else{
                List<String> nodelist= zk.getChildren(Constants.zk_rootQueue+ "/" + queueName, false);
                if(nodelist.size() == capacity){
                    //队列满了入队失败，返回false
                    logger.warn("Queue Is Full");
                    return false;
                }
            }
            zk.create(Constants.zk_rootQueue + "/" + queueName + "/" + queueName, e, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
        } catch (Exception e1) {
            logger.error("Create Node Error", e1);
        }

        return true;
    }

    @Override
    public byte[] poll() {
        String firstchild = null;
        byte[] data = new byte[0];

        //保证线程安全，poll操作需要加锁
        synchronized(DistributedConcurrentLinkedQueue.class) {
            firstchild = FirstChild();
            if(firstchild == null){
                //队列满了入队失败，返回false
                logger.warn("Queue Is Empty");
                return new byte[0];
            }
            //获取头节点（最先入队的），对nodelist排序获取最小的
            String headnode = Constants.zk_rootQueue + "/"  + queueName +  "/" + firstchild;
            try {
                data = zk.getData(headnode, false, null);
                zk.delete(headnode, -1);

            } catch (Exception e) {
                logger.error("Queue Poll Error",e);
            }

        }

        return data;
    }


    @Override
    public boolean contains(byte[] e) {
        //遍历节点检查value
        List<String> nodes = null;
        try {
            nodes = zk.getChildren(Constants.zk_rootQueue + "/" + queueName,false);

            if( nodes.size() == 0 ){
                logger.warn("Queue Is Empty");
                return false;
            }

            for(String node : nodes){
                byte[] data = zk.getData(Constants.zk_rootQueue + "/" + queueName + "/" + node, false, null);
                if (Bytes.compareTo(e,data) == 0){
                    return true;
                }
            }
        } catch (Exception e1) {
            logger.error("Get Child Node Error", e1);
        }
        return false;
    }

    @Override
    public int size() {

        List<String> nodes = null;
        try {
            nodes = zk.getChildren(Constants.zk_rootQueue+ "/" + queueName,false);
        } catch (Exception e) {
            logger.error("Get Child Node Error", e);
        }

        return nodes.size();
    }

    @Override
    public boolean clear() {
        List<String> nodes = null;
        try {
            nodes = zk.getChildren(Constants.zk_rootQueue+ "/" + queueName,false);
            for(String node : nodes){
                zk.delete(Constants.zk_rootQueue + "/" + queueName + "/" + node, -1);
            }
            zk.delete(Constants.zk_rootQueue + "/" + queueName , -1);
        } catch (Exception e) {
            logger.error("Clear Nodes Error", e);
            return false;
        }
        return true;
    }

    private String FirstChild(){
        try{
            List<String> children =zk.getChildren(Constants.zk_rootQueue + "/" + queueName,false);
            Collections.sort(children,
                    new Comparator<String>() {
                        public int compare(String lhs, String rhs) {
                            return getQueueNodeNumber(lhs, queueName).compareTo(getQueueNodeNumber(rhs, queueName));
                        }
                    }
            );
            if (children.size() == 0){
                return null;
            }
            return children.get(0);

        }catch(Exception e){
            logger.error("get FirstChild Error", e);
            return null;
        }
    }

    private String getQueueNodeNumber(String str, String queuename) {
        int index = str.lastIndexOf(queuename);
        if ( index >= 0 ){
            index += queuename.length();
            return index <= str.length() ? str.substring(index) : "";
        }
        return str;
    }
}
