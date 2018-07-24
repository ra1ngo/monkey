package ru.raingo.monkey;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.util.Log;

class OpenGLRenderer implements GLSurfaceView.Renderer {

    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private ShortBuffer indexBuffer;
    private int shaderProgram;
    private int VBO;
    private int colorBO;
    private int IBO;

    @Override
    public void onDrawFrame(GL10 arg0) {
        GLES20.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);	// включает использование буфера глубины
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);	// определяет работу буфера глубины: более ближние объекты перекрывают дальние


        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        int alphaLocation = GLES20.glGetUniformLocation(shaderProgram, "alpha");
        GLES20.glUseProgram(shaderProgram);
        GLES20.glUniform1f(alphaLocation, 0.2f);


        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, VBO);
        int posLoc = GLES20.glGetAttribLocation(shaderProgram, "position");
        GLES20.glVertexAttribPointer(posLoc, 3, GLES20.GL_FLOAT, false, 0, 0);
        GLES20.glEnableVertexAttribArray(posLoc);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, colorBO);
        int colorLoc = GLES20.glGetAttribLocation(shaderProgram, "color");
        GLES20.glVertexAttribPointer(colorLoc, 3, GLES20.GL_FLOAT, false, 0, 0);
        GLES20.glEnableVertexAttribArray(colorLoc);


        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, IBO);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, 0);

        GLES20.glDisable(GLES20.GL_BLEND);
    }

    @Override
    public void onSurfaceChanged(GL10 arg0, int w, int h) {
        GLES20.glViewport(0, 0, w, h);
    }

    @Override
    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {


        String shader_vertex_source =
                  "attribute vec3 position;\n"
                + "attribute vec3 color;\n"
                + "varying vec3 vertexColor;\n"
                + "void main()\n"
                + "{\n"
                + "   gl_Position = vec4(position, 1.0);\n"
                + "   vertexColor = color;\n"
                + "}\n";

        String shader_fragment_source =
                  "precision mediump float;\n"
                + "varying vec3 vertexColor;\n"
                + "uniform float alpha;\n"
                + "void main()\n"
                + "{\n"
                + "   gl_FragColor=vec4(vertexColor, alpha);\n"
                + "}\n";

        int vertexShader = createShader(GLES20.GL_VERTEX_SHADER, shader_vertex_source);
        int fragmentShader = createShader(GLES20.GL_FRAGMENT_SHADER, shader_fragment_source);

        shaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(shaderProgram, vertexShader);
        GLES20.glAttachShader(shaderProgram, fragmentShader);
        GLES20.glLinkProgram(shaderProgram);

        //отладка
        int[] linked = new int[1];
        // Check the link status
        GLES20.glGetProgramiv(shaderProgram, GLES20.GL_LINK_STATUS, linked, 0);

        if (linked[0] == 0) {
            Log.e("app", "Error linking program:");
            Log.e("app", GLES20.glGetProgramInfoLog(shaderProgram));
            GLES20.glDeleteProgram(shaderProgram);
            return;
        }

        ////////////////

        //массив вершин треугольника
        float vertices[] = {
                0.5f,  0.5f, 0.0f, // Верхний правый угол
                0.5f, -0.5f, 0.0f, // Нижний правый угол
                -0.5f, -0.5f, 0.0f, // Нижний левый угол
                -0.5f,  0.5f, 0.0f  // Верхний левый угол
        };
        short indices[] = {
                0, 1, 3,   // Первый треугольник
                1, 2, 3    // Второй треугольник
        };
        float colors[] = {
                1.0f, 0.0f, 0.0f, // Верхний правый угол
                0.0f, 1.0f, 0.0f, // Нижний правый угол
                0.0f, 0.0f, 1.0f, // Нижний левый угол
                1.0f, 0.0f, 1.0f // Верхний левый угол
        };

        vertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        colorBuffer = ByteBuffer.allocateDirect(colors.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        colorBuffer.put(colors);
        colorBuffer.position(0);

        indexBuffer = ByteBuffer.allocateDirect(indices.length * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
        indexBuffer.put(indices);
        indexBuffer.position(0);


        int buffers[] = new int[3];
        GLES20.glGenBuffers(3, buffers, 0);
        VBO = buffers[0];
        colorBO = buffers[1];
        IBO = buffers[2];

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, VBO);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBuffer.capacity()*4, vertexBuffer, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, colorBO);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, colorBuffer.capacity()*4, colorBuffer, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, IBO);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity()*2, indexBuffer, GLES20.GL_STATIC_DRAW);

        Log.e("app", "Понеслась!");
    }

    private int createShader(int type, String source) {
        // Create the shader object
        int shader = GLES20.glCreateShader(type);

        // Load the shader source
        GLES20.glShaderSource(shader, source);
        // Compile the shader
        GLES20.glCompileShader(shader);

        //отладка
        int[] compiled = new int[1];
        // Check the compile status
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);

        if (compiled[0] == 0) {
            Log.e("app", "Error create shader:");
            Log.e("app", GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            return 0;
        }

        return shader;
    }
}
