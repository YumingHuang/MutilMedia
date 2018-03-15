package com.example.multimedia.ui.activity.image;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;

import com.example.multimedia.gl.render.MyRender;
import com.example.multimedia.ui.activity.BaseActivity;

import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;

/**
 * @author huangyuming
 */
public class SimpleOpenGLActivity extends BaseActivity {

    private GLSurfaceView glSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        glSurfaceView = new GLSurfaceView(this);
        // Pick an OpenGL ES 2.0 context.
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(new MyRender(this));
        setContentView(glSurfaceView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }
}
