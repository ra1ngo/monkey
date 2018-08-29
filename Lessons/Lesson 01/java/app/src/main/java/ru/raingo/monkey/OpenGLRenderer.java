package ru.raingo.monkey;

import android.opengl.GLSurfaceView;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glViewport;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;

class OpenGLRenderer implements GLSurfaceView.Renderer {
    @Override
    public void onDrawFrame(GL10 arg0) {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

    }

    @Override
    public void onSurfaceChanged(GL10 arg0, int width, int height) {
        glViewport(0, 0, width, height);

    }

    @Override
    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
        glClearColor(0f, 1f, 0f, 1f);
    }
}
