package com.study.serializeutil.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by lf52 on 2017/12/5.
 */
public class User implements Serializable{

    /**
     * 标识当前序列化类的版本号，否则在跨操作系统、跨编译器之间进行序列化和反序列化时容易出现InvalidClassException异常.
     */
    private static final long serialVersionUID = 8294180014912103005L;

    /*
     * 无参构造，必须要有
     */
    public User(){

    }
    public User(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    private String name;
    private Integer age;

    private List<String> hobby;

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getHobby() {
        return hobby;
    }

    public void setHobby(List<String> hobby) {
        this.hobby = hobby;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", hobby=" + hobby +
                '}';
    }
}
