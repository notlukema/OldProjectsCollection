import image.Image;
import math.Vector2f;

import java.util.ArrayList;

public class Engine {

    public static YinYangTu tu;

    public static ArrayList<Vector2f> particles;

    public static void init(Image image) {
        particles = new ArrayList<Vector2f>();
        int width = image.getWidth();
        int height = image.getHeight();
        tu = new YinYangTu(width/2f, height/2f, Math.min(width/2, height/2)-50);
    }

    public static void loop(float delta, Image image, Vector2f mousePos, int[] mouse) {
        image.clear(0.5f, 0.5f, 0.5f, 1f);
        tu.spin(180f * delta);
        tu.setRadius(400f);
        tu.draw(image);
        /* Old code; particles are the same as vector2f
        int width = image.getWidth();
        int height = image.getHeight();
        if (mouse[Main.LEFT_MOUSE] > 0) {
            particles.add(new Particle(mousePos));
        }
        for (Particle p : particles) {
            image.set((int)p.getX(), (int)p.getY(), 1f, 1f, 1f, 1f);
        }
        final float max = 50;
        for (int x=0;x<width;x++) {
            for (int y=0;y<height;y++) {
                float percent = 0;
                float minDist = max * max;
                for (Particle p : particles) {
                    float dx = p.getX() - x;
                    float dy = p.getY() - y;
                    minDist = Math.min(minDist, dx*dx + dy*dy);
                    percent += Math.max(0, 1 - (float)Math.sqrt(minDist) / max);
                }
                image.set(x, y, 1f, 1f, 1f, Math.min(percent, 1));
            }
        }
        */
    }

}
