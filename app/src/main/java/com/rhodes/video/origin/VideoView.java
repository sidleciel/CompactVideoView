package com.rhodes.video.origin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;

import com.rhodes.video.CompactVideoView;
import com.rhodes.video.controller.MediaController;

import java.io.IOException;


/**
 * Created by xiet on 2015/12/16.
 */
public class VideoView extends CompactVideoView implements MediaController.MediaPlayerControl {
    private String TAG = getClass().getSimpleName();

    private Context mContext;

    // settable by the client
    private Uri mUri;
    private int mDuration;

    // All the stuff we need for playing and showing a video
    private SurfaceHolder mSurfaceHolder = null;
    public  MediaPlayer   mMediaPlayer   = null;
    private boolean                               mIsPrepared;
    private int                                   mVideoWidth;
    private int                                   mVideoHeight;
    private int                                   mSurfaceWidth;
    private int                                   mSurfaceHeight;
    private MediaController                       mMediaController;
    private MediaPlayer.OnCompletionListener      mOnCompletionListener;
    private MediaPlayer.OnPreparedListener        mOnPreparedListener;
    private MediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener;
    private int                                   mCurrentBufferPercentage;
    private OnErrorListener                       mOnErrorListener;
    private boolean                               mStartWhenPrepared;
    private int                                   mSeekWhenPrepared;

    private MySizeChangeLinstener   mMyChangeLinstener;
    private OnScreenChangedListener mScreenChangedListener;

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    public void setVideoScale(int width, int height) {
        LayoutParams lp = getLayoutParams();
        lp.height = height;
        lp.width = width;
        setLayoutParams(lp);
    }

    public interface MySizeChangeLinstener {
        public void doMyThings();
    }

    public void setMySizeChangeLinstener(MySizeChangeLinstener l) {
        mMyChangeLinstener = l;
    }

    public VideoView(Context context) {
        super(context);
        mContext = context;
        initVideoView();
    }

