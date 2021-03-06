package com.example.multimedia.utils;

import android.content.Context;
import android.view.MotionEvent;


public abstract class BaseGestureDetector {

    protected boolean mGestureInProgress;

    protected MotionEvent mPreMotionEvent;
    protected MotionEvent mCurMotionEvent;

    protected Context mContext;

    public BaseGestureDetector(Context context) {
        mContext = context;
    }


    public boolean onToucEvent(MotionEvent event) {
        if (!mGestureInProgress) {
            handleStartProgressEvent(event);
        } else {
            handleInProgressEvent(event);
        }
        return true;
    }

    protected abstract void handleInProgressEvent(MotionEvent event);

    protected abstract void handleStartProgressEvent(MotionEvent event);

    protected abstract void updateStateByEvent(MotionEvent event);

    protected void resetState() {
        if (mPreMotionEvent != null) {
            mPreMotionEvent.recycle();
            mPreMotionEvent = null;
        }
        if (mCurMotionEvent != null) {
            mCurMotionEvent.recycle();
            mCurMotionEvent = null;
        }
        mGestureInProgress = false;
    }
}