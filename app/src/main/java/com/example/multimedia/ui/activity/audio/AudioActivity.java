package com.example.multimedia.ui.activity.audio;

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
public class AudioActivity extends ListActivity {
    private Intent mIntent = new Intent();
    private ArrayList<String> mTaskList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTaskList.add("MediaRecord和MediaPlayer API 完成音频的采集和播放");
        mTaskList.add("AudioRecord和AudioTrack API 完成音频 PCM 数据的采集和播放");
        mTaskList.add("AudioRecord和AudioTrack API 完成音频 PCM 转 WAV格式");
        mTaskList.add("MediaCodec API，完成音频 AAC 硬编、硬解");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mTaskList);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        mIntent.putExtra(BaseActivity.TITLE, mTaskList.get(position));
        switch (position) {
            case 0:
                mIntent.setClass(this, MediaRecordActivity.class);
                break;
            case 1:
                mIntent.setClass(this, AudioPcmRecordActivity.class);
                break;
            case 2:
                mIntent.setClass(this, AudioWavRecordActivity.class);
                break;
            case 3:
                mIntent.setClass(this, AudioMediaCodecActivity.class);
                break;
            default:
                break;
        }
        startActivity(mIntent);
    }
}
