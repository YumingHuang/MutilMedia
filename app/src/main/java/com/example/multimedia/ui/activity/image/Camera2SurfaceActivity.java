package com.example.multimedia.ui.activity.image;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.multimedia.R;
import com.example.multimedia.ui.activity.BaseActivity;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author huangyuming
 */
public class Camera2SurfaceActivity extends BaseActivity implements View.OnClickListener {
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    ///为了使照片竖直显示
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;
    private ImageView mShowImage;
    private CameraManager mCameraManager;
    private Handler mChildHandler, mMainHandler;
    /*** 摄像头Id 0 为后  1 为前 */
    private String mCameraID;
    private ImageReader mImageReader;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraDevice mCameraDevice;
    /*** mSurfaceView的宽和高 */
    private int mViewWidth, mViewHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_camera2);
        initView();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        mShowImage = findViewById(R.id.iv_show_camera2_activity);
        //mSurfaceView
        mSurfaceView = findViewById(R.id.surface_view_camera2_activity);
        mSurfaceHolder = mSurfaceView.getHolder();
        // mSurfaceView 不需要自己的缓冲区
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // mSurfaceView添加回调
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                initCamera2();
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
     * 初始化相机
     */
    private void initCamera2() {
        HandlerThread handlerThread = new HandlerThread("Camera2");
        handlerThread.start();
        mChildHandler = new Handler(handlerThread.getLooper());
        mMainHandler = new Handler(getMainLooper());
        //后摄像头
        mCameraID = "" + CameraCharacteristics.LENS_FACING_FRONT;
        mImageReader = ImageReader.newInstance(1080, 1920, ImageFormat.JPEG, 1);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Log.d(TAG, "onImageAvailable");
                //可以在这里处理拍照得到的临时照片 例如，写入本地
                mCameraDevice.close();
                mSurfaceView.setVisibility(View.GONE);
                mShowImage.setVisibility(View.VISIBLE);
                // 拿到拍照照片数据
                Image image = reader.acquireNextImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                //由缓冲区存入字节数组
                buffer.get(bytes);
                final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bitmap != null) {
                    mShowImage.setImageBitmap(bitmap);
                }
            }
        }, mMainHandler);
        //获取摄像头管理
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            //已经授权,打开摄像头
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                mCameraManager.openCamera(mCameraID, stateCallback, mMainHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放Camera资源
     */
    private void releaseCamera() {
        if (null != mCameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    @Override
    public void onClick(View v) {
        takePicture();
    }

    /**
     * 摄像头创建监听
     */
    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //打开摄像头
            Log.d(TAG, "onOpened");
            mCameraDevice = camera;
            takePreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            //关闭摄像头
            Log.d(TAG, "onDisconnected");
            if (null != mCameraDevice) {
                mCameraDevice.close();
                Camera2SurfaceActivity.this.mCameraDevice = null;
            }
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            //发生错误
            Log.d(TAG, "onError");
            Toast.makeText(Camera2SurfaceActivity.this, "摄像头开启失败", Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * 开始预览
     */
    private void takePreview() {
        try {
            // 创建预览需要的CaptureRequest.Builder
            final CaptureRequest.Builder previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            // 将SurfaceView的surface作为CaptureRequest.Builder的目标
            previewRequestBuilder.addTarget(mSurfaceHolder.getSurface());
            // 创建CameraCaptureSession，该对象负责管理处理预览请求和拍照请求
            mCameraDevice.createCaptureSession(Arrays.asList(mSurfaceHolder.getSurface(), mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            Log.d(TAG, "onConfigured");
                            if (null == mCameraDevice) {
                                return;
                            }
                            // 当摄像头已经准备好时，开始显示预览
                            mCameraCaptureSession = cameraCaptureSession;
                            try {
                                // 自动对焦
                                previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                // 打开闪光灯
                                previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                                // 显示预览
                                CaptureRequest previewRequest = previewRequestBuilder.build();
                                mCameraCaptureSession.setRepeatingRequest(previewRequest, null, mChildHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                            Log.d(TAG, "onConfigureFailed");
                            Toast.makeText(Camera2SurfaceActivity.this, "配置失败", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onReady(@NonNull CameraCaptureSession session) {
                            super.onReady(session);
                            Log.d(TAG, "onReady");
                        }

                        @Override
                        public void onActive(@NonNull CameraCaptureSession session) {
                            super.onActive(session);
                            Log.d(TAG, "onActive");
                        }

                        @Override
                        public void onClosed(@NonNull CameraCaptureSession session) {
                            super.onClosed(session);
                            Log.d(TAG, "onClosed");
                        }

                        @Override
                        public void onCaptureQueueEmpty(@NonNull CameraCaptureSession session) {
                            super.onCaptureQueueEmpty(session);
                            Log.d(TAG, "onCaptureQueueEmpty");
                        }

                        @Override
                        public void onSurfacePrepared(@NonNull CameraCaptureSession session, @NonNull Surface surface) {
                            super.onSurfacePrepared(session, surface);
                            Log.d(TAG, "onSurfacePrepared");
                        }
                    }, mChildHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 拍照
     */
    private void takePicture() {
        if (mCameraDevice == null) {
            return;
        }
        // 创建拍照需要的CaptureRequest.Builder
        final CaptureRequest.Builder captureRequestBuilder;
        try {
            captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            // 将imageReader的surface作为CaptureRequest.Builder的目标
            captureRequestBuilder.addTarget(mImageReader.getSurface());
            // 自动对焦
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // 自动曝光
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            // 获取手机方向
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            // 根据设备方向计算设置照片的方向
            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            //拍照
            CaptureRequest mCaptureRequest = captureRequestBuilder.build();
            mCameraCaptureSession.capture(mCaptureRequest, null, mChildHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
