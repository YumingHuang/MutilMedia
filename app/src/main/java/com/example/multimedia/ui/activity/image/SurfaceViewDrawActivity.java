package com.example.multimedia.ui.activity.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.multimedia.R;
import com.example.multimedia.ui.activity.BaseActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author huangyuming
 */
public class SurfaceViewDrawActivity extends BaseActivity implements SurfaceHolder.Callback, Runnable {
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Canvas mCanvas;
    /*** 控制绘画线程的标志位 */
    private boolean mDrawing;
    private Paint mPaint;
    private Bitmap mBitmap;
    private int mScreenWidth, mScreenHeight;
    /*** 定义并初始化要绘的图形的坐标*/
    private float mCurrentX, mCurrentY;
    private float mBitmapWidth, mBitmapHeight;
    /***定义并初始化坐标移动的变化量*/
    private float mAddX = 2, mAddY = 2;
    private ExecutorService mSingleThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_surface_draw);
        init();
    }

    private void init() {
        mSurfaceView = findViewById(R.id.sf_img);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mScreenWidth = getScreenWidth();
        mScreenHeight = getScreenHeight();
        mBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.android);
        mBitmapWidth = mBitmap.getWidth();
        mBitmapHeight = mBitmap.getHeight();
        mSingleThread = Executors.newSingleThreadExecutor();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mDrawing = true;
        mPaint = new Paint();
        mSingleThread.execute(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mDrawing = false;
        Log.d(TAG, "surfaceDestroyed");
    }

    @Override
    public void run() {
        while (mDrawing) {
            draw();
        }
    }

    private void draw() {
        // 锁定画布
        mCanvas = mSurfaceHolder.lockCanvas();
        if (mCanvas != null) {
            // 初始化画布
            mCanvas.drawColor(Color.WHITE);
            //绘制图形
            mCanvas.drawBitmap(mBitmap, mCurrentX, mCurrentY, mPaint);
        }
        mCurrentX += mAddX;
        mCurrentY += mAddY;
        //下面是矩形的移动路径
        if (mCurrentX < 0) {
            //如果图形左边界坐标超出左屏幕则向右移动
            mAddX = Math.abs(mAddX);
        }
        if (mCurrentX > mScreenWidth - mBitmapWidth) {
            //如果图形右边界坐标超出屏幕的宽度则向左移动
            mAddX = -Math.abs(mAddX);
        }
        if (mCurrentY < 0) {
            mAddY = Math.abs(mAddY);
        }
        if (mCurrentY > mScreenHeight - mBitmapHeight) {
            mAddY = -Math.abs(mAddY);
        }

        if (mCanvas != null) {
            //对画布内容进行提交
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSingleThread != null) {
            mSingleThread.shutdown();
        }
    }
}