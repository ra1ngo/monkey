package ru.raingo.raiser;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    Monkey.Screen currentScreen;
    Monkey.TextureAnim texture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("app", "New Activity");
        //флаги
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Monkey monk = new Monkey(this);
        this.currentScreen = monk.createScreen(this);
        monk.setScreen(R.id.container, currentScreen);

        texture = monk.createTextureAnim(R.drawable.elli_walk, 1.0f, 1.0f, 4, 4, 0.25f);
        float[][] animLoc = {{0,0}, {0.25f,0}, {0.5f,0}, {0.75f,0}};
        texture.addAnim("walk", animLoc);
        texture.setAnim("walk");

        final Monkey.Entity elli = currentScreen.game.createEntity();
        elli.addComponent(texture);
        //elli.addComponent(monk.createTransform(elli, texture.regWidth, texture.regHeight));
        elli.addComponent(monk.createTransform(elli, 32, 32));

        final Monkey.GameLoop loop = new Monkey.GameLoop(){
            public void run(){
                //texture1.draw(currentScreen.getDeltaTime());
                currentScreen.game.currentStage.scene.draw();
                currentScreen.game.root.update();
                Monkey.Transform tr = (Monkey.Transform) elli.getComponentFromType("Transform");
                tr.translate(new float[]{0.f, 0.01f, 0.f} );
            }
        };

        this.currentScreen.game.setGameLoop(loop);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.currentScreen.onPause();
        Log.e("app", "Activity onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.currentScreen.onResume();
        Log.e("app", "Activity onResume");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e("app", "Activity onConfigurationChanged");
        /*// Проверяем ориентацию экрана
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

        }*/
    }
}
