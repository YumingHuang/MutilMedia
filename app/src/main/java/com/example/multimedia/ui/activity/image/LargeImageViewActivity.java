package com.example.multimedia.ui.activity.image;

import android.os.Bundle;

import com.example.multimedia.R;
import com.example.multimedia.ui.activity.BaseActivity;
import com.example.multimedia.ui.widget.LargeImageView;

import java.io.IOException;
import java.io.InputStream;

public class LargeImageViewActivity extends BaseActivity {
    private LargeImageView mLargeImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_large_view);
        mLargeImageView = (LargeImageView) findViewById(R.id.iv_large);
        try {
            // 省内存,这里用小图
            InputStream inputStream = getAssets().open("cy.jpg");
            mLargeImageView.setInputStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}