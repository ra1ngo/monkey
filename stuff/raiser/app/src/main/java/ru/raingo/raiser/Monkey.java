package ru.raingo.raiser;

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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Monkey {

    private Activity activity;
    private Screen currentScreen;
    private GameSurfaceView currentGame;

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
        this.currentGame = currentScreen.game;
    }

    private boolean supportsEs2() {
        ActivityManager activityManager = (ActivityManager) this.activity.getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        return (configurationInfo.reqGlEsVersion >= 0x20000);
    }


    //проверка нахождения точки в boundingBox
    //возвращает внутренние координаты boundingBox по которым произошел клик
    //или [-1,-1] в случае, если клика по boundingBox не было
    public float[] collides(float pX, float pY, float x, float y, float width, float height) {
        if (width >= pX && x < pX && height >= pY && y < pY){
            float nX = pX - x;
            float nY = pY - y;
            return new float[]{nX, nY};
        } else {
            return new float[]{-1, -1};
        }
    }

    public boolean isClick(float pX, float pY, float x, float y, float width, float height) {
        if (width >= pX && x < pX && height >= pY && y < pY)
            return true;
        else
            return false;
    }

















    public Viewport createViewport(int w,int h){
        return new Viewport(w,h);
    }
    public Screen createScreen(Activity act){
        Screen s = new Screen();
        GameSurfaceView game = new GameSurfaceView(activity, s);
        s.setGame(act, game);
        return s;
    }

    public Texture createTexture(int p, float zBuffer, float a){
        Texture tex = new Texture(p, zBuffer, a, currentGame);
        currentScreen.game.currentStage.scene.addActor(tex);
        return tex;
    }

    public TextureAnim createTextureAnim(int p, float zBuffer, float a, int c, int r, float sp){
        TextureAnim tex = new TextureAnim(p, zBuffer, a, currentGame, c, r, sp);
        currentScreen.game.currentStage.scene.addActor(tex);
        return tex;
    }

    public Transform createTransform(Entity p, float w, float h){
        Transform tr = new Transform(p, w,h);
        return tr;
    }

    public Camera createCamera() {
        return new Camera();
    }

    public Stage createStage(Screen screen, Camera camera) {
        return new Stage(currentGame, camera);
    };


    public GroupEntity createGroupEntity(Screen screen) {
        return new GroupEntity(currentGame);
    }

    public GameSurfaceView createGameSurfaceView(Activity activity, Screen screen) {
        return new GameSurfaceView(activity, screen);
    }


    /////////////////////////////
