package ru.raingo.monkey;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    Monkey.Screen currentScreen;
    Monkey.Texture texture1;
    Monkey.Texture texture2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //флаги
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Monkey monk = new Monkey(this);
        this.currentScreen = monk.createScreen();
        monk.setScreen(R.id.container, currentScreen);

        texture1 = monk.createTexture(R.drawable.elli_walk, 1.1f, 0.5f);
        //std::cout << "Texture1: " << texture1->path << std::endl;
        texture2 = monk.createTexture(R.drawable.img, 1.0f, 1.0f);

        Monkey.GameLoop loop = new Monkey.GameLoop(){
            public void run(){
                texture1.draw();
                texture2.draw();
                //Log.e("app", "Thread name: " + Thread.currentThread().getName());
            }
        };

        this.currentScreen.setGameLoop(loop);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.currentScreen.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.currentScreen.onResume();
    }
}
