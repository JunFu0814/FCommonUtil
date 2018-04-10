package com.study.distributedqueue;

/**
 * Created by lf52 on 2018/1/25.
 */
public interface DLinkQueue {

     boolean offer(byte[] e);

     byte[] poll();

     boolean contains(byte[] e);

     int size();

     boolean clear();

}
