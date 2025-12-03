import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

import image.Image;
import math.Vector2f;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import java.util.Arrays;

public class Main {

    public static final int LEFT_MOUSE = 0,
            RIGHT_MOUSE = 1,
            MIDDLE_MOUSE = 2;

    public static Vector2f cursorPos;
    public static boolean[] mouse;
    public static int[] mouseCount;
    public static float scroll;

    public static void main(String[] args) {
        glfwInit();

        int width = 1000;
        int height = 1000;
        Image image = new Image(width, height);

        long window = glfwCreateWindow(width, height, "graphics", 0, 0);
        glfwMakeContextCurrent(window);
        createCapabilities();

        cursorPos = new Vector2f();
        mouse = new boolean[3];
        Arrays.fill(mouse, false);
        mouseCount = new int[3];
        Arrays.fill(mouseCount, 0);

        glfwSetCursorPosCallback(window, new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double x, double y) {
                cursorPos.setX((float)x);
                cursorPos.setY((float)y);
            }
        });
        glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                if (action == GLFW_PRESS) {
                    mouse[button] = true;
                }
                if (action == GLFW_RELEASE) {
                    mouse[button] = false;
                }
            }
        });
        glfwSetScrollCallback(window, new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xoff, double yoff) {
                scroll = (float)yoff;
            }
        });

        float fps = 0;
        float wait = 1;

        int count = 0;
        long fpstime = System.nanoTime();
        long last = fpstime;
        long time = fpstime;

        Engine.init(image);

        while(!glfwWindowShouldClose(window)) {
            glfwPollEvents();

            for (int i=0;i<mouse.length;i++) {
                if (mouse[i]) {
                    mouseCount[i]++;
                } else {
                    mouseCount[i] = 0;
                }
            }

            glClearColor(0f, 0f, 0f, 1f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            int[] w = new int[1];
            int[] h = new int[1];
            glfwGetWindowSize(window, w, h);

            float ratio = Math.min((float)w[0]/width, (float)h[0]/height);
            int x = (w[0] - (int)(width * ratio)) / 2;
            int y = (h[0] - (int)(height * ratio)) / 2;

            float cx = (cursorPos.getX() - x) / ratio;
            float cy = height - (cursorPos.getY() - y) / ratio;
            Engine.loop((time - last) / 1000000000f, image, new Vector2f(cx, cy), mouseCount);

            glViewport(x, y, (int)(width*ratio), (int)(height*ratio));
            glEnable(GL_TEXTURE_2D);
            glEnable(GL_DEPTH_TEST);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

            int textureID = glGenTextures();
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, textureID);
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, image.getBuffer());

            glColor4f(1f, 1f, 1f, 1f);
            glBegin(GL_QUADS);

            glTexCoord2f(0, 1);
            glVertex2f(-1, 1);
            glTexCoord2f(1, 1);
            glVertex2f(1, 1);
            glTexCoord2f(1, 0);
            glVertex2f(1, -1);
            glTexCoord2f(0, 0);
            glVertex2f(-1, -1);

            glEnd();
            glDeleteTextures(textureID);

            glFlush();
            glfwSwapBuffers(window);

            count++;
            last = time;
            time = System.nanoTime();
            if (time > fpstime + 1000000000 * wait) {
                fps = count / wait;
                System.out.println("fps: "+fps);
                count = 0;
                fpstime = time;
            }
        }
    }

}