package com.example.multimedia.ui.activity.video;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.multimedia.R;
import com.example.multimedia.ui.activity.BaseActivity;

/**
 * @author huangyuming
 */
public class VideoMediaCodecActivity extends BaseActivity implements View.OnClickListener {

    private Button mEncodeBtn;
    private Button mDecodeBtn;
    private MediaCodec mMediaCodec;
    private MediaExtractor mExtractor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_media_codec);
        init();
    }

    private void init() {
        mEncodeBtn = findViewById(R.id.btn_encode);
        mDecodeBtn = findViewById(R.id.btn_decode);
        mEncodeBtn.setOnClickListener(this);
        mDecodeBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_encode:
                startEncode();
                break;
            case R.id.btn_decode:
                startDecode();
                break;
            default:
                break;
        }
    }

    private void startEncode() {
        mEncodeBtn.setText(getString(R.string.audio_btn_encoding));
        //  File pcmFile = new File(Constants.AUDIO_PATH + "test.pcm");
        //  File accFile = new File(Constants.AUDIO_PATH + "audio_encoded.acc");

    }

    private void startDecode() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
