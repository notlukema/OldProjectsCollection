package UseShaders;

import math.Vector2f;
import math.Vector2i;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Scanner;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWNativeWin32.glfwGetWin32Window;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.windows.User32.*;

public class RealMain {

    public static final int LEFT_MOUSE = 0,
            RIGHT_MOUSE = 1,
            MIDDLE_MOUSE = 2;

    public static final int POSITION = 0,
            UV = 1;

    public static final int SPEED = 0,
            SIZE = 1;

    public static Vector2f cursorPos;
    public static boolean[] mouse;
    public static int[] mouseCount;
    public static float scroll;
    public static float dir, spin;

    public static Vector2f grabbed;
    public static Vector2i lastSize;

    public static int scrollType;

    public static void hide(long window) {
        glfwSetWindowAttrib(window, GLFW_DECORATED, GLFW_FALSE);
        glfwSetWindowAttrib(window, GLFW_RESIZABLE, GLFW_FALSE);
    }

    public static void show(long window) {
        glfwSetWindowAttrib(window, GLFW_DECORATED, GLFW_TRUE);
        glfwSetWindowAttrib(window, GLFW_RESIZABLE, GLFW_TRUE);
    }

    public static void main(String[] args) {
        glfwInit();

        int size = 800;
        int extra = 25;
        dir = 0;
        spin = 180f;
        grabbed = null;
        scrollType = SPEED;

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_TRUE);
        glfwWindowHint(GLFW_FLOATING, GLFW_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        long window = glfwCreateWindow((size + extra)/2, (size + extra)/2, "", 0, 0);
        glfwMakeContextCurrent(window);
        createCapabilities();
        recordSize(window);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer buffer = stack.mallocInt(1);
            SetWindowLongPtr(buffer, glfwGetWin32Window(window), GWL_EXSTYLE, WS_EX_TOOLWINDOW);
        }
        glfwSetCursor(window, glfwCreateStandardCursor(GLFW_HAND_CURSOR));

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

