package DevTool;


import java.awt.*;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class LittleEndianReader {
    private final ByteArrayByteStream bs;

    public LittleEndianReader(final ByteArrayByteStream bs) {
        this.bs = bs;
    }

    public final byte[] getByteArray() {
        return bs.getByteArray();
    }

    public final byte readByte() {
        return (byte) bs.readByte();
    }

    public final int readByteToInt() {
        return bs.readByte();
    }

    public final int readInt() {
        final int byte1 = bs.readByte();
        final int byte2 = bs.readByte();
        final int byte3 = bs.readByte();
        final int byte4 = bs.readByte();
        return (byte4 << 24) + (byte3 << 16) + (byte2 << 8) + byte1;
    }

    public final short readShort() {
        final int byte1 = bs.readByte();
        final int byte2 = bs.readByte();
        return (short) ((byte2 << 8) + byte1);
    }

    public final int readUShort() {
        int quest = readShort();
        if (quest < 0) { //questid 50000 and above, WILL cast to negative, this was tested.
            quest += 65536; //probably not the best fix, but whatever
        }
        return quest;
    }

    public final char readChar() {
        return (char) readShort();
    }

    public final long readLong() {
        final long byte1 = bs.readByte();
        final long byte2 = bs.readByte();
        final long byte3 = bs.readByte();
        final long byte4 = bs.readByte();
        final long byte5 = bs.readByte();
        final long byte6 = bs.readByte();
        final long byte7 = bs.readByte();
        final long byte8 = bs.readByte();

        return ((byte8 << 56) + (byte7 << 48) + (byte6 << 40) + (byte5 << 32) + (byte4 << 24) + (byte3 << 16)
                + (byte2 << 8) + byte1);
    }

    public final float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    public final double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    public String readAsciiString(int n) {
        byte ret[] = new byte[n];
        for (int x = 0; x < n; x++) {
            ret[x] = readByte();
        }
        return new String(ret, StandardCharsets.UTF_16);
    }

    public final String readLengthAsciiString() {
        return readAsciiString(readShort());
    }

    public final Point readPos() {
        final int x = readShort();
        final int y = readShort();
        return new Point(x, y);
    }

    public final Point readIntPos() {
        final int x = readInt();
        final int y = readInt();
        return new Point(x, y);
    }

    public final byte[] read(final int num) {
        byte[] ret = new byte[num];
        for (int x = 0; x < num; x++) {
            ret[x] = readByte();
        }
        return ret;
    }

    public final long available() {
        return bs.available();
    }

    public final String toString() {
        return bs.toString();
    }

    public final String toString(final boolean b) {
        return bs.toString(b);
    }

    public final void seek(final long offset) {
        try {
            bs.seek(offset);
        } catch (IOException e) {
            System.err.println("Seek failed" + e);
        }
    }

    public final long getPosition() {
        return bs.getPosition();
    }
}
