package com.example.multimedia.utils;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import com.example.multimedia.common.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioTrackUtil {

    private static AudioTrackUtil mInstance;
    private AudioTrack mAudioTrack;
    private ExecutorService mSingleThread;

    /*** 声音播放类型 */
    private static int mStreamType = AudioManager.STREAM_MUSIC;
    /*** 录音时采用的采样频率，所以播放时同样的采样频率 */
    private static int mAudioSimpleRate = 44100;
    /*** 播放的声道OUT，单声道,录音IN */
    private static int mAudioChannel = AudioFormat.CHANNEL_OUT_MONO;
    /*** 量化的深度 */
    private static int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    /*** 流模式 */
    private static int mMode = AudioTrack.MODE_STREAM;
    /*** 缓存的大小 */
    private static int mBufferSize = AudioTrack.getMinBufferSize(mAudioSimpleRate, mAudioChannel, mAudioFormat);
    /*** 记录播放状态 */
    private boolean mIsPlaying = false;
    /*** 记录是否播放失败 */
    private boolean mIsPlayError = false;
    /*** 语音播放文件 */
    private File mPlayFile;
    private byte[] mBuffer;
    /***　buffer值不能太大，避免OOM　*/
    private static final int BUFFER_SIZE = 2048;

    private AudioTrackUtil() {
        mBuffer = new byte[BUFFER_SIZE];
        //构造AudioTrack  不能小于AudioTrack的最低要求，也不能小于我们每次读的大小
        mAudioTrack = new AudioTrack(mStreamType, mAudioSimpleRate, mAudioChannel, mAudioFormat,
                Math.max(mBufferSize, BUFFER_SIZE), mMode);
    }

    public synchronized static AudioTrackUtil getInstance() {
        if (mInstance == null) {
            mInstance = new AudioTrackUtil();
        }
        return mInstance;
    }

    /**
     * 读取录音数字数据线程
     */
    class WriteThread implements Runnable {
        @Override
        public void run() {
            Log.d("TAG", "1--");
            mIsPlayError = false;
            try {
                mAudioTrack.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //从文件流读数据
            Log.d("TAG", "2--");
            FileInputStream inputStream = null;
            try {
                //循环读数据，写到播放器去播放
                inputStream = new FileInputStream(mPlayFile);
                int read;
                //只要没读完，循环播放
                mIsPlaying = true;
                while ((read = inputStream.read(mBuffer)) > 0 && mIsPlaying) {
                    Log.d("TAG", "read = " + read);
                    int ret = mAudioTrack.write(mBuffer, 0, read);
                    //检查write的返回值，处理错误
                    switch (ret) {
                        case AudioTrack.ERROR_INVALID_OPERATION:
                        case AudioTrack.ERROR_BAD_VALUE:
                        case AudioManager.ERROR_DEAD_OBJECT:
                            mIsPlayError = true;
                            return;
                        default:
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                mIsPlayError = true;
            } finally {
                //关闭文件输入流
                if (inputStream != null) {
                    closeStream(inputStream);
                }
                //TODO　播放完毕或者异常错误需要通知Activity
                resetQuietly(mAudioTrack);
            }
        }
    }

    /**
     * 开始播放
     */
    public void startPlay(String audioFile) {
        mIsPlaying = true;
        mPlayFile = new File(audioFile);
        mSingleThread = Executors.newSingleThreadExecutor();
        mSingleThread.submit(new WriteThread());
    }

    /**
     * 停止播放
     */
    public void stopPlay() {
        mIsPlaying = false;
        if (mSingleThread != null) {
            mSingleThread.shutdownNow();
        }
    }

    /**
     * 关闭输入流
     *
     * @param inputStream inputStream
     */
    private void closeStream(FileInputStream inputStream) {
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetQuietly(AudioTrack audioTrack) {
        Log.d("TAG", "resetQuietly");
        stopPlay();
        try {
            audioTrack.stop();
            audioTrack.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}