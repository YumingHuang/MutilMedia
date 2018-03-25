package com.example.multimedia.ui.activity.video;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.multimedia.ui.activity.BaseActivity;

import java.util.ArrayList;

/**
 * @author huangyuming
 */
public class VideoActivity extends ListActivity {
    private Intent mIntent = new Intent();
    private ArrayList<String> mTaskList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTaskList.add("使用Camera1 API 进行视频录制");
        mTaskList.add("使用Camera2 API 进行视频录制");
        mTaskList.add("使用 MediaExtractor 和 MediaMuxer API 解析和封装mp4文件");
        mTaskList.add("使用 MediaCodec API，将YUV硬编成H264");
        mTaskList.add("使用 MediaCodec API，H264硬解");
        mTaskList.add("使用 MediaCodec API，H264硬解");
        mTaskList.add("串联整个音视频录制流程，完成音视频的采集、编码、封包成 mp4 输出");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                mTaskList);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        mIntent.putExtra(BaseActivity.TITLE, mTaskList.get(position));
        switch (position) {
            case 0:
                mIntent.setClass(this, Camera1RecordActivity.class);
                break;
            case 1:
                mIntent.setClass(this, Camera2RecordActivity.class);
                break;
            case 2:
                mIntent.setClass(this, VideoExtractorMuxerActivity.class);
                break;
            case 3:
                mIntent.setClass(this, VideoYUVToH264Activity.class);
                break;
            case 4:
                mIntent.setClass(this, VideoParseH264Activity.class);
                break;
            case 5:
                mIntent.setClass(this, VideoRawRecordActivity.class);
                break;
            default:
                break;
        }
        startActivity(mIntent);
    }
}
