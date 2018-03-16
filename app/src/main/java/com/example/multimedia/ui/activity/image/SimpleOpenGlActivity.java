package com.example.multimedia.ui.activity.image;

import android.os.Bundle;

import com.example.multimedia.ui.activity.BaseActivity;
import com.example.multimedia.ui.widget.MyGLSurfaceView;

/**
 * @author huangyuming
 */
public class SimpleOpenGlActivity extends BaseActivity {

    private MyGLSurfaceView mGlSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGlSurfaceView = new MyGLSurfaceView(this);
        setContentView(mGlSurfaceView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGlSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGlSurfaceView.onResume();
    }

}
