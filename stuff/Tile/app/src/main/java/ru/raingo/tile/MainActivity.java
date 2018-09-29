package ru.raingo.tile;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {
    public int i =0;
    public float lastTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //флаги
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Monkey monk = new Monkey(this);
        Monkey.GameSurfaceView gsv = monk.createGameSurfaceView(this);
        monk.setView(gsv);

        final Monkey.TileMap map = monk.createTileMap(R.drawable.tileset, gsv, 1, 4, 4, 32, 32);

        //map.tileSet.setTile(0,0,0,11);

        //tileSet.setTile();

        lastTime = System.nanoTime() / 1000000000.0f;
        Monkey.GameLoop loop = new Monkey.GameLoop(){
            public void run(){
                float currentTime = System.nanoTime() / 1000000000.0f;
                if ((currentTime - lastTime) >= 0.5f) {
                    map.tileSet.setTile(0, 0, 0, i);
                    i++;
                    if (i >= 144) i = 0;
                    lastTime = System.nanoTime() / 1000000000.0f;
                }
                map.draw();
            }
        };

        gsv.setGameLoop(loop);
    }
}
