package com.example.mutilmedia.ui.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.mutilmedia.ui.activity.audio.AudioActivity;
import com.example.mutilmedia.ui.activity.image.ImageActivity;

import java.util.ArrayList;

/**
 * @author huangyuming
 */
public class BaseActivity extends AppCompatActivity {
    public static final String TITLE = "title";
    public static final String TAG = "media";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null) {
            setTitle(getIntent().getStringExtra(TITLE));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
