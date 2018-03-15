package com.example.multimedia.ui.activity.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.multimedia.R;
import com.example.multimedia.common.Constants;
import com.example.multimedia.ui.activity.BaseActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author huangyuming
 */
public class Camera1TextureActivity extends BaseActivity implements View.OnClickListener {

    private TextureView mTextureView;
    private SurfaceTexture mSurfaceTexture;
    private ImageView mShowImage;
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
        mShowImage = findViewById(R.id.iv_show_camera1_activity);
        mTextureView = findViewById(R.id.tv_camera);
        mTextureView.setSurfaceTextureListener(new MySurfaceTextureViewListener());
        mTextureView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (mCamera != null) {
            //自动对焦后拍照
            mCamera.autoFocus(autoFocusCallback);
        }
    }

    private class MySurfaceTextureViewListener implements TextureView.SurfaceTextureListener {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.d(TAG, "onSurfaceTextureAvailable");
            mSurfaceTexture = surface;
            initCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Log.d(TAG, "onSurfaceTextureSizeChanged");
            int displayOrientation = getDisplayOrientation();
            mCamera.setDisplayOrientation(displayOrientation);
            List<Camera.Size> supportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            Camera.Size optimalPreviewSize = getOptimalPreviewSize(supportedPreviewSizes, width, height);
            mCamera.getParameters().setPictureSize(optimalPreviewSize.width, optimalPreviewSize.height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            Log.d(TAG, "onSurfaceTextureDestroyed");
            surface.release();
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            Log.d(TAG, "onSurfaceTextureUpdated");
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
            //通过设置SurfaceTexture，类似 surfaceView的setPreviewDisplay(mSurfaceHolder);
            try {
                mCamera.setPreviewTexture(mSurfaceTexture);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
                Toast.makeText(Camera1TextureActivity.this, "拍照失败", Toast.LENGTH_SHORT).show();
            }
            final Matrix matrix = new Matrix();
            matrix.setRotate(90);
            final Bitmap bitmap = Bitmap.createBitmap(resource, 0, 0, resource.getWidth(), resource
                    .getHeight(), matrix, true);
            if (bitmap != null && mShowImage != null && mShowImage.getVisibility() == View.GONE) {
                mCamera.stopPreview();
                mShowImage.setVisibility(View.VISIBLE);
                mTextureView.setVisibility(View.GONE);
                Toast.makeText(Camera1TextureActivity.this, "拍照", Toast.LENGTH_SHORT).show();
                mShowImage.setImageBitmap(bitmap);
                saveBitmap(Camera1TextureActivity.this, bitmap);
            }
        }
    };

    /**
     * 保存bitmap到本地
     *
     * @param context
     * @param mBitmap
     */
    public static void saveBitmap(Context context, Bitmap mBitmap) {
        String savePath;
        File filePic;
        try {
            filePic = new File(Constants.IMAGE_PATH + System.currentTimeMillis() + Constants.IMAGE_JPG);
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断摄像头是否可用
     *
     * @return boolean
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
