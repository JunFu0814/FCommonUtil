package com.study.tcpserver.nio;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;

/**
 * Created by lf52 on 2018/2/12.
 */
public class NIOTcpServcer {

    /**
     * start server
     * @param port
     * @throws IOException
     */
    public static void startServer(int port) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
    }
}
