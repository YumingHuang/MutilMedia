package com.example.multimedia.ui.activity.video;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.multimedia.R;
import com.example.multimedia.common.Constants;
import com.example.multimedia.ui.activity.BaseActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author huangyuming
 */
public class Camera2RecordActivity extends BaseActivity implements View.OnClickListener {
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    ///为了使照片竖直显示
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private Button mBtnStartStop;
    private Button mBtnPlay;
    private TextView mTextView;
    private ImageView mImageView;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;
    private Camera.Parameters mParameters;
    private MediaPlayer mMediaPlayer;
    private MediaRecorder mMediaRecorder;
    /*** 是否正在播放录像 */
    private boolean mPlaying = false;
    /*** 是否正在录像 */
    private boolean mRecording = false;
    /*** mSurfaceView的宽和高 */
    private int mViewWidth, mViewHeight;
    /***  相机的尺寸 */
    private Camera.Size mSize = null;
    /*** 摄像头Id ,0为后 1为前 */
    private String mCameraID;
    private Handler mChildHandler, mMainHandler;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraDevice mCameraDevice;
    private CameraManager mCameraManager;
    private File mVideoRecordFile;
    private int mTimeText = 0;
    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mTimeText++;
            mTextView.setText(mTimeText + "");
            mHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_camera1);
        initView();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        mImageView = findViewById(R.id.iv_perView);
        mBtnStartStop = findViewById(R.id.btn_start_stop);
        mBtnPlay = findViewById(R.id.btn_play_video);
        mTextView = findViewById(R.id.tv_time);
        mBtnStartStop.setOnClickListener(this);
        mBtnPlay.setOnClickListener(this);
        mSurfaceView = findViewById(R.id.sf_video);
        mSurfaceHolder = mSurfaceView.getHolder();
        // mSurfaceView 不需要自己的缓冲区，必须设置
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mSurfaceHolder = holder;
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
    }

    public void initCamera2() {
        HandlerThread handlerThread = new HandlerThread("Camera2");
        handlerThread.start();
        mChildHandler = new Handler(handlerThread.getLooper());
        mMainHandler = new Handler(getMainLooper());
        //后摄像头
        mCameraID = "" + CameraCharacteristics.LENS_FACING_FRONT;

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
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (mSurfaceView != null) {
            mViewWidth = mSurfaceView.getWidth();
            mViewHeight = mSurfaceView.getHeight();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mRecording) {
            mImageView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_stop:
                releaseMediaPlayer();
                if (!mRecording) {
                    startRecord();
                } else {
                    stopRecord();
                }
                break;
            case R.id.btn_play_video:
                if (!mPlaying) {
                    startPlay();
                }
                break;
            default:
                break;
        }
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
                Camera2RecordActivity.this.mCameraDevice = null;
            }
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            //发生错误
            Log.d(TAG, "onError");
            Toast.makeText(Camera2RecordActivity.this, "摄像头开启失败", Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * 开始预览
     */
    private void takePreview() {
        try {
            setUpMediaRecorder();
            // 创建预览需要的CaptureRequest.Builder
            final CaptureRequest.Builder previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            // 将SurfaceView的surface作为CaptureRequest.Builder的目标
            List<Surface> surfaces = new ArrayList<>();
            // Set up Surface for the camera preview
            Surface previewSurface = mSurfaceHolder.getSurface();
            surfaces.add(previewSurface);
            previewRequestBuilder.addTarget(previewSurface);

            // Set up Surface for the MediaRecorder
            Surface recorderSurface = mMediaRecorder.getSurface();
            surfaces.add(recorderSurface);
            previewRequestBuilder.addTarget(recorderSurface);
            // 自动对焦
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // 自动曝光
            previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            // 获取手机方向
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            // 根据设备方向计算设置照片的方向
            previewRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            // 创建CameraCaptureSession，该对象负责管理处理预览请求和拍照请求
            mCameraDevice.createCaptureSession(surfaces,
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
                            Toast.makeText(Camera2RecordActivity.this, "配置失败", Toast.LENGTH_SHORT).show();
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

    private void startRecord() {
        Log.d(TAG, "startRecord");
        mRecording = true;
        mBtnStartStop.setText(getString(R.string.audio_btn_stop_record));
        mBtnPlay.setEnabled(false);
        mHandler.postDelayed(mRunnable, 1000);
        mImageView.setVisibility(View.GONE);
        if (mMediaRecorder != null) {
            mMediaRecorder.start();
        }
    }

    /**
     * 设置媒体录制器的配置参数
     * <p>
     * 音频，视频格式，文件路径，频率，编码格式等等
     */
    private void setUpMediaRecorder() {
        //创建录音文件
        mVideoRecordFile = new File(Constants.VIDEO_PATH + System.currentTimeMillis() +
                Constants.VIDEO_MP4);
        if (!mVideoRecordFile.getParentFile().exists()) {
            mVideoRecordFile.getParentFile().mkdirs();
        }
        try {
            mVideoRecordFile.createNewFile();
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setOutputFile(mVideoRecordFile.getAbsolutePath());
            mMediaRecorder.setVideoEncodingBitRate(10000000);
            //每秒30帧
            mMediaRecorder.setVideoFrameRate(30);
            // mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight())
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecord() {
        mRecording = false;
        mHandler.removeCallbacks(mRunnable);
        mBtnStartStop.setText(getString(R.string.audio_btn_start_record));
        mBtnPlay.setEnabled(true);
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        releaseCamera();
    }

    private void startPlay() {
        mPlaying = true;
        mImageView.setVisibility(View.GONE);
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
        mMediaPlayer.reset();
        Log.d(TAG, "path = " + mVideoRecordFile.getAbsolutePath());
        Uri uri = Uri.parse("file://" + mVideoRecordFile.getAbsolutePath());
        mMediaPlayer = MediaPlayer.create(Camera2RecordActivity.this, uri);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setDisplay(mSurfaceHolder);
        try {
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMediaPlayer.start();
    }

    /**
     * 释放正在播放的MediaPlayer
     */
    private void releaseMediaPlayer() {
        if (mPlaying) {
            if (mMediaPlayer != null) {
                mPlaying = false;
                mMediaPlayer.stop();
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        }
    }
}
