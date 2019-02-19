package com.study.tcpserver.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by lf52 on 2018/2/12.
 */
public class NIOTcpServcer {


    public static void main(String[] args) {
        try {
            StartServer(8889);
        } catch (IOException e) {
            log(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * start server
     * @param port
     * @throws IOException
     */
    public static void StartServer(int port) throws IOException {
        //定义ServerSocketChannel 并监听本机指定的端口
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //设置非阻塞模式
        serverSocketChannel.configureBlocking(false);

        //检索与此通道关联的服务器套接字
        ServerSocket serverSocket = serverSocketChannel.socket();
        //进行服务的绑定
        serverSocket.bind(new InetSocketAddress(port));

        //定义Selector
        Selector selector = Selector.open();

        //注册selector
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        log("Starting Server , Port is : " + port);

        // 7.保持Server运行
        while (true) {

            //返回读事件已经就绪的那些通道。 并进行判断
            if (selector.select(1000 * 4) == 0) {
                log("Server Started , Waiting ...... ");
                continue;
            }

            // 一旦调用了select()方法，并且返回值表明有一个或更多个通道就绪了然后可以通过调用selector的selectedKeys()方法，访问“已选择键集（selected keyset）”中的就绪通道。

            // --- 返回此选择器的已选择键集。
            Set<SelectionKey> selectionKeySet = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeySet.iterator();

            SocketChannel client =  null;

            // 7.3 遍历获取
            while (iterator.hasNext()) {

                SelectionKey myKey = iterator.next();


                //此键的通道是否已准备好接受新的套接字连接。
                if (myKey.isAcceptable()) {
                    // 接受到此通道套接字的连接。
                    // 此方法返回的套接字通道（如果有）将处于阻塞模式。
                    client = serverSocketChannel.accept();
                    //配置为非阻塞
                    client.configureBlocking(false);
                    Socket socket = client.socket();
                    SocketAddress remoteAddr = socket.getRemoteSocketAddress();
                    log("Connected to: " + remoteAddr + "  \t Connection Accepted:   \n");

                    // 7.4.3注册到selector，等待连接
                    client.register(selector, SelectionKey.OP_READ);


                }
                if (myKey.isReadable()) {

                    //返回为之创建此键的通道。
                    client = (SocketChannel) myKey.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 3);
                    //将缓冲区清空以备下次读取
                    byteBuffer.clear();

                    // 读取服务器发送来的数据到缓冲区中
                    int readCount = -1;
                    readCount = client.read(byteBuffer);

                    // 如果没有数据，关闭当前socket channel继续监听
                    if (readCount == -1) {
                        Socket socket = client.socket();
                        SocketAddress remoteAddr = socket.getRemoteSocketAddress();
                        log("Connection closed by client: " + remoteAddr);
                        client.close();
                        myKey.cancel();
                    }else{
                        // Todo 真正的数据的处理逻辑
                        String receiveText = new String( byteBuffer.array(),0,readCount);
                        log("Server Get Data : "+receiveText);
                    }


                }

                // 移除
                iterator.remove();
            }
        }
    }


    private static void log(String str) {
        System.out.println(str);
    }
}



