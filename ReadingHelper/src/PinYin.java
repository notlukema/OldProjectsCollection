import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.stb.*;
import org.lwjgl.system.MemoryStack;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static net.sourceforge.pinyin4j.PinyinHelper.*;
import static net.sourceforge.pinyin4j.format.HanyuPinyinToneType.WITH_TONE_MARK;
import static net.sourceforge.pinyin4j.format.HanyuPinyinVCharType.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWNativeWin32.glfwGetWin32Window;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.windows.User32.*;

public class PinYin {

    private static float scroll;
    private static int size;

    private static String clipboard;
    private static String pinyin;
    private static HanyuPinyinOutputFormat format;

    private static final int BMSIZE = 2048;

    public static boolean isDecorated(long context) {
        return glfwGetWindowAttrib(context, GLFW_DECORATED) == GLFW_TRUE;
    }

    public static void decorated(long context, boolean decorated) {
        glfwSetWindowAttrib(context, GLFW_DECORATED, decorated ? GLFW_TRUE : GLFW_FALSE);
    }

    private static void vertex(float x, float y, float width, float height) {
        glVertex2f((x-width/2f)*2/width, (y-height/2f)*2/height);
    }

    private static void rect(float x1, float y1, float x2, float y2) {
        glBegin(GL_QUADS);
        glVertex2f(x1, y1);
        glVertex2f(x2, y1);
        glVertex2f(x2, y2);
        glVertex2f(x1, y2);
        glEnd();
    }

    private static String toLen(String str, int len) {
        return " ".repeat(Math.max(0, len-str.length())) + str;
    }

    public static void main(String[] args) {
        if (!glfwInit()) {
            System.err.println("Failed to initialize GLFW");
            System.exit(1);
        }
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_TRUE);
        glfwWindowHint(GLFW_FLOATING, GLFW_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        long context = glfwCreateWindow(50, 50, "", 0, 0);
        glfwMakeContextCurrent(context);
        GL.createCapabilities();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer buffer = stack.mallocInt(1);
            SetWindowLongPtr(buffer, glfwGetWin32Window(context), GWL_EXSTYLE, WS_EX_TOOLWINDOW);
        }

        int texture = glGenTextures();
        glActiveTexture(GL_TEXTURE0);
        STBTTFontinfo info = STBTTFontinfo.malloc();
        STBTTBakedChar.Buffer charData = STBTTBakedChar.malloc(334);
        ByteBuffer bitmap = BufferUtils.createByteBuffer(BMSIZE * BMSIZE);
        float baseline = 0;
        float whitespace = 0;
        try {
            MemoryStack stack = MemoryStack.stackPush();

            byte[] bytes = PinYin.class.getResourceAsStream("Helvetica.ttf").readAllBytes();
            ByteBuffer font = BufferUtils.createByteBuffer(bytes.length);
            font.put(bytes);
            font.flip();

            STBTruetype.stbtt_InitFont(info, font);
            stbtt_BakeFontBitmap(font, 128, bitmap, BMSIZE, BMSIZE, 32, charData);

            float scale = stbtt_ScaleForPixelHeight(info, 128);
            IntBuffer ascent = BufferUtils.createIntBuffer(1);
            IntBuffer descent = BufferUtils.createIntBuffer(1);
            IntBuffer lineGap = BufferUtils.createIntBuffer(1);
            stbtt_GetFontVMetrics(info, ascent, descent, lineGap);
            baseline = ascent.get(0) * scale;
            IntBuffer advance = BufferUtils.createIntBuffer(1);
            STBTruetype.stbtt_GetCodepointHMetrics(info, 32, advance, BufferUtils.createIntBuffer(1));
            whitespace = advance.get(0) * scale;

            glBindTexture(GL_TEXTURE_2D, texture);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, BMSIZE,BMSIZE, 0, GL_ALPHA, GL_UNSIGNED_BYTE, bitmap);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glBindTexture(GL_TEXTURE_2D, 0);

