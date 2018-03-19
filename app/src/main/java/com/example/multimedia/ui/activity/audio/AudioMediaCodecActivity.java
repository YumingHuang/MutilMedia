package com.example.multimedia.ui.activity.audio;

import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.multimedia.R;
import com.example.multimedia.common.Constants;
import com.example.multimedia.ui.activity.BaseActivity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author huangyuming
 */
public class AudioMediaCodecActivity extends BaseActivity implements View.OnClickListener {

    private Button mEncodeBtn;
    private Button mDecodeBtn;
    private MediaCodec mMediaCodec;
    private MediaExtractor mExtractor;
    private BufferedOutputStream mOutputStream;
    private String mMediaType = "OMX.google.aac.encoder";
    private ByteBuffer[] mInputBuffers = null;
    private ByteBuffer[] mOutputBuffers = null;
    private int sampleRate = 0, channels = 0, bitrate = 0;
    private long presentationTimeUs = 0, duration = 0;
    private byte[] mBuffer;
    private int mAudioInputBufferSize;
    /***　buffer值不能太大，避免OOM　*/
    private static final int BUFFER_SIZE = 2048;
    public static final int KEY_CHANNEL_COUNT = 0;
    private static final int KEY_SAMPLE_RATE = 0;

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
        mBuffer = new byte[BUFFER_SIZE];
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
        File pcmFile = new File(Constants.AUDIO_PATH + "test.pcm");
        File accFile = new File(Constants.AUDIO_PATH + "audio_encoded.acc");
        if (!accFile.getParentFile().exists()) {
            accFile.getParentFile().mkdirs();
        }
        try {
            accFile.createNewFile();
            mOutputStream = new BufferedOutputStream(new FileOutputStream(accFile));
            Log.e("AudioEncoder", "outputStream initialized");

            mMediaCodec = MediaCodec.createByCodecName(mMediaType);
        } catch (IOException e) {
            e.printStackTrace();
        }

