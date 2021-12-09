package Netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class NettyEncoder extends MessageToByteEncoder<byte[]> {

    @Override
    protected void encode(ChannelHandlerContext ctx, byte[] pData, ByteBuf buffer) throws Exception{
        try {
            int i = pData.length;
            buffer.writeInt(i);
            buffer.writeBytes(pData);
        } finally {
            ctx.flush();
        }
    }

}
