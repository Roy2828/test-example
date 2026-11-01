package com.example.myapplication.video;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.myapplication.R;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

public class CampusGSYVideoPlayer extends StandardGSYVideoPlayer {
    private View.OnTouchListener mOnTouchListener;

    public CampusGSYVideoPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public CampusGSYVideoPlayer(Context context) {
        super(context);
    }

    public CampusGSYVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int getLayoutId() {
        return R.layout.video_layout_campus_standard;
    }

    @Override
    protected void startDismissControlViewTimer() {
        // super.startDismissControlViewTimer();
    }

    @Override
    protected void cancelDismissControlViewTimer() {
        // super.cancelDismissControlViewTimer();
    }

    @Override
    protected void onClickUiToggle(MotionEvent e) {
        super.onClickUiToggle(e);
        if (mOnTouchListener != null) {
            mOnTouchListener.onTouch(null, null);
        }
    }

    @Override
    protected void init(Context context) {
        super.init(context);
    }

    public void setLockOnTouchListener(OnTouchListener onTouchListener) {
        this.mOnTouchListener = onTouchListener;
    }

    public void setControllerVisible(boolean visi) {
        if (visi) {
            showController();
        } else {
            hideAllWidget();
        }
    }

    public void hideController() {
        hideAllWidget();
    }

    private void showController() {
        setViewShowState(mBottomContainer, VISIBLE);
        setViewShowState(mTopContainer, VISIBLE);
        setViewShowState(mBottomProgressBar, VISIBLE);
        setViewShowState(mStartButton, VISIBLE);
    }
}