    public VideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
        initVideoView();
    }

    public VideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initVideoView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Log.i("@@@@", "onMeasure");
        int width  = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        /*
         * if (mVideoWidth > 0 && mVideoHeight > 0) { if ( mVideoWidth * height
		 * > width * mVideoHeight ) { //Log.i("@@@",
		 * "image too tall, correcting"); height = width * mVideoHeight /
		 * mVideoWidth; } else if ( mVideoWidth * height < width * mVideoHeight
		 * ) { //Log.i("@@@", "image too wide, correcting"); width = height *
		 * mVideoWidth / mVideoHeight; } else { //Log.i("@@@",
		 * "aspect ratio is correct: " + //width+"/"+height+"="+
		 * //mVideoWidth+"/"+mVideoHeight); } }
		 */
        // Log.i("@@@@@@@@@@", "setting size: " + width + 'x' + height);
        setMeasuredDimension(width, height);
    }

    public int resolveAdjustedSize(int desiredSize, int measureSpec) {
        int result   = desiredSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
            /*
             * Parent says we can be as big as we want. Just don't be larger
			 * than max size imposed on ourselves.
			 */
                result = desiredSize;
                break;

            case MeasureSpec.AT_MOST:
            /*
             * Parent says we can be as big as we want, up to specSize. Don't be
			 * larger than specSize, and don't be larger than the max size
			 * imposed on ourselves.
			 */
                result = Math.min(desiredSize, specSize);
                break;

            case MeasureSpec.EXACTLY:
                // No choice. Do what we are told.
                result = specSize;
                break;
        }
        return result;
    }

    private void initVideoView() {
        mVideoWidth = 0;
        mVideoHeight = 0;
        getHolder().setFormat(PixelFormat.TRANSPARENT);

        getHolder().addCallback(mSHCallback);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    public void setMediaPlayer(MediaPlayer player) {
        this.mMediaPlayer = player;
    }

    public MediaController getMediaController() {
        return mMediaController;
    }

    public void setVideoPath(String path) {
        setVideoURI(Uri.parse(path));
    }

    public void setVideoURI(Uri uri) {
        mUri = uri;
        mStartWhenPrepared = false;
        mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();
    }

    public void reset() {
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.reset();
                //mMediaPlayer.prepare();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mIsPrepared = false;
            mUri = null;
        }
    }

    private void openVideo() {
        if (mUri == null || mSurfaceHolder == null) {
            // not ready for playback just yet, will try again later
            return;
        }
        // Tell the music playback service to pause
        // TODO: these constants need to be published somewhere in the
        // framework.
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        mContext.sendBroadcast(i);

        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mIsPrepared = false;
            Log.v(TAG, "reset duration to -1 in openVideo");
            mDuration = -1;
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mCurrentBufferPercentage = 0;
            if (mContext != null && mUri != null)
                mMediaPlayer.setDataSource(mContext, mUri);
            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();
            attachMediaController();
            requestFocus();
        } catch (IOException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            return;
        } catch (IllegalArgumentException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            return;
        }
    }

    public void setMediaController(MediaController controller) {
        if (mMediaController != null) {
            mMediaController.hide();
        }
        mMediaController = controller;
        attachMediaController();
    }

    private void attachMediaController() {
        if (mMediaPlayer != null && mMediaController != null) {
            mMediaController.setMediaPlayer(this);
            View anchorView = this.getParent() instanceof View ? (View) this.getParent() : this;
            mMediaController.setAnchorView(anchorView);
            mMediaController.setEnabled(mIsPrepared);
        }
    }

    MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();

            if (mMyChangeLinstener != null) {
                mMyChangeLinstener.doMyThings();
            }

            if (mVideoWidth != 0 && mVideoHeight != 0) {
            }
            getHolder().setFixedSize(mVideoWidth, mVideoHeight);
        }
    };

    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            // briefly show the mediacontroller
            mIsPrepared = true;
            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(mMediaPlayer);
            }
            if (mMediaController != null) {
                mMediaController.setEnabled(true);
            }
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                // Log.i("@@@@", "video size: " + mVideoWidth +"/"+  mVideoHeight);
                getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                if (mSurfaceWidth == mVideoWidth
                        && mSurfaceHeight == mVideoHeight) {
                    // We didn't actually change the size (it was already at the size we need), so we won't get a "surface changed" callback,
                    // so start the video here instead of in the callback.
                    if (mSeekWhenPrepared != 0) {
                        mMediaPlayer.seekTo(mSeekWhenPrepared);
                        mSeekWhenPrepared = 0;
                    }
                    if (mStartWhenPrepared) {
                        mMediaPlayer.start();
                        mStartWhenPrepared = false;
                        if (mMediaController != null) {
                            mMediaController.show();
                        }
                    } else if (!isPlaying() && (mSeekWhenPrepared != 0 || getCurrentPosition() > 0)) {
                        if (mMediaController != null) {
                            // Show the media controls when we're paused into a video and make 'em stick.
                            mMediaController.show(0);
                        }
                    }
                }
            } else {
                // We don't know the video size yet, but should start anyway.
                // The video size might be reported to us later.
                if (mSeekWhenPrepared != 0) {
                    mMediaPlayer.seekTo(mSeekWhenPrepared);
                    mSeekWhenPrepared = 0;
                }
                if (mStartWhenPrepared) {
                    mMediaPlayer.start();
                    mStartWhenPrepared = false;
                }
            }
        }
    };

    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            if (mMediaController != null) {
                mMediaController.hide();
            }
            if (mOnCompletionListener != null) {
                mOnCompletionListener.onCompletion(mMediaPlayer);
            }
        }
    };

    private OnErrorListener mErrorListener = new OnErrorListener() {
        public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
            Log.d(TAG, "Error: " + framework_err + "," + impl_err);
            if (mMediaController != null) {
                mMediaController.hide();
            }

			/* If an error handler has been supplied, use it and finish. */
            if (mOnErrorListener != null) {
                if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                    return true;
                }
            }

			/*
             * Otherwise, pop up an error dialog so the user knows that
			 * something bad has happened. Only try and pop up the dialog if
			 * we're attached to a window. When we're going away and no longer
			 * have a window, don't bother showing the user an error.
			 */
            if (getWindowToken() != null) {
                Resources r = mContext.getResources();
                int       messageId;

				/*
                 * if (framework_err ==
				 * MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
				 * messageId = com.android.internal.R.string.
				 * VideoView_error_text_invalid_progressive_playback; } else {
				 * messageId =
				 * com.android.internal.R.string.VideoView_error_text_unknown; }
				 *
				 * new AlertDialog.Builder(mContext)
				 * .setTitle(com.android.internal
				 * .R.string.VideoView_error_title) .setMessage(messageId)
				 * .setPositiveButton
				 * (com.android.internal.R.string.VideoView_error_button, new
				 * DialogInterface.OnClickListener() { public void
				 * onClick(DialogInterface dialog, int whichButton) { If we get
				 * here, there is no onError listener, so at least inform them
				 * that the video is over.
				 *
				 * if (mOnCompletionListener != null) {
				 * mOnCompletionListener.onCompletion(mMediaPlayer); } } })
				 * .setCancelable(false) .show();
				 */
            }
            return true;
        }
    };

    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            mCurrentBufferPercentage = percent;
            Log.v("percent", "" + percent);
            if (mOnBufferingUpdateListener != null) {
                mOnBufferingUpdateListener.onBufferingUpdate(mp, percent);
            }
        }
    };

    /**
     * Register a callback to be invoked when the media file is loaded and ready
     * to go.
     *
     * @param l The callback that will be run
     */
    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
        mOnPreparedListener = l;
    }

    public void setOnBufferingUpdateListener(MediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener) {
        this.mOnBufferingUpdateListener = mOnBufferingUpdateListener;
    }

    /**
     * Register a callback to be invoked when the end of a media file has been
     * reached during playback.
     *
     * @param l The callback that will be run
     */
    public void setOnCompletionListener(MediaPlayer.OnCompletionListener l) {
        mOnCompletionListener = l;
    }

    /**
     * Register a callback to be invoked when an error occurs during playback or
     * setup. If no listener is specified, or if the listener returned false,
     * VideoView will inform the user of any errors.
     *
     * @param l The callback that will be run
     */
    public void setOnErrorListener(OnErrorListener l) {
        mOnErrorListener = l;
    }

    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        public void surfaceChanged(SurfaceHolder holder, int format, int w,
                                   int h) {
            mSurfaceWidth = w;
            mSurfaceHeight = h;
            if (mMediaPlayer != null && mIsPrepared && mVideoWidth == w && mVideoHeight == h) {
                if (mSeekWhenPrepared != 0) {
                    mMediaPlayer.seekTo(mSeekWhenPrepared);
                    mSeekWhenPrepared = 0;
                }
                //������ס�󣬲�Ȼ����
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.start();
                } else {
                    mMediaPlayer.pause();
                }

                if (mMediaController != null) {
                    mMediaController.show();
                }
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceHolder = holder;
//            if (mIsPrepared)
            openVideo();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // after we return from this we can't use the surface any more
            mSurfaceHolder = null;
            if (mMediaController != null)
                mMediaController.hide();
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mIsPrepared && mMediaPlayer != null && mMediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return false;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (mIsPrepared && mMediaPlayer != null && mMediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mIsPrepared && keyCode != KeyEvent.KEYCODE_BACK
                && keyCode != KeyEvent.KEYCODE_VOLUME_UP
                && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN
                && keyCode != KeyEvent.KEYCODE_MENU
                && keyCode != KeyEvent.KEYCODE_CALL
                && keyCode != KeyEvent.KEYCODE_ENDCALL && mMediaPlayer != null
                && mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                } else {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP && mMediaPlayer.isPlaying()) {
                pause();
                mMediaController.show();
            } else {
                toggleMediaControlsVisiblity();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void toggleMediaControlsVisiblity() {
        if (mMediaController.isShowing()) {
            mMediaController.hide();
        } else {
            mMediaController.show();
        }
    }

    public void start() {
        if (mMediaPlayer != null && mIsPrepared) {
            mMediaPlayer.start();
            mStartWhenPrepared = false;
        } else {
            mStartWhenPrepared = true;
        }
        requestFocus();
    }

    public void pause() {
        if (mMediaPlayer != null && mIsPrepared) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        }
        mStartWhenPrepared = false;
    }

    public int getDuration() {
        if (mMediaPlayer != null && mIsPrepared) {
            if (mDuration > 0) {
                return mDuration;
            }
            mDuration = mMediaPlayer.getDuration();
            return mDuration;
        }
        mDuration = -1;
        return mDuration;
    }

    public int getCurrentPosition() {
        if (mMediaPlayer != null && mIsPrepared) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public void seekTo(int msec) {
        if (mMediaPlayer != null && mIsPrepared) {
            mMediaPlayer.seekTo(msec);
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    public boolean isPlaying() {
        if (mMediaPlayer != null && mIsPrepared) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }

    public boolean isPrepared() {
        return mIsPrepared;
    }

    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    @Override
    public boolean canPause() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canSeekBackward() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canSeekForward() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getAudioSessionId() {
        // TODO Auto-generated method stub
        return 0;
    }

    //full screen section**************************************************************************

    public interface OnScreenChangedListener {
        public boolean onScreenChanged(boolean flag);
    }

    public void setScreenChangedListener(OnScreenChangedListener l) {
        this.mScreenChangedListener = l;
    }

    public boolean isFullscreen() {
        if (mMediaController != null) {
            return mMediaController.isFullscreen();
        }

        return false;
    }

    @Override
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

            if (mScreenChangedListener != null) {
                boolean consumed = mScreenChangedListener.onScreenChanged(true);
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

            if (mScreenChangedListener != null) {
                boolean consumed = mScreenChangedListener.onScreenChanged(false);
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

}
