package com.example.multimedia.ui.activity.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.multimedia.R;
import com.example.multimedia.common.Constants;
import com.example.multimedia.ui.activity.BaseActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author huangyuming
 */
public class AudioWavRecordActivity extends BaseActivity implements View.OnClickListener {

    private Button mRecordBtn;
    private Button mPlayBtn;
    private TextView mRecordInfo;
    private Boolean mIsRecording = false;
    private Boolean mIsPlaying = false;
    private ExecutorService mExecutorService;
    private AudioRecord mAudioRecord;
    private File mAudioRecordFile;
    private FileOutputStream mFileOutputStream;
    private long mStartRecorderTime, mStopRecorderTime;
    private byte[] mBuffer;
    /***　buffer值不能太大，避免OOM　*/
    private static final int BUFFER_SIZE = 2048;

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
        mBuffer = new byte[BUFFER_SIZE];
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
                break;
            case R.id.btn_play:
                if (!mIsPlaying) {
                    Log.d(TAG, "startPlay");
                    startPlay();
                } else {
                    Log.d(TAG, "stopPlay");
                    stopPlay();
                }
                break;
            default:
                break;
        }
    }

    private void startRecord() {
        mIsRecording = true;
        mPlayBtn.setEnabled(false);
        mRecordBtn.setText(getString(R.string.audio_btn_stop_record));
        mRecordInfo.setText("");
        //提交后台任务，开始录音
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
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
            //记录开始录音时间
            mStartRecorderTime = System.currentTimeMillis();
            //创建录音文件
            mAudioRecordFile = new File(Constants.AUDIO_PATH + System.currentTimeMillis() + Constants.AUDIO_PCM);
            if (!mAudioRecordFile.getParentFile().exists()) {
                mAudioRecordFile.getParentFile().mkdirs();
            }
            mAudioRecordFile.createNewFile();
            //创建文件输出流
            mFileOutputStream = new FileOutputStream(mAudioRecordFile);
            //配置AudioRecord
            int audioSource = MediaRecorder.AudioSource.MIC;
            //所有android系统都支持
            int sampleRate = 44100;
            //单声道输入
            int channelConfig = AudioFormat.CHANNEL_IN_MONO;
            //PCM_16是所有android系统都支持的
            int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
            //计算AudioRecord内部buffer最小
            int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
            //buffer不能小于最低要求，也不能小于我们每次我们读取的大小。
            mAudioRecord = new AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, Math.max(minBufferSize, BUFFER_SIZE));
            //开始录音
            mAudioRecord.startRecording();

            //循环读取数据，写入输出流中
            while (mIsRecording) {
                Log.d(TAG, "recording");
                //只要还在录音就一直读取
                int read = mAudioRecord.read(mBuffer, 0, BUFFER_SIZE);
                if (read <= 0) {
                    return false;
                } else {
                    mFileOutputStream.write(mBuffer, 0, read);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (mAudioRecord != null) {
                mAudioRecord.release();
            }
        }
        return true;
    }

    /**
     * 关闭录音
     *
     * @return
     */
    private boolean doStop() {
        Log.d(TAG, "doStop");
        //停止录音，关闭文件输出流
        try {
            // 防止某些手机崩溃，例如联想
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        //记录结束时间，统计录音时长
        mStopRecorderTime = System.currentTimeMillis();
        //大于3秒算成功，在主线程更新UI
        final int second = (int) (mStopRecorderTime - mStartRecorderTime) / 1000;
        Log.d(TAG, "second = " + second);
        if (second > 3) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRecordInfo.setText("录制成功：" + second + "秒");
                }
            });
        } else {
            return false;
        }
        return true;
    }

    /**
     * 释放上一次的录音
     */
    private void releaseRecorder() {
        if (mAudioRecord != null) {
            mAudioRecord.release();
            mAudioRecord = null;
        }
    }

    /**
     * 录音失败逻辑
     */
    private void recorderFail() {
        mAudioRecordFile = null;
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
        mIsRecording = false;
        mPlayBtn.setEnabled(true);
        mRecordBtn.setText(getString(R.string.audio_btn_start_record));
        //提交后台任务，停止录音
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (!doStop()) {
                    recorderFail();
                }
            }
        });
    }

    private void startPlay() {
        mPlayBtn.setText(getString(R.string.audio_btn_stop_play));
        mRecordBtn.setEnabled(false);
        Log.d(TAG, "mIsPlaying = " + mIsPlaying);
        if (!mIsPlaying) {
            mExecutorService.submit(new Runnable() {
                @Override
                public void run() {
                    doPlay(mAudioRecordFile);
                }
            });
        } else {
            Toast.makeText(this, "正在播放", Toast.LENGTH_SHORT).show();
        }
    }

    private void doPlay(File audioFile) {
        if (audioFile != null) {
            Log.d(TAG, "doPlay");
            //配置播放器
            //音乐类型，扬声器播放
            int streamType = AudioManager.STREAM_MUSIC;
            //录音时采用的采样频率，所以播放时同样的采样频率
            int sampleRate = 44100;
            //单声道，和录音时设置的一样
            int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
            //录音时使用16bit，所以播放时同样采用该方式
            int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
            //流模式
            int mode = AudioTrack.MODE_STREAM;

            //计算最小buffer大小
            int minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat);

            //构造AudioTrack  不能小于AudioTrack的最低要求，也不能小于我们每次读的大小
            AudioTrack audioTrack = new AudioTrack(streamType, sampleRate, channelConfig, audioFormat,
                    Math.max(minBufferSize, BUFFER_SIZE), mode);
            audioTrack.play();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRecordInfo.setText("正在播放");
                }
            });
            //从文件流读数据
            FileInputStream inputStream = null;
            try {
                //循环读数据，写到播放器去播放
                inputStream = new FileInputStream(audioFile);

                //循环读数据，写到播放器去播放
                int read;
                //只要没读完，循环播放
                mIsPlaying = true;
                while ((read = inputStream.read(mBuffer)) > 0 && mIsPlaying) {
                    Log.i(TAG, "read = " + read);
                    int ret = audioTrack.write(mBuffer, 0, read);
                    //检查write的返回值，处理错误
                    switch (ret) {
                        case AudioTrack.ERROR_INVALID_OPERATION:
                        case AudioTrack.ERROR_BAD_VALUE:
                        case AudioManager.ERROR_DEAD_OBJECT:
                            playFail();
                            return;
                        default:
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                playFail();
            } finally {
                //关闭文件输入流
                if (inputStream != null) {
                    closeStream(inputStream);
                }
                //播放器释放
                resetQuietly(audioTrack);
            }
            //循环读数据，写到播放器去播放
            //错误处理，防止闪退
        }
    }

    private void stopPlay() {
        mIsPlaying = false;
        mRecordInfo.setText("播放結束");
        mPlayBtn.setText(getString(R.string.audio_btn_start_play));
        mRecordBtn.setEnabled(true);
    }

    /**
     * 关闭输入流
     *
     * @param inputStream
     */
    private void closeStream(FileInputStream inputStream) {
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetQuietly(AudioTrack audioTrack) {
        Log.d(TAG, "resetQuietly");
        stopPlay();
        try {
            audioTrack.stop();
            audioTrack.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 播放失败
     */
    private void playFail() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRecordInfo.setText("播放失败");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mExecutorService != null) {
            mExecutorService.shutdownNow();
        }
        if (mAudioRecord != null) {
            try {
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
