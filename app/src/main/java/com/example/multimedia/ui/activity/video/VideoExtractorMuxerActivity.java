package com.example.multimedia.ui.activity.video;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.multimedia.R;
import com.example.multimedia.common.Constants;
import com.example.multimedia.ui.activity.BaseActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoExtractorMuxerActivity extends BaseActivity implements View.OnClickListener {
    private Button mExtraVideo;
    private Button mExtraVoice;
    private Button mMixVideoVoice;

    private MediaExtractor mMediaExtractor;
    private MediaMuxer mMediaMuxer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_extractor_muxer);
        initView();
        mMediaExtractor = new MediaExtractor();
    }

    private void initView() {
        mExtraVideo = findViewById(R.id.btn_extra_video);
        mExtraVoice = findViewById(R.id.btn_extra_voice);
        mMixVideoVoice = findViewById(R.id.btn_mix);
        mExtraVideo.setOnClickListener(this);
        mExtraVoice.setOnClickListener(this);
        mMixVideoVoice.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_extra_video:
                muxerMedia();
                break;
            case R.id.btn_extra_voice:
                muxerAudio();
                break;
            case R.id.btn_mix:
                combineVideo();
                break;
            default:
                break;
        }
    }

    /**
     * 分隔出audio信道和video信道
     */
    private void extractorMedia() {
        FileOutputStream videoOutputStream = null;
        FileOutputStream audioOutputStream = null;
        try {
            //分离的视频文件
            File videoFile = new File(Constants.VIDEO_PATH, "output_video.mp4");
            //分离的音频文件
            File audioFile = new File(Constants.VIDEO_PATH, "output_audio");
            videoOutputStream = new FileOutputStream(videoFile);
            audioOutputStream = new FileOutputStream(audioFile);
            //源文件
            mMediaExtractor.setDataSource(Constants.VIDEO_PATH + "/input.mp4");
            //信道总数
            int trackCount = mMediaExtractor.getTrackCount();
            int audioTrackIndex = -1;
            int videoTrackIndex = -1;
            for (int i = 0; i < trackCount; i++) {
                MediaFormat trackFormat = mMediaExtractor.getTrackFormat(i);
                String mineType = trackFormat.getString(MediaFormat.KEY_MIME);
                //视频信道
                if (mineType.startsWith("video/")) {
                    videoTrackIndex = i;
                }
                //音频信道
                if (mineType.startsWith("audio/")) {
                    audioTrackIndex = i;
                }
            }

            ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
            //切换到视频信道
            mMediaExtractor.selectTrack(videoTrackIndex);
            while (true) {
                int readSampleCount = mMediaExtractor.readSampleData(byteBuffer, 0);
                if (readSampleCount < 0) {
                    break;
                }
                //保存视频信道信息
                byte[] buffer = new byte[readSampleCount];
                byteBuffer.get(buffer);
                videoOutputStream.write(buffer);
                byteBuffer.clear();
                mMediaExtractor.advance();
            }
            //切换到音频信道
            mMediaExtractor.selectTrack(audioTrackIndex);
            while (true) {
                int readSampleCount = mMediaExtractor.readSampleData(byteBuffer, 0);
                if (readSampleCount < 0) {
                    break;
                }
                //保存音频信息
                byte[] buffer = new byte[readSampleCount];
                byteBuffer.get(buffer);
                audioOutputStream.write(buffer);
                byteBuffer.clear();
                mMediaExtractor.advance();
            }
            Toast.makeText(VideoExtractorMuxerActivity.this, "分离成功", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(VideoExtractorMuxerActivity.this, "分离失败", Toast.LENGTH_SHORT).show();
        } finally {
            mMediaExtractor.release();
            try {
                videoOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 分离视频
     */
    private void muxerMedia() {
        mMediaExtractor = new MediaExtractor();
        int videoIndex = -1;
        try {
            mMediaExtractor.setDataSource(Constants.VIDEO_PATH + "input.mp4");
            int trackCount = mMediaExtractor.getTrackCount();
            Log.d(TAG, "trackCount = " + trackCount);
            for (int i = 0; i < trackCount; i++) {
                MediaFormat trackFormat = mMediaExtractor.getTrackFormat(i);
                String mimeType = trackFormat.getString(MediaFormat.KEY_MIME);
                // 取出视频的信号
                if (mimeType.startsWith("video/")) {
                    videoIndex = i;
                    break;
                }
            }
            //切换道视频信号的信道
            mMediaExtractor.selectTrack(videoIndex);
            MediaFormat trackFormat = mMediaExtractor.getTrackFormat(videoIndex);
            mMediaMuxer = new MediaMuxer(Constants.VIDEO_PATH + "output_video_v2.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            //追踪此信道
            int trackIndex = mMediaMuxer.addTrack(trackFormat);
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 500);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            mMediaMuxer.start();
            long videoSampleTime;
            //获取每帧的之间的时间
            {
                //读取一帧数据
                mMediaExtractor.readSampleData(byteBuffer, 0);
                //skip first I frame
                if (mMediaExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC) {
                    mMediaExtractor.advance();
                }
                mMediaExtractor.readSampleData(byteBuffer, 0);
                long firstVideoPTS = mMediaExtractor.getSampleTime();
                mMediaExtractor.advance();
                mMediaExtractor.readSampleData(byteBuffer, 0);
                long SecondVideoPTS = mMediaExtractor.getSampleTime();
                videoSampleTime = Math.abs(SecondVideoPTS - firstVideoPTS);
                Log.d(TAG, "videoSampleTime is " + videoSampleTime);
            }
            //重新切换此信道，不然上面跳过了3帧,造成前面的帧数模糊
            mMediaExtractor.unselectTrack(videoIndex);
            mMediaExtractor.selectTrack(videoIndex);
            while (true) {
                //读取帧之间的数据
                int readSampleSize = mMediaExtractor.readSampleData(byteBuffer, 0);
                if (readSampleSize < 0) {
                    break;
                }
                mMediaExtractor.advance();
                bufferInfo.size = readSampleSize;
                bufferInfo.offset = 0;
                bufferInfo.flags = mMediaExtractor.getSampleFlags();
                bufferInfo.presentationTimeUs += videoSampleTime;
                //写入帧的数据
                mMediaMuxer.writeSampleData(trackIndex, byteBuffer, bufferInfo);
            }
            //release
            mMediaMuxer.stop();
            mMediaExtractor.release();
            mMediaMuxer.release();
            Toast.makeText(VideoExtractorMuxerActivity.this, "分离视频成功", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "finish");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 分离音频
     */
    private void muxerAudio() {
        mMediaExtractor = new MediaExtractor();
        int audioIndex = -1;
        try {
            mMediaExtractor.setDataSource(Constants.VIDEO_PATH + "input.mp4");
            int trackCount = mMediaExtractor.getTrackCount();
            for (int i = 0; i < trackCount; i++) {
                MediaFormat trackFormat = mMediaExtractor.getTrackFormat(i);
                if (trackFormat.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
                    audioIndex = i;
                }
            }
            mMediaExtractor.selectTrack(audioIndex);
            MediaFormat trackFormat = mMediaExtractor.getTrackFormat(audioIndex);
            mMediaMuxer = new MediaMuxer(Constants.VIDEO_PATH + "output_audio_v2", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int writeAudioIndex = mMediaMuxer.addTrack(trackFormat);
            mMediaMuxer.start();
            ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            long stampTime = 0;
            //获取帧之间的间隔时间
            {
                mMediaExtractor.readSampleData(byteBuffer, 0);
                if (mMediaExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC) {
                    mMediaExtractor.advance();
                }
                mMediaExtractor.readSampleData(byteBuffer, 0);
                long secondTime = mMediaExtractor.getSampleTime();
                mMediaExtractor.advance();
                mMediaExtractor.readSampleData(byteBuffer, 0);
                long thirdTime = mMediaExtractor.getSampleTime();
                stampTime = Math.abs(thirdTime - secondTime);
                Log.e(TAG, stampTime + "");
            }

            mMediaExtractor.unselectTrack(audioIndex);
            mMediaExtractor.selectTrack(audioIndex);
            while (true) {
                int readSampleSize = mMediaExtractor.readSampleData(byteBuffer, 0);
                if (readSampleSize < 0) {
                    break;
                }
                mMediaExtractor.advance();

                bufferInfo.size = readSampleSize;
                bufferInfo.flags = mMediaExtractor.getSampleFlags();
                bufferInfo.offset = 0;
                bufferInfo.presentationTimeUs += stampTime;
                // 直接使用getSampleTime?
                // bufferInfo.presentationTimeUs = mMediaExtractor.getSampleTime()

                mMediaMuxer.writeSampleData(writeAudioIndex, byteBuffer, bufferInfo);
            }
            mMediaMuxer.stop();
            mMediaMuxer.release();
            mMediaExtractor.release();
            Toast.makeText(VideoExtractorMuxerActivity.this, "分离音频成功", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "finish");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 合成视频
     */
    private void combineVideo() {
        try {
            MediaExtractor videoExtractor = new MediaExtractor();
            videoExtractor.setDataSource(Constants.VIDEO_PATH + "output_video_v2.mp4");
            MediaFormat videoFormat = null;
            int videoTrackIndex = -1;
            int videoTrackCount = videoExtractor.getTrackCount();
            for (int i = 0; i < videoTrackCount; i++) {
                videoFormat = videoExtractor.getTrackFormat(i);
                String mimeType = videoFormat.getString(MediaFormat.KEY_MIME);
                if (mimeType.startsWith("video/")) {
                    videoTrackIndex = i;
                    break;
                }
            }

            MediaExtractor audioExtractor = new MediaExtractor();
            audioExtractor.setDataSource(Constants.VIDEO_PATH + "output_audio_v2");
            MediaFormat audioFormat = null;
            int audioTrackIndex = -1;
            int audioTrackCount = audioExtractor.getTrackCount();
            for (int i = 0; i < audioTrackCount; i++) {
                audioFormat = audioExtractor.getTrackFormat(i);
                String mimeType = audioFormat.getString(MediaFormat.KEY_MIME);
                if (mimeType.startsWith("audio/")) {
                    audioTrackIndex = i;
                    break;
                }
            }

            videoExtractor.selectTrack(videoTrackIndex);
            audioExtractor.selectTrack(audioTrackIndex);

            MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();

            MediaMuxer mediaMuxer = new MediaMuxer(Constants.VIDEO_PATH + "output.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int writeVideoTrackIndex = mediaMuxer.addTrack(videoFormat);
            int writeAudioTrackIndex = mediaMuxer.addTrack(audioFormat);
            mediaMuxer.start();

            ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
            long sampleTime = 0;
            {
                videoExtractor.readSampleData(byteBuffer, 0);
                if (videoExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC) {
                    videoExtractor.advance();
                }
                videoExtractor.readSampleData(byteBuffer, 0);
                long secondTime = videoExtractor.getSampleTime();
                videoExtractor.advance();
                long thirdTime = videoExtractor.getSampleTime();
                sampleTime = Math.abs(thirdTime - secondTime);
            }
            videoExtractor.unselectTrack(videoTrackIndex);
            videoExtractor.selectTrack(videoTrackIndex);

            while (true) {
                int readVideoSampleSize = videoExtractor.readSampleData(byteBuffer, 0);
                if (readVideoSampleSize < 0) {
                    break;
                }
                videoBufferInfo.size = readVideoSampleSize;
                videoBufferInfo.presentationTimeUs += sampleTime;
                videoBufferInfo.offset = 0;
                videoBufferInfo.flags = videoExtractor.getSampleFlags();
                mediaMuxer.writeSampleData(writeVideoTrackIndex, byteBuffer, videoBufferInfo);
                videoExtractor.advance();
            }

            while (true) {
                int readAudioSampleSize = audioExtractor.readSampleData(byteBuffer, 0);
                if (readAudioSampleSize < 0) {
                    break;
                }

                audioBufferInfo.size = readAudioSampleSize;
                audioBufferInfo.presentationTimeUs += sampleTime;
                audioBufferInfo.offset = 0;
                audioBufferInfo.flags = videoExtractor.getSampleFlags();
                mediaMuxer.writeSampleData(writeAudioTrackIndex, byteBuffer, audioBufferInfo);
                audioExtractor.advance();
            }

            mediaMuxer.stop();
            mediaMuxer.release();
            videoExtractor.release();
            audioExtractor.release();
            Toast.makeText(VideoExtractorMuxerActivity.this, "合成视频成功", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}