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
public class ImageActivity extends ListActivity {
    private Intent mIntent = new Intent();
    private ArrayList<String> mTaskList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTaskList.add("绘制图片");
        mTaskList.add("加载大图");
        mTaskList.add("使用Camera1 API 进行图像拍摄(SurfaceView 预览)");
        mTaskList.add("使用Camera1 API 进行图像拍摄(TextureView 预览)");
        mTaskList.add("使用Camera2 API 进行图像拍摄");
        mTaskList.add("OpenGLES － 绘制一个三角形");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mTaskList);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        mIntent.putExtra(BaseActivity.TITLE, mTaskList.get(position));
        switch (position) {
            case 0:
                startActivity(new Intent(this, DrawImgActivity.class));
                return;
            case 1:
                mIntent.setClass(this, LargeImageViewActivity.class);
                break;
            case 2:
                mIntent.setClass(this, Camera1SurfaceActivity.class);
                break;
            case 3:
                mIntent.setClass(this, Camera1TextureActivity.class);
                break;
            case 4:
                mIntent.setClass(this, Camera2SurfaceActivity.class);
                break;
            case 5:
                mIntent.setClass(this, SimpleOpenGlActivity.class);
                break;
            default:
                return;
        }
        startActivity(mIntent);
    }
}
