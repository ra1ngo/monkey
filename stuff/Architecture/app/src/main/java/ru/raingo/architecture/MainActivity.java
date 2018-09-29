package ru.raingo.architecture;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private GameSurfaceView gsv;
    GameSurfaceView.Texture texture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gsv = new GameSurfaceView(this);
        setContentView(gsv);

        texture = gsv.createTexture();

        GameSurfaceView.Loop loop = new GameSurfaceView.Loop(){
            public void run(){
                texture.draw();
                //Log.e("app", "Thread name: " + Thread.currentThread().getName());
            }
        };

        gsv.setLoop(loop);

    }

    @Override
    protected void onPause() {
        super.onPause();
        gsv.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gsv.onResume();
    }

}
