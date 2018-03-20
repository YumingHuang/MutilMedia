package com.example.multimedia.ui.activity.image;

import android.os.Bundle;

import com.example.multimedia.R;
import com.example.multimedia.ui.activity.BaseActivity;
import com.example.multimedia.ui.widget.LargeImageView;

import java.io.IOException;
import java.io.InputStream;

public class LargeImageViewActivity extends BaseActivity {
    private LargeImageView mLargeImageView;
    private InputStream mInputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_large_view);
        mLargeImageView = findViewById(R.id.iv_large);
        try {
            mInputStream = getAssets().open("big_picture.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mInputStream != null) {
            mLargeImageView.setInputStream(mInputStream);
        }
    }
}