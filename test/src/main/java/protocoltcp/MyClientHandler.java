package protocoltcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;

public class MyClientHandler extends SimpleChannelInboundHandler<MessageProtocol> {
    private int conut;
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessageProtocol msg) throws Exception {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
       for (int i=0;i<5;i++){
          String mes="升职加薪走向人生巅峰666";
           MessageProtocol messageProtocol = new MessageProtocol();
           messageProtocol.setContent(mes.getBytes(CharsetUtil.UTF_8));
           messageProtocol.setLen(mes.getBytes(CharsetUtil.UTF_8).length);
           ctx.channel().writeAndFlush(messageProtocol);
       }
    }


}
