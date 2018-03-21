package com.example.multimedia.ui.activity.image;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.multimedia.R;
import com.example.multimedia.ui.activity.BaseActivity;

import java.util.Random;
import java.util.UUID;

/**
 * @author huangyuming
 */
public class SimpleJniActivity extends BaseActivity {

    public String key = "john";
    private static int count = 10;

    native void ntInvokeVoid();
    native int ntInvokeReturn(int a, int b);
    native void ntAccessField();
    native void ntAccessStaticField();
    native void ntAccessMethod();
    native void ntAccessStaticMethod();
    native void ntAccessConstructMethod();

    static {
        System.loadLibrary("hello");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_simple_jni);
    }

    public void invokeVoid(View view) {
        ntInvokeVoid();
        Log.d(TAG, "invokeVoid end");
    }

    public void invokeReturn(View view) {
        Log.d(TAG, "after add = " + ntInvokeReturn(1, 2));
        Log.d(TAG, "invokeReturn end");
    }

    public void invokeJavaFiled(View view) {
        System.out.println("修改前：" + key);
        ntAccessField();
        System.out.println("修改后：" + key);
    }

    public void invokeJavaStaticFiled(View view) {
        System.out.println("修改前：" + count);
        ntAccessStaticField();
        System.out.println("修改后：" + count);
    }

    public void invokeJavaMethod(View view) {
        ntAccessMethod();
        Log.d(TAG, "invokeJavaMethod end");
    }

    public void invokeJavaStaticMethod(View view) {
        ntAccessStaticMethod();
        Log.d(TAG, "invokeJavaStaticMethod end");
    }

    public void invokeJavaConstructMethod(View view) {
        ntAccessConstructMethod();
        Log.d(TAG, "invokeJavaConstructMethod end");
    }

    public int getRandomInt(int value) {
        System.out.println("getRandomInt 执行了");
        show();
        return new Random().nextInt(value);
    }

    public static String getUUID() {
        return UUID.randomUUID().toString();
    }

    public void show() {
        Toast.makeText(SimpleJniActivity.this, "被调用了", Toast.LENGTH_SHORT).show();
    }

}