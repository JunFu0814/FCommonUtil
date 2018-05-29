package com.study.javabasic;

public class JavaStudy {

    //类变量（类加载的准备阶段进行内存分配，并赋初始值）
    public static Integer a;

    //实例变量(在对象实例化(初始化)阶段会随对象一起分配在java堆中，也会赋默认初始值。)
    public Integer c;

    //--> 了解类加载机制
    public void testClassLoad(){
        //局部变量（方法调用的时候才赋值，必须初始化以后才可以使用）
        int b = 10;
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);

    }


    //值传递 引用传递的区别
    static class Person{
        int age;
    }
    public void show(Person person){
        person = new Person();
        person.age = 100;
    }

    //动态链接 静态链接  重载 重写
    public static void main(String[] args) {

        //1.
        new JavaStudy().testClassLoad();

        /* String a = "123";
        String b = "12"+"3";
        System.out.println(a==b);//true 类加载
        String a1 = "123";
        System.out.println(a==a1);//true
        //while(true) metaspace 爆掉  outofmemory
        String a2 = ("12"+Integer.valueOf("3")).intern();//堆 //StringBuilder 运行时 //常量池
        System.out.println(a==a2);
        System.out.println();*/

    }
}
