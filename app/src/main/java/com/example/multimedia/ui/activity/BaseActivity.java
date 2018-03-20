package com.example.multimedia.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * @author huangyuming
 */
public class BaseActivity extends AppCompatActivity {
    public static final String TITLE = "title";
    public static final String TAG = "media";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, this.getClass().getSimpleName() + " onCreate");
        if (getIntent() != null) {
            setTitle(getIntent().getStringExtra(TITLE));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, this.getClass().getSimpleName() + " onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, this.getClass().getSimpleName() + " onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, this.getClass().getSimpleName() + " onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, this.getClass().getSimpleName() + " onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, this.getClass().getSimpleName() + " onDestroy");
    }

    protected int getScreenWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    protected int getScreenHeight() {
        return getResources().getDisplayMetrics().heightPixels;
    }
}
