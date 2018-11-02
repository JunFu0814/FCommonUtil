package com.study.zkpersistence;

/**
 * Created by lf52 on 2018/11/1.
 */
public class ZkNode {
    private String path;
    private String data;

    public ZkNode(String path, String data) {
        this.path = path;
        this.data = data;
    }

    public ZkNode() {

    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ZkNode{" +
                "path='" + path + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
