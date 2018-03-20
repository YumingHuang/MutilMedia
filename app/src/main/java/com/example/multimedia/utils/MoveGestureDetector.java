package com.example.multimedia.utils;

import android.content.Context;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;

public class MoveGestureDetector extends BaseGestureDetector {

    private PointF mCurPointer;
    private PointF mPrePointer;
    //用于记录最终结果，并返回
    private PointF mExtenalPointer = new PointF();

    private OnMoveGestureListener mListener;

    public MoveGestureDetector(Context context, OnMoveGestureListener listener) {
        super(context);
        mListener = listener;
    }

    @Override
    protected void handleStartProgressEvent(MotionEvent event) {
        int actionCode = event.getActionMasked();
        switch (actionCode) {
            case MotionEvent.ACTION_DOWN:
                resetState();//防止没有接收到CANCEL or UP ,保险起见
                mPreMotionEvent = MotionEvent.obtain(event);
                updateStateByEvent(event);
                break;
            case MotionEvent.ACTION_MOVE:
                mGestureInProgress = mListener.onMoveBegin(this);
                break;
            default:
                break;
        }
    }

    @Override
    protected void handleInProgressEvent(MotionEvent event) {
        int actionCode = event.getActionMasked();
        switch (actionCode) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mListener.onMoveEnd(this);
                resetState();
                break;
            case MotionEvent.ACTION_MOVE:
                updateStateByEvent(event);
                boolean update = mListener.onMove(this);
                if (update) {
                    mPreMotionEvent.recycle();
                    mPreMotionEvent = MotionEvent.obtain(event);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void updateStateByEvent(MotionEvent event) {
        MotionEvent prev = mPreMotionEvent;
        mPrePointer = calculateFocalPointer(prev);
        mCurPointer = calculateFocalPointer(event);

        Log.e("TAG", "pre = " + mPrePointer.toString() + " , cur = " + mCurPointer);

        boolean mSkipThisMoveEvent = prev.getPointerCount() != event.getPointerCount();

        Log.e("TAG", "mSkipThisMoveEvent = " + mSkipThisMoveEvent);
        mExtenalPointer.x = mSkipThisMoveEvent ? 0 : mCurPointer.x - mPrePointer.x;
        mExtenalPointer.y = mSkipThisMoveEvent ? 0 : mCurPointer.y - mPrePointer.y;
    }

    /**
     * 根据event计算多指中心点
     *
     * @param event event
     * @return PointF
     */
    private PointF calculateFocalPointer(MotionEvent event) {
        final int count = event.getPointerCount();
        float x = 0, y = 0;
        for (int i = 0; i < count; i++) {
            x += event.getX(i);
            y += event.getY(i);
        }
        x /= count;
        y /= count;
        return new PointF(x, y);
    }

    public float getMoveX() {
        return mExtenalPointer.x;
    }

    public float getMoveY() {
        return mExtenalPointer.y;
    }

    public interface OnMoveGestureListener {
        boolean onMoveBegin(MoveGestureDetector detector);

        boolean onMove(MoveGestureDetector detector);

        void onMoveEnd(MoveGestureDetector detector);
    }

    public static class SimpleMoveGestureDetector implements OnMoveGestureListener {

        @Override
        public boolean onMoveBegin(MoveGestureDetector detector) {
            return true;
        }

        @Override
        public boolean onMove(MoveGestureDetector detector) {
            return false;
        }

        @Override
        public void onMoveEnd(MoveGestureDetector detector) {
        }
    }
}