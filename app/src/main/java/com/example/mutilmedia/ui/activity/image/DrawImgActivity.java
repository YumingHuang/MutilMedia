package com.example.mutilmedia.ui.activity.image;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * @author huangyuming
 */
public class DrawImgActivity extends ListActivity {

    private ArrayList<String> mTaskList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTaskList.add("SurfaceView绘制");
        mTaskList.add("自定义View绘制");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mTaskList);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        switch (position) {
            case 0:
                startActivity(new Intent(this, SurfaceDrawActivity.class));
                break;
            case 1:
                startActivity(new Intent(this, CustomImgActivity.class));
                break;
            default:
                break;
        }
    }
}
