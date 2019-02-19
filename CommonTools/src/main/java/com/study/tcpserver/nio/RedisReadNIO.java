package com.study.tcpserver.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by lf52 on 2019/2/19.
 */
public class RedisReadNIO {

    public static void main(String[] args) {

        String command = "*2\r\n$6\r\nmemory\r\n$6\r\ndoctor\r\n";
        SocketChannel channel = null;

        try {
            //打开一个SocketChannel并连接到某台服务器
            channel = SocketChannel.open();
            //使用非阻塞模式
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress("10.16.50.223",6000));
            while(!channel.finishConnect() ){
                //wait
            }

            //定义Buffer 并将redis客户端命令写入channel中
            ByteBuffer buffer = ByteBuffer.wrap(command.getBytes("UTF-8"));
            channel.write(buffer);
            buffer.clear();

            //sleep 1s 等待redis server端准备好数据
            Thread.sleep(1000);

            int bytesRead = -1;
            //从channel中读取redis server端返回的结果
            bytesRead = channel.read(buffer);
            while (bytesRead != -1) {

                if(bytesRead <= 0){
                    break;
                }
                //将Buffer从写模式切换到读模式
                buffer.flip();
                while (buffer.hasRemaining()) {
                    System.out.print((char) buffer.get());//一次读1个字节
                }
                //clear()方法会清空整个缓冲区,compact()方法只会清除已经读过的数据
                buffer.clear();
                bytesRead = channel.read(buffer);

            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
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
