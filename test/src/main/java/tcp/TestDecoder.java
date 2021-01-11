package tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.CharsetUtil;
import java.nio.charset.Charset;
import java.util.List;

public class TestDecoder extends ReplayingDecoder<Void>{
    private int conut;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        System.out.println("TestDecoder被解码调用=="+(++conut));
        int lengtj="[hello,server1] ".getBytes(CharsetUtil.UTF_8).length;
            byte[] bytes= new byte[lengtj];
            byteBuf.readBytes(bytes);
            String msg=new String(bytes,CharsetUtil.UTF_8);
            list.add(msg);
    }
}