//////ВНУТРЕННИЕ КЛАССЫ//////
/////////////////////////////
    public interface GameLoop{
        public void run();
    }

    public class GameSurfaceView extends GLSurfaceView {
        public OpenGLRenderer renderer;
        public StagesNavigator stages;
        public GroupEntity root;
        public GameLoop loop;
        public Stage currentStage;
        private Screen screen;
        public Viewport viewport;
        //public Viewport viewport;

        public GameSurfaceView(Context context, Screen screen) {
            super(context);

            stages = new StagesNavigator(this);
            currentStage = stages.currentStage;
            root = new GroupEntity(this);
            loop = new GameLoop(){
                public void run() {
                    stages.currentStage.scene.draw();
                    stages.currentStage.root.update();
                }
            };


            viewport = new Viewport(screen.width, screen.height);

            setEGLContextClientVersion(2);
            this.renderer = new OpenGLRenderer(viewport, loop);
            setRenderer(this.renderer);
            //this.setRenderMode(RENDERMODE_WHEN_DIRTY);
        }

        public void setGameLoop(GameLoop l) {
            this.loop = l;
            renderer.setGameLoop(this.loop);
        }

        public Entity createEntity(){
            Entity ent = root.createEntity();
            return ent;
        }

        public float getDeltaTime(){
            return renderer.deltaTime;
        }

        public void setCurrentStage(Stage st){
            stages.currentStage = st;
            currentStage = stages.currentStage;
        }

        public void setScreen(Screen screen) {
            this.screen = screen;
            //viewport = new Viewport(screen.width, screen.height);
        }
    }



    private class OpenGLRenderer implements GLSurfaceView.Renderer {

        public GameLoop loop;
        private Viewport viewport;
        //private Runnable runnable = null;
        //private int i =0;
        private Queue<Runnable> queue = new LinkedList<>();
        public float deltaTime;
        private float lastFrame;

        public OpenGLRenderer(Viewport v, GameLoop l) {
            //this.context = c;
            //Log.e("app", "context load:");
            //Log.e("app", String.valueOf(this.context));
            this.viewport = v;
            this.loop = l;
            Log.e("app", "new OpenGLRenderer");
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
            lastFrame = System.nanoTime();
            Log.e("app", "OpenGLRenderer onSurfaceCreated()");
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int w, int h) {
            Log.e("app", "onSurfaceChanged" + "\nWidth: " + w + "\nHeight: " + h);
            this.viewport.set(0,0,w,h);
            //lastFrame = System.nanoTime();
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

            deltaTime = (System.nanoTime()-lastFrame) / 1000000000.0f;
            lastFrame = System.nanoTime();




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












    public class Viewport{
        public int width;
        public int height;
        public int x;
        public int y;
        public float zoom = 0.25f;
        private float[] projection = new float[16];

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

        public float[] getMatrix(){
            //Matrix.setIdentityM(this.projection, 0);
            Matrix.orthoM(projection, 0,
                    -1.f*width * zoom, 1.f*width * zoom,
                    -1.f*height * zoom, 1.f*height * zoom,
                    0.1f, 20.f
            );
            //float aspectRatio = width / height;
            //Matrix.orthoM(projection, 0, -aspectRatio, aspectRatio, -1, 1, -1, 1);
            //Matrix.setIdentityM(this.projection, 0);
            return projection;
        }
    }


    public static class Screen extends Fragment {
        //public Viewport viewport;
        public GameSurfaceView game;
        public Activity activity;
        public int width;
        public int height;


        public void setGame(Activity activity, GameSurfaceView gameSurfaceView){
            this.game = gameSurfaceView;
            this.activity = activity;
            Display display = this.activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            Log.e("app", "ScreenWidth:  " + size.x + "\nScreenHeight: " + size.y);
            width = size.x;
            height = size.y;

            game.setScreen(this);

            Log.e("app", "Screen setGame()");
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
            Log.e("app", "Screen onCreateView()");
            return this.game;
        }


    }



































    public class StagesNavigator{
        private List<Stage> stages = new ArrayList<>();
        public Stage currentStage;
        public GameSurfaceView game;

        public StagesNavigator(GameSurfaceView gameSurfaceView) {
            this.game = gameSurfaceView;
            //scene = new Scene(this);
            Stage inSt = new Stage(this.game, new Camera());
            addStage(inSt);
            currentStage = inSt;
        }

        public void addStage(Stage stage){
            stages.add(stage);
        }
    }

    public class Stage{
        public Camera currentCamera;
        public Scene scene;
        public GameSurfaceView game;
        public GroupEntity root;
        public Map map;

        public Stage(GameSurfaceView gameSurfaceView, Camera camera) {
            this.currentCamera = camera;
            this.game = gameSurfaceView;
            scene = new Scene(game, this);
            root = new GroupEntity(this);

        }

        public void setMap(Map map){
            this.map = map;
        }
    }




    public class Scene{
        private List<Actor> actors = new ArrayList<>();
        private GameSurfaceView game;
        private Stage stage;


        public Scene(GameSurfaceView gameSurfaceView, Stage stage) {
            this.game = gameSurfaceView;
            this.stage = stage;

            //Matrix.setIdentityM(this.projection, 0);
            //Log.e("app", String.valueOf(zoom));
        }

        public Scene(GameSurfaceView gameSurfaceView) {
            this.game = gameSurfaceView;
            //Matrix.setIdentityM(this.projection, 0);
        }

        public void addActor(Actor actor){
            actors.add(actor);
        }

        public void draw(){
            //Log.e("app", "Scene draw()");
            for (int i = 0; i < actors.size(); i++) {
                actors.get(i).draw(game.getDeltaTime());
            }
        }

        public Texture createTexture(int p, float zBuffer, float a){
            Texture tex = new Texture(p, zBuffer, a, game);
            addActor(tex);
            return tex;
        }

        public TextureAnim createTextureAnim(int p, float zBuffer, float a, int c, int r, float sp){
            TextureAnim tex = new TextureAnim(p, zBuffer, a, game, c, r, sp);
            addActor(tex);
            return tex;
        }
    }



    public class GroupEntity{
        private List<Entity> entities = new ArrayList<>();
        public GameSurfaceView game;
        public Stage stage;

        public GroupEntity(GameSurfaceView gameSurfaceView) {
            this.game = gameSurfaceView;
        }

        public GroupEntity(Stage stage) {
            this.stage = stage;
        }

        public void addEntity(Entity entity){

            entities.add(entity);
            entity.group = this;
        }

        public void update(){
            for (int i = 0; i < entities.size(); i++) {
                entities.get(i).update();
            }
        }

        public Entity createEntity(){
            Entity ent = new Entity();
            //ent.addComponent(new Transform());
            addEntity(ent);
            return ent;
        }
    }


    //КОНСТРУКТОР ЗДЕСЬ ЖЕЛАТЕЛЬНО ОСТАВИТЬ ПУСТЫМ
    public class Entity{
        public GroupEntity group;
        private List<Component> components = new ArrayList<>();
        //private Transform transform;
/*
        public Entity(GroupEntity group) {
            this.group = group;
            transform = new Transform(this, 0, 0);
        }
*/
        public Component getComponent(String name){
            for (int i = 0; i < components.size(); i++) {
                if (components.get(i).name == name)
                    return components.get(i);
            }
            return null;
        }

        public Component getComponentFromType(String type){
            for (int i = 0; i < components.size(); i++) {
                if (components.get(i).type == type)
                    return components.get(i);
            }
            return null;
        }

        public void addComponent(Component component){

            components.add(component);
            component.entity = this;
        }

        public void update(){
            for (int i = 0; i < components.size(); i++) {
                components.get(i).update();
            }
        }

        /*public Transform getTransform() {
            return transform;
        }*/
    }

    public abstract class Component{
        public String name;
        public String type;
        public Entity entity;

        public void update(){};
    }

    public interface Actor{
        public void draw(float deltaTime);
    }

    public class Transform extends Component{
        public float x;
        public float y;
        public float z;
        public float width;
        public float height;
        public String type = "Transform";

        public float[] position = new float[3];
        public float[] rotation = new float[3];
        public float[] scale = new float[3];

        private float[] trans = new float[16];
        public float[] MVP = new float[16];

        public Transform(Entity entity, float width, float height) {
            this.entity = entity;
            Matrix.setIdentityM(this.trans, 0);
            position = new float[]{0.f, 0.f, 0.0f};
            rotation = new float[]{0.0f, 0.0f, 1.0f};
            scale    = new float[]{1.f, 1.f, 1.0f};
            this.width = width;
            this.height = height;
            Matrix.scaleM(this.trans, 0, this.width, this.height, 1.0f);

            //Matrix.setIdentityM(this.MVP, 0);
            //Matrix.translateM(this.trans, 0, position[0], position[1], position[2]);
        }

        public void translate(float pos[]){

            //Matrix.setIdentityM(this.trans, 0);
            //float deltaTime = entity.group.screen.getDeltaTime();
            Matrix.translateM(this.trans, 0, pos[0], pos[1], pos[2]);
        }

        public void rotate(float angle ){
            //Matrix.setIdentityM(this.trans, 0);

            Matrix.rotateM(this.trans, 0, angle, rotation[0], rotation[1], rotation[2]);
        }

        public void scale(float scle[]){

            //Matrix.setIdentityM(this.trans, 0);

            Matrix.scaleM(this.trans, 0, scle[0], scle[1], scle[2]);
        }

        public float[] getMatrix(){
            //Log.e("app", "Transform.getMatrix()");
            float[] projection = entity.group.game.viewport.getMatrix();
            float[] CameraMatrix = entity.group.game.currentStage.currentCamera.getMatrix();

            /*
            float cameraPosition[] = {0.f, 0.5f, 10.f};
            float cameraTarget[] = {0.0f, 0.5f, 0.0f};
            float upVector[] = {0.0f, 1.0f, 0.0f};
            float[] CameraMatrix = new float[16];
            Matrix.setLookAtM(CameraMatrix, 0,
                    cameraPosition[0], cameraPosition[1], cameraPosition[2],
                    cameraTarget[0], cameraTarget[1], cameraTarget[2],
                    upVector[0], upVector[1], upVector[2]
            );
            float zoom = 1.f;
            float[] projection = new float[16];
            Matrix.orthoM(projection, 0,
                    -1.f * zoom, 1.f * zoom,
                    -1.f * zoom, 1.f * zoom,
                    0.1f, 20.f
            );
            */
            //Matrix.setIdentityM(this.MVP, 0);
            Matrix.multiplyMM(this.MVP, 0, CameraMatrix, 0, this.trans, 0);
            Matrix.multiplyMM(this.MVP, 0, projection, 0, this.MVP, 0);

            //Matrix.setIdentityM(this.MVP, 0);

            return MVP;
        }
    }

    //Компонент события клика по ентити
    public abstract class Clickable extends Component{
        public String type = "Clickable";

        //обработчик клика для переопределения в наследнике
        public abstract void click();
    }




    public class Camera{
        public float cameraPosition[];
        public float cameraTarget[];
        public float upVector[];

        public float CameraMatrix[] = new float[16];

        public Camera() {
            cameraPosition = new float[]{0.f, 0.0f, 10.f};
            cameraTarget = new float[]{0.0f, 0.0f, 0.0f};
            upVector = new float[]{0.0f, 1.0f, 0.0f};
            //Matrix.setIdentityM(this.CameraMatrix, 0);
        }

        public float[] getMatrix(){
            //float[] CameraMatrix1 = new float[16];
            //Matrix.setIdentityM(this.CameraMatrix, 0);
            Matrix.setLookAtM(CameraMatrix, 0,
                    cameraPosition[0], cameraPosition[1], cameraPosition[2],
                    cameraTarget[0], cameraTarget[1], cameraTarget[2],
                    upVector[0], upVector[1], upVector[2]
            );
            //Matrix.setIdentityM(this.CameraMatrix, 0);
            return CameraMatrix;
        }
    }




































    public abstract class State{
        public String name;

        public void update(){};
    }



    public class StateMachine extends Component{
        private List<State> states = new ArrayList<>();
        private State currentState;

        public void addState(State state){

            states.add(state);
        }

        public void setState(String name){
            for (int i = 0; i < states.size(); i++) {
                if (states.get(i).name == name)
                    currentState = states.get(i);
            }
        }

        public void update(){
            currentState.update();
        }
    }

    public abstract class Map{
        public int width;
        public int height;


        public void click(float x, float y){};
    }

    public class GridMap extends Map{
        public int sizeTile;
        public int columns;
        public int rows;
        public int[][][] tiles; //отображение тайлов, тут хранятся типы
        public int[][] grid; //занятость  //для поиска пути или для хранения инфы о Entity на ней
        public int layers; //количество слоев

        public GridMap(int sizeTile, int rows, int columns, int layers) {
            this.sizeTile = sizeTile;
            this.columns = columns;
            this.rows = rows;
            this.width = this.rows*this.sizeTile;
            this.height = this.columns*this.sizeTile;
            this.layers = layers;

            for (int y = 0; y < this.columns; y++) {

                for (int x = 0; x < this.rows; x++) {

                    for (int l = 0; l < this.layers; l++) {
                        this.tiles[l][y][x] = 0;
                    }

                    grid[y][x] = 0;
                }

            }
        }

        public int getTile(int x, int y, int l) {
            return tiles[l][y][x];
        }

        public void setTile(int x, int y, int l, int type) {
            tiles[l][y][x] = type;
        }

        public int getCell(int x, int y) {
            return grid[y][x];
        }

        public void setCell(int x, int y, int type) {
            grid[y][x] = type;
        }

        @Override
        public void click(float x, float y) {
            //...высчитываем кликнутую клетку
            //получение ячейки по координатам
            int row = (int) Math.floor(x/sizeTile);
            int column = (int) Math.floor(y/sizeTile);
            clickTile(row, column);
        }

        //функция для переопределения в наследнике
        public void clickTile(int x, int y){};
    }






































    public class TileSet extends Component implements Actor {

        @Override
        public void draw(float deltaTime) {

        }
    }




    public class Texture extends Component implements Actor{
        public String name;
        public boolean init;

        private Context context;
        private TextureGL textureGL;

        private TextureDraw textureDraw;

        public int path;   //путь к текстуре
        public float z; //zBuffer
        public float alpha;
        public Bitmap img;

        private GameSurfaceView game; //к какому окну принадлежит текстура



        public Texture(int p, float zBuffer, float a, GameSurfaceView gameSurfaceView) {
            init = false;
            this.path = p;
            this.name = activity.getResources().getResourceName(path);
            this.z = zBuffer;
            this.alpha = a;
            this.game = gameSurfaceView;

            textureDraw = new TextureDraw(){ @Override public void run(){}
                /*@Override public void run(float zBuffer) {}*/};

            final Texture self = this;

            // получение Bitmap
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            this.img = BitmapFactory.decodeResource(
                    activity.getResources(), this.path, options);
            if (this.img == null) {
                Log.e("app", "Error create bitmap!");
            }

            game.renderer.act(new Runnable(){
                @Override
                public void run() {
                    textureGL = new TextureGL(img, z, alpha, game.currentStage.scene, (Component) self);
                    init = true;

                    textureDraw = new TextureDraw(){
                        @Override public void run(){
                            //Log.e("app", "TextureDraw run");
                            textureGL.draw();
                        }
                        //@Override
                        /*public void run(float zBuffer) {
                            textureGL.draw(zBuffer);
                        }*/
                    };
                }
            });
        }
/*
        public void draw(float zBuffer, float deltaTime) {
            textureDraw.run(zBuffer);
            //Log.e("app", name  + " = " + String.valueOf(init));
        }
        */
        @Override
        public void draw(float deltaTime) {
            textureDraw.run();
            //Log.e("app", name  + " = " + String.valueOf(init));
        }

        private abstract class TextureDraw{
            public abstract void run();
            //public abstract void run(float zBuffer);
        }
    }


    public class TextureAnim extends Component implements Actor{
        public String name;
        public boolean init;

        private Context context;
        private TextureGL textureGL;

        private TextureDraw textureDraw;

        public int path;   //путь к текстуре
        public float z; //zBuffer
        public float alpha;
        public Bitmap img;
        public int width;
        public int height;

        private GameSurfaceView game; //к какому окну принадлежит текстура

        public float regWidth;
        public float regHeight;
        public int columns;
        public int rows;
        private float speed;
        private List<Anim> animations = new ArrayList<>();
        private Anim currentAnim;

        private float currentTime;
        private float lastTime;


        public TextureAnim(int p, float zBuffer, float a, GameSurfaceView gameSurfaceView,
                       int c, int r, float sp) {
            init = false;
            this.path = p;
            this.name = activity.getResources().getResourceName(path);
            this.z = zBuffer;
            this.alpha = a;
            this.game = gameSurfaceView;

            this.columns = c;
            this.rows = r;
            this.regWidth = 1.0f/rows;
            this.regHeight = 1.0f/columns;
            this.speed = sp;

            // получение Bitmap
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            this.img = BitmapFactory.decodeResource(
                    activity.getResources(), this.path, options);
            if (this.img == null) {
                Log.e("app", "Error create bitmap!");
            }
            //Log.e("app", "Width: " + options.outWidth + "/nHeight: " + options.outHeight);
            //Log.e("app", "Width: " + img.getWidth() + "\nHeight: " + img.getHeight());
            width = img.getWidth();
            height = img.getHeight();

            textureDraw = new TextureDraw(){ @Override public void run(){}
                /*@Override public void run(float zBuffer) {}*/};

            final TextureAnim self = this;

            game.renderer.act(new Runnable(){
                @Override
                public void run() {
                    textureGL = new TextureGL(img, z, alpha, game.currentStage.scene, (Component) self);
                    textureGL.setRegion(0.0f, 0.f, regWidth, regHeight);
                    init = true;

                    textureDraw = new TextureDraw(){
                        @Override public void run(){
                            //Log.e("app", "TextureDraw run");
                            textureGL.draw();
                        }
                        /*
                        @Override
                        public void run(float zBuffer) {
                            textureGL.draw(zBuffer);
                        }
                        */
                    };
                }
            });


            lastTime = System.nanoTime() / 1000000000.0f;
        }
/*
        public void draw(float zBuffer) {
            textureDraw.run(zBuffer);
            //Log.e("app", name  + " = " + String.valueOf(init));
        }
*/
        public void draw(float deltaTime) {
            //textureDraw.run();
            //float FPS = 1/(deltaTime);
            currentTime = System.nanoTime() / 1000000000.0f;
            //Log.e("app", String.valueOf((currentTime - lastTime)));



            if ((currentTime - lastTime) >= speed && currentAnim != null){
                currentAnim.nextFrame();
                //Log.e("app", "X: " + currentAnim.getX() + "  Y: " + currentAnim.getY());
                //Log.e("app", "regWidth: " + regWidth + "  regHeight: " + regHeight);
                //textureGL.setRegion(0.25f, 0.f, regWidth, regHeight);
                textureGL.setRegion(currentAnim.getX(), currentAnim.getY(), regWidth, regHeight);
                lastTime = System.nanoTime() / 1000000000.0f;
            }

            textureDraw.run();

        }

        public void addAnim(String name, float[][] frames){
            animations.add(new Anim(name, frames));
        }

        public void setAnim(String name){
            if (currentAnim != null && currentAnim.name != name)
                currentAnim.iterator=0;

            for (int i = 0; i < animations.size(); i++) {
                if (animations.get(i).name == name)
                    currentAnim = animations.get(i);
            }
        }

        private abstract class TextureDraw{
            public abstract void run();
            //public abstract void run(float zBuffer);
        }

    }



    private class Anim{
        public String name;
        //public Frame[] frames;
        public float[][] frames; //int[][] frames = {{1,0},{1,1},{2,1}};
        public int iterator;

        public Anim(String name, float[][] frames) {
            this.name = name;
            this.frames = frames;
            iterator = 0;
        }

        public float getX(){
            return frames[iterator][0];
        }

        public float getY(){
            return frames[iterator][1];
        }

        public void nextFrame(){
            if (iterator < frames.length-1){
                iterator++;
            } else {
                iterator=0;
            }
        }
    }
/*
    private class Frame{
        public int x;
        public int y;

        public Frame(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
*/





















    public class TextureGL{
        private int width;
        private int height;
        //public int path;   //путь к текстуре
        public Shader shader;
        public Mesh mesh;
        public float z; //zBuffer
        public float alpha;
        public boolean init;  //проверка загрузки изо
        public Bitmap img;

        //private float[] MVP = new float[16];
        private Scene scene; //к какому окну принадлежит текстура
        private float[] texMat = new float[16];

        //public Transform transform;
        public Component textureComp;

        public TextureGL(Bitmap i, float zBuffer, float a, Scene s, Component tex) {
            this.z = zBuffer;
            this.alpha = a;
            this.init = false;
            this.scene = s;
            textureComp = tex;

            this.img = i;

            this.shader = new Shader(this);
            this.mesh = new Mesh(this.shader.shaderProgram, this.img);
            Matrix.setIdentityM(texMat, 0);

            width = img.getWidth();
            height = img.getHeight();
/*

            //затем убрать это
            float position[] = {0.0f, 0.0f, 0.0f};
            float rotation[] = {0.0f, 0.0f, 1.0f};
            float scale[] = {0.5f, 0.5f, 1.0f};

            float[] trans = new float[16];
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

            Matrix.multiplyMM(this.MVP, 0, CameraMatrix, 0, trans, 0);
            Matrix.multiplyMM(this.MVP, 0, projection, 0, this.MVP, 0);
*/
        }

        public void draw(){

            //Log.e("app", "TextureGL.draw()");

            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

            this.shader.use();

            int alphaLoc = GLES20.glGetUniformLocation(this.shader.shaderProgram, "alpha");
            GLES20.glUniform1f(alphaLoc, this.alpha);



            Transform transform = (Transform) textureComp.entity.getComponentFromType("Transform");
            int MVPLoc = GLES20.glGetUniformLocation(this.shader.shaderProgram, "MVP");
            GLES20.glUniformMatrix4fv(MVPLoc, 1, false, transform.getMatrix(), 0);
            //GLES20.glUniformMatrix4fv(MVPLoc, 1, false, MVP, 0);

            //float[] trans = new float[16];
            //Matrix.setIdentityM(trans, 0);
            //GLES20.glUniformMatrix4fv(MVPLoc, 1, false, trans, 0);

            int zLoc = GLES20.glGetUniformLocation(this.shader.shaderProgram, "z");
            GLES20.glUniform1f(zLoc, this.z);

            int texLoc = GLES20.glGetUniformLocation(this.shader.shaderProgram, "textureMatrix");
            GLES20.glUniformMatrix4fv(texLoc, 1, false, this.texMat, 0);

            this.mesh.draw();

            GLES20.glDisable(GLES20.GL_BLEND);

        }

        public void draw(float zBuffer){
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

            this.shader.use();

            int alphaLoc = GLES20.glGetUniformLocation(this.shader.shaderProgram, "alpha");
            GLES20.glUniform1f(alphaLoc, alpha);

            //Transform transform = (Transform) textureComp.entity.getComponent("transform");
            Transform transform = (Transform) textureComp.entity.getComponentFromType("Transform");
            int MVPLoc = GLES20.glGetUniformLocation(this.shader.shaderProgram, "MVP");
            GLES20.glUniformMatrix4fv(MVPLoc, 1, false, transform.getMatrix(), 0);

            int zLoc = GLES20.glGetUniformLocation(this.shader.shaderProgram, "z");
            GLES20.glUniform1f(zLoc, zBuffer);

            int texLoc = GLES20.glGetUniformLocation(this.shader.shaderProgram, "textureMatrix");
            GLES20.glUniformMatrix4fv(texLoc, 1, false, this.texMat, 0);

            this.mesh.draw();

            GLES20.glDisable(GLES20.GL_BLEND);
        }

        public void setRegion(float srcX, float srcY, float srcWidth, float srcHeight){
            float texPosition[] = {srcX, srcY, 0.0f};
            float texScale[] = {srcWidth, srcHeight, 1.0f};
            float[] chMat = new float[16];

            Matrix.setIdentityM(chMat, 0);
            Matrix.translateM(chMat, 0, texPosition[0], texPosition[1], texPosition[2]);
            Matrix.scaleM(chMat, 0, texScale[0], texScale[1], texScale[2]);

            this.texMat = chMat;

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
                                + "uniform mat4 textureMatrix;\n"
                                + "uniform float z;\n"
                                + "void main()\n"
                                + "{\n"
                                //ЭТА СТРОЧКА ДАЕТ БАГ
                                //+ "   gl_Position = vec4(position.x, position.y, z, 1.0) * MVP;\n"
                                + "   gl_Position = MVP * vec4(position.x, position.y, z, 1.0);\n"
                                + "   vertexColor = color;\n"
                                //+ "   TexCoord = vec2(texCoord.x, 1.0 - texCoord.y);\n"
                                + "   TexCoord = (textureMatrix * vec4(vec2(texCoord.x, 1.0 - texCoord.y), 0, 1)).xy;\n"
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
                        1.f,  1.f, 0.0f,  // Верхний правый угол
                        1.f, -1.f, 0.0f,  // Нижний правый угол
                        -1.f, -1.f, 0.0f, // Нижний левый угол
                        -1.f,  1.f, 0.0f  // Верхний левый угол
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
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, img, 0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
                img.recycle();






                this.vertexBuffer = ByteBuffer.allocateDirect(this.vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
                this.vertexBuffer.put(this.vertices);
                this.vertexBuffer.position(0);

                this.colorBuffer = ByteBuffer.allocateDirect(this.colors.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
                this.colorBuffer.put(this.colors);
                this.colorBuffer.position(0);

                this.textureBuffer = ByteBuffer.allocateDirect(this.texCoords.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
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