            stack.close();
        } catch (Exception e) {
            System.err.println("Failed to initialize font:");
            e.printStackTrace();
            System.exit(1);
        }

        glfwShowWindow(context);

        size = 50;
        scroll = 0;

        clipboard = "";
        pinyin = "";
        format = new HanyuPinyinOutputFormat();
        format.setToneType(WITH_TONE_MARK);
        format.setVCharType(WITH_U_UNICODE);

        glfwSetCursorPosCallback(context, new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double x, double y) {
                // Nothing
            }
        });
        glfwSetMouseButtonCallback(context, new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                if (button == GLFW_MOUSE_BUTTON_LEFT) {
                    if (action == GLFW_PRESS) {
                        // Nothing
                    }
                    if (action == GLFW_RELEASE) {
                        // Nothing
                    }
                }
            }
        });
        glfwSetScrollCallback(context, new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xoff, double yoff) {
                scroll = (float)yoff;
            }
        });
        glfwSetKeyCallback(context, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (action == GLFW_RELEASE) {
                    if (key == GLFW_KEY_ESCAPE) {
                        decorated(context, !isDecorated(context));
                    }
                }
            }
        });

        long next = System.nanoTime() + 1000000000/60;
        int width = size;
        int lastSize = size;
        while (!glfwWindowShouldClose(context)) {
            if (System.nanoTime() < next) {
                continue;
            } else {
                next = System.nanoTime() + 1000000000/60;
            }
            glfwPollEvents();

            size += (int)(scroll * 10);
            if (size < 40) {
                size = 40;
            }
            if (size > 150) {
                size = 150;
            }
            scroll = 0;

            boolean remeasure = false;
            if (size != lastSize) {
                lastSize = size;
                remeasure = true;
            }
            float scale = (size * 0.8f) / 128f;
            try {
                String data = (String)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
                if (!data.equals(clipboard)) {
                    StringBuilder sb = new StringBuilder();
                    for (int i=0;i<data.length();i++) {
                        String[] arr = toHanyuPinyinStringArray(data.charAt(i), format);
                        if (arr.length > 0) {
                            int j=0;
                            for (;j<arr.length-1;j++) {
                                sb.append(arr[j]).append('/');
                            }
                            sb.append(arr[j]);
                        }
                        if (i < data.length()-1) {
                            sb.append(' ');
                        }
                    }
                    pinyin = sb.toString();
                    clipboard = data;
                    remeasure = true;
                }
            } catch (Exception e) {
                pinyin = "";
                clipboard = "";
                if (e instanceof net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination) {
                    remeasure = true;
                }
            }

            if (remeasure) {
                float x = 40 * scale;
                for (int i=0;i<pinyin.length();i++) {
                    char c = pinyin.charAt(i);
                    if (c == ' ') {
                        x += whitespace * scale;
                        continue;
                    }
                    STBTTAlignedQuad q = STBTTAlignedQuad.malloc();
                    float[] advance = new float[1];
                    stbtt_GetBakedQuad(charData, BMSIZE, BMSIZE, c-32, advance, new float[1], q, true);
                    x += advance[0] * scale;
                }
                width = Math.max(size, (int)x);
            }

            glClearColor(0.8f, 0.8f, 0.8f, 1f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glfwSetWindowSize(context, width, size);
            glViewport(0, 0, width, size);
            glEnable(GL_DEPTH_TEST);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE);
            /*
            glEnable(GL_TEXTURE_2D);
            glBindTexture(GL_TEXTURE_2D, texture);
            glBegin(GL_QUADS);
            glColor4f(0f, 0f, 0f, 1f);
            glVertex2f(-0.8f, 0.8f);
            glTexCoord2f(0,1);
            glVertex2f(-0.8f, -0.8f);
            glTexCoord2f(1,1);
            glVertex2f(0.8f, -0.8f);
            glTexCoord2f(1,0);
            glVertex2f(0.8f, 0.8f);
            glTexCoord2f(0,0);
            glDisable(GL_TEXTURE_2D);
            */
            glEnable(GL_TEXTURE_2D);
            glBindTexture(GL_TEXTURE_2D, texture);
            glBegin(GL_QUADS);
            glColor4f(0f, 0f, 0f, 1f);
            float x = 20 * scale;
            float y = (baseline - 60) * scale;
            for (int i=0;i<pinyin.length();i++) {
                char c = pinyin.charAt(i);
                if (c == ' ') {
                    x += whitespace * scale;
                    continue;
                }
                STBTTAlignedQuad q = STBTTAlignedQuad.malloc();
                float[] advance = new float[1];
                stbtt_GetBakedQuad(charData, BMSIZE, BMSIZE, c-32, advance, new float[1], q, true);
                float dx = (q.x1()-q.x0()) * scale;
                float dy = (q.y1()-q.y0()) * scale;
                float tx = x + q.x0() * scale;
                float ty = y - q.y0() * scale;
                glTexCoord2f(q.s0(),q.t1());
                vertex(tx, ty-dy, width, size);
                glTexCoord2f(q.s1(),q.t1());
                vertex(tx+dx, ty-dy, width, size);
                glTexCoord2f(q.s1(),q.t0());
                vertex(tx+dx, ty, width, size);
                glTexCoord2f(q.s0(),q.t0());
                vertex(tx, ty, width, size);
                x += advance[0] * scale;
            }
            glDisable(GL_TEXTURE_2D);

            glColor4f(0.55f, 0.55f, 0.55f, 1f);
            rect(-1, -1, 1, -0.97f);
            rect(1, 1, 0.97f, -1);
            glColor4f(0.7f, 0.7f, 0.7f, 1f);
            rect(-1, 1, -0.98f, -1);
            rect(-1, 1, 1, 0.98f);

            glFinish();

            glfwSwapBuffers(context);
        }

        glfwTerminate();
        System.exit(0);
    }

}