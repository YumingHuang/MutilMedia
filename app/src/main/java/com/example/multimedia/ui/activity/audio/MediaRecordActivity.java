package com.example.multimedia.ui.activity.audio;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.multimedia.R;
import com.example.multimedia.common.Constants;
import com.example.multimedia.ui.activity.BaseActivity;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author huangyuming
 */
public class MediaRecordActivity extends BaseActivity implements View.OnClickListener {

    private Button mRecordBtn;
    private Button mPlayBtn;
    private TextView mRecordInfo;
    private Boolean mRecording = false;
    private Boolean mPlaying = false;
    private ExecutorService mExecutorService;
    private MediaRecorder mMediaRecorder;
    private MediaPlayer mediaPlayer;
    private File mRecorderFile;
    private long mStartRecorderTime, mStopRecorderTime;
    public static final int RECORD_START = 1;
    public static final int RECORD_STOP = 2;
    public static final int RECORD_ERROR = 3;
    public static final int PLAYING = 4;
    public static final int PLAY_ERROR = 5;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RECORD_START:
                    mRecordInfo.setText("正在录制");
                    break;
                case RECORD_STOP:
                    int second = (int) (mStopRecorderTime - mStartRecorderTime) / 1000;
                    mRecordInfo.setText("录制成功：" + second + "秒");
                    break;
                case RECORD_ERROR:
                    mRecordInfo.setText("录音失败请重新录音");
                    break;
                case PLAYING:
                    mPlaying = true;
                    mRecordInfo.setText("正在播放");
                    break;
                case PLAY_ERROR:
                    mPlaying = false;
                    mRecordInfo.setText("文件打开失败");
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_media_record);
        init();
    }

    private void init() {
        mRecordInfo = findViewById(R.id.tv_record_info);
        mRecordBtn = findViewById(R.id.btn_record);
        mPlayBtn = findViewById(R.id.btn_play);
        mRecordBtn.setOnClickListener(this);
        mPlayBtn.setOnClickListener(this);
        mPlayBtn.setEnabled(false);
        mExecutorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_record:
                if (!mRecording) {
                    startRecord();
                } else {
                    stopRecorder();
                }
                refreshRecordText(mRecording);
                mRecording = !mRecording;
                break;
            case R.id.btn_play:
                // 手动停止正在播放
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    refreshPlayText(mPlaying);
                    stopPlay();
                    return;
                }
                if (!mPlaying) {
                    startPlay();
                } else {
                    stopPlay();
                }
                refreshPlayText(mPlaying);
                break;
            default:
                break;
        }
    }

    private void refreshRecordText(boolean isRecording) {
        if (!isRecording) {
            Log.d(TAG, "startRecord");
        } else {
            Log.d(TAG, "stopRecord");
        }
        mPlayBtn.setEnabled(isRecording);
        mRecordBtn.setText(isRecording ? getString(R.string.audio_btn_start_record)
                : getString(R.string.audio_btn_stop_record));
    }

    private void refreshPlayText(boolean isPlaying) {
        if (!isPlaying) {
            Log.d(TAG, "startPlay");
        } else {
            Log.d(TAG, "stopPlay");
        }
        mRecordBtn.setEnabled(isPlaying);
        mPlayBtn.setText(isPlaying ? getString(R.string.audio_btn_start_play)
                : getString(R.string.audio_btn_stop_play));
        if (isPlaying) {
            mRecordInfo.setText("播放結束");
        }
    }

    private void startRecord() {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                releaseRecorder();
                //开始录音
                if (!doStart()) {
                    recorderFail();
                }
            }
        });
    }

    /**
     * 启动录音
     *
     * @return
     */
    private boolean doStart() {
        try {
            //创建录音文件
            mRecorderFile = new File(Constants.AUDIO_PATH + System.currentTimeMillis() + Constants.AUDIO_M4A);
            if (!mRecorderFile.getParentFile().exists()) {
                mRecorderFile.getParentFile().mkdirs();
            }
            mRecorderFile.createNewFile();

            //创建MediaRecorder
            mMediaRecorder = new MediaRecorder();
            //从麦克风采集
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //设置封装输出格式 ,需要在编码格式设置前面
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            //所有android系统都支持的适中采样的频率44.1kHz
            mMediaRecorder.setAudioSamplingRate(44100);
            //设置编码格式
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            //设置音质频率
            mMediaRecorder.setAudioEncodingBitRate(96000);
            //设置文件录音的位置
            mMediaRecorder.setOutputFile(mRecorderFile.getAbsolutePath());
            //开始录音
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            Log.d(TAG, "media recorder start");
            mHandler.sendEmptyMessage(RECORD_START);
            mStartRecorderTime = System.currentTimeMillis();
        } catch (Exception e) {
            Toast.makeText(this, "录音失败，请重试", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void stopRecorder() {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (!doStop()) {
                    recorderFail();
                }
                releaseRecorder();
            }
        });
    }

    /**
     * 关闭录音
     *
     * @return
     */
    private boolean doStop() {
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        mStopRecorderTime = System.currentTimeMillis();
        final int second = (int) (mStopRecorderTime - mStartRecorderTime) / 1000;
        Log.d(TAG, "record time  = " + second);
        if (second < 3) {
            return false;
        }
        mHandler.sendEmptyMessage(RECORD_STOP);
        return true;
    }

    /**
     * 释放上一次的录音
     */
    private void releaseRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    /**
     * 录音失败逻辑
     */
    private void recorderFail() {
        mRecorderFile = null;
        mHandler.sendEmptyMessage(RECORD_ERROR);
    }

    ///////////////////////////////////////////////////////////////////////

    private void startPlay() {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                doPlay(mRecorderFile);
            }
        });
    }

    private void doPlay(File audioFile) {
        try {
            mediaPlayer = new MediaPlayer();
            //设置声音文件
            mediaPlayer.setDataSource(audioFile.getAbsolutePath());
            //配置音量,中等音量
            mediaPlayer.setVolume(1, 1);
            //播放是否循环
            mediaPlayer.setLooping(false);
            //设置监听回调 播放完毕
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    refreshPlayText(mPlaying);
                    stopPlay();
                }
            });
            //设置错误监听回调
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    refreshPlayText(mPlaying);
                    stopPlay();
                    Toast.makeText(MediaRecordActivity.this, "播放失败", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
            //设置播放
            mediaPlayer.prepare();
            mediaPlayer.start();
            mHandler.sendEmptyMessage(PLAYING);
        } catch (Exception e) {
            e.printStackTrace();
            mHandler.sendEmptyMessage(PLAY_ERROR);
            stopPlay();
        }
    }

    private void stopPlay() {
        mPlaying = false;
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //当activity关闭时，停止这个线程，防止内存泄漏
        mExecutorService.shutdownNow();
        releaseRecorder();
        stopPlay();
    }
}
