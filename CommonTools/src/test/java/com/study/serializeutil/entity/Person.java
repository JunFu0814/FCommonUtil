package com.study.serializeutil.entity;

import java.io.Serializable;

/**
 * Created by lf52 on 2017/12/5.
 */
public class Person  implements Serializable {

    /**
     * 标识当前序列化类的版本号，否则在跨操作系统、跨编译器之间进行序列化和反序列化时容易出现InvalidClassException异常.
     */
    private static final long serialVersionUID = 8294180014912103006L;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    private String name;

    //age 字段被标记为transient
    private transient int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