                if (grabbed != null) {
                    int[] mx = new int[1];
                    int[] my = new int[1];
                    glfwGetWindowPos(window, mx, my);
                    glfwSetWindowPos(window, mx[0] - (int)(grabbed.getX() - cursorPos.getX()),
                            my[0] - (int)(grabbed.getY() - cursorPos.getY()));
                }
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
                    if (key == GLFW_KEY_1) {
                        scrollType = SPEED;
                    }
                    if (key == GLFW_KEY_2) {
                        scrollType = SIZE;
                    }
                    if (key == GLFW_KEY_MINUS) {
                        if (scrollType == SPEED) {
                            changeWindowSize(window, -30);
                        }
                        if (scrollType == SIZE) {
                            spin -= 20;
                        }
                    }
                    if (key == GLFW_KEY_EQUAL) {
                        if (scrollType == SPEED) {
                            changeWindowSize(window, 30);
                        }
                        if (scrollType == SIZE) {
                            spin += 20;
                        }
                    }
                }
            }
        });
        glfwSetWindowSizeCallback(window, new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                if (width != lastSize.getX()) {
                    int[] s = new int[1];
                    glfwGetWindowSize(window, s, new int[1]);
                    glfwSetWindowSize(window, s[0], s[0]);
                } else if (height != lastSize.getY()) {
                    int[] s = new int[1];
                    glfwGetWindowSize(window, new int[1], s);
                    glfwSetWindowSize(window, s[0], s[0]);
                }

                recordSize(window);
            }
        });

        int vao = glGenVertexArrays();
        glBindVertexArray(vao);
        int[] vbo = new int[3];
        float[] pos = new float[] {
                -1, -1,
                -1, 1,
                1, 1,
                1, -1
        };
        float[] uv = new float[] {
                0, 0,
                1, 0,
                1, 1,
                0, 1
        };
        int[] indices = new int[] {
                0, 1, 2,
                0, 2, 3
        };
        vbo[0] = storeData(POSITION, 2, pos);
        vbo[1] = storeData(UV, 2, uv);
        vbo[2] = bindIndices(indices);

        int program = glCreateProgram();
        attachShader(program, loadFile("yinyang.vs"), GL_VERTEX_SHADER);
        attachShader(program, loadFile("yinyang.fs"), GL_FRAGMENT_SHADER);
        glLinkProgram(program);

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

            boolean hidden = glfwGetWindowAttrib(window, GLFW_DECORATED) == GLFW_FALSE;

            float c = hidden ? 0f : 0.5f;
            glClearColor(c, c, c, hidden ? 0f : 1f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            int[] w = new int[1];
            int[] h = new int[1];
            glfwGetWindowSize(window, w, h);

            float ratio = Math.min((float)(w[0] - extra)/size, (float)(h[0] - extra)/size);
            int x = (w[0] - (int)(size * ratio)) / 2;
            int y = (h[0] - (int)(size * ratio)) / 2;

            //float cx = (cursorPos.getX() - x) / ratio;
            //float cy = size - (cursorPos.getY() - y) / ratio;
            float delta = (time - last) / 1000000000f;

            if (mouse[LEFT_MOUSE]) {
                if (grabbed == null) {
                    grabbed = cursorPos.clone();
                }
            } else {
                grabbed = null;
            }

            if (scrollType == SPEED) {
                spin += scroll * 10;
            }
            if (scrollType == SIZE) {
                changeWindowSize(window, (int)(scroll * 15));
            }
            scroll = 0;
            dir += spin * delta;

            glViewport(x, y, (int)(size*ratio), (int)(size*ratio));
            glEnable(GL_TEXTURE_2D);
            glEnable(GL_DEPTH_TEST);
            glDepthFunc(GL_LESS);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

            glUseProgram(program);

            uniformFloat(program, (float)Math.sin(Math.toRadians(dir)), "sin");
            uniformFloat(program, (float)Math.cos(Math.toRadians(dir)), "cos");

            glBindVertexArray(vao);
            glEnableVertexAttribArray(POSITION);
            glEnableVertexAttribArray(UV);

            glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);

            glfwSwapBuffers(window);

            count++;
            last = time;
            time = System.nanoTime();
            if (time > fpstime + 1000000000 * wait) {
                fps = count / wait;
                System.out.println("fps: " + fps);
                count = 0;
                fpstime = time;
            }
        }
    }

    private static void changeWindowSize(long window, int change) {
        int[] s = new int[1];
        glfwGetWindowSize(window, s, new int[1]);
        glfwSetWindowSize(window, s[0] + change, s[0] + change);
        recordSize(window);

        int[] mx = new int[1];
        int[] my = new int[1];
        glfwGetWindowPos(window, mx, my);
        glfwSetWindowPos(window, mx[0] - change/2, my[0] - change/2);
    }

    private static void recordSize(long window) {
        int[] w = new int[1];
        int[] h = new int[1];
        glfwGetWindowSize(window, w, h);
        lastSize = new Vector2i(w[0], h[0]);
    }

    private static String loadFile(String name) {
        Scanner s = new Scanner(RealMain.class.getResourceAsStream(name)).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private static int storeData(int attribute, int dimensions, float[] data) {
        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length).put(data).flip();
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        glVertexAttribPointer(attribute, dimensions, GL_FLOAT, false, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        return vbo;
    }

    private static int bindIndices(int[] data) {
        int vbo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo);
        IntBuffer buffer = BufferUtils.createIntBuffer(data.length).put(data).flip();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        return vbo;
    }

    private static void attachShader(int program, String shader, int type) {
        int id = glCreateShader(type);
        glShaderSource(id, shader);
        glCompileShader(id);
        if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Couldn't compile shader:\n" + glGetShaderInfoLog(id, glGetShaderi(id, GL_INFO_LOG_LENGTH)));
        }
        glAttachShader(program, id);
    }

    public static void uniformFloat(int program, float f, String name) {
        int uniform = glGetUniformLocation(program, name);
        glUniform1f(uniform, f);
    }

}