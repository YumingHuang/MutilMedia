package com.example.multimedia.utils;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class AudioCodecUtil {

    private static final String TAG = "AudioCodecUtil";
    private String encodeType;
    private String srcPath;
    private String dstPath;
    private MediaCodec mediaDecode;
    private MediaCodec mediaEncode;
    private MediaExtractor mediaExtractor;
    private ByteBuffer[] decodeInputBuffers;
    private ByteBuffer[] decodeOutputBuffers;
    private ByteBuffer[] encodeInputBuffers;
    private ByteBuffer[] encodeOutputBuffers;
    private MediaCodec.BufferInfo decodeBufferInfo;
    private MediaCodec.BufferInfo encodeBufferInfo;
    private FileOutputStream fos;
    private BufferedOutputStream bos;
    private FileInputStream fis;
    private BufferedInputStream bis;
    //PCM数据块容器
    private ArrayList<byte[]> chunkPCMDataContainer;
    private OnCompleteListener onCompleteListener;
    private OnProgressListener onProgressListener;
    private long fileTotalSize;
    private long decodeSize;
    private boolean mIsCodeOver = false;
    private static AudioCodecUtil mAudioCodec;

    public static AudioCodecUtil getInstance() {
        if (mAudioCodec == null) {
            mAudioCodec = new AudioCodecUtil();
        }
        return mAudioCodec;
    }

    /**
     * 设置编码器类型
     *
     * @param encodeType encodeType(本类支持ACC编码)
     */
    public void setEncodeType(String encodeType) {
        this.encodeType = encodeType;
    }

    /**
     * 设置输入输出文件位置
     *
     * @param srcPath 输入Path
     * @param dstPath 输出Path
     */
    public void setIOPath(String srcPath, String dstPath) {
        this.srcPath = srcPath;
        this.dstPath = dstPath;
    }

    /**
     * 此类已经过封装
     * 调用prepare方法 会初始化Decode 、Encode 、输入输出流 等一些列操作
     */
    public void prepare() {
        if (encodeType == null) {
            throw new IllegalArgumentException("encodeType can't be null");
        }

        if (srcPath == null) {
            throw new IllegalArgumentException("srcPath can't be null");
        }

        if (dstPath == null) {
            throw new IllegalArgumentException("dstPath can't be null");
        }

        try {
            fos = new FileOutputStream(new File(dstPath));
            bos = new BufferedOutputStream(fos, 200 * 1024);
            File file = new File(srcPath);
            fileTotalSize = file.length();
        } catch (IOException e) {
            e.printStackTrace();
        }
        chunkPCMDataContainer = new ArrayList<>();
        initMediaDecode();//解码器    

        if (TextUtils.equals(encodeType, MediaFormat.MIMETYPE_AUDIO_AAC)) {
            initAACMediaEncode();//AAC编码器    
        } else if (TextUtils.equals(encodeType, MediaFormat.MIMETYPE_AUDIO_MPEG)) {
            initMPEGMediaEncode();//mp3编码器    
        }

    }

    /**
     * 初始化解码器
     */
    private void initMediaDecode() {
        try {
            mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(srcPath);
            //遍历媒体轨道 此处我们传入的是音频文件，所以也就只有一条轨道
            for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
                MediaFormat format = mediaExtractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("audio")) {
                    mediaExtractor.selectTrack(i);
                    mediaDecode = MediaCodec.createDecoderByType(mime);
                    mediaDecode.configure(format, null, null, 0);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mediaDecode == null) {
            Log.e(TAG, "create mediaDecode failed");
            return;
        }
        //启动MediaCodec ，等待传入数据
        mediaDecode.start();
        //MediaCodec在此ByteBuffer[]中获取输入数据
        decodeInputBuffers = mediaDecode.getInputBuffers();
        //MediaCodec将解码后的数据放到此ByteBuffer[]中 我们可以直接在这里面得到PCM数据
        decodeOutputBuffers = mediaDecode.getOutputBuffers();
        //用于描述解码得到的byte[]数据的相关信息
        decodeBufferInfo = new MediaCodec.BufferInfo();
        showLog("buffers:" + decodeInputBuffers.length);
    }


    /**
     * 初始化AAC编码器
     */
    private void initAACMediaEncode() {
        try {
            //参数对应-> mime type、采样率、声道数
            MediaFormat encodeFormat = MediaFormat.createAudioFormat(encodeType, 44100, 2);
            //比特率
            encodeFormat.setInteger(MediaFormat.KEY_BIT_RATE, 96000);
            encodeFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            encodeFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 100 * 1024);
            mediaEncode = MediaCodec.createEncoderByType(encodeType);
            mediaEncode.configure(encodeFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mediaEncode == null) {
            Log.e(TAG, "create mediaEncode failed");
            return;
        }
        mediaEncode.start();
        encodeInputBuffers = mediaEncode.getInputBuffers();
        encodeOutputBuffers = mediaEncode.getOutputBuffers();
        encodeBufferInfo = new MediaCodec.BufferInfo();
    }

    /**
     * 初始化MPEG编码器
     */
    private void initMPEGMediaEncode() {

    }

    /**
     * 开始转码
     * 音频数据{@link #srcPath}先解码成PCM  PCM数据在编码成想要得到的{@link #encodeType}音频格式
     * mp3->PCM->aac
     */
    public void startAsync() {
        showLog("start");
        new Thread(new DecodeRunnable()).start();
        new Thread(new EncodeRunnable()).start();
    }

    /**
     * 将PCM数据存入{@link #chunkPCMDataContainer}
     *
     * @param pcmChunk PCM数据块
     */
    private void putPCMData(byte[] pcmChunk) {
        //记得加锁
        synchronized (AudioCodecUtil.class) {
            chunkPCMDataContainer.add(pcmChunk);
        }
    }

    /**
     * 在Container中{@link #chunkPCMDataContainer}取出PCM数据
     *
     * @return PCM数据块
     */
    private byte[] getPCMData() {
        //记得加锁
        synchronized (AudioCodecUtil.class) {
            showLog("getPCM:" + chunkPCMDataContainer.size());
            if (chunkPCMDataContainer.isEmpty()) {
                return null;
            }
            //每次取出index 0 的数据
            byte[] pcmChunk = chunkPCMDataContainer.get(0);
            //取出后将此数据remove掉 既能保证PCM数据块的取出顺序 又能及时释放内存
            chunkPCMDataContainer.remove(pcmChunk);
            return pcmChunk;
        }
    }


    /**
     * 解码{@link #srcPath}音频文件 得到PCM数据块
     */
    private void srcAudioFormatToPCM() {
        for (int i = 0; i < decodeInputBuffers.length - 1; i++) {
            //获取可用的inputBuffer -1代表一直等待，0表示不等待 建议-1,避免丢帧
            int inputIndex = mediaDecode.dequeueInputBuffer(-1);
            if (inputIndex < 0) {
                mIsCodeOver = true;
                return;
            }
            //拿到inputBuffer
            ByteBuffer inputBuffer = decodeInputBuffers[inputIndex];
            //清空之前传入inputBuffer内的数据
            inputBuffer.clear();
            //MediaExtractor读取数据到inputBuffer中
            int sampleSize = mediaExtractor.readSampleData(inputBuffer, 0);
            //小于0 代表所有数据已读取完成
            if (sampleSize < 0) {
                mIsCodeOver = true;
            } else {
                //通知MediaDecode解码刚刚传入的数据
                mediaDecode.queueInputBuffer(inputIndex, 0, sampleSize, 0, 0);
                //MediaExtractor移动到下一取样处
                mediaExtractor.advance();
                decodeSize += sampleSize;
            }
        }

        //获取解码得到的byte[]数据 参数BufferInfo上面已介绍 10000同样为等待时间 同上-1代表一直等待，0代表不等待。此处单位为微秒
        //此处建议不要填-1 有些时候并没有数据输出，那么他就会一直卡在这等待
        int outputIndex = mediaDecode.dequeueOutputBuffer(decodeBufferInfo, 10000);

        ByteBuffer outputBuffer;
        byte[] chunkPCM;
        //每次解码完成的数据不一定能一次吐出 所以用while循环，保证解码器吐出所有数据
        while (outputIndex >= 0) {
            //拿到用于存放PCM数据的Buffer
            outputBuffer = decodeOutputBuffers[outputIndex];
            //BufferInfo内定义了此数据块的大小
            chunkPCM = new byte[decodeBufferInfo.size];
            //将Buffer内的数据取出到字节数组中
            outputBuffer.get(chunkPCM);
            //数据取出后一定记得清空此Buffer MediaCodec是循环使用这些Buffer的，不清空下次会得到同样的数据
            outputBuffer.clear();
            //自己定义的方法，供编码器所在的线程获取数据,下面会贴出代码
            putPCMData(chunkPCM);
            //此操作一定要做，不然MediaCodec用完所有的Buffer后 将不能向外输出数据
            mediaDecode.releaseOutputBuffer(outputIndex, false);
            //再次获取数据，如果没有数据输出则outputIndex=-1 循环结束
            outputIndex = mediaDecode.dequeueOutputBuffer(decodeBufferInfo, 10000);
        }
    }

    /**
     * 编码PCM数据 得到{@link #encodeType}格式的音频文件，并保存到{@link #dstPath}
     */
    private void dstAudioFormatFromPCM() {

        int inputIndex;
        int outputIndex;
        ByteBuffer inputBuffer;
        ByteBuffer outputBuffer;
        byte[] chunkAudio;
        byte[] chunkPCM;
        int outBitSize;
        int outPacketSize;

        showLog("doEncode");
        for (int i = 0; i < encodeInputBuffers.length - 1; i++) {
            //获取解码器所在线程输出的数据 代码后边会贴上
            chunkPCM = getPCMData();
            if (chunkPCM == null) {
                break;
            }
            //同解码器
            inputIndex = mediaEncode.dequeueInputBuffer(-1);
            inputBuffer = encodeInputBuffers[inputIndex];
            inputBuffer.clear();
            inputBuffer.limit(chunkPCM.length);
            //PCM数据填充给inputBuffer
            inputBuffer.put(chunkPCM);
            //通知编码器 编码
            mediaEncode.queueInputBuffer(inputIndex, 0, chunkPCM.length, 0, 0);
        }

        //同解码器
        outputIndex = mediaEncode.dequeueOutputBuffer(encodeBufferInfo, 10000);
        while (outputIndex >= 0) {

            outBitSize = encodeBufferInfo.size;
            //7为ADTS头部的大小
            outPacketSize = outBitSize + 7;
            //拿到输出Buffer
            outputBuffer = encodeOutputBuffers[outputIndex];
            outputBuffer.position(encodeBufferInfo.offset);
            outputBuffer.limit(encodeBufferInfo.offset + outBitSize);
            chunkAudio = new byte[outPacketSize];
            //添加ADTS
            addADTStoPacket(chunkAudio, outPacketSize);
            //将编码得到的AAC数据 取出到byte[]中 偏移量offset=7 你懂得
            outputBuffer.get(chunkAudio, 7, outBitSize);
            outputBuffer.position(encodeBufferInfo.offset);
            showLog("outPacketSize:" + outPacketSize + " encodeOutBufferRemain:" + outputBuffer.remaining());
            try {
                //BufferOutputStream 将文件保存到内存卡中 *.aac
                bos.write(chunkAudio, 0, chunkAudio.length);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mediaEncode.releaseOutputBuffer(outputIndex, false);
            outputIndex = mediaEncode.dequeueOutputBuffer(encodeBufferInfo, 10000);
        }
    }

    /**
     * 添加ADTS头
     *
     * @param packet    packet
     * @param packetLen packetLen
     */
    private void addADTStoPacket(byte[] packet, int packetLen) {
        // AAC LC
        int profile = 2;
        // 44.1KHz
        int freqIdx = 4;
        // CPE
        int chanCfg = 2;

        // fill in ADTS data
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }

    /**
     * 释放资源
     */
    public void release() {
        try {
            if (bos != null) {
                bos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    bos = null;
                }
            }
        }

        try {
            if (fos != null) {
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            fos = null;
        }

        if (mediaEncode != null) {
            mediaEncode.stop();
            mediaEncode.release();
            mediaEncode = null;
        }

        if (mediaDecode != null) {
            mediaDecode.stop();
            mediaDecode.release();
            mediaDecode = null;
        }

        if (mediaExtractor != null) {
            mediaExtractor.release();
            mediaExtractor = null;
        }

        if (onCompleteListener != null) {
            onCompleteListener = null;
        }

        if (onProgressListener != null) {
            onProgressListener = null;
        }
        showLog("release");
    }

    /**
     * 解码线程
     */
    private class DecodeRunnable implements Runnable {
        @Override
        public void run() {
            while (!mIsCodeOver) {
                srcAudioFormatToPCM();
            }
        }
    }

    /**
     * 编码线程
     */
    private class EncodeRunnable implements Runnable {
        @Override
        public void run() {
            long t = System.currentTimeMillis();
            while (!mIsCodeOver || !chunkPCMDataContainer.isEmpty()) {
                dstAudioFormatFromPCM();
            }
            if (onCompleteListener != null) {
                onCompleteListener.completed();
            }
            showLog("size:" + fileTotalSize + " decodeSize:" + decodeSize + "time:" + (System.currentTimeMillis() - t));
        }
    }


    /**
     * 转码完成回调接口
     */
    public interface OnCompleteListener {
        void completed();
    }

    /**
     * 转码进度监听器
     */
    public interface OnProgressListener {
        void progress();
    }

    /**
     * 设置转码完成监听器
     *
     * @param onCompleteListener onCompleteListener
     */
    public void setOnCompleteListener(OnCompleteListener onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }

    public void setOnProgressListener(OnProgressListener onProgressListener) {
        this.onProgressListener = onProgressListener;
    }

    private void showLog(String msg) {
        Log.e("AudioCodecUtil", msg);
    }
} 