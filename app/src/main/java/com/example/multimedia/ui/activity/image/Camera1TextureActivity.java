package com.example.multimedia.ui.activity.image;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.multimedia.R;
import com.example.multimedia.ui.activity.BaseActivity;

import java.io.IOException;
import java.util.List;

/**
 * @author huangyuming
 */
public class Camera1TextureActivity extends BaseActivity {

    private TextureView mTextureView;
    private SurfaceTexture mSurfaceTexture;
    private Camera mCamera;
    private int mViewWidth, mViewHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_camera1_texture);

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
        mTextureView = findViewById(R.id.tv_camera);
        mTextureView.setSurfaceTextureListener(new MySurfaceTextureViewListener());
    }

    private class MySurfaceTextureViewListener implements TextureView.SurfaceTextureListener {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            mSurfaceTexture = surface;
            initCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            int displayOrientation = getDisplayOrientation();
            mCamera.setDisplayOrientation(displayOrientation);
            List<Camera.Size> supportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            Camera.Size optimalPreviewSize = getOptimalPreviewSize(supportedPreviewSizes, width, height);
            mCamera.getParameters().setPictureSize(optimalPreviewSize.width, optimalPreviewSize.height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            surface.release();
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (mTextureView != null) {
            mViewWidth = mTextureView.getWidth();
            mViewHeight = mTextureView.getHeight();
        }
    }

    private void initCamera() {
        //默认开启后置
        mCamera = Camera.open();
        //摄像头进行旋转90°
        mCamera.setDisplayOrientation(90);
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            //设置预览照片的大小
            parameters.setPreviewFpsRange(mViewWidth, mViewHeight);
            //设置相机预览照片帧数
            parameters.setPreviewFpsRange(4, 10);
            //设置图片格式
            parameters.setPictureFormat(ImageFormat.JPEG);
            //设置图片的质量
            parameters.set("jpeg-quality", 90);
            //设置照片的大小
            parameters.setPictureSize(mViewWidth, mViewHeight);
            //通过SurfaceView显示预览
            // mCamera.setPreviewDisplay(mSurfaceHolder);
            // 开始预览
            mCamera.startPreview();
        }
    }

    /**
     * 获取最佳的分辨率 而且是16：9的
     *
     * @param sizes
     * @param w
     * @param h
     * @return
     */
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.75;
        double targetRatio = (double) w / h;
        if (sizes == null) {
            return null;
        }
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue;
            }
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    /**
     * 这里是一小段算法算出摄像头转多少都和屏幕方向一致
     */
    private int getDisplayOrientation() {
        WindowManager windowManager = getWindowManager();
        Display defaultDisplay = windowManager.getDefaultDisplay();
        int orientation = defaultDisplay.getOrientation();
        int degress = 0;
        switch (orientation) {
            case Surface.ROTATION_0:
                degress = 0;
                break;
            case Surface.ROTATION_90:
                degress = 90;
                break;
            case Surface.ROTATION_180:
                degress = 180;
                break;
            case Surface.ROTATION_270:
                degress = 270;
                break;
            default:
                break;
        }
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, cameraInfo);
        int result = (cameraInfo.orientation - degress + 360) % 360;
        return result;
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
}