        final int[] sampleRates = {8000, 11025, 22050, 44100, 48000};
        final int[] bitRates = {64000, 96000, 128000};
        MediaFormat mediaFormat = MediaFormat.createAudioFormat(
                "audio/mp4a-latm", sampleRates[3], 2);
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE,
                MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRates[1]);
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 8192);
        mMediaCodec.configure(mediaFormat, null, null,
                MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaCodec.start();
        mInputBuffers = mMediaCodec.getInputBuffers();
        mOutputBuffers = mMediaCodec.getOutputBuffers();

        //从文件流读数据
        FileInputStream inputStream = null;
        try {
            //循环读数据，去编码
            inputStream = new FileInputStream(pcmFile);

            while (inputStream.read(mBuffer) > 0) {
                encodePCMToAAC(mBuffer);
            }
            Toast.makeText(AudioMediaCodecActivity.this, "編碼成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(AudioMediaCodecActivity.this, "編碼失敗", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                closeStream(inputStream);
            }
            if (mOutputStream != null) {
                try {
                    mOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mEncodeBtn.setText(getString(R.string.audio_btn_start_encode));
        }
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

    /**
     * PCM格式編碼成AAC格式
     *
     * @param bytes bytes
     * @throws IOException
     */
    private void encodePCMToAAC(byte[] bytes) throws IOException {
        Log.e("AudioEncoder", bytes.length + " is coming");


        int inputBufferIndex = mMediaCodec.dequeueInputBuffer(-1);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = mInputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(bytes);
            mMediaCodec.queueInputBuffer(inputBufferIndex, 0, bytes.length, 0, 0);
        }

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);


        //trying to add a ADTS
        while (outputBufferIndex >= 0) {
            int outBitsSize = bufferInfo.size;
            // 7 is ADTS size
            int outPacketSize = outBitsSize + 7;
            ByteBuffer outputBuffer = mOutputBuffers[outputBufferIndex];

            outputBuffer.position(bufferInfo.offset);
            outputBuffer.limit(bufferInfo.offset + outBitsSize);

            byte[] outData = new byte[outPacketSize];
            addADTStoPacket(outData, outPacketSize);

            outputBuffer.get(outData, 7, outBitsSize);
            outputBuffer.position(bufferInfo.offset);

            // byte[] outData = new byte[bufferInfo.size];
            try {
                mOutputStream.write(outData, 0, outData.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.e("AudioEncoder", outData.length + " bytes written");

            mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
        }
    }


    /**
     * Add ADTS header at the beginning of each and every AAC packet. This is
     * needed as MediaCodec encoder generates a packet of raw AAC data.
     * <p>
     * Note the packetLen must count in the ADTS header itself.
     **/
    public void addADTStoPacket(byte[] packet, int packetLen) {
        // AAC LC
        int profile = 2;
        // 39=MediaCodecInfo.CodecProfileLevel.AACObjectELD;
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

    private void startDecode() {
        if (!prepareDecode()) {
            Log.d(TAG, "初始化解码器失败");
        }
        final ByteBuffer[] buffers = mMediaCodec.getOutputBuffers();
        int sz = buffers[0].capacity();
        if (sz <= 0) {
            sz = mAudioInputBufferSize;
        }
        byte[] mAudioOutTempBuf = new byte[sz];
        ByteBuffer[] codecInputBuffers = mMediaCodec.getInputBuffers();
        ByteBuffer[] codecOutputBuffers = mMediaCodec.getOutputBuffers();

        final long kTimeOutUs = 0;
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        boolean sawInputEOS = false;
        boolean sawOutputEOS = false;
        int totalRawSize = 0;
        try {
            while (!sawOutputEOS) {
                if (!sawInputEOS) {
                    int inputBufIndex = mMediaCodec.dequeueInputBuffer(kTimeOutUs);
                    if (inputBufIndex >= 0) {
                        ByteBuffer dstBuf = codecInputBuffers[inputBufIndex];
                        int sampleSize = mExtractor.readSampleData(dstBuf, 0);
                        if (sampleSize < 0) {
                            Log.i(TAG, "saw input EOS.");
                            sawInputEOS = true;
                            mMediaCodec.queueInputBuffer(inputBufIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        } else {
                            long presentationTimeUs = mExtractor.getSampleTime();
                            Log.i(TAG, "presentationTimeUs.");
                            mMediaCodec.queueInputBuffer(inputBufIndex, 0, sampleSize, presentationTimeUs, 0);
                            mExtractor.advance();
                        }
                    }
                }
                int res = mMediaCodec.dequeueOutputBuffer(info, kTimeOutUs);
                if (res >= 0) {

                    int outputBufIndex = res;
                    // Simply ignore codec config buffers.
                    if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        Log.i("TAG", "audio encoder: codec config buffer");
                        mMediaCodec.releaseOutputBuffer(outputBufIndex, false);
                        continue;
                    }
                    Log.d(TAG, "info.size = " + info.size);
                    if (info.size != 0) {
                        ByteBuffer outBuf = codecOutputBuffers[outputBufIndex];
                        outBuf.position(info.offset);
                        outBuf.limit(info.offset + info.size);
                        byte[] data = new byte[info.size];
                        outBuf.get(data);
                        totalRawSize += data.length;
                        mOutputStream.write(data, 0, data.length);
                        Log.d(TAG, "data.length = " + data.length);
                    }

                    mMediaCodec.releaseOutputBuffer(outputBufIndex, false);

                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        Log.i("TAG", "saw output EOS.");
                        sawOutputEOS = true;
                    }
                } else if (res == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    codecOutputBuffers = mMediaCodec.getOutputBuffers();
                    Log.i("TAG", "output buffers have changed.");
                    return;
                } else if (res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat oformat = mMediaCodec.getOutputFormat();
                    Log.i("TAG", "output format has changed to " + oformat);
                    return;
                }
            }
            Toast.makeText(AudioMediaCodecActivity.this, "解码成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(AudioMediaCodecActivity.this, "解码失败", Toast.LENGTH_SHORT).show();
        } finally {
            if (mOutputStream != null) {
                try {
                    mOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mExtractor.release();
        }
    }

    private boolean prepareDecode() {
        try {
            File accFile = new File(Constants.AUDIO_PATH + "audio_encoded.acc");
            File pcmFile = new File(Constants.AUDIO_PATH + "audio_encoded.pcm");
            if (!pcmFile.getParentFile().exists()) {
                pcmFile.getParentFile().mkdirs();
            }
            //创建文件输出流
            mOutputStream = new BufferedOutputStream(new FileOutputStream(pcmFile));

            final String encodeFile = accFile.getAbsolutePath();
            mExtractor = new MediaExtractor();
            mExtractor.setDataSource(encodeFile);

            // 音频文件信息
            for (int i = 0; i < mExtractor.getTrackCount(); i++) {
                MediaFormat mediaFormat = mExtractor.getTrackFormat(i);
                String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("audio/")) {
                    mExtractor.selectTrack(i);
                    int audioChannels = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                    int audioSampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                    int minBufferSize = AudioTrack.getMinBufferSize(audioSampleRate,
                            (audioChannels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO),
                            AudioFormat.ENCODING_PCM_16BIT);
                    // 手动输入,不知道为什么mediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE) 空指针异常
                    int maxInputSize = 8192;
                    mAudioInputBufferSize = minBufferSize > 0 ? minBufferSize * 4 : maxInputSize;
                    int frameSizeInBytes = audioChannels * 2;
                    mAudioInputBufferSize = (mAudioInputBufferSize / frameSizeInBytes) * frameSizeInBytes;
                }
            }
            MediaFormat format = mExtractor.getTrackFormat(0);
            String mime = format.getString(MediaFormat.KEY_MIME);
            sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            // 声道个数：单声道或双声道
            channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
            duration = format.getLong(MediaFormat.KEY_DURATION);
            Log.d(TAG, "sampleRate = " + sampleRate + " , channels =" + channels + " , duration = " + duration);
            if (format == null || !mime.startsWith("audio/")) {
                Log.e(TAG, "不是音频文件 end !");
                return false;
            }
            mMediaCodec = MediaCodec.createDecoderByType(mime);
            if (mMediaCodec == null) {
                Log.e(TAG, "create mediaDecode failed");
                return false;
            }
            mMediaCodec.configure(format, null, null, 0);
            mMediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
