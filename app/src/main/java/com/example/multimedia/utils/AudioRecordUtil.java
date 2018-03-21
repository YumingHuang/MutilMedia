package com.example.multimedia.utils;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.example.multimedia.common.Constants;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Executors;

public class AudioRecordUtil {

    private static AudioRecordUtil mInstance;
    private AudioRecord mRecorder;
    /*** 录音源 */
    private static int mAudioSource = MediaRecorder.AudioSource.MIC;
    /*** 录音的采样频率 */
    private static int mAudioRate = 44100;
    /*** 录音的声道，单声道 */
    private static int mAudioChannel = AudioFormat.CHANNEL_IN_MONO;
    /*** 量化的深度 */
    private static int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    /*** 缓存的大小 */
    private static int mBufferSize = AudioRecord.getMinBufferSize(mAudioRate, mAudioChannel, mAudioFormat);
    /*** 记录播放状态 */
    private boolean mRecording = false;
    /*** 数字信号数组 */
    private byte[] mNoteArray;
    /*** PCM文件 */
    private File mPcmFile;
    /*** WAV文件 */
    private File mWavFile;
    /*** 文件输出流 */
    private OutputStream mOs;
    /*** 文件根目录 */
    private String mBasePath = Constants.AUDIO_PATH;
    /*** wav文件目录 */
    private String mOutFileName = mBasePath + System.currentTimeMillis() + Constants.AUDIO_WAV;
    /*** pcm文件目录 */
    private String mInFileName = mBasePath + System.currentTimeMillis() + Constants.AUDIO_PCM;

    private AudioRecordUtil() {
        createFile();//创建文件
        mRecorder = new AudioRecord(mAudioSource, mAudioRate, mAudioChannel, mAudioFormat, mBufferSize);
    }

    public synchronized static AudioRecordUtil getInstance() {
        if (mInstance == null) {
            mInstance = new AudioRecordUtil();
        }
        return mInstance;
    }

    /**
     * 读取录音数字数据线程
     */
    class WriteThread implements Runnable {
        @Override
        public void run() {
            writeData();
        }
    }

    /**
     * 开始录音
     */
    public void startRecord() {
        mRecording = true;
        mRecorder.startRecording();
    }

    /**
     * 记录数据，写入文件
     */
    public void recordData() {
        Executors.newSingleThreadExecutor().submit(new WriteThread());
    }

    /**
     * 停止录音
     */
    public void stopRecord() {
        mRecording = false;
        mRecorder.stop();
    }

    /**
     * 将数据写入文件夹,文件的写入没有做优化
     */
    public void writeData() {
        mNoteArray = new byte[mBufferSize];
        //建立文件输出流
        try {
            mOs = new BufferedOutputStream(new FileOutputStream(mPcmFile));

            while (mRecording) {
                int recordSize = mRecorder.read(mNoteArray, 0, mBufferSize);
                if (recordSize > 0) {
                    mOs.write(mNoteArray, 0, recordSize);
                }
            }
            if (mOs != null) {
                mOs.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 这里得到可播放的音频文件
     */
    public String convertWaveFile() {
        FileInputStream in;
        FileOutputStream out;
        long totalAudioLen;
        long totalDataLen;
        long longSampleRate = AudioRecordUtil.mAudioRate;
        int channels = 1;
        long byteRate = 16 * AudioRecordUtil.mAudioRate * channels / 8;
        byte[] data = new byte[mBufferSize];
        try {
            in = new FileInputStream(mInFileName);
            out = new FileOutputStream(mOutFileName);
            totalAudioLen = in.getChannel().size();
            //由于不包括RIFF和WAV
            totalDataLen = totalAudioLen + 36;
            writeWaveFileHeader(out, totalAudioLen, totalDataLen, longSampleRate, channels, byteRate);
            while (in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mOutFileName;
    }

    /**
     * 任何一种文件在头部添加相应的头文件才能够确定的表示这种文件的格式，wave是RIFF文件结构，每一部分为一个chunk，其中有RIFF WAVE chunk，
     * FMT Chunk，Fact chunk,Data chunk,其中Fact chunk是可以选择的，
     */
    private void writeWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate,
                                     int channels, long byteRate) throws IOException {
        byte[] header = new byte[44];
        // RIFF
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        //数据大小
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        //WAVE
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';

        //FMT Chunk
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        //过渡字节
        header[15] = ' ';
        //数据大小 4 bytes: size of 'fmt ' chunk
        header[16] = 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        //编码方式 10H为PCM编码格式 format = 1
        header[20] = 1;
        header[21] = 0;
        //通道数
        header[22] = (byte) channels;
        header[23] = 0;
        //采样率，每个通道的播放速度
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        //音频数据传送速率,采样率*通道数*采样深度/8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
        header[32] = (byte) (1 * 16 / 8);
        header[33] = 0;
        //每个样本的数据位数
        header[34] = 16;
        header[35] = 0;

        //Data chunk
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
        Log.d("TAG", "write head success");
    }

    /**
     * 创建文件夹,首先创建目录，然后创建对应的文件
     */
    public void createFile() {
        File baseFile = new File(mBasePath);
        if (!baseFile.exists()) {
            baseFile.mkdirs();
        }
        mPcmFile = new File(mInFileName);
        mWavFile = new File(mOutFileName);
        if (mPcmFile.exists()) {
            mPcmFile.delete();
        }
        if (mWavFile.exists()) {
            mWavFile.delete();
        }
        try {
            mPcmFile.createNewFile();
            mWavFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}