package com.example.multimedia.ui.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.multimedia.ui.activity.audio.AudioActivity;
import com.example.multimedia.ui.activity.image.ImageActivity;
import com.example.multimedia.ui.activity.video.VideoActivity;
import com.example.multimedia.utils.PermissionUtil;

import java.util.ArrayList;

/**
 * @author huangyuming
 */
public class MainActivity extends ListActivity {

    private ArrayList<String> mTaskList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTaskList.add("图像处理");
        mTaskList.add("音频处理");
        mTaskList.add("视频处理");
        mTaskList.add("流媒体处理");
        mTaskList.add("直播技术");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mTaskList);
        setListAdapter(adapter);
        PermissionUtil.requestMultiPermissions(this, new PermissionUtil.PermissionGrant() {
            @Override
            public void onPermissionGranted(int requestCode) {
                Log.d("TAG", "onPermissionGranted");
            }
        });
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        switch (position) {
            case 0:
                startActivity(new Intent(this, ImageActivity.class));
                break;
            case 1:
                startActivity(new Intent(this, AudioActivity.class));
                break;
            case 2:
                startActivity(new Intent(this, VideoActivity.class));
                break;
            case 3:
                break;
            case 4:
                break;
            default:
                break;
        }
    }
}
