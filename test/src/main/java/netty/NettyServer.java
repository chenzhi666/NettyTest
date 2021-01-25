package netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class NettyServer {
    public static void main(String[] args) throws InterruptedException {
        System.out.println(Thread.currentThread().getName());
        //创建BOSSGroup和workergroup
        EventLoopGroup bossGroup=new NioEventLoopGroup(2);
        EventLoopGroup workergroup=new NioEventLoopGroup();

        //创建服务器端配置对象，配置参数
        ServerBootstrap bootstrap=new ServerBootstrap();
        try {
            bootstrap.group(bossGroup,workergroup) //设置线程
                    .channel(NioServerSocketChannel.class)  //设置服务器通道
                    .option(ChannelOption.SO_BACKLOG,128) //设置线程队列得到连接数
                    .option(ChannelOption.SO_KEEPALIVE,true)  //设置保持活动连接状态
                     .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {//创建一个通道测试对象
                        //给pipelline 设置处理器
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new NettyServerHandler());//设置处理器
                        }
                    });
            System.out.println(".....服务 is ready........");
            //绑定一个端口并且同步，生成一个channelFuture对象
            //启动服务器绑定端口
            ChannelFuture cf=bootstrap.bind(6668).sync();
            //对通道进行监听
            cf.channel().closeFuture().sync();
        } finally {
               bossGroup.shutdownGracefully();
               workergroup.shutdownGracefully();
        }
    }
}
