package com.example.multimedia.ui.activity.video;

import android.content.Context;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.multimedia.R;
import com.example.multimedia.common.Constants;
import com.example.multimedia.ui.activity.BaseActivity;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author huangyuming
 */
public class Camera1RecordActivity extends BaseActivity implements View.OnClickListener {
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
    private boolean mIsPlaying = false;
    /*** 是否正在录像 */
    private boolean mIsRecording = false;
    /*** mSurfaceView的宽和高 */
    private int mViewWidth, mViewHeight;
    // 相机的尺寸
    private Camera.Size mSize = null;
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
        // mSurfaceView添加回调
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mSurfaceHolder = holder;
                initCamera();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                // 将holder，这个holder为开始在onCreate里面取得的holder，将它赋给mSurfaceHolder
                mSurfaceHolder = holder;
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                releaseAll();
            }
        });
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
        if (!mIsRecording) {
            mImageView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_stop:
                releaseMediaPlayer();
                if (!mIsRecording) {
                    startRecord();
                } else {
                    stopRecord();
                }
                break;
            case R.id.btn_play_video:
                if (!mIsPlaying) {
                    startPlay();
                }
                break;
            default:
                break;
        }
    }

    private void startPlay() {
        mIsPlaying = true;
        mImageView.setVisibility(View.GONE);
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
        mMediaPlayer.reset();
        Uri uri = Uri.parse(mVideoRecordFile.getAbsolutePath());
        mMediaPlayer = MediaPlayer.create(Camera1RecordActivity.this, uri);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setDisplay(mSurfaceHolder);
        try {
            mMediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMediaPlayer.start();
    }

    private void startRecord() {
        Log.d(TAG, "startRecord");
        mHandler.postDelayed(mRunnable, 1000);
        mImageView.setVisibility(View.GONE);
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }

        Log.d(TAG, "startRecord --1");
        if (mCamera != null) {
            Log.d(TAG, "startRecord --1-1");
            mCamera.unlock();
            mMediaRecorder.setCamera(mCamera);
        }

        try {
            // 这两项需要放在setOutputFormat之前
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

            // Set output file format
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

            // 这两项需要放在setOutputFormat之后
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

            // mMediaRecorder.setVideoSize(640, 480);
            // mMediaRecorder.setVideoFrameRate(30);

            mMediaRecorder.setVideoEncodingBitRate(3 * 1024 * 1024);
            mMediaRecorder.setOrientationHint(90);
            //设置记录会话的最大持续时间（毫秒）
            mMediaRecorder.setMaxDuration(30 * 1000);
            mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

            //创建录音文件
            mVideoRecordFile = new File(Constants.VIDEO_PATH + System.currentTimeMillis() + Constants.VIDEO_MP4);
            if (!mVideoRecordFile.getParentFile().exists()) {
                mVideoRecordFile.getParentFile().mkdirs();
            }
            mVideoRecordFile.createNewFile();

            Log.d(TAG, "path = " + mVideoRecordFile.getAbsolutePath());
            mMediaRecorder.setOutputFile(mVideoRecordFile.getAbsolutePath());
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            Log.d(TAG, "startRecord --2");
            mIsRecording = true;
            mBtnStartStop.setText(getString(R.string.audio_btn_stop_record));
            mBtnPlay.setEnabled(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initCamera() {
        //默认开启后置
        mCamera = Camera.open();

        if (mCamera != null) {
            if (mCamera == null) {
                mCamera = Camera.open();
                //摄像头进行旋转90°
                mCamera.setDisplayOrientation(90);
                Log.d(TAG, "camera.open");
            }
            if (mCamera != null) {
                try {
                    CameraSizeComparator sizeComparator = new CameraSizeComparator();
                    Camera.Parameters parameters = mCamera.getParameters();

                    if (mSize == null) {
                        List<Camera.Size> vSizeList = parameters.getSupportedPreviewSizes();
                        Collections.sort(vSizeList, sizeComparator);

                        for (int num = 0; num < vSizeList.size(); num++) {
                            Camera.Size size = vSizeList.get(num);

                            if (size.width >= 800 && size.height >= 480) {
                                this.mSize = size;
                                break;
                            }
                        }
                        mSize = vSizeList.get(0);

                        List<String> focusModesList = parameters.getSupportedFocusModes();

                        //增加对聚焦模式的判断
                        if (focusModesList.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                        } else if (focusModesList.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                        }
                        mCamera.setParameters(parameters);
                    }
                    mCamera.setPreviewDisplay(mSurfaceHolder);
                    mCamera.startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(Camera1RecordActivity.this, "初始化相机错误",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void stopRecord() {
        try {
            mHandler.removeCallbacks(mRunnable);
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;

            mIsRecording = false;
            mBtnStartStop.setText(getString(R.string.audio_btn_start_record));
            mBtnPlay.setEnabled(true);
            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放正在播放的MediaPlayer
     */
    private void releaseMediaPlayer() {
        if (mIsPlaying) {
            if (mMediaPlayer != null) {
                mIsPlaying = false;
                mMediaPlayer.stop();
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        }
    }

    /**
     * 释放所有引用
     */
    private void releaseAll() {
        mSurfaceView = null;
        mSurfaceHolder = null;
        mHandler.removeCallbacks(mRunnable);
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
            Log.d(TAG, "surfaceDestroyed release mRecorder");
        }
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private class CameraSizeComparator implements Comparator<Camera.Size> {
        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            if (lhs.width == rhs.width) {
                return 0;
            } else if (lhs.width > rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }
    }

}
