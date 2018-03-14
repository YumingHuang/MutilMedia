package com.example.multimedia.ui.activity.image;

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
public class DrawImgActivity extends ListActivity {

    private ArrayList<String> mTaskList = new ArrayList<>();
    private Intent mIntent = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTaskList.add("SurfaceView绘制图片");
        mTaskList.add("自定义View绘制图片");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mTaskList);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        switch (position) {
            case 0:
                mIntent.setClass(this, SurfaceDrawActivity.class);
                mIntent.putExtra(BaseActivity.TITLE, mTaskList.get(0));
                startActivity(mIntent);
                break;
            case 1:
                mIntent.setClass(this, CustomImgActivity.class);
                mIntent.putExtra(BaseActivity.TITLE, mTaskList.get(1));
                startActivity(mIntent);
                break;
            default:
                break;
        }
    }
}
