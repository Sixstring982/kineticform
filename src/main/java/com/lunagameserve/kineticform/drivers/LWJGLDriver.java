package com.lunagameserve.kineticform.drivers;

import com.lunagameserve.kineticform.model.BallGrid;
import com.lunagameserve.kineticform.output.RXTXArduinoHub;
import com.lunagameserve.kineticform.twitter.TweetScanner;
import com.lunagameserve.kineticform.twitter.TwitterCommand;
import com.lunagameserve.kineticform.twitter.TwitterCommandQueue;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.io.IOException;
import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class LWJGLDriver {
    GLFWErrorCallback errorCallback;
    GLFWKeyCallback   keyCallback;
    GLFWFramebufferSizeCallback fbCallback;

    long window;
    public static final int WIDTH = 640;
    public static final int HEIGHT = 480;

    private BallGrid grid = new BallGrid(6, 8);
    private RXTXArduinoHub arduinos = new RXTXArduinoHub();
    private TwitterCommandQueue queue = new TwitterCommandQueue();
    private TwitterCommandQueue arduinoQueue = new TwitterCommandQueue();

    private TweetScanner scanner = new TweetScanner();

    // JOML matrices
    Matrix4f projMatrix = new Matrix4f();
    Matrix4f viewMatrix = new Matrix4f();

    // FloatBuffer for transferring matrices to OpenGL
    FloatBuffer fb = BufferUtils.createFloatBuffer(16);

    void run() {
        try {
            init();
            loop();

            glfwDestroyWindow(window);
            keyCallback.release();
        } finally {
            glfwTerminate();
            errorCallback.release();
            scanner.stop();
            arduinos.stop();
        }
    }

    void init() {
        glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
        if (glfwInit() != GL11.GL_TRUE) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        scanner.start();
        arduinos.connect();

        // Configure our window
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);

        window = glfwCreateWindow(WIDTH, HEIGHT, "Hello World!", NULL, NULL);
        if ( window == NULL ) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key,
                               int scancode, int action, int mods) {
                if (action == GLFW_RELEASE) {
                    switch (key) {
                        case GLFW_KEY_ESCAPE:
                            glfwSetWindowShouldClose(window, GL_TRUE);
                            break;
                        case GLFW_KEY_C:
                            queue.insert(TwitterCommand.CENTER);
                            break;
                        case GLFW_KEY_W:
                            queue.insert(TwitterCommand.WEDGE);
                            break;
                        case GLFW_KEY_R:
                            queue.insert(TwitterCommand.CORNER);
                            break;
                    }
                }

            }
        });

        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window, (vidmode.width() - WIDTH) / 2, (vidmode.height() - HEIGHT) / 2);

        glfwMakeContextCurrent(window);
        glfwSwapInterval(0);
        glfwShowWindow(window);
    }

    void loop() {
        GL.createCapabilities();

        // Set the clear color
        glClearColor(0, 0, 0, 1);
        // Enable depth testing
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);

        glPointSize(3.0f);

        long firstTime = System.nanoTime();
        long lastTime = System.nanoTime();
        while ( glfwWindowShouldClose(window) == GL_FALSE ) {
            // Build time difference between this and first time. 
            long thisTime = System.nanoTime();
            float diff = (thisTime - firstTime) / 1E9f;
            // Compute some rotation angle.
            float angle = diff * 0.25f;

            float delta = (thisTime - lastTime) / 1e9f;
            lastTime = thisTime;

            TwitterCommand command = scanner.getNextCommand();
            if (command != null) {
                queue.insert(command);
                arduinoQueue.insert(command);
            }

            // Make the viewport always fill the whole window.
            glViewport(0, 0, WIDTH, HEIGHT);

            // Build the projection matrix in JOML by computing the vertical
            // field-of-view of based on the distance between viewer and screen
            // and the screen size and compute the correct aspect ratio based
            // on window width and height. Make sure to cast them to float
            // before dividing, or else we would do an integer division!
            projMatrix.setPerspective((float) Math.atan((WIDTH * HEIGHT / HEIGHT) / 1.0),
                    (float)WIDTH/HEIGHT, 0.01f, 100.0f)
                    .get(fb);
            glMatrixMode(GL_PROJECTION);
            glLoadMatrixf(fb);

            // Build a model-view matrix which first rotates the cube
            // about the Y-axis and then lets a "camera" look at that
            // cube from a certain distance.
            viewMatrix.setLookAt((float)(3.5 * Math.sin(angle)), 3.5f,
                                 (float)(3.5 * Math.cos(angle)),
                                 0.0f, 2.5f, 0.0f,
                                 0.0f, 1.0f, 0.0f)
                    // rotate 90 degrees per second
                    .get(fb);
            glMatrixMode(GL_MODELVIEW);
            glLoadMatrixf(fb);

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            try {
                arduinos.update(arduinoQueue);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            grid.update(queue, delta);
            grid.gl(-1.5f, 4.5f, -2.0f, 0.5f, 0.5f, 0.5f);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new LWJGLDriver().run();
    }
}