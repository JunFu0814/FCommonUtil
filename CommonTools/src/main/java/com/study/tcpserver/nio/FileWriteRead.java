package com.study.tcpserver.nio;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by lf52 on 2019/2/19.
 *
 * 利用nio，我们在处理大文件时候，可以映射文件中的某一部分数据以读写模式到内存中，这样有两个好处;
 * </p> 1. 不要复制文件中所有的数据，只需要修改文件中局部的数据。
 * </p> 2. 并行\分段处理大文件。
 */
public class FileWriteRead {

    public static void main(String[] args) throws FileNotFoundException {

        RandomAccessFile file = new RandomAccessFile("D://leo.txt", "rw");
        try (FileChannel channel = file.getChannel()) {

            long length=file.length();
            System.out.println("file length : "+length);

            //映射文件中的某一部分数据以读写模式到内存中
            MappedByteBuffer buffer =  channel.map(FileChannel.MapMode.READ_WRITE, 1, 4);

            for(int i=0;i<4;i++){
                byte src= buffer.get(i);
                buffer.put(i,(byte)(src-32));//修改Buffer中映射的字节的值（只能一个字节的去修改）
                System.out.println("被改为大写的原始字节是:"+src);
            }

            buffer.force();//强制输出,在buffer中的改动生效到文件
            buffer.clear();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(file != null){
                try {
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
