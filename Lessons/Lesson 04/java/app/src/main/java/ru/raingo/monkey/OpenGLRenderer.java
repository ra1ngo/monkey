package ru.raingo.monkey;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.opengl.GLUtils;
import android.util.Log;

class OpenGLRenderer implements GLSurfaceView.Renderer {

    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private FloatBuffer textureBuffer;
    private ShortBuffer indexBuffer;
    private int shaderProgram;
    private int VBO;
    private int colorBO;
    private int texBO;
    private int IBO;

    private Context context;
    private int texture;

    public OpenGLRenderer(Context context) {
        this.context = context;
    }

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

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, texBO);
        int texLoc = GLES20.glGetAttribLocation(shaderProgram, "texCoord");
        GLES20.glVertexAttribPointer(texLoc, 2, GLES20.GL_FLOAT, false, 0, 0);
        GLES20.glEnableVertexAttribArray(texLoc);
/*
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindBuffer(GLES20.GL_TEXTURE_2D, texture);
        int samplerUniform = GLES20.glGetUniformLocation(shaderProgram, "ourTexture");
        GLES20.glUniform1i(samplerUniform, 0);
*/

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
                + "attribute vec2 texCoord;\n"
                + "varying vec3 vertexColor;\n"
                + "varying vec2 TexCoord;\n"
                + "void main()\n"
                + "{\n"
                + "   gl_Position = vec4(position, 1.0);\n"
                + "   vertexColor = color;\n"
                + "   TexCoord = texCoord;\n"
                + "}\n";

        String shader_fragment_source =
                  "precision mediump float;\n"
                + "varying vec3 vertexColor;\n"
                + "varying vec2 TexCoord;\n"
                + "uniform float alpha;\n"
                + "uniform sampler2D ourTexture;\n"
                + "void main()\n"
                + "{\n"
                + "   gl_FragColor = texture2D(ourTexture, vec2(TexCoord.x, 1.0-TexCoord.y));\n"
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
                0.5f,  0.5f, 0.0f,  // Верхний правый угол
                0.5f, -0.5f, 0.0f,  // Нижний правый угол
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
                1.0f, 0.0f, 1.0f  // Верхний левый угол
        };
        float texCoords[] = {
                1.0f, 1.0f,     // Верхний правый угол
                1.0f, 0.0f,     // Нижний правый угол
                0.0f, 0.0f,     // Нижний левый угол
                0.0f, 1.0f      // Верхний левый угол
        };

        // получение Bitmap
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        Bitmap img = BitmapFactory.decodeResource(
                this.context.getResources(), R.drawable.elli_walk, options);

        if (img == null) {
            Log.e("app", "Error create bitmap!");
        }


        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
        /*GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                img.getWidth(), img.getHeight(), 0, GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, img);*/
        //GLES20.glPixelStorei(GLES20.GL_UNPACK_IMAGE_HEIGHT);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, img, 0);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        //GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, img, 0);
        //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        //img.recycle();






        vertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        colorBuffer = ByteBuffer.allocateDirect(colors.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        colorBuffer.put(colors);
        colorBuffer.position(0);

        textureBuffer = ByteBuffer.allocateDirect(colors.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        textureBuffer.put(texCoords);
        textureBuffer.position(0);

        indexBuffer = ByteBuffer.allocateDirect(indices.length * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
        indexBuffer.put(indices);
        indexBuffer.position(0);


        int buffers[] = new int[4];
        GLES20.glGenBuffers(4, buffers, 0);
        VBO = buffers[0];
        colorBO = buffers[1];
        texBO = buffers[2];
        IBO = buffers[3];

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, VBO);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBuffer.capacity()*4, vertexBuffer, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, colorBO);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, colorBuffer.capacity()*4, colorBuffer, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, texBO);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, textureBuffer.capacity()*4, textureBuffer, GLES20.GL_STATIC_DRAW);

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
