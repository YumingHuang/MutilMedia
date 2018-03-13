package com.example.mutilmedia.ui.activity.video;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mTaskList);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        switch (position) {
            case 0:
                break;
            case 1:
                break;
            default:
                break;
        }
    }
}
