package groupchat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Scanner;


public class ChatClient {
    private  final  String host;
    private  final  int port;

    public static void main(String[] args) throws InterruptedException {
        new ChatClient("127.0.0.1",6668).run();
    }


    public ChatClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public  void run() throws InterruptedException {
        EventLoopGroup group=new NioEventLoopGroup();
        try {

            Bootstrap bootstrap=new Bootstrap().group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new StringDecoder())
                                    .addLast(new StringEncoder())
                                    .addLast(new ChatClientHandler());
                        }
                    });
            ChannelFuture sync = bootstrap.connect(host, port).sync();
            System.out.println("--------"+ sync.channel().localAddress());
            Scanner scanner=new Scanner(System.in);
            while (scanner.hasNext()){
                String msg=scanner.nextLine();
                sync.channel().writeAndFlush(msg);
            }
        } finally {
                group.shutdownGracefully();
        }
    }
}
