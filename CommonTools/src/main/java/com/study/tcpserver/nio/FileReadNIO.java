package com.study.tcpserver.nio;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by lf52 on 2019/2/19.
 */
public class FileReadNIO {

    public static void main(String[] args) {

        RandomAccessFile file = null;
        FileChannel channel = null;
        try {
            //创建容量为48字节的缓冲区
            ByteBuffer buf = ByteBuffer.allocate(48);
            file = new RandomAccessFile("D://itemnumber.txt", "rw");
            channel = file.getChannel();
            int bytesRead = -1;
            bytesRead = channel.read(buf);

            while (bytesRead != -1){

                buf.flip();

                while (buf.hasRemaining()) {
                    System.out.print((char) buf.get());//一次读1个字节
                }
                //clear()方法会清空整个缓冲区,compact()方法只会清除已经读过的数据
                buf.clear();
                bytesRead = channel.read(buf);
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(file != null){
                try {
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(channel.isOpen()){
                try {
                    channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
