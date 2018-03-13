package com.example.mutilmedia.ui.activity.image;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.mutilmedia.ui.activity.BaseActivity;
import com.example.mutilmedia.ui.activity.audio.MediaRecordActivity;

import java.util.ArrayList;

/**
 * @author huangyuming
 */
public class ImageActivity extends ListActivity {
    private Intent mIntent = new Intent();
    private ArrayList<String> mTaskList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTaskList.add("绘制图片");
        mTaskList.add("加载大图");
        mTaskList.add("使用Camera1 API 进行图像拍摄");
        mTaskList.add("使用Camera2 API 进行图像拍摄");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mTaskList);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        switch (position) {
            case 0:
                startActivity(new Intent(this, DrawImgActivity.class));
                break;
            case 1:
                mIntent.setClass(this, LargeImageViewActivity.class);
                mIntent.putExtra(BaseActivity.TITLE, mTaskList.get(1));
                startActivity(mIntent);
                break;
            case 2:
                mIntent.setClass(this, Camera1Activity.class);
                mIntent.putExtra(BaseActivity.TITLE, mTaskList.get(1));
                startActivity(mIntent);
                break;
            default:
                break;
        }
    }
}
