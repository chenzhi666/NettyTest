package nio;

import java.io.IOException;
import java.io.InputStream;
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

public class ServerTest {
    private Selector selector;

    private ByteBuffer buffer = ByteBuffer.allocate(1024);

    public static void main(String[] args) {
       // server();
        new ServerTest(8888).runServer();
    }

    public ServerTest(int port) {

        try {
            //1 创建一个传送带
            selector = Selector.open();
            //2.创建一个管道
            ServerSocketChannel ssc = ServerSocketChannel.open();
            //3.设置服务器非阻塞
            ssc.configureBlocking(false);
            //4.绑定tcp地址
            ssc.bind(new InetSocketAddress(port));
            // 把管道放到传送带上，并在传送带上注册一个感兴趣事件，此处传送带感兴趣事件为连接事件
            ssc.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("开启服务器成功，当前端口：" + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void runServer() {
        while (true) {
            try {
                System.out.println("轮询传送带....");
                //开启传送带，开始轮询
                selector.select();
                //遍历所有感兴趣的事件
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey selectionKey = keys.next();
                    keys.remove();
                    if (selectionKey.isValid()) {
                        if (selectionKey.isAcceptable()) {
                            System.out.println("得到连接事件...");
                            accept(selectionKey);
                        }
                        if (selectionKey.isReadable()){
                            System.out.println("得到读取事件....");
                            read(selectionKey);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void accept(SelectionKey key) {
        try {
            //1 获取服务器通道
            ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
            //2 执行阻塞方法
            SocketChannel sc = ssc.accept();
            //3 设置阻塞模式为非阻塞
            sc.configureBlocking(false);
            //4 注册到多路复用选择器上，并设置读取标识
            sc.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void read(SelectionKey key) {
        try {
            //1 清空缓冲区中的旧数据
            buffer.clear();
            //2 获取之前注册的SocketChannel通道
            SocketChannel sc = (SocketChannel) key.channel();
            //3 将sc中的数据放入buffer中
            int count = sc.read(buffer);
            if (count==-1){//==-1则表示没有数据
                sc.close();
                key.cancel();
                return;
            }
            //读取到了数据，将buffer的position复位到0
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            //将buffer中的数据写入byte[]中
            buffer.get(bytes);
            String body = new String(bytes).trim();
            System.out.println("Server：" + body);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void server() {
        ServerSocket serverSocket = null;
        InputStream in = null;
        try {
            System.out.println("开启serverSocket.....");
            serverSocket = new ServerSocket(8888);
            int recvMsgSize = 0;
            byte[] recvBuf = new byte[1024];
            while (true) {
                Socket clntSocket = serverSocket.accept();
                SocketAddress clientAddress = clntSocket.getRemoteSocketAddress();
                System.out.println("Handling client at " + clientAddress);
                in = clntSocket.getInputStream();
                while ((recvMsgSize = in.read(recvBuf)) != -1) {
                    byte[] temp = new byte[recvMsgSize];
                    System.arraycopy(recvBuf, 0, temp, 0, recvMsgSize);
                    System.out.println(new String(temp));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                    System.out.println("关闭serverSocket.....");
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
