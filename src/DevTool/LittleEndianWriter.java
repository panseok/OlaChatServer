package DevTool;


import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class LittleEndianWriter {
    private final ByteArrayOutputStream baos;
    private static final Charset ASCII = StandardCharsets.UTF_16;

    public LittleEndianWriter() {
        this(32);
    }

    public LittleEndianWriter(final int size) {
        this.baos = new ByteArrayOutputStream(size);
    }

    public final byte[] getPacket() {
        return baos.toByteArray();
    }

    public final void write(final boolean b) {
        baos.write(b ? 1 : 0);
    }

    public final void write(final byte[] b) {
        for (int x = 0; x < b.length; x++) {
            baos.write(b[x]);
        }
    }

    public void write(int b) {
        if (b != -88888) {
            baos.write((byte) b);
        }
    }

    public final void writeShort(final int i) {
        baos.write((byte) (i & 0xFF));
        baos.write((byte) ((i >>> 8) & 0xFF));
    }

    public final void writeInt(final int i) {
        if (i != -88888) {
            baos.write((byte) (i & 0xFF));
            baos.write((byte) ((i >>> 8) & 0xFF));
            baos.write((byte) ((i >>> 16) & 0xFF));
            baos.write((byte) ((i >>> 24) & 0xFF));
        }
    }

    public void writeInt(long i) {
        baos.write((byte) (i & 0xFF));
        baos.write((byte) ((i >>> 8) & 0xFF));
        baos.write((byte) ((i >>> 16) & 0xFF));
        baos.write((byte) ((i >>> 24) & 0xFF));
    }

    public final void writeAsciiString(final String s) {
        write(s.getBytes(ASCII));
    }

    public final void writeAsciiString(String s, final int max) {
        if (s.getBytes(ASCII).length > max) {
            s = s.substring(0, max);
        }
        write(s.getBytes(ASCII));
        for (int i = s.getBytes(ASCII).length; i < max; i++) {
            write(0);
        }
    }

    public final void writeLengthAsciiString(final String s) {
        writeShort((short) s.getBytes(ASCII).length);
        writeAsciiString(s);
    }

    public final void writePos(final Point s) {
        writeShort(s.x);
        writeShort(s.y);
    }

    public void writePosInt(Point s) {
        writeInt(s.x);
        writeInt(s.y);
    }

    public final void writeLong(final long l) {
        baos.write((byte) (l & 0xFF));
        baos.write((byte) ((l >>> 8) & 0xFF));
        baos.write((byte) ((l >>> 16) & 0xFF));
        baos.write((byte) ((l >>> 24) & 0xFF));
        baos.write((byte) ((l >>> 32) & 0xFF));
        baos.write((byte) ((l >>> 40) & 0xFF));
        baos.write((byte) ((l >>> 48) & 0xFF));
        baos.write((byte) ((l >>> 56) & 0xFF));
    }

    public final void writeReversedLong(final long l) {
        baos.write((byte) ((l >>> 32) & 0xFF));
        baos.write((byte) ((l >>> 40) & 0xFF));
        baos.write((byte) ((l >>> 48) & 0xFF));
        baos.write((byte) ((l >>> 56) & 0xFF));
        baos.write((byte) (l & 0xFF));
        baos.write((byte) ((l >>> 8) & 0xFF));
        baos.write((byte) ((l >>> 16) & 0xFF));
        baos.write((byte) ((l >>> 24) & 0xFF));
    }

}
