package com.example.multimedia.ui.activity.audio;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.multimedia.R;
import com.example.multimedia.ui.activity.BaseActivity;
import com.example.multimedia.utils.AudioRecordUtil;
import com.example.multimedia.utils.AudioTrackUtil;

/**
 * @author huangyuming
 */
public class AudioWavRecordActivity extends BaseActivity implements View.OnClickListener {

    private Button mRecordBtn;
    private Button mPlayBtn;
    private TextView mRecordInfo;
    private Boolean mIsRecording = false;
    private Boolean mIsPlaying = false;
    private String mPlayFilePath;
    private long mStartRecorderTime, mStopRecorderTime;
    private AudioTrackUtil mAudioTrack;

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
        mRecordInfo.setText("开始录音");
        AudioRecordUtil.getInstance().startRecord();
        AudioRecordUtil.getInstance().recordData();
    }

    /**
     * 停止录音
     */
    private void stopRecorder() {
        mIsRecording = false;
        mPlayBtn.setEnabled(true);
        mRecordBtn.setText(getString(R.string.audio_btn_start_record));
        mRecordInfo.setText("停止录音");
        AudioRecordUtil.getInstance().stopRecord();
        // pcm -> wav
        mPlayFilePath = AudioRecordUtil.getInstance().convertWaveFile();
    }

    private void startPlay() {
        mIsPlaying = true;
        mRecordInfo.setText("开始播放");
        mPlayBtn.setText(getString(R.string.audio_btn_stop_play));
        mRecordBtn.setEnabled(false);
        mAudioTrack = new AudioTrackUtil(this);
        mAudioTrack.startPlay(mPlayFilePath);
    }


    private void stopPlay() {
        refreshPlayText();
        if (mAudioTrack != null) {
            mAudioTrack.stopPlay();
            mAudioTrack = null;
        }
    }

    public void refreshPlayText() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIsPlaying = false;
                mRecordInfo.setText("播放結束");
                mPlayBtn.setText(getString(R.string.audio_btn_start_play));
                mRecordBtn.setEnabled(true);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
