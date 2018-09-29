package ru.raingo.monkey;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Monkey {

    private Activity activity;
    private Screen currentScreen;

    public Monkey (Activity a){
        this.activity = a;

        // Проверяем поддерживается ли OpenGL ES 2.0.
        if (!supportsEs2()) {
            Log.e("app", "OpenGl ES 2.0 is not supported");
            this.activity.finish();
        }
    }

    void setScreen(int container, Screen s){
        this.activity.setContentView(R.layout.activity_main);
        FragmentTransaction fTrans = this.activity.getFragmentManager().beginTransaction();
        fTrans.replace(container, s);
        fTrans.addToBackStack(null);
        fTrans.commit();
        this.currentScreen = s;
    }

    private boolean supportsEs2() {
        ActivityManager activityManager = (ActivityManager) this.activity.getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        return (configurationInfo.reqGlEsVersion >= 0x20000);
    }

    public Viewport createViewport(int w,int h){
        return new Viewport(w,h);
    }
    public Screen createScreen(Viewport v){
        Screen s = new Screen();
        s.init(v);
        return s;
    }
    public Screen createScreen(){
        Screen s = new Screen();
        s.init();
        return s;
    }
    public Texture createTexture(int p, float zBuffer, float a){
        return new Texture(p, zBuffer, a, currentScreen);
    }


/////////////////////////////
//////ВНУТРЕННИЕ КЛАССЫ//////
/////////////////////////////
    private class GameSurfaceView extends GLSurfaceView {
        public OpenGLRenderer renderer;

        public GameSurfaceView(Context context, Viewport v, GameLoop l) {
            super(context);
            setEGLContextClientVersion(2);
            this.renderer = new OpenGLRenderer(v, l);
            setRenderer(this.renderer);
            //this.setRenderMode(RENDERMODE_WHEN_DIRTY);
        }
    }



    private class OpenGLRenderer implements GLSurfaceView.Renderer {

        public GameLoop loop;
        private Viewport viewport;
        //private Runnable runnable = null;
        //private int i =0;
        private Queue<Runnable> queue = new LinkedList<>();

        public OpenGLRenderer(Viewport v, GameLoop l) {
            //this.context = c;
            //Log.e("app", "context load:");
            //Log.e("app", String.valueOf(this.context));
            this.viewport = v;
            this.loop = l;
        }

        public void setGameLoop(GameLoop l) {
            this.loop = l;
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            //Log.e("app", "onSurfaceCreated");
            /*texture = new Texture(context);
            this.loop = new Loop(){ public void run(){
                texture.draw();
            } };*/
            this.viewport.set(this.viewport.x,this.viewport.y,this.viewport.width,this.viewport.height);

        }

        @Override
        public void onSurfaceChanged(GL10 gl, int w, int h) {
            this.viewport.set(0,0,w,h);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST); // включает использование буфера глубины
            GLES20.glDepthFunc(GLES20.GL_LEQUAL); // определяет работу буфера глубины: более ближние объекты перекрывают дальние
            GLES20.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
            //GLES20.glClearColor(1.f, 1.f, 1.f, 1.0f);
            //GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            //texture.draw();
            //loop.run();
            //GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);

            /*
            i++;
            Log.e("app", "Итерация: " + String.valueOf(i));
            */

            //if(!queue.isEmpty()){
                while (!queue.isEmpty()){
                    queue.poll().run();
                    Log.e("app", "runnable run");
                }
            //}

            loop.run();

        }

        public void act(Runnable runnable){
            queue.add(runnable);
        }

    }












    public interface GameLoop{
        public void run();
    }

    public class Viewport{
        public int width;
        public int height;
        public int x;
        public int y;
        public Viewport(int w,int h){
            this.width = w;
            this.height = h;
        }
        public void set(int x, int y, int w,int h){
            this.width = w;
            this.height = h;
            this.x = x;
            this.y = y;
            GLES20.glViewport(this.x, this.y, this.width, this.height);
        }
    }


    public class Screen extends Fragment {
        public Viewport viewport;
        public GameSurfaceView gameSurfaceView;

        private GameLoop loop;

        public void init(){
            this.loop = new GameLoop(){
                public void run() {
                }
            };
            Display display = activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            this.viewport = new Viewport(size.x, size.y);
            this.gameSurfaceView = new GameSurfaceView(activity, this.viewport, this.loop);
        }

        public void init(Viewport v){
            this.loop = new GameLoop(){
                public void run() {
                }
            };
            this.viewport = v;
            this.gameSurfaceView = new GameSurfaceView(activity, this.viewport, this.loop);
        }

        public void setGameLoop(GameLoop l) {
            this.loop = l;
            this.gameSurfaceView.renderer.setGameLoop(this.loop);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            //TextView textView = new TextView(getActivity());
            //textView.setText(R.string.hello_blank_fragment);
            //return textView;

            //gameSurfaceView = new GLSurfaceView(getActivity());
            /*
            this.gameSurfaceView = new GLSurfaceView(activity);
            this.gameSurfaceView.setEGLContextClientVersion(2);
            //gameSurfaceView.setRenderer(new OpenGLRenderer(getActivity()));
            this.gameSurfaceView.setRenderer(this.renderer);
            */
            return this.gameSurfaceView;
        }
    }















    public class Texture {
        public String name;
        public boolean init;

        private Context context;
        private TextureGL textureGL;

        private TextureDraw textureDraw;

        public int path;   //путь к текстуре
        public float z; //zBuffer
        public float alpha;
        public Bitmap img;

        private Screen screen; //к какому окну принадлежит текстура


        public Texture(int p, float zBuffer, float a, Screen s) {
            init = false;
            this.path = p;
            this.name = activity.getResources().getResourceName(path);
            this.z = zBuffer;
            this.alpha = a;
            this.screen = s;

            textureDraw = new TextureDraw(){ @Override public void run(){}
                @Override public void run(float zBuffer) {}};

            screen.gameSurfaceView.renderer.act(new Runnable(){
                @Override
                public void run() {
                    textureGL = new TextureGL(path, z, alpha, screen);
                    init = true;

                    textureDraw = new TextureDraw(){
                        @Override public void run(){
                            //Log.e("app", "TextureDraw run");
                            textureGL.draw();
                        }
                        @Override
                        public void run(float zBuffer) {
                            textureGL.draw(zBuffer);
                        }
                    };
                }
            });
        }

        public void draw(float zBuffer) {
            textureDraw.run(zBuffer);
            //Log.e("app", name  + " = " + String.valueOf(init));
        }

        public void draw() {
            textureDraw.run();
            //Log.e("app", name  + " = " + String.valueOf(init));
        }

        private abstract class TextureDraw{
            public abstract void run();
            public abstract void run(float zBuffer);
        }
    }


    public class TextureGL{
        public int path;   //путь к текстуре
        public Shader shader;
        public Mesh mesh;
        public float z; //zBuffer
        public float alpha;
        public boolean init;  //проверка загрузки изо
        public Bitmap img;

        private float[] trans = new float[16];
        private float[] MVP = new float[16];
        private Screen screen; //к какому окну принадлежит текстура

        public TextureGL(int p, float zBuffer, float a, Screen s) {
            this.path = p;
            this.z = zBuffer;
            this.alpha = a;
            this.init = false;
            this.screen = s;

            // получение Bitmap
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;

            this.img = BitmapFactory.decodeResource(
                    activity.getResources(), path, options);

            if (this.img == null) {
                Log.e("app", "Error create bitmap!");
            }

            this.shader = new Shader(this);
            this.mesh = new Mesh(this.shader.shaderProgram, img);





            //затем убрать это
            float position[] = {0.0f, 0.5f, 0.0f};
            float rotation[] = {0.0f, 0.0f, 1.0f};
            float scale[] = {0.5f, 0.5f, 1.0f};

            Matrix.setIdentityM(this.trans, 0);

            float angle = 90.0f;

            Matrix.translateM(this.trans, 0, position[0], position[1], position[2]);
            Matrix.rotateM(this.trans, 0, angle, rotation[0], rotation[1], rotation[2]);
            Matrix.scaleM(this.trans, 0, scale[0], scale[1], scale[2]);

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

            Matrix.multiplyMM(this.MVP, 0, CameraMatrix, 0, this.trans, 0);
            Matrix.multiplyMM(this.MVP, 0, projection, 0, this.MVP, 0);
        }

        public void draw(){

            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

            this.shader.use();

            int alphaLoc = GLES20.glGetUniformLocation(this.shader.shaderProgram, "alpha");
            GLES20.glUniform1f(alphaLoc, this.alpha);

            int MVPLoc = GLES20.glGetUniformLocation(this.shader.shaderProgram, "MVP");
            GLES20.glUniformMatrix4fv(MVPLoc, 1, false, this.MVP, 0);

            int zLoc = GLES20.glGetUniformLocation(this.shader.shaderProgram, "z");
            GLES20.glUniform1f(zLoc, this.z);

            this.mesh.draw();

            GLES20.glDisable(GLES20.GL_BLEND);

        }

        public void draw(float zBuffer){
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

            this.shader.use();

            int alphaLoc = GLES20.glGetUniformLocation(this.shader.shaderProgram, "alpha");
            GLES20.glUniform1f(alphaLoc, alpha);

            int MVPLoc = GLES20.glGetUniformLocation(this.shader.shaderProgram, "MVP");
            GLES20.glUniformMatrix4fv(MVPLoc, 1, false, this.MVP, 0);

            int zLoc = GLES20.glGetUniformLocation(this.shader.shaderProgram, "z");
            GLES20.glUniform1f(zLoc, zBuffer);

            this.mesh.draw();

            GLES20.glDisable(GLES20.GL_BLEND);
        }


        ////////////
        //внутряки//
        ////////////
        private class Shader{
            public String vertex;
            public String fragment;
            public int shaderProgram;

            private int vertexShader;
            private int fragmentShader;

            private TextureGL texture;


            public Shader(TextureGL t) {
                this.texture = t;
                this.vertex =
                        "attribute vec3 position;\n"
                        + "attribute vec3 color;\n"
                        + "attribute vec2 texCoord;\n"
                        + "varying vec3 vertexColor;\n"
                        + "varying vec2 TexCoord;\n"
                        + "uniform mat4 MVP;\n"
                        + "uniform float z;\n"
                        + "void main()\n"
                        + "{\n"
                        + "   gl_Position = vec4(position.x, position.y, z, 1.0) * MVP;\n"
                        + "   vertexColor = color;\n"
                        + "   TexCoord = vec2(texCoord.x, 1.0 - texCoord.y);\n"
                        + "}\n";

                this.fragment =
                        "precision mediump float;\n"
                        + "varying vec3 vertexColor;\n"
                        + "varying vec2 TexCoord;\n"
                        + "uniform float alpha;\n"
                        + "uniform sampler2D ourTexture;\n"
                        + "void main()\n"
                        + "{\n"
                        + "    vec4 texColor = texture2D(ourTexture, TexCoord);\n"
                        + "    if(texColor.a < 0.1) discard;\n"
                        + "    texColor.a = alpha;\n"
                        + "    gl_FragColor = texColor;\n"
                        + "}\n";

                this.vertexShader = createShader(GLES20.GL_VERTEX_SHADER, this.vertex);
                this.fragmentShader = createShader(GLES20.GL_FRAGMENT_SHADER, this.fragment);

                this.shaderProgram = GLES20.glCreateProgram();
                GLES20.glAttachShader(this.shaderProgram, this.vertexShader);
                GLES20.glAttachShader(this.shaderProgram, this.fragmentShader);
                GLES20.glLinkProgram(this.shaderProgram);

                //отладка
                int[] linked = new int[1];
                // Check the link status
                GLES20.glGetProgramiv(this.shaderProgram, GLES20.GL_LINK_STATUS, linked, 0);

                if (linked[0] == 0) {
                    Log.e("app", "Error linking program:");
                    Log.e("app", GLES20.glGetProgramInfoLog(this.shaderProgram));
                    GLES20.glDeleteProgram(this.shaderProgram);
                }

                Log.e("app", "Текстура создана!");
            }

            public void use() { GLES20.glUseProgram(this.shaderProgram);}


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


        private class Mesh{
            public float[] vertices;
            public float[] colors;
            public short[] indices;
            public float[] texCoords;

            private int shaderProgram;
            private Bitmap img;


            private FloatBuffer vertexBuffer;
            private FloatBuffer colorBuffer;
            private FloatBuffer textureBuffer;
            private ShortBuffer indexBuffer;

            private int VBO;
            private int colorBO;
            private int texBO;
            private int IBO;
            private int texture;

            public Mesh(int s, Bitmap i) {
                this.shaderProgram = s;
                this.img = i;

                //массив вершин треугольника
                this.vertices = new float[]{
                        0.5f,  0.5f, 0.0f,  // Верхний правый угол
                        0.5f, -0.5f, 0.0f,  // Нижний правый угол
                        -0.5f, -0.5f, 0.0f, // Нижний левый угол
                        -0.5f,  0.5f, 0.0f  // Верхний левый угол
                };
                this.indices = new short[]{
                        0, 1, 3,   // Первый треугольник
                        1, 2, 3    // Второй треугольник
                };
                this.colors = new float[]{
                        1.0f, 0.0f, 0.0f, // Верхний правый угол
                        0.0f, 1.0f, 0.0f, // Нижний правый угол
                        0.0f, 0.0f, 1.0f, // Нижний левый угол
                        1.0f, 0.0f, 1.0f  // Верхний левый угол
                };
                this.texCoords = new float[]{
                        1.0f, 1.0f,     // Верхний правый угол
                        1.0f, 0.0f,     // Нижний правый угол
                        0.0f, 0.0f,     // Нижний левый угол
                        0.0f, 1.0f      // Верхний левый угол
                };



                int[] textures = new int[1];
                GLES20.glGenTextures(1, textures, 0);
                this.texture = textures[0];
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.texture);
                /*GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                img.getWidth(), img.getHeight(), 0, GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, img);*/
                //GLES20.glPixelStorei(GLES20.GL_UNPACK_IMAGE_HEIGHT);
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, this.img, 0);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
                //GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, img, 0);
                //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
                //img.recycle();






                this.vertexBuffer = ByteBuffer.allocateDirect(this.vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
                this.vertexBuffer.put(this.vertices);
                this.vertexBuffer.position(0);

                this.colorBuffer = ByteBuffer.allocateDirect(this.colors.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
                this.colorBuffer.put(this.colors);
                this.colorBuffer.position(0);

                this.textureBuffer = ByteBuffer.allocateDirect(this.colors.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
                this.textureBuffer.put(this.texCoords);
                this.textureBuffer.position(0);

                this.indexBuffer = ByteBuffer.allocateDirect(this.indices.length * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
                this.indexBuffer.put(this.indices);
                this.indexBuffer.position(0);


                int buffers[] = new int[4];
                GLES20.glGenBuffers(4, buffers, 0);
                this.VBO = buffers[0];
                this.colorBO = buffers[1];
                this.texBO = buffers[2];
                this.IBO = buffers[3];

                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, this.VBO);
                GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, this.vertexBuffer.capacity()*4, this.vertexBuffer, GLES20.GL_STATIC_DRAW);

                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, this.colorBO);
                GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, this.colorBuffer.capacity()*4, this.colorBuffer, GLES20.GL_STATIC_DRAW);

                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, this.texBO);
                GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, this.textureBuffer.capacity()*4, this.textureBuffer, GLES20.GL_STATIC_DRAW);

                GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, this.IBO);
                GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity()*2, this.indexBuffer, GLES20.GL_STATIC_DRAW);
            }

            public void draw(){
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, this.VBO);
                int posLoc = GLES20.glGetAttribLocation(this.shaderProgram, "position");
                GLES20.glVertexAttribPointer(posLoc, 3, GLES20.GL_FLOAT, false, 0, 0);
                GLES20.glEnableVertexAttribArray(posLoc);

                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, this.colorBO);
                int colorLoc = GLES20.glGetAttribLocation(this.shaderProgram, "color");
                GLES20.glVertexAttribPointer(colorLoc, 3, GLES20.GL_FLOAT, false, 0, 0);
                GLES20.glEnableVertexAttribArray(colorLoc);

                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, this.texBO);
                int texLoc = GLES20.glGetAttribLocation(this.shaderProgram, "texCoord");
                GLES20.glVertexAttribPointer(texLoc, 2, GLES20.GL_FLOAT, false, 0, 0);
                GLES20.glEnableVertexAttribArray(texLoc);

                //GLES20.glActiveTexture(gl.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.texture);
                //int samplerUniform = gl.glGetUniformLocation(shaderProgram, "ourTexture");
                //gl.glUniform1i(samplerUniform, 0);


                GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, this.IBO);
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, 0);
            }
        }

    }

}
