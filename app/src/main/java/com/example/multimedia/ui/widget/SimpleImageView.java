package com.example.multimedia.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.example.multimedia.R;

/**
 * Created by hym
 */
public class SimpleImageView extends View {
    private Drawable mDrawable;
    private Bitmap mBitmap;
    private Context mContext;
    private Paint mBitmapPaint;
    private int mViewWidth, mViewHeight;

    public SimpleImageView(Context context) {
        this(context, null);
    }

    public SimpleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs);
        mContext = context;
        mBitmapPaint = new Paint();
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray array = null;
        try {
            //获得属性集合，并从属性集合中获得对应属性的资源
            array = getContext().obtainStyledAttributes(attrs, R.styleable.myImageView);
            mDrawable = array.getDrawable(R.styleable.myImageView_src);
            measureDrawable();
        } finally {
            if (array != null) {
                array.recycle();
            }
        }
    }

    private void measureDrawable() {
        if (mDrawable != null) {
            mViewWidth = mDrawable.getIntrinsicWidth();
            mViewHeight = mDrawable.getIntrinsicHeight();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        measuredWidth(widthMode, widthSize);
        measureHeight(heightMode, heightSize);

        setMeasuredDimension(mViewWidth, mViewHeight);
    }

    private void measureHeight(int heightMode, int heightSize) {
        switch (heightMode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                break;
            case MeasureSpec.EXACTLY:
                mViewHeight = heightSize;
                break;
            default:
                break;
        }
    }

    private void measuredWidth(int widthMode, int widthSize) {
        switch (widthMode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                break;
            case MeasureSpec.EXACTLY:
                mViewWidth = widthSize;
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mDrawable != null) {
            if (mBitmap == null) {
                mBitmap = Bitmap.createScaledBitmap(drawableToBitmap(mDrawable),
                        getMeasuredWidth(), getMeasuredHeight(), true);
            }
            canvas.drawBitmap(mBitmap, getLeft(), getTop(), mBitmapPaint);
        }
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE
                ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        //注意，下面三行代码要用到，否则在View或者SurfaceView里的canvas.drawBitmap会看不到图
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }
}