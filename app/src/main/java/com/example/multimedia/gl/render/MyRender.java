package com.example.multimedia.gl.render;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

import com.example.multimedia.gl.shape.SquareV2;
import com.example.multimedia.gl.shape.Triangle;
import com.example.multimedia.ui.activity.BaseActivity;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;

public class MyRender implements Renderer {

    private Context mContext;
    /*** 定义三角形对象 */
    private Triangle mTriangle;
    private SquareV2 mSquare;

    public MyRender(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(BaseActivity.TAG, "onSurfaceCreated");
        //First:设置清空屏幕用的颜色，前三个参数对应红绿蓝，最后一个对应alpha  
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        mTriangle = new Triangle(mContext);
        // mSquare = new SquareV2(mContext);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(BaseActivity.TAG, "onSurfaceChanged");
        //Second:设置视口尺寸，即告诉openGl可以用来渲染的surface大小
        GLES20.glViewport(0, 0, width, height);
        mTriangle.change(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.d(BaseActivity.TAG, "onDrawFrame");
        //Third:清空屏幕，擦除屏幕上所有的颜色，并用之前glClearColor定义的颜色填充整个屏幕
        GLES20.glClear(GL_COLOR_BUFFER_BIT);
        //绘制三角形
        mTriangle.preDraw();
        mTriangle.draw();
        //  mSquare.draw();
    }

    public float getAngle() {
        return mTriangle.getAngle();
    }

    public void setAngle(float angle) {
        mTriangle.setAngle(angle);
    }
}