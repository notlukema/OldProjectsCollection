import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

public class Image { // Literally unused

    private final int width, height;

    private final ByteBuffer buffer;

    public Image(int width, int height) {
        this.width = width;
        this.height = height;
        buffer = BufferUtils.createByteBuffer(width * height * 4);
        clear();
    }

    public void clear() {
        for (int i=0;i<width*height*4;i++) {
            buffer.put(i, (byte)0);
        }
    }

    public void clear(int r, int g, int b, int a) {
        for (int i=0;i<width*height;i++) {
            int j = i * 4;
            buffer.put(j, (byte)r);
            buffer.put(j+1, (byte)g);
            buffer.put(j+2, (byte)b);
            buffer.put(j+3, (byte)a);
        }
    }

    public boolean outBounds(int x, int y) {
        return x < 0 || y < 0 || x >= width || y >= height;
    }

    public int getIndex(int x, int y) {
        return (x + width * y) * 4;
    }
    /*
    public Vector4f get(int x, int y) {
        if (outBounds(x, y)) {
            return null;
        }
        int i = getIndex(x, y);
        return new Vector4f(buffer.get(i), buffer.get(i+1), buffer.get(i+2), buffer.get(i+3));
    }

    public void set(int x, int y, Vector4f vec) {
        if (outBounds(x, y)) {
            return;
        }
        int i = getIndex(x, y);
        buffer.put(i, (byte)(vec.getR()*255));
        buffer.put(i+1, (byte)(vec.getG()*255));
        buffer.put(i+2, (byte)(vec.getB()*255));
        buffer.put(i+3, (byte)(vec.getA()*255));
    }
    */
    public void set(int x, int y, int r, int g, int b, int a) {
        if (outBounds(x, y)) {
            return;
        }
        int i = getIndex(x, y);
        buffer.put(i, (byte)r);
        buffer.put(i+1, (byte)g);
        buffer.put(i+2, (byte)b);
        buffer.put(i+3, (byte)a);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

}
