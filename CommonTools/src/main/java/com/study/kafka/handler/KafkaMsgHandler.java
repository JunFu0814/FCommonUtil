package com.study.kafka.handler;

import kafka.message.MessageAndOffset;

/**
 * Created by lf52 on 2018/4/11.
 */
public interface KafkaMsgHandler {

    /**
     * read message handler
     * @param messageAndOffset
     * @return
     * @throws Exception
     */
     void callback(MessageAndOffset messageAndOffset) throws Exception;

}
