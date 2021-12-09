package Netty;

import DevTool.ByteArrayByteStream;
import DevTool.LittleEndianReader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class NettyDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {

        int headSize = 4;
        int readAble = byteBuf.readableBytes();
        if (readAble < headSize) {
            return;
        }

        byteBuf.markReaderIndex();
        int size = byteBuf.readInt();
        if (readAble < (size + headSize)) {
            byteBuf.resetReaderIndex();
            return;
        }

        byte[] data = new byte[size];
        byteBuf.readBytes(data);
        byteBuf.markReaderIndex();

        list.add(new LittleEndianReader(new ByteArrayByteStream(data)));
    }
}
