package com.example.multimedia.gl.render;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

import com.example.multimedia.gl.shape.Triangle;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;

public class MyRender implements Renderer {

    private Context context;

    public MyRender(Context context) {
        this.context = context;
    }

    //定义三角形对象  
    Triangle triangle;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.w("MyRender", "onSurfaceCreated");
        // TODO Auto-generated method stub  
        //First:设置清空屏幕用的颜色，前三个参数对应红绿蓝，最后一个对应alpha  
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        triangle = new Triangle(context);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.w("MyRender", "onSurfaceChanged");
        // TODO Auto-generated method stub  
        //Second:设置视口尺寸，即告诉opengl可以用来渲染的surface大小  
        glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.w("MyRender", "onDrawFrame");
        // TODO Auto-generated method stub  
        //Third:清空屏幕，擦除屏幕上所有的颜色，并用之前glClearColor定义的颜色填充整个屏幕  
        glClear(GL_COLOR_BUFFER_BIT);
        //绘制三角形  
        triangle.draw();
    }
}