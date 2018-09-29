package ru.raingo.architecture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class GameSurfaceView extends GLSurfaceView {

    public OpenGLRenderer renderer;

    public GameSurfaceView(Context context) {
        super(context);

        setEGLContextClientVersion(2);
        this.renderer = new OpenGLRenderer(context);
        setRenderer(this.renderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    public GameSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }





    public void setLoop (Loop loop){
        renderer.loop = loop;
    }










    private class OpenGLRenderer implements GLSurfaceView.Renderer {

        private Context context;
        public Loop loop;
        //private Texture texture;
        public int shaderProgram;
        public FloatBuffer vertexBuffer;
        private Runnable runnable = null;
        private int i =0;


        public OpenGLRenderer(Context context) {

            this.context = context;
            this.loop = new Loop(){ public void run(){Log.e("app", "OpenGLRenderer loop init");} };
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            //Log.e("app", "onSurfaceCreated");
            /*texture = new Texture(context);
            this.loop = new Loop(){ public void run(){
                texture.draw();
            } };*/
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {

        }

        @Override
        public void onDrawFrame(GL10 gl) {
            //GLES20.glEnable(GLES20.GL_DEPTH_TEST); // включает использование буфера глубины
            //GLES20.glDepthFunc(GLES20.GL_LEQUAL); // определяет работу буфера глубины: более ближние объекты перекрывают дальние
            GLES20.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
            //GLES20.glClearColor(1.f, 1.f, 1.f, 1.0f);
            //GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            //texture.draw();
            //loop.run();
            //GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);

            i++;
            Log.e("app", "Итерация: " + String.valueOf(i));

            if(runnable!=null){
                runnable.run();
                runnable = null;
            }

            loop.run();

        }

        public void act(Runnable runnable){
            this.runnable = runnable;
        }

    }








    public interface Loop{
        public void run();
    }





    public class Texture {
        public String name;
        public boolean init;

        private Context context;
        private TextureGL textureGL;

        private TextureDraw textureDraw;


        public Texture(Context context) {
            this.context = context;
            init = false;
            name = "Irij";

            this.textureDraw = new TextureDraw(){ public void run(){
                Log.e("app", "TextureDraw init");
            } };

            renderer.act(new Runnable(){
                @Override
                public void run() {
                    textureGL = new TextureGL(getContext());
                    init = true;

                    textureDraw = new TextureDraw(){ public void run(){
                        textureGL.draw();
                    } };
                }
            });
        }

        public void draw() {
            textureDraw.run();
        }

        private abstract class TextureDraw{
            public abstract void run();
        }
    }

    public Texture createTexture(){
        return new Texture(getContext());
    }



    public class TextureGL{

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
        private int textureBO;
        private float[] trans = new float[16];
        private float[] MVP = new float[16];


        public TextureGL(Context context) {
            this.context = context;

            String shader_vertex_source =
                    "attribute vec3 position;\n"
                            + "attribute vec3 color;\n"
                            + "attribute vec2 texCoord;\n"
                            + "varying vec3 vertexColor;\n"
                            + "varying vec2 TexCoord;\n"
                            + "uniform mat4 MVP;\n"
                            + "void main()\n"
                            + "{\n"
                            + "   gl_Position = MVP * vec4(position, 1.0);\n"
                            + "   vertexColor = color;\n"
                            + "   TexCoord = vec2(texCoord.x, 1.0 - texCoord.y);\n"
                            + "}\n";

            String shader_fragment_source =
                    "precision mediump float;\n"
                            + "varying vec3 vertexColor;\n"
                            + "varying vec2 TexCoord;\n"
                            + "uniform float alpha;\n"
                            + "uniform sampler2D ourTexture;\n"
                            + "void main()\n"
                            + "{\n"
                            + "   gl_FragColor = texture2D(ourTexture, TexCoord);\n"
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


            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);
            textureBO = textures[0];
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureBO);
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









            float position[] = {0.0f, 0.5f, 0.0f};
            float rotation[] = {0.0f, 0.0f, 1.0f};
            float scale[] = {0.5f, 0.5f, 1.0f};

            Matrix.setIdentityM(trans, 0);

            float angle = 90.0f;

            Matrix.translateM(trans, 0, position[0], position[1], position[2]);
            Matrix.rotateM(trans, 0, angle, rotation[0], rotation[1], rotation[2]);
            Matrix.scaleM(trans, 0, scale[0], scale[1], scale[2]);

            //без декомпозиции
            //ибо математических библиотек для opengl нет



            float zoom = 1.f;

            float cameraPosition[] = {0.f, 0.5f, 10.f};
            float cameraTarget[] = {0.0f, 0.5f, 0.0f};
            float upVector[] = {0.0f, 1.0f, 0.0f};

            float projection[] = new float[16];
            float CameraMatrix[] = new float[16];

            Matrix.orthoM(projection, 0,
                    -1.f * zoom, 1.f * zoom,
                    -1.f * zoom, 1.f * zoom,
                    0.1f, 20.f
            );


            Matrix.setLookAtM(CameraMatrix, 0,
                    cameraPosition[0], cameraPosition[1], cameraPosition[2],
                    cameraTarget[0], cameraTarget[1], cameraTarget[2],
                    upVector[0], upVector[1], upVector[2]
            );

            zoom = 0.75f;
            Matrix.orthoM(projection, 0,
                    -1.f * zoom, 1.f * zoom,
                    -1.f * zoom, 1.f * zoom,
                    0.1f, 20.f
            );

            Matrix.multiplyMM(MVP, 0, CameraMatrix, 0, trans, 0);
            Matrix.multiplyMM(MVP, 0, projection, 0, MVP, 0);

            Log.e("app", "Текстура создана!");
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



        public void draw() {
            Log.e("app", "draw Texture");
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

            GLES20.glUseProgram(shaderProgram);

            int alphaLoc = GLES20.glGetUniformLocation(shaderProgram, "alpha");
            GLES20.glUniform1f(alphaLoc, 0.2f);

            int MVPLoc = GLES20.glGetUniformLocation(shaderProgram, "MVP");
            GLES20.glUniformMatrix4fv(MVPLoc, 1, false, MVP, 0);


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

            //GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureBO);
            //int samplerUniform = GLES20.glGetUniformLocation(shaderProgram, "ourTexture");
            //GLES20.glUniform1i(samplerUniform, 0);


            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, IBO);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, 0);

            GLES20.glDisable(GLES20.GL_BLEND);
        }
    }
}
