package com.study.tcpserver.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

/**
 * Created by lf52 on 2019/2/19.
 */
public class NIOClient {

    public static void main(String[] args) throws IOException, InterruptedException {

        InetSocketAddress crunchifyAddr = new InetSocketAddress("localhost", 8889);
        SocketChannel crunchifyClient = SocketChannel.open(crunchifyAddr);

        log("Connecting to Server on port 8889...");

        // 2.定义发送的数据
        ArrayList<String> companyDetails = new ArrayList<String>();
        companyDetails.add("Lenovo");
        companyDetails.add("Samsung");
        companyDetails.add("Huawei");
        companyDetails.add("Facebook");
        companyDetails.add("Twitter");
        companyDetails.add("IBM");
        companyDetails.add("Google");

        // 3.循环发送
        for (String companyName : companyDetails) {

            // 3.1 将字符转转换为字节
            byte[] message = new String(companyName).getBytes("UTF-8");

            // 3.2 定义Buffer 并将字节数组数据封装在Buffer中
            ByteBuffer buffer = ByteBuffer.wrap(message);

            // 3.3 Channel读取Buffer中的数据
            crunchifyClient.write(buffer);

            log("sending: " + companyName);
            // 清空Buffer
            buffer.clear();

            // 等待2000毫秒
            Thread.sleep(500);
        }
        // 关闭Channel
        crunchifyClient.close();
    }

    private static void log(String str) {
        System.out.println(str);
    }


}
