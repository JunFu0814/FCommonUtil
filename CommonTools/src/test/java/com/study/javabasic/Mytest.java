package com.study.javabasic;

/**
 * Created by lf52 on 2018/7/6.
 */
public class Mytest {

    /**
     * 通过该例子深入理解java类加载的机制
     * @param args
     */
    public static void main(String[] args) {

        SingleTon singleTon = new SingleTon();
        SingleTon1 singleTon1 = SingleTon1.getInstance();
        SingleTon2 singleTon2 = SingleTon2.getInstance();
        SingleTon3 singleTon3 = SingleTon3.getInstance();
        SingleTon4 singleTon4 = SingleTon4.getInstance();

        System.out.println("count=" + singleTon.count);
        System.out.println("count1=" + singleTon1.count1);
        System.out.println("count2=" + singleTon2.count2);
        System.out.println("count3=" + singleTon3.count3);
        System.out.println("count4=" + singleTon4.count4);

    }

}

class SingleTon {

    public static int count;

    static {
        count++;
    }

    public SingleTon() {
    }

}

class SingleTon1 {

    private static SingleTon1 singleTon = new SingleTon1();
    public static int count1;
    private SingleTon1() {
        count1++;
    }

    public static SingleTon1 getInstance() {
        return singleTon;
    }
}

class SingleTon2 {

    private static SingleTon2 singleTon = new SingleTon2();
    public static int count2 = 0;
    private SingleTon2() {
        count2++;
    }

    public static SingleTon2 getInstance() {
        return singleTon;
    }
}

class SingleTon3 {

    public static int count3;
    private static SingleTon3 singleTon = new SingleTon3();

    private SingleTon3() {
        count3++;
    }

    public static SingleTon3 getInstance() {
        return singleTon;
    }
}

class SingleTon4 {

    public static int count4 = 0;
    private static SingleTon4 singleTon = new SingleTon4();

    private SingleTon4() {
        count4++;
    }
    public static SingleTon4 getInstance() {
        return singleTon;
    }
}
