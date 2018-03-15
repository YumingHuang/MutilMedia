package com.example.multimedia.common;

import android.os.Environment;

public class Constants {
    public static final String BASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    /*** 图像目录 */
    public static final String IMAGE_PATH = BASE_PATH + "/Multimedia/Image/";
    /*** 音频目录 */
    public static final String AUDIO_PATH = BASE_PATH + "/Multimedia/Audio/";
    /*** 视频目录 */
    public static final String VIDEO_PATH = BASE_PATH + "/Multimedia/Video/";
    /*** 音频pcm格式 */
    public static final String AUDIO_PCM = ".pcm";
    /*** 音频wav格式 */
    public static final String AUDIO_WAV = ".wav";
    /*** 音频m4a格式 */
    public static final String AUDIO_M4A = ".m4a";
    /*** 图片jpg格式 */
    public static final String IMAGE_JPG = ".jpg";
    /*** 视频mp4格式 */
    public static final String VIDEO_MP4 = ".mp4";
}
