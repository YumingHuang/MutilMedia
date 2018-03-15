package com.example.multimedia.ui.activity.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.multimedia.R;
import com.example.multimedia.ui.activity.BaseActivity;

import java.io.IOException;

/**
 * @author huangyuming
 */
public class Camera1SurfaceActivity extends BaseActivity implements View.OnClickListener {

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;
    private ImageView mShowImage;
    /*** mSurfaceView的宽和高 */
    private int mViewWidth, mViewHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_camera1_surface);
        initView();
        if (!isCameraCanUse()) {
            finish();
            Toast.makeText(this, "Camera cannot not use!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 初始化控件
     */
    private void initView() {
        mShowImage = findViewById(R.id.iv_show_camera1_activity);
        //mSurfaceView
        mSurfaceView = findViewById(R.id.surface_view_camera1_activity);
        mSurfaceHolder = mSurfaceView.getHolder();
        // mSurfaceView 不需要自己的缓冲区
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // mSurfaceView添加回调
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                initCamera();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                releaseCamera();
            }
        });
        //设置点击监听
        mSurfaceView.setOnClickListener(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (mSurfaceView != null) {
            mViewWidth = mSurfaceView.getWidth();
            mViewHeight = mSurfaceView.getHeight();
        }
    }

    /**
     * 判断摄像头是否可用
     *
     * @return
     */
    public boolean isCameraCanUse() {
        boolean canUse = false;
        Camera mCamera = null;
        try {
            mCamera = Camera.open(0);
            Camera.Parameters mParameters = mCamera.getParameters();
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            canUse = false;
        }

        if (mCamera != null) {
            mCamera.release();
            canUse = true;
        }
        return canUse;
    }

    /**
     * 初始化相机
     */
    private void initCamera() {
        releaseCamera();
        //默认开启后置
        mCamera = Camera.open();
        //摄像头进行旋转90°
        mCamera.setDisplayOrientation(90);
        if (mCamera != null) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                //设置预览照片的大小
                parameters.setPreviewFpsRange(mViewWidth, mViewHeight);
                //设置相机预览照片帧数
                parameters.setPreviewFpsRange(4, 10);
                //设置图片格式
                parameters.setPictureFormat(ImageFormat.JPEG);
                parameters.setPreviewFormat(ImageFormat.NV21);
                //设置图片的质量
                parameters.set("jpeg-quality", 90);
                //设置照片的大小
                parameters.setPictureSize(mViewWidth, mViewHeight);
                //通过SurfaceView显示预览
                mCamera.setPreviewDisplay(mSurfaceHolder);
                //开始预览
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 释放Camera资源
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
        }
    }

    @Override
    public void onClick(View v) {
        if (mCamera != null) {
            //自动对焦后拍照
            mCamera.autoFocus(autoFocusCallback);
        }
    }

    /**
     * 自动对焦 对焦成功后 就进行拍照
     */
    @SuppressWarnings("AliDeprecation")
    private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            //对焦成功
            if (success) {
                //按下快门
                camera.takePicture(new Camera.ShutterCallback() {
                    @Override
                    public void onShutter() {
                        //按下快门瞬间的操作
                        Log.d(TAG, "onShutter");
                    }
                }, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        //是否保存原始图片的信息
                        Log.d(TAG, "autoFocusCallback onPictureTaken");
                    }
                }, pictureCallback);
            }
        }
    };

    /**
     * 获取图片
     */
    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "pictureCallback onPictureTaken");
            final Bitmap resource = BitmapFactory.decodeByteArray(data, 0, data.length);
            if (resource == null) {
                Toast.makeText(Camera1SurfaceActivity.this, "拍照失败", Toast.LENGTH_SHORT).show();
            }
            final Matrix matrix = new Matrix();
            matrix.setRotate(90);
            final Bitmap bitmap = Bitmap.createBitmap(resource, 0, 0, resource.getWidth(), resource
                    .getHeight(), matrix, true);
            if (bitmap != null && mShowImage != null && mShowImage.getVisibility() == View.GONE) {
                mCamera.stopPreview();
                mShowImage.setVisibility(View.VISIBLE);
                mSurfaceView.setVisibility(View.GONE);
                Toast.makeText(Camera1SurfaceActivity.this, "拍照", Toast.LENGTH_SHORT).show();
                mShowImage.setImageBitmap(bitmap);
            }
        }
    };
}
