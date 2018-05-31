package com.study.javabasic;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lf52 on 2018/5/30.
 */
public class SoftReferenceTest {
    /**
     * 软引用的equals 也是比较内存地址的相等(等价于 == )
     * @param args
     */
    public static void main(String[] args) {
        Map<SoftReference,String> map = new HashMap<>();

        String temp = "123";
        SoftReference aSoftRef = new SoftReference(temp);

        String temp1 = "12" + Integer.valueOf(3);
        SoftReference bSoftRef = new SoftReference(temp1.intern());

        System.out.println(temp.equals(temp1));
        System.out.println(temp == temp1.intern());

        System.out.println(aSoftRef.equals(bSoftRef));

        map.put(aSoftRef, "leo");
        System.out.println(map.get(aSoftRef));
        System.out.println(map.get(bSoftRef));
    }
}
