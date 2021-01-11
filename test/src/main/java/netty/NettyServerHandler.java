package netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

/**
 * 自定义处理器
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
       // System.out.println("server ctx="+ctx);
        //System.out.println(ctx.);
        System.out.println("当前线程读取客户端消息线程："+Thread.currentThread().getName());
        ByteBuf buf= (ByteBuf) msg;
        System.out.println("客户端发送消息："+buf.toString(CharsetUtil.UTF_8));
        // System.out.println("客户端的地址："+ctx.channel().remoteAddress());
        ctx.channel().eventLoop().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ctx.writeAndFlush(Unpooled.copiedBuffer("来自服务端==>hello,客户端,异步任务完成~",CharsetUtil.UTF_8));
            }
        });
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("服务端向客户端发送消息，当前目标客户端=》"+ctx.channel().remoteAddress());
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello,客户端~",CharsetUtil.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
