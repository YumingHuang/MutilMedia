package com.example.multimedia.ui.activity.video;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.multimedia.R;
import com.example.multimedia.common.Constants;
import com.example.multimedia.ui.activity.BaseActivity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author huangyuming
 */
@SuppressWarnings("AliDeprecation")
public class VideoYUVToH264Activity extends BaseActivity implements SurfaceHolder.Callback,
        Camera.PreviewCallback {

    private boolean isPreview = false;
    private Camera camera;
    private Encode encode;
    private SurfaceView surfaceView;
    private BufferedOutputStream mOutputStream;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_yuv_h264);
        init();
    }

    private void init() {
        surfaceView = findViewById(R.id.sf_camera);
        surfaceView.getHolder().addCallback(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (encode != null) {
            encode.releaseMediaCodec();
            encode = null;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        camera.addCallbackBuffer(data);
        encode.encoderYUV420(data);
    }

    private void onEncoderResult(byte[] data) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (isPreview) {
            stopPreview();
        }
        startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopPreview();
    }

    private void startPreview() {
        if (camera == null) {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        }

        try {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewFormat(ImageFormat.NV21);

            Camera.Size previewSize = parameters.getPreviewSize();
            int size = previewSize.width * previewSize.height;
            size = size * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8;

            if (encode == null) {
                encode = new Encode(previewSize.width, previewSize.height,
                        2000 * 1000, 15);
            }

            camera.addCallbackBuffer(new byte[size]);
            camera.setPreviewDisplay(surfaceView.getHolder());
            camera.setPreviewCallbackWithBuffer(this);
            camera.setParameters(parameters);
            camera.startPreview();
            isPreview = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopPreview() {
        if (camera != null) {
            if (isPreview) {
                isPreview = false;
                camera.setPreviewCallbackWithBuffer(null);
                camera.stopPreview();
            }
            camera.release();
            camera = null;
        }
    }

    class Encode {

        private MediaCodec codec;

        private int videoW;
        private int videoH;
        private int videoBitrate;
        private int videoFrameRate;

        private static final String TAG = "Encode";
        private static final String MIME = "Video/AVC";

        public Encode(int videoW, int videoH, int videoBitrate, int videoFrameRate) {
            this.videoW = videoW;
            this.videoH = videoH;
            this.videoBitrate = videoBitrate;
            this.videoFrameRate = videoFrameRate;

            initMediaCodec();
        }

        private void initMediaCodec() {
            try {
                codec = MediaCodec.createEncoderByType(MIME);
            } catch (IOException e) {
                e.printStackTrace();
            }
            MediaFormat format = MediaFormat.createVideoFormat(MIME, videoW, videoH);
            format.setInteger(MediaFormat.KEY_BIT_RATE, videoBitrate);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, videoFrameRate);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);

            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            codec.start();

            // 初始化输出流,保存
            File h264File = new File(Constants.VIDEO_PATH + "yuv_to_h264.h264");
            if (!h264File.getParentFile().exists()) {
                h264File.getParentFile().mkdirs();
            }
            try {
                h264File.createNewFile();
                mOutputStream = new BufferedOutputStream(new FileOutputStream(h264File));
                Log.e(TAG, "outputStream initialized");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void encoderYUV420(byte[] input) {
            try {
                int inputBufferIndex = codec.dequeueInputBuffer(-1);
                if (inputBufferIndex >= 0) {
                    ByteBuffer inputBuffer = codec.getInputBuffer(inputBufferIndex);
                    inputBuffer.clear();
                    inputBuffer.put(input);
                    codec.queueInputBuffer(inputBufferIndex, 0, input.length, System.currentTimeMillis(), 0);
                }

                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                int outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0);
                while (outputBufferIndex >= 0) {
                    ByteBuffer outputBuffer = codec.getOutputBuffer(outputBufferIndex);
                    byte[] outData = new byte[outputBuffer.remaining()];
                    outputBuffer.get(outData, 0, outData.length);

                    // 可以处理实时播放 VideoYUVToH264Activity.this.onEncoderResult(outData)
                    // 这里保存
                    mOutputStream.write(outData, 0, outData.length);
                    Log.d(TAG, "coding length = " + outData.length);
                    codec.releaseOutputBuffer(outputBufferIndex, false);
                    outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void releaseMediaCodec() {
            if (codec != null) {
                codec.stop();
                codec.release();
                codec = null;
            }
            if (mOutputStream != null) {
                try {
                    mOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
