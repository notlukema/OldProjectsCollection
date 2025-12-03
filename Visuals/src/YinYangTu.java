import image.Image;
import math.Vector2f;

public class YinYangTu extends Vector2f {

    public float r;

    public float spin;

    public YinYangTu(float x, float y, float r) {
        super(x, y);
        this.r = r;
        spin = 0;
    }

    public void spin(float change) {
        spin += change;
    }

    public void draw(Image image) {
        int rad = (int)r;
        float inner = rad * 0.1f;

        for (int x=-rad;x<=rad;x++) {
            int size = (int)Math.sqrt(rad*rad - x*x);
            for (int y=-size;y<=size;y++) {
                float sin = (float)Math.sin(Math.toRadians(spin));
                float cos = (float)Math.cos(Math.toRadians(spin));
                float nx = x * cos - y * sin;
                float ny = x * sin + y * cos;

                float nr = rad / 2f;
                float dy = Math.abs(nr - Math.abs(ny));
                float compare = (float)Math.sqrt(nr*nr - dy*dy);

                if (ny < 0) {
                    compare = -compare;
                }

                float color = nx < compare ? 0f : 1f;
                if (nx*nx + dy*dy < inner * inner) {
                    color = 1 - color;
                }

                image.set((int)this.x+x, (int)this.y+y, color, color, color, 1f);
            }
        }
    }

    public float getRadius() {
        return r;
    }

    public void setRadius(float r) {
        this.r = r;
    }

}
