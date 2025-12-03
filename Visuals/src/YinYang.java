import image.Image;
import math.Vector2f;
import org.lwjgl.glfw.*;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWNativeWin32.*;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.system.windows.User32.*;

public class YinYang {

    public static final int LEFT_MOUSE = 0,
            RIGHT_MOUSE = 1,
            MIDDLE_MOUSE = 2;

    public static Vector2f cursorPos;
    public static boolean[] mouse;
    public static int[] mouseCount;
    public static float scroll;
    public static float spin;
    public static Vector2f grabbed;

    public static YinYangTu tu;

    public static void hide(long window) {
        glfwSetWindowAttrib(window, GLFW_DECORATED, GLFW_FALSE);
    }

    public static void show(long window) {
        glfwSetWindowAttrib(window, GLFW_DECORATED, GLFW_TRUE);
    }

    public static void main(String[] args) {
        glfwInit();

        int size = 800;
        int extra = 50;
        Image image = new Image(size + extra, size + extra);
        tu = new YinYangTu((size + extra)/2f, (size + extra)/2f, size/2f);
        spin = 180f;

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_TRUE);
        glfwWindowHint(GLFW_FLOATING, GLFW_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        long window = glfwCreateWindow((size + extra)/2, (size + extra)/2, "", 0, 0);
        glfwMakeContextCurrent(window);
        createCapabilities();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer buffer = stack.mallocInt(1);
            SetWindowLongPtr(buffer, glfwGetWin32Window(window), GWL_EXSTYLE, WS_EX_TOOLWINDOW);
        }
        //glfwSetCursor(window, glfwCreateStandardCursor(GLFW_HAND_CURSOR));

        hide(window);

        glfwShowWindow(window);

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
        glfwSetKeyCallback(window, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (action == GLFW_PRESS) {
                }
                if (action == GLFW_RELEASE) {
                    if (key == GLFW_KEY_ESCAPE) {
                        if (glfwGetWindowAttrib(window, GLFW_DECORATED) == GLFW_TRUE) {
                            hide(window);
                        } else {
                            show(window);
                        }
                    }
                }
            }
        });

        float fps = 0;
        float wait = 1;

        int count = 0;
        long fpstime = System.nanoTime();
        long last = fpstime;
        long time = fpstime;

        while(!glfwWindowShouldClose(window)) {
            glfwPollEvents();

            for (int i=0;i<mouse.length;i++) {
                if (mouse[i]) {
                    mouseCount[i]++;
                } else {
                    mouseCount[i] = 0;
                }
            }

            glClearColor(0f, 0f, 0f, 0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            int[] w = new int[1];
            int[] h = new int[1];
            glfwGetWindowSize(window, w, h);

            float ratio = Math.min((float)w[0]/size, (float)h[0]/size);
            int x = (w[0] - (int)(size * ratio)) / 2;
            int y = (h[0] - (int)(size * ratio)) / 2;

            float cx = (cursorPos.getX() - x) / ratio;
            float cy = size - (cursorPos.getY() - y) / ratio;
            float delta = (time - last) / 1000000000f;

            boolean hidden = glfwGetWindowAttrib(window, GLFW_DECORATED) == GLFW_FALSE;
            if (hidden) {
                spin += scroll * 10;
            } else {
                int[] s = new int[1];
                glfwGetWindowSize(window, s, new int[1]);
                glfwSetWindowSize(window, s[0]+(int)(scroll*10), s[0]+(int)(scroll*10));
            }
            scroll = 0;

            image.clear(0.5f, 0.5f, 0.5f, hidden ? 0f : 1f);
            tu.spin(spin * delta);
            tu.draw(image);
            /*
            if (mouseCount[LEFT_MOUSE] > 0) {
                if (grabbed == null) {
                    grabbed = cursorPos.clone();
                }
                int[] mx = new int[1];
                int[] my = new int[1];
                glfwGetWindowPos(window, mx, my);
                glfwSetWindowPos(window, mx[0]+(int)Math.round(cursorPos.getX()-grabbed.getX()), my[0]+(int)Math.round(cursorPos.getY()-grabbed.getY()));
            } else {
                grabbed = null;
            }
            */
            glViewport(x, y, (int)(size*ratio), (int)(size*ratio));
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