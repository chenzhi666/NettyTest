package tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;

public class MyClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private int conut;
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
        byte[] buffer= new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(buffer);
        System.out.println("客户端接收到数据："+new String(buffer, Charset.forName("utf-8")));
        System.out.println("客户端接收到消息量="+(++conut));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
       for (int i=1;i<10;i++){
           System.out.println("客户端发送了消息数量="+i);
           ByteBuf buf = Unpooled.copiedBuffer("[hello,server" + i+"] ", CharsetUtil.UTF_8);
           ctx.channel().writeAndFlush(buf);
       }
    }


}
