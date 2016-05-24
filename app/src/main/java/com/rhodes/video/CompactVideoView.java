package com.rhodes.video;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.rhodes.video.controller.MediaController;

/**
 * Created by xiet on 2016/3/22.
 */
public abstract class CompactVideoView extends SurfaceView {

    protected Context         mContext;
    protected MediaController mMediaController;

    //region Construct
    public CompactVideoView(Context context) {
        super(context);

        initWithContext(context);
    }

    public CompactVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initWithContext(context);
    }

    public CompactVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initWithContext(context);
    }
    //endregion

    protected void initWithContext(Context context) {
        this.mContext = context;
    }

    //region FullScreen Handle
    private OnFullScreenListener mFullScreenListener;

    public interface OnFullScreenListener {
        public boolean onScreenChanged(boolean flag);
    }

    public void setFullScreenListener(OnFullScreenListener l) {
        this.mFullScreenListener = l;
    }

    public boolean isFullscreen() {
        if (mMediaController != null) {
            return mMediaController.isFullscreen();
        }

        return false;
    }

    public void fullScreen(boolean flag) {
        if (flag) {
            intoFullscreen();
        } else {
            quitFullscreen();
        }
    }

    private void intoFullscreen() {
        Activity activity   = (Activity) mContext;
        boolean  isPortrait = (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
        if (isPortrait) {
            //TODO:set brothers view attr or not

            if (mFullScreenListener != null) {
                boolean consumed = mFullScreenListener.onScreenChanged(true);
                if (consumed) return;
            }
            if (mMediaController != null) {
                mMediaController.setFullScreen(true);
            }

            //window attr settings
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//after u can set flag, is fullscreen success.
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        }
    }

    private void quitFullscreen() {
        Activity activity   = (Activity) mContext;
        boolean  isPortrait = (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);

        if (!isPortrait) {
            //TODO:set brothers view attr or not

            if (mFullScreenListener != null) {
                boolean consumed = mFullScreenListener.onScreenChanged(false);
                if (consumed) return;
            }
            if (mMediaController != null) {
                mMediaController.setFullScreen(false);
            }

            //window attr settings
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            final WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
            attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            activity.getWindow().setAttributes(attrs);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }
    //endregion
}
