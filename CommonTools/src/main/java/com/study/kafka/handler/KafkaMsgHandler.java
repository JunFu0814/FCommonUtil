package com.study.kafka.handler;

/**
 * Created by lf52 on 2018/4/11.
 */
public interface KafkaMsgHandler<T> {

    /**
     * read message handler
     * @param t
     * @return
     * @throws Exception
     */
    Boolean callback(T t) throws Exception;

}
