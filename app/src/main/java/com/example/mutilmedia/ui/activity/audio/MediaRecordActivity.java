package com.example.mutilmedia.ui.activity.audio;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mutilmedia.R;
import com.example.mutilmedia.ui.activity.BaseActivity;

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
    private Boolean mIsRecording = false;
    private Boolean mIsPlaying = false;
    private ExecutorService mExecutorService;
    private MediaRecorder mMediaRecorder;
    private MediaPlayer mediaPlayer;
    private File mRecorderFile;
    private long mStartRecorderTime, mStopRecorderTime;

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
                if (!mIsRecording) {
                    Log.d(TAG, "startRecord");
                    startRecord();
                } else {
                    Log.d(TAG, "stopRecorder");
                    stopRecorder();
                }
                mIsRecording = !mIsRecording;
                break;
            case R.id.btn_play:
                if (!mIsPlaying) {
                    Log.d(TAG, "startPlay");
                    startPlay();
                } else {
                    Log.d(TAG, "stopPlay");
                    stopPlay();
                }
                mIsPlaying = !mIsPlaying;
                break;
            default:
                break;
        }
    }

    private void startRecord() {
        mPlayBtn.setEnabled(false);
        mRecordBtn.setText(getString(R.string.audio_btn_stop_record));
        mRecordInfo.setText("");
        //提交后台任务，开始录音
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                //释放上一次的录音
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
            //创建MediaRecorder
            mMediaRecorder = new MediaRecorder();
            //创建录音文件
            mRecorderFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/recorder_demo/" + System.currentTimeMillis() + ".m4a");
            if (!mRecorderFile.getParentFile().exists()) {
                mRecorderFile.getParentFile().mkdirs();
            }
            mRecorderFile.createNewFile();
            //配置MediaRecorder
            //从麦克风采集
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //保存文件为MP4格式
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            //所有android系统都支持的适中采样的频率
            mMediaRecorder.setAudioSamplingRate(44100);
            //通用的AAC编码格式
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
            //设置音质频率
            mMediaRecorder.setAudioEncodingBitRate(96000);
            //设置文件录音的位置
            mMediaRecorder.setOutputFile(mRecorderFile.getAbsolutePath());
            //开始录音
            mMediaRecorder.prepare();
            Log.d(TAG, "prepare");
            mMediaRecorder.start();
            Log.d(TAG, "start");
            mStartRecorderTime = System.currentTimeMillis();
        } catch (Exception e) {
            Toast.makeText(this, "录音失败，请重试", Toast.LENGTH_SHORT).show();
            return false;
        }
        //记录开始录音时间，用于统计时长，小于3秒中，录音不发送
        return true;
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
                Log.d(TAG, "IllegalState");
            }
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        mStopRecorderTime = System.currentTimeMillis();
        final int second = (int) (mStopRecorderTime - mStartRecorderTime) / 1000;
        Log.d(TAG, "second = " + second);
        //按住时间小于3秒钟，算作录取失败，不进行发送
        if (second < 3) {
            return false;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRecordInfo.setText("录制成功：" + second + "秒");
            }
        });

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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRecordInfo.setText("录音失败请重新录音");
            }
        });
    }

    /**
     * 停止录音
     */
    private void stopRecorder() {
        mPlayBtn.setEnabled(true);
        mRecordBtn.setText(getString(R.string.audio_btn_start_record));
        //提交后台任务，停止录音
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

    private void startPlay() {
        mPlayBtn.setText(getString(R.string.audio_btn_stop_play));
        mRecordBtn.setEnabled(false);
        if (!mIsPlaying) {
            mExecutorService.submit(new Runnable() {
                @Override
                public void run() {
                    doPlay(mRecorderFile);
                }
            });
        } else {
            Toast.makeText(this, "正在播放", Toast.LENGTH_SHORT).show();
        }
    }

    private void doPlay(File audioFile) {
        try {
            //配置播放器 MediaPlayer
            mediaPlayer = new MediaPlayer();
            //设置声音文件
            try {
                mediaPlayer.setDataSource(audioFile.getAbsolutePath());
            } catch (Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRecordInfo.setText("文件打开失败");
                    }
                });
                return;
            }
            //配置音量,中等音量
            mediaPlayer.setVolume(1, 1);
            //播放是否循环
            mediaPlayer.setLooping(false);

            //设置监听回调 播放完毕
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlay();
                }
            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    stopPlay();
                    Toast.makeText(MediaRecordActivity.this, "播放失败", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });

            //设置播放
            mediaPlayer.prepare();
            mediaPlayer.start();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRecordInfo.setText("正在播放");
                }
            });
            //异常处理，防止闪退
        } catch (Exception e) {
            e.printStackTrace();
            stopPlay();
        }
    }

    private void stopPlay() {
        mRecordInfo.setText("播放結束");
        mPlayBtn.setText(getString(R.string.audio_btn_start_play));
        mRecordBtn.setEnabled(true);
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
    }
}
