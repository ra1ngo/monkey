package ru.raingo.monkey;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.util.Log;

class OpenGLRenderer implements GLSurfaceView.Renderer {

    private FloatBuffer vertexBuffer;
    private int shaderProgram;

    @Override
    public void onDrawFrame(GL10 arg0) {
        GLES20.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
    }

    @Override
    public void onSurfaceChanged(GL10 arg0, int w, int h) {
        GLES20.glViewport(0, 0, w, h);
    }

    @Override
    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {


        String shader_vertex_source =
                  "attribute vec4 vPosition;\n"
                + "void main()\n"
                + "{\n"
                + "   gl_Position = vPosition;\n"
                + "}\n";

        String shader_fragment_source =
                  "precision mediump float;\n"
                + "uniform vec4 vColor;\n"
                + "void main()\n"
                + "{\n"
                + "   gl_FragColor=vColor;\n"
                + "}\n";

        int vertexShader = createShader(GLES20.GL_VERTEX_SHADER, shader_vertex_source);
        int fragmentShader = createShader(GLES20.GL_FRAGMENT_SHADER, shader_fragment_source);

        shaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(shaderProgram, vertexShader);
        GLES20.glAttachShader(shaderProgram, fragmentShader);

        //GLES20.glBindAttribLocation(shaderProgram, 0, "vPosition");
        //GLES20.glBindAttribLocation(shaderProgram, 0, "vColor");

        GLES20.glLinkProgram(shaderProgram);

        GLES20.glUseProgram(shaderProgram);

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
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f,
                0.0f,  0.5f, 0.0f
        };
/*
        // Инициализируем буфер для координат фигуры
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (количество координат * 4 байта для float)
                vertices.length * 4);
        // используем порядок по умолчанию
        bb.order(ByteOrder.nativeOrder());

        // создаем буфер с плавающей точкой из ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
*/
        vertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        //int[] vbo = new int[1];
        //GLES20.glGenBuffers(1, vbo, 0);
        //GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);
        //GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBuffer.capacity(), vertexBuffer, GLES20.GL_STATIC_DRAW);


        ///////////////////////
        // Разрешаем обработку вершин треугольника
        GLES20.glEnableVertexAttribArray(0);

        // Подготавливаем данные о координатах треугольника
        GLES20.glVertexAttribPointer(
                0,                  // Атрибут 0. Подробнее об этом будет рассказано в части, посвященной шейдерам.
                3,                  // Размер
                GLES20.GL_FLOAT,    // Тип
                false,              // Указывает, что значения не нормализованы
                0,                  // Шаг
                vertexBuffer                   // Смещение массива в буфере
        );

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
