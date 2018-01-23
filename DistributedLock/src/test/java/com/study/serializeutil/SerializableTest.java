package com.study.serializeutil;


import com.study.serializedutil.KryoSerializationUtils;
import com.study.serializedutil.ProtoStuffUtils;
import com.study.serializedutil.SerializableUtils;
import com.study.serializeutil.entity.Person;
import com.study.serializeutil.entity.User;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by lf52 on 2017/12/6.
 */
public class SerializableTest {

    /**
     * 关于transient关键字：
     *   1.一旦变量被transient修饰，变量将不再是对象持久化的一部分，该变量内容在序列化后无法获得访问。
     *   2.transient关键字只能修饰成员变量，而不能修饰本地变量，方法和类。
     *   3.被transient关键字修饰的变量不再能被序列化，一个静态变量不管是否被transient修饰，均不能被序列化。
     *    （反序列化后static型变量的值为当前JVM中对应static变量的值）
     */

    @Test
    public void test1(){
        User user = (User) SerializableUtils.deSerialByte(SerializableUtils.serialByte(new User("user", 15)));

        Person person = (Person)SerializableUtils.deSerialByte(SerializableUtils.serialByte(new Person("person", 18)));

        System.out.println("user:"+user.toString());

        System.out.println("person:" + person.toString());
    }

    /**
     * test protostuff
     */
    @Test
    public void test2(){
        User user = new User("user", 15);
        Person person = new Person("person", 18);
        System.out.println(ProtoStuffUtils.deserialize(ProtoStuffUtils.serialize(user), User.class));
        System.out.println(ProtoStuffUtils.deserialize(ProtoStuffUtils.serialize(person), Person.class));
    }

    @Test
    public void test3(){
        User user = new User("user", 15);
        System.out.println(KryoSerializationUtils.deserializationObject(KryoSerializationUtils.serializationObject(user), User.class));
    }

    /**
     * 测试序列化以后文件的大小
     * kyro 34b
     * protostuff 9b
     */
    @Test
    public void test4(){
        User user = new User("fujun", 15);
        List<String> hobby = new ArrayList();
        hobby.add("basketball");
        hobby.add("football");
        hobby.add("running");
        hobby.add("test");
        user.setHobby(hobby);
        System.out.println(KryoSerializationUtils.serializationObject(user).length);
        System.out.println(ProtoStuffUtils.serialize(user).length);
    }

    /**
     * test protostuff序列化反序列化速度
     *
     */
    @Test
    public void test5(){
        List<byte[]> plist = new LinkedList();
        List<byte[]> klist = new LinkedList();

        byte[] data=null;
        byte[] data1=null;
        int size = 100;
        long start = System.currentTimeMillis();
        for(int i = 0;i < size;i ++){
            User user = new User("user" + i, i);
            data = ProtoStuffUtils.serialize(user);
            plist.add(data);
        }
        long end = System.currentTimeMillis();
        long time = end - start;
        System.out.println("protostuff serialize cost time : " + time + " ms, size is :" + data.length);

        long dstart = System.currentTimeMillis();

        for(byte[] user : plist){
            ProtoStuffUtils.deserialize(user,User.class);
        }
        long dend = System.currentTimeMillis();
        long dtime = dend - dstart;
        System.out.println("protostuff deSerialize cost time : " + dtime + "ms");

//---------------------------------------------------------------------------------------------

       long kstart = System.currentTimeMillis();
        for(int i = 0;i < size;i ++){
            User user = new User("user" + i, i);
            data1 = KryoSerializationUtils.serializationObject(user);
            klist.add(data1);
        }

        long kend = System.currentTimeMillis();
        long ktime = kend - kstart;
        System.out.println("kyro serialize cost time : " + ktime + " ms, size is :" + data1.length);

        long dkstart = System.currentTimeMillis();

        for(byte[] user : klist){
            KryoSerializationUtils.deserializationObject(user,User.class);
        }
        long dkend = System.currentTimeMillis();
        long dktime = dkend - dkstart;
        System.out.println("kyro deSerialize cost time : " + dktime + "ms");

    }


    @Test
    public void test6(){

    }
}
