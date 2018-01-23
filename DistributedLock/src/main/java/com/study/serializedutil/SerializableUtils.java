package com.study.serializedutil;

import java.io.*;

/**
 * java 原生序列化
 * Created by lf52 on 2017/12/6.
 */
public class SerializableUtils {
    //序列化一个对象,可以存储到一个文件也可以存储到字节数组
    public static byte[] serialByte(Object obj)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    //反序列化一个对象
    public static Object deSerialByte(byte[] by)
    {
        ObjectInputStream ois;
        try {
            ois = new ObjectInputStream(new ByteArrayInputStream(by));
            return ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
