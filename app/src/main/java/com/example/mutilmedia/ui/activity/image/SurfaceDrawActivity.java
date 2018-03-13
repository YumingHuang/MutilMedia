package com.example.mutilmedia.ui.activity.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.mutilmedia.R;

/**
 * @author huangyuming
 */
public class SurfaceDrawActivity extends AppCompatActivity implements SurfaceHolder.Callback, Runnable {
    private SurfaceView mSurView;
    private SurfaceHolder mHolder;
    private Canvas mCanvas;
    /*** 控制绘画线程的标志位 */
    private boolean mIsDrawing;
    private Paint mPaint;
    private Bitmap mBitmap;
    private int mWidth;
    private int mHeight;
    /*** 定义并初始化要绘的图形的坐标*/
    private float x = 0, y = 0;
    /*** 定义并初始化要绘的图形的x,y偏移值*/
    private float mSpeedX = 0, mSpeedY = 0;
    /***定义并初始化坐标移动的变化量*/
    private float mAddX = 2, mAddY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_surface_draw);
        init();
    }

    private void init() {
        mWidth = getWindowManager().getDefaultDisplay().getWidth();
        mHeight = getWindowManager().getDefaultDisplay().getHeight();
        mSurView = findViewById(R.id.sf_img);
        //获取SurfaceHolder对象
        mHolder = mSurView.getHolder();
        mHolder.addCallback(this);
        mBitmap = BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsDrawing = true;
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.colorPrimary));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5);
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsDrawing = false;
    }

    @Override
    public void run() {
        while (mIsDrawing) {
            draw();
        }
    }

    private void draw() {
        try {
            // 锁定画布
            mCanvas = mHolder.lockCanvas();
            // 初始化画布
            mCanvas.drawColor(Color.WHITE);
            //绘制图形
            mCanvas.drawBitmap(mBitmap, x, y, mPaint);
            x += mAddX;
            y += mAddY;
            //下面是矩形的移动路径
            if (x < 0) {
                //如果图形左边界坐标超出左屏幕则向右移动
                mAddX = Math.abs(mAddX);
            }
            if (x > mWidth - mSpeedX) {
                //如果图形右边界坐标超出屏幕的宽度则向左移动
                mAddX = -Math.abs(mAddX);
            }
            if (y < 0) {
                mAddY = Math.abs(mAddY);
            }
            if (y > mHeight - mSpeedY) {
                mAddY = -Math.abs(mAddY);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCanvas != null) {
                //对画布内容进行提交
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }
}
