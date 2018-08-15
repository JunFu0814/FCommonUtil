package com.study.javabasic;

import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 类唯一的识别是 ClassLoader id + PackageName + ClassName
 * 类加载器双亲委派模型流程：
 *    如果一个类加载器收到了类加载的请求，它首先不会自己去尝试加载这个类，而是把这个请求委派给父类加载器去完成，每一个层次的加载器都是如此，
 *    因此所有的类加载请求都会传给顶层的启动类加载器，只有当父加载器反馈自己无法完成该加载请求（该加载器的搜索范围中没有找到对应的类）时，
 *    子加载器才会尝试自己去加载。
 */
public class MyClassLoader extends ClassLoader {

    private String path;

    MyClassLoader(String path) {
        this.path = path;
    }

    MyClassLoader(ClassLoader parent,String path) {
        super(parent);
        this.path = path;
    }

    private byte[] loadByte(String name) throws Exception {
        name = name.replaceAll("\\.", "/");
        FileInputStream fis = new FileInputStream(path + "/" + name
                + ".class");
        int len = fis.available();
        byte[] data = new byte[len];
        fis.read(data);
        fis.close();
        return data;

    }

    /**
     * 重写findClass方法
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            byte[] data = loadByte(name);
            return defineClass(name, data, 0, data.length);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClassNotFoundException();
        }
    }

    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        //自定义类加载器的加载路径
        MyClassLoader myClassLoader=new MyClassLoader("D:/test");
        Class c=myClassLoader.loadClass("java.lang.String");
        if(c!=null){

            Method[] methods =  c.getDeclaredMethods();
            Field[]  fields =  c.getDeclaredFields();
            for(Method method : methods){
                System.out.println(method.getName());
            }
            for(Field field : fields){
                System.out.println(field.getName());
            }

            Object obj=c.newInstance();
            Method method=c.getMethod("hello", null);
            method.invoke(obj, null);
            System.out.println(c.getClassLoader().toString());
        }
    }

}
