package com.study.tcpserver;

import org.junit.Test;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by lf52 on 2018/2/24.
 */
public class BIOTest {

    /**
     * 使用tcp连接到redis发送命令读取数据（BIO）
     */
    @Test
    public void testSocketConnectRedis(){
        //String command = "*2\r\n$3\r\nget\r\n$5\r\nhello\r\n";
        //String command = "*1\r\n$6\r\ndbsize\r\n";
        //String command = "*1\r\n$4\r\ninfo\r\n";
        //pipeline
        String command = "*2\r\n$3\r\nget\r\n$5\r\nhello\r\n*1\r\n$6\r\ndbsize\r\n";

        BufferedOutputStream out = null;
        BufferedReader bufferedReader = null;
        Socket socket = new Socket();
        try {

            socket.setReuseAddress(true);
            socket.setKeepAlive(true); // Will monitor the TCP connection is
            socket.setTcpNoDelay(true); // Socket buffer Whetherclosed, to
            socket.setSoLinger(true, 0); // Control calls close () method,
            socket.connect(new InetSocketAddress("10.16.46.172", 8009), 3000);
            socket.setSoTimeout(30000);
            // 向客户端回复信息
            out = new BufferedOutputStream(socket.getOutputStream());
            out.write(command.getBytes());
            out.flush();

            InputStreamReader read = new InputStreamReader(socket.getInputStream());
            bufferedReader = new BufferedReader(read,10000);

            String line = null;
            while ((line = bufferedReader.readLine()) != null){
                System.out.println(line);
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
                bufferedReader.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}
