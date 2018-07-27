package com.study.javabasic;

import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lf52 on 2018/7/5.
 */
public class OOMTest {

    /**
     * -Xms5M -Xmx5M
     * jdk1.6 : java.lang.OutOfMemoryError: PermGen space
     * jdk1.7,jdk1.8 : java.lang.OutOfMemoryError: Java Heap space
     * 1.6 字符串常量池在方法区，jdk1.7,jdk1.8以后被移入堆区中
     */
    @Test
    public void test1(){
        List<String> list = new ArrayList();
        for (int i = 0;i < Integer.MAX_VALUE;i++){
            String.valueOf(i).intern();
            list.add(String.valueOf(i).intern());
            System.out.println(String.valueOf(i).intern());
        }
    }

    /**
     * -XX:MetaspaceSize=5m -XX:MaxMetaspaceSize=5m
     * jdk1.6,jdk1.7 : java.lang.OutOfMemoryError: PermGen space
     * jdk1.8 : java.lang.OutOfMemoryError: Metaspace
     * jdk1.8以后移除了永久代PermGen space，类加载的信息被放入metaspace中
     */
    @Test
    public void test2(){
        URL url = null;
        List<ClassLoader> classLoaderList = new ArrayList<ClassLoader>();
        try {
            url = new File("D:\\test.txt").toURI().toURL();
            URL[] urls = {url};
            while (true){
                ClassLoader loader = new URLClassLoader(urls);
                classLoaderList.add(loader);
                loader.loadClass("com.study.javabasic.OOMTest");
                System.out.println(loader);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
