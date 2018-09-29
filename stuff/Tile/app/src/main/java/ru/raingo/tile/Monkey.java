package ru.raingo.tile;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Monkey {
    private Activity activity;

    public Monkey (Activity a){
        this.activity = a;
        // Проверяем поддерживается ли OpenGL ES 2.0.
        if (!supportsEs2()) {
            Log.e("app", "OpenGl ES 2.0 is not supported");
            this.activity.finish();
        }
    }

    public void setView(GameSurfaceView gameSurfaceView){
        this.activity.setContentView(gameSurfaceView);
    }


    private boolean supportsEs2() {
        ActivityManager activityManager = (ActivityManager) this.activity.getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        return (configurationInfo.reqGlEsVersion >= 0x20000);
    }


    public TileMap createTileMap(int path, GameSurfaceView gameSurfaceView, int count,
                                 int column, int row, int w, int h){
        return new TileMap(path, gameSurfaceView, count, column, row, w, h);
    }

    public GameSurfaceView createGameSurfaceView(Activity activity) {
        return new GameSurfaceView(activity);
    }

/////////////////////////////
//////ВНУТРЕННИЕ КЛАССЫ//////
/////////////////////////////
    public interface GameLoop{
        public void run();
    }

    public class GameSurfaceView extends GLSurfaceView {
        public OpenGLRenderer renderer;
        public GameLoop loop;

        public GameSurfaceView(Context context) {
            super(context);


            loop = new GameLoop(){
                public void run() {
                }
            };



            setEGLContextClientVersion(2);
            this.renderer = new OpenGLRenderer(loop);
            setRenderer(this.renderer);
            //this.setRenderMode(RENDERMODE_WHEN_DIRTY);
        }

        public void setGameLoop(GameLoop l) {
            this.loop = l;
            renderer.setGameLoop(this.loop);
        }

        public float getDeltaTime(){
            return renderer.deltaTime;
        }

    }



    private class OpenGLRenderer implements GLSurfaceView.Renderer {

        public GameLoop loop;

        private Queue<Runnable> queue = new LinkedList<>();
        public float deltaTime;
        private float lastFrame;

        public OpenGLRenderer(GameLoop l) {
            this.loop = l;
        }

        public void setGameLoop(GameLoop l) {
            this.loop = l;
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            lastFrame = System.nanoTime();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int w, int h) {
            GLES20.glViewport(0, 0, w, h);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST); // включает использование буфера глубины
            GLES20.glDepthFunc(GLES20.GL_LEQUAL); // определяет работу буфера глубины: более ближние объекты перекрывают дальние
            GLES20.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);


            deltaTime = (System.nanoTime()-lastFrame) / 1000000000.0f;
            lastFrame = System.nanoTime();



            while (!queue.isEmpty()){
                queue.poll().run();
                //Log.e("app", "runnable run");
            }

            loop.run();

        }

        public void act(Runnable runnable){
            queue.add(runnable);
        }

    }

















    public class TileMapInfo{
        public Map<String, Integer> types = new HashMap<String, Integer>();
        public int[][][] tiles;
        public String name;
        public int column;          //высота карты в ячейках
        public int row;             //ширина карты в ячейках

        public void putTileInfo(String s, int i){
            types.put(s, i);
        }

        public int getTileInfo(String s){
            return types.get(s);
        }
    }

    /*
    //нет смысла в отдельном классе
    //встроил хэшмап в TileSet
    public class TileSetInfo{
        public Map<String, Integer> types = new HashMap<String, Integer>();

        public void put(String s, int i){
            types.put(s, i);
        }

        public int get(String s){
            return types.get(s);
        }
    }
*/




    public class TileMap{
        public int count;           //количество слоев
        public TileLayer[] layers;
        public int[][][] tiles;

        public int column;          //высота карты в ячейках
        public int row;             //ширина карты в ячейках
        public int tileWidth;       //ширина одного тайла в пикселях
        public int tileHeight;      //высота одного тайла в пикселях

        public int path;            //путь к текстуре
        public GameSurfaceView game;

        public TileSet tileSet;

        /*
        Это для трансформаций
         */
        public int mapWidth;        //ширина карты в "пикселях"
        public int mapHeight;       //высота карты в "пикселях"

        public TileMap(int p, GameSurfaceView gameSurfaceView, int count, int column, int row, int tileWidth, int tileHeight) {
            this.count = count;
            this.column = column;
            this.row = row;
            this.tileWidth = tileWidth;
            this.tileHeight = tileHeight;
            path = p;
            game = gameSurfaceView;


            layers = new TileLayer[this.count];
            for (int l = 0; l < this.count; l++){
                layers[l] = new TileLayer(path, game, this.column, this.row);
            }

            tiles = new int[this.count][this.column][this.row];
            for (int l = 0; l < this.count; l++) {
                for (int y = 0; y <  this.column; y++) {
                    for (int x = 0; x <  this.row; x++) {
                        tiles[l][y][x] = 0;
                    }
                }
            }

            createTileSet();
            //Конец конструктора
        }

        public TileSet createTileSet(){

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            Bitmap img = BitmapFactory.decodeResource(
                    activity.getResources(), this.path, options);

            //Log.e("app", "img Height: " + img.getDensity() + "\nimg Width: " + img.getHeight()/img.getDensity());
            this.tileSet = new TileSet(layers, img.getWidth(), img.getHeight(), tileWidth, tileHeight);
            return tileSet;
        }

        public void draw(){
            for (int l = 0; l < this.count; l++) {
                layers[l].draw();
            }
        }

    }









    public interface AutoTileSet{

        public void set(TileLayer[] layers, int l, int x, int y, int type);
    }










    /*
        Вся инфа о ресурсе тайлсета лежит в TileSet.
        TileSet нужно переопределять (наследовать) каждому юзеру отдельно.
        TileSet хранит ссылку на все TileLayer.
    */
    /*
        В "type" функции setTile() 4 аргументом подставляется int,
        которая может извне преобразовываться из string name в int
        во внешнем массиве.
     */
    public class TileSet{
        public TileLayer[] layers;                                          //ссылка извне
        public int[][][] tiles; //отображение тайлов, тут хранятся типы     //ссылка извне
        public float[][] type;    //основной массив, указывающий под каким типом тайла
                                //какие текстурные координаты рисовать.
                                //генерируется автоматически в конструкторе из
                                //ширины, высоты тайлсета и ширины/высоты отдельного тайла

                                //type[1][2] - в первом массиве собственно сам id типа
                                //во втором массиве float x1, float y1, float x2, float y2
        public int width;       //ширина текстуры в пикселях
        public int height;      //высоты текстуры в пикселях
        public int tileWidth;   //ширина одного тайлсета в пикселях
        public int tileHeight;  //высота одного тайлсета в пикселях

        public float sizeX;     //ширина тайлсета в относительных значениях от всей текстуры
        public float sizeY;     //высота тайлсета в относительных значениях от всей текстуры




        public Map<String, Integer> tileInfo = new HashMap<String, Integer>(); //название тайлов

        public TileSet(TileLayer[] layers, int width, int height, int tileWidth, int tileHeight) {
            this.layers = layers;
            this.width = width;
            this.height = height;
            this.tileWidth = tileWidth;
            this.tileHeight = tileHeight;
            //width/tileWidth - это количество тайлов в текстуре по ширине
            sizeX = 1.f/(width/tileWidth);
            sizeY = 1.f/(height/tileHeight);

            //Log.e("app", "sizeX: " + sizeX + "\nsizeY: " + sizeY);

            //int length = (int) ((width/tileWidth)*(height/tileHeight));
            type = new float[height/tileHeight * width/tileWidth][4];
            for (int y = 0; y < height/tileHeight; y++) {
                for (int x = 0; x < width/tileWidth; x++) {

                    type[x+y*width/tileWidth][0] = x*sizeX;
                    type[x+y*width/tileWidth][1] = 1.f-y*sizeY-sizeY;
                    type[x+y*width/tileWidth][2] = x*sizeX+sizeX;
                    type[x+y*width/tileWidth][3] = 1.f-y*sizeY;
                }
            }

/*
            Log.e("app", "height: " + height + "\nwidth: " + width);
            Log.e("app", "tileHeight: " + tileHeight + "\ntileWidth: " + tileWidth);
            Log.e("app", "height/tileHeight: " + height/tileHeight + "\nwidth/tileWidth: " + width/tileWidth);
            Log.e("app", "type.length: " + type.length);
            */
        }

        //меняет и tiles и TileLayer
        public void setTile(int l, int x, int y, int type){
            //Log.e("app", "TexCoords: " + this.type[type][0] + ", " + this.type[type][1]
            //+ ", " + this.type[type][2] + ", " + this.type[type][3]);
            Log.e("app", "TexCoords: " + this.type[type][1] + ", " + this.type[type][3]);
            layers[l].setTile(x,y, this.type[type][0],this.type[type][1],this.type[type][2],this.type[type][3]);
            //layers[l].setTile(x, y, 0.f, 1.f-0.0833f, 0.0833f, 1.f);
        }

        public void autoTileSet(AutoTileSet as, int l, int x, int y, int type){
            as.set(layers, l, x, y, type);
        }

        public void setTile(int l, int x, int y, String t){
            int type = getTileInfo(t);
            Log.e("app", "TexCoords: " + this.type[type][1] + ", " + this.type[type][3]);
            layers[l].setTile(x,y, this.type[type][0],this.type[type][1],this.type[type][2],this.type[type][3]);
        }

        public void autoTileSet(AutoTileSet as, int l, int x, int y, String type){
            as.set(layers, l, x, y, getTileInfo(type));
        }


        public void putTileInfo(String s, int i){
            tileInfo.put(s, i);
        }

        public int getTileInfo(String s){
            return tileInfo.get(s);
        }
    }




    /*
    TileLayer не надо знать о типе тайла, проходимости и т.д.
    TileLayer занимается только отрисовкой. Он берет инфу из TileSetInfo и
    переводит ее в инфу для отрисовки TileGL.
    */
    public class TileLayer {

        private TileGL tileGL;

        private TextureDraw textureDraw;

        public int path;   //путь к текстуре
        public Bitmap img;

        public int column;
        public int row;

        private GameSurfaceView game; //к какому окну принадлежит текстура


        public TileLayer(int p, GameSurfaceView gameSurfaceView, int c, int r) {
            this.path = p;
            this.game = gameSurfaceView;
            this.column = c;
            this.row = r;

            textureDraw = new TextureDraw(){ @Override public void run(){}
                /*@Override public void run(float zBuffer) {}*/};

            // получение Bitmap
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            this.img = BitmapFactory.decodeResource(
                    activity.getResources(), this.path, options);
            if (this.img == null) {
                Log.e("app", "Error create bitmap!");
            }


            this.game.renderer.act(new Runnable(){
                @Override
                public void run() {
                    tileGL = new TileGL(img, column, row);

                    textureDraw = new TextureDraw(){
                        @Override public void run(){
                            tileGL.draw();
                        }
                    };
                }
            });

        }

        public void setTile(int x, int y, final float x1, final float y1, final float x2, final float y2){
            final int iterator = ((column-y-1) * row + (row-x-1)) * 8;  //8 - количество текст. коорд. на 1 квадрат
            this.game.renderer.act(new Runnable(){
                @Override
                public void run() {

                    tileGL.setTile(iterator, x1, y1, x2, y2);
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


    public class TileGL {
        public Shader shader;
        public Mesh mesh;
        public Bitmap img;
        public int column;
        public int row;
        //public float[] texCoords;

        private float[] MVP = new float[16];

        public TileGL(Bitmap img, int column, int row/*, float[] texCoords*/) {
            this.img = img;
            this.column = column;
            this.row = row;
            //this.texCoords = texCoords;

            this.shader = new Shader();
            this.mesh = new Mesh(this.shader.shaderProgram, this.img, this.column, this.row);

            Matrix.setIdentityM(this.MVP, 0);
        }

        //public void setTile(float xTile, float yTile, float xTex, float yTex){
        public void setTile(int iterator, float x1, float y1, float x2, float y2){
            mesh.setTile(iterator, x1, y1, x2, y2);
        }


        public void draw(){

            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

            this.shader.use();

            int MVPLoc = GLES20.glGetUniformLocation(this.shader.shaderProgram, "MVP");
            GLES20.glUniformMatrix4fv(MVPLoc, 1, false, this.MVP, 0);


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


            public Shader() {
                this.vertex =
                          "attribute vec3 position;\n"
                        + "attribute vec2 texCoord;\n"
                        + "uniform mat4 MVP;\n"
                        + "varying vec2 TexCoord;\n"
                        + "void main()\n"
                        + "{\n"
                        + "   gl_Position = MVP * vec4(position.x, position.y, 1.0, 1.0);\n"
                        + "   TexCoord = vec2(texCoord.x, 1.0 - texCoord.y);\n"
                        + "}\n";

                this.fragment =
                          "precision mediump float;\n"
                        + "varying vec2 TexCoord;\n"
                        + "uniform sampler2D ourTexture;\n"
                        + "void main()\n"
                        + "{\n"
                        + "    vec4 texColor = texture2D(ourTexture, TexCoord);\n"
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
                GLES20.glGetProgramiv(this.shaderProgram, GLES20.GL_LINK_STATUS, linked, 0);
                if (linked[0] == 0) {
                    Log.e("app", "Error linking program:");
                    Log.e("app", GLES20.glGetProgramInfoLog(this.shaderProgram));
                    GLES20.glDeleteProgram(this.shaderProgram);
                }
            }

            public void use() { GLES20.glUseProgram(this.shaderProgram);}


            private int createShader(int type, String source) {
                int shader = GLES20.glCreateShader(type);
                GLES20.glShaderSource(shader, source);
                GLES20.glCompileShader(shader);
                //отладка
                int[] compiled = new int[1];
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
            public short[] indices;
            public float[] texCoords;

            private int shaderProgram;
            private Bitmap img;

            private FloatBuffer vertexBuffer;
            private FloatBuffer textureBuffer;
            private ShortBuffer indexBuffer;

            private int VBO;
            private int texBO;
            private int IBO;
            private int texture;

            private int column;
            private int row;

            public Mesh(int s, Bitmap i, int c, int r) {
                this.shaderProgram = s;
                this.img = i;
                this.column = c;
                this.row = r;

                this.vertices = new float[column*row*4*3];
                FloatArrayPut3f vert = new FloatArrayPut3f(vertices);
                this.indices = new short[column*row*6];
                IndicesPut ind = new IndicesPut(indices);
                this.texCoords = new float[column*row*8];
                TexCoordsPut tex = new TexCoordsPut(texCoords);

                float curX = row;
                float curY = column;
                for (int y = 0; y < column; y++) {

                    curX = row;

                    for (int x = 0; x < row; x++) {
                        vert.put( curX/row, curY/column, 0.0f );
                        vert.put( curX/row, (curY-2)/column, 0.0f );
                        vert.put( (curX-2)/row, (curY-2)/column, 0.0f );
                        vert.put( (curX-2)/row, curY/column, 0.0f );

                        curX -= 2;

                        ind.put();
                        tex.init();
                        Log.e("app", "Indicies iterator: " + ind.iterator +
                                "\nIndicies value: " + ind.value);

                    }

                    curY -= 2;

                }

                Log.e("app", "Vert 1: " + vertices[0] + " " + vertices[1]);
                Log.e("app", "Vert 2: " + vertices[3] + " " + vertices[4]);
                Log.e("app", "Vert 3: " + vertices[6] + " " + vertices[7]);
                Log.e("app", "Vert 4: " + vertices[9] + " " + vertices[10]);
                Log.e("app", "Vert 5: " + vertices[12] + " " + vertices[13]);
                Log.e("app", "Vert 6: " + vertices[15] + " " + vertices[16]);
                Log.e("app", "Vert 7: " + vertices[18] + " " + vertices[19]);
                Log.e("app", "Vert 8: " + vertices[21] + " " + vertices[22]);


/*
                this.vertices = new float[]{
                        1.f,  1.f, 0.0f,   // Верхний правый угол
                        1.f, -1.f, 0.0f,   // Нижний правый угол
                        0.f, -1.f, 0.0f,   // Нижний левый угол
                        0.f,  1.f, 0.0f,   // Верхний левый угол

                        0.f,  1.f, 0.0f,   // Верхний правый угол
                        0.f, -1.f, 0.0f,   // Нижний правый угол
                        -1.f, -1.f, 0.0f,  // Нижний левый угол
                        -1.f,  1.f, 0.0f   // Верхний левый угол
                };
*/

/*
                this.indices = new short[]{
                        0, 1, 3,    // Первый треугольник
                        1, 2, 3,    // Второй треугольник

                        4, 5, 7,    // Третий треугольник
                        5, 6, 7     // Четвертый треугольник
                };

                this.texCoords = new float[]{
                        1.0f, 1.0f,     // Верхний правый угол
                        1.0f, 0.0f,     // Нижний правый угол
                        0.5f, 0.0f,     // Нижний левый угол
                        0.5f, 1.0f,     // Верхний левый угол

                        1.0f, 1.0f,     // Верхний правый угол
                        1.0f, 0.0f,     // Нижний правый угол
                        0.5f, 0.0f,     // Нижний левый угол
                        0.5f, 1.0f      // Верхний левый угол
                };
*/


/*
                //массив вершин треугольника
                this.vertices = new float[]{
                        1.f,  1.f, 0.0f,  // Верхний правый угол
                        1.f, -1.f, 0.0f,  // Нижний правый угол
                        -1.f, -1.f, 0.0f, // Нижний левый угол
                        -1.f,  1.f, 0.0f  // Верхний левый угол
                };
                this.indices = new short[]{
                        0, 1, 3,   // Первый треугольник
                        1, 2, 3    // Второй треугольник
                };

                this.texCoords = new float[]{
                        1.0f, 1.0f,     // Верхний правый угол
                        1.0f, 0.0f,     // Нижний правый угол
                        0.0f, 0.0f,     // Нижний левый угол
                        0.0f, 1.0f      // Верхний левый угол
                };
*/


                int[] textures = new int[1];
                GLES20.glGenTextures(1, textures, 0);
                this.texture = textures[0];
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.texture);
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, this.img, 0);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, img, 0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
                img.recycle();






                this.vertexBuffer = ByteBuffer.allocateDirect(this.vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
                this.vertexBuffer.put(this.vertices);
                this.vertexBuffer.position(0);

                this.textureBuffer = ByteBuffer.allocateDirect(this.texCoords.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
                this.textureBuffer.put(this.texCoords);
                this.textureBuffer.position(0);

                this.indexBuffer = ByteBuffer.allocateDirect(this.indices.length * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
                this.indexBuffer.put(this.indices);
                this.indexBuffer.position(0);


                int buffers[] = new int[4];
                GLES20.glGenBuffers(4, buffers, 0);
                this.VBO = buffers[0];
                this.texBO = buffers[2];
                this.IBO = buffers[3];

                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, this.VBO);
                GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, this.vertexBuffer.capacity()*4, this.vertexBuffer, GLES20.GL_STATIC_DRAW);

                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, this.texBO);
                GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, this.textureBuffer.capacity()*4, this.textureBuffer, GLES20.GL_STATIC_DRAW);

                GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, this.IBO);
                GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity()*2, this.indexBuffer, GLES20.GL_STATIC_DRAW);
            }

            public void setTile(int iterator, float x1, float y1, float x2, float y2) {

                TexCoordsPut tex = new TexCoordsPut(texCoords);
                tex.setTile(iterator, x1, y1, x2, y2);

                this.textureBuffer.put(this.texCoords);
                this.textureBuffer.position(0);
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, this.texBO);
                GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, this.textureBuffer.capacity()*4, this.textureBuffer, GLES20.GL_DYNAMIC_DRAW);

                /*
                this.texCoords = new float[]{
                        1.0f, 1.0f,     // Верхний правый угол
                        1.0f, 0.0f,     // Нижний правый угол
                        0.0f, 0.0f,     // Нижний левый угол
                        0.0f, 1.0f      // Верхний левый угол
                };
                //this.textureBuffer = ByteBuffer.allocateDirect(this.texCoords.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
                this.textureBuffer.put(this.texCoords);
                this.textureBuffer.position(0);
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, this.texBO);
                GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, this.textureBuffer.capacity()*4, this.textureBuffer, GLES20.GL_DYNAMIC_DRAW);
                */
            }



            public void draw(){
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, this.VBO);
                int posLoc = GLES20.glGetAttribLocation(this.shaderProgram, "position");
                GLES20.glVertexAttribPointer(posLoc, 3, GLES20.GL_FLOAT, false, 0, 0);
                GLES20.glEnableVertexAttribArray(posLoc);


                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, this.texBO);
                int texLoc = GLES20.glGetAttribLocation(this.shaderProgram, "texCoord");
                GLES20.glVertexAttribPointer(texLoc, 2, GLES20.GL_FLOAT, false, 0, 0);
                GLES20.glEnableVertexAttribArray(texLoc);

                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.texture);

                GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, this.IBO);
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length, GLES20.GL_UNSIGNED_SHORT, 0);
                //GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.length);

            }


        }

    }


























    private class TexCoordsPut{
        public float[] arr;
        public int iterator;

        public TexCoordsPut(float[] arr) {
            this.arr = arr;
            iterator = 0;
        }

        /*
                        1.0f, 1.0f,     // Верхний правый угол
                        1.0f, 0.0f,     // Нижний правый угол
                        0.0f, 0.0f,     // Нижний левый угол
                        0.0f, 1.0f      // Верхний левый угол
         */

        public void init(){
            arr[iterator] = 1.f;
            iterator++;
            arr[iterator] = 1.f;
            iterator++;
            arr[iterator] = 1.f;
            iterator++;
            arr[iterator] = 0.f;
            iterator++;
            arr[iterator] = 0.f;
            iterator++;
            arr[iterator] = 0.f;
            iterator++;
            arr[iterator] = 0.f;
            iterator++;
            arr[iterator] = 1.f;
            iterator++;
        }

        public void setTile(int it, float x1, float y1, float x2, float y2) {
            arr[it] = x2;
            it++;
            arr[it] = y2;
            it++;
            arr[it] = x2;
            it++;
            arr[it] = y1;
            it++;
            arr[it] = x1;
            it++;
            arr[it] = y1;
            it++;
            arr[it] = x1;
            it++;
            arr[it] = y2;
            it++;
        }
    }


    private class FloatArrayPut3f{
        public float[] arr;
        public int iterator;

        public FloatArrayPut3f(float[] arr) {
            this.arr = arr;
            iterator = 0;
        }

        public void put(float f1, float f2, float f3){
            arr[iterator] = f1;
            iterator++;
            arr[iterator] = f2;
            iterator++;
            arr[iterator] = f3;
            iterator++;
        }
    }

    private class IndicesPut{
        public short[] arr;
        public int iterator;
        public int value;

        public IndicesPut(short[] arr) {
            this.arr = arr;
            iterator = 0;
            value = 0;
        }

        public void put(){
            arr[iterator] = (short) value;
            iterator++;
            arr[iterator] = (short) (value+1);
            iterator++;
            arr[iterator] = (short) (value+3);
            iterator++;
            arr[iterator] = (short) (value+1);
            iterator++;
            arr[iterator] = (short) (value+2);
            iterator++;
            arr[iterator] = (short) (value+3);
            iterator++;

            value += 4;
        }
    }




}
