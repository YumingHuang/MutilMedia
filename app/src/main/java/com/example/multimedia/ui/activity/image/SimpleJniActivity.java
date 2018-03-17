package com.example.multimedia.ui.activity.image;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.multimedia.R;
import com.example.multimedia.ui.activity.BaseActivity;

/**
 * @author huangyuming
 */
public class SimpleJniActivity extends BaseActivity {

    native void printHello();

    native String printString(String string);

    static {
        System.loadLibrary("hello");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_simple_jni);
    }

    public void javaCallC(View view) {
        printHello();
        String result = printString("adb");
        Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
    }
}