package tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;
import java.util.UUID;

public class MyServerHandler extends SimpleChannelInboundHandler<String> {
    private  int conut;
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String byteBuf) throws Exception {
            //  byte[] buffer= new byte[byteBuf.readableBytes()];
            //  byteBuf.readBytes(buffer);
             //  System.out.println("服务器接收到数据："+new String(buffer, Charset.forName("utf-8")));
        System.out.println("服务器接收到数据："+byteBuf);
        System.out.println("服务器器接收到消息量="+(++conut));
        //服务器返回随机id给客户端
        channelHandlerContext.writeAndFlush(Unpooled.copiedBuffer(UUID.randomUUID().toString(), CharsetUtil.UTF_8));
    }
}
