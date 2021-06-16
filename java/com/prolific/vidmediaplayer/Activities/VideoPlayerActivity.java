package com.prolific.vidmediaplayer.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.MotionEventCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bullhead.equalizer.EqualizerFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.prolific.vidmediaplayer.Adapter.VideoAdapter;
import com.prolific.vidmediaplayer.BuildConfig;
import com.prolific.vidmediaplayer.Models.MessageEvent;
import com.prolific.vidmediaplayer.Models.ModelClassForVideo;
import com.prolific.vidmediaplayer.Others.CountDownTimerWithPause;
import com.prolific.vidmediaplayer.R;
import com.prolific.vidmediaplayer.Services.BackgroundPlay;
import com.prolific.vidmediaplayer.Services.FloatService;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static android.media.MediaPlayer.SEEK_CLOSEST;

public class VideoPlayerActivity extends BaseActivity implements View.OnTouchListener, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, View.OnClickListener {

    public static ArrayList<Object> arrayListVideos;
    public static boolean isFullScreenActivityVisible = true;
    public static String mBucketName, mBucketId, strCheck, strCheckVal;
    public ImageView mPausePlay;
    public static int mPosition = 0, seekPosition = 0;
    public static SeekBar mSeekBar;
    public static String mSongN;
    public static TextView mSongName;
    public static String mVideoPath;
    public static String mVideoName;
    public static VideoView mVideoView;
    public static int mWhereFrom = 0;
    public static boolean playInBack = false;
    public static boolean videoIsPlaying = true;
    private int Brightness;
    private boolean ScreenIsLockedAndLockIsVisible = true;
    private boolean backPressed = false;
    private boolean controlsAreVisible = true;
    private int getVolume;
    private boolean isEqualizerShowed = false;
    private boolean isVolumeMute = false;
    private boolean isXPressed = false;
    private boolean isYPressed = false;
    private boolean isSpeedVisible = false;
    private boolean isScrolled = false;
    private AudioManager mAudioManager;
    private ImageView mBackBtn;
    private LinearLayout mBrightLayout;
    private SeekBar mBrightSeekBar;
    private LinearLayout mControls;
    private TextView mCurrentBri;
    private int mCurrentSongPosition, time;
    private TextView mCurrentVol;
    private TextView mDecre;
    private ImageView mEnableBackPlay;
    private ImageView mEqualizer;
    private FrameLayout mEqualizerLayout;
    private TextView mFirstText;
    private FrameLayout mFrame;
    private GestureDetector mGestureDetector;
    private Handler mHandler;
    private TextView mInrement;
    private ImageView mLockScreen, mUnLockScreen;
    private ImageView mSettings;
    private ImageView mMute;
    private ImageView mNext, ivVideoList;
    private ImageView mPrevious;
    private ImageView mRotate;
    private ImageView mScreenShot;
    private ImageView mOverlay;
    private TextView mSecondText;
    private ImageView mShowBrightness;
    private ImageView mSoundImageShow;
    private LinearLayout mSoundShow;
    private LinearLayout mTopBar, mLeftBar, mRightBar, llSpeed, llSettings, llSpeedDisplay, llPlayList;
    private TextView playSpeed, tv0_5x, tv0_75x, tv1_0x, tv1_25x, tv1_5x, tv2_0x;
    private Handler mViewsHandler;
    private Runnable mViewsRunnable, mRunnable;
    private SeekBar mVolumeSeek;
    private boolean mediaWasPlayed = false;
    private boolean onResumeHasCalled = false;
    private boolean repeatThisSOng = false;
    private boolean screenIsLandscape = true;
    private boolean screenIsNotLocked = true;
    public int videoHeight, imgHeight;
    public int videoWidth, imgWidth;
    private float speedVal = 1.0f;
    private MediaPlayer mp;
    private PlaybackParams myPlayBackParams;
    public static int orientation;
    private RecyclerView rvPlayList;
    private VideoAdapter mAdapter;
    private ArrayList<Uri> videoUri;
    private LinearLayout llSleep;
    private boolean isScreenSleep = false;
    private boolean isPlayerAutoPlay;
    private int screenOrientation;
    private TextView tvPlayerErrorMsg;
    private CountDownTimerWithPause cdt;
    private Dialog dialogSettings, dialogSS;
    private TextView tvSleepOff, tvSleep10, tvSleep30, tvSleep60, tvSleepToEnd, tvSleepCustom;
    private TextView tvBestFit, tvFill, tvOriginal, tv18_9, tv16_9, tv4_3, tvFit_h, tvFit_v;
    private int newHeight;
    private String flag = "";
    private FloatService floatingService;

    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return false;
    }

    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
        return false;
    }

    public void onLongPress(MotionEvent motionEvent) {
    }

    public void onShowPress(MotionEvent motionEvent) {

    }

    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return true;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(flags);
//        int flags = WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
        getWindow().addFlags(flags);
        setContentView(R.layout.activity_video_player);

        this.mViewsHandler = new Handler();
        this.mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        floatingService = new FloatService();

        try {

            mVideoPath = getIntent().getStringExtra("path");
            mVideoName = getIntent().getStringExtra("name");
            mPosition = getIntent().getIntExtra("position", 0);
            seekPosition = getIntent().getIntExtra("seekPosition", 0);
            if (VideoPlayerActivity.mWhereFrom == 2) {//Folder tab
                mBucketName = getIntent().getStringExtra("bucketName");
                mBucketId = getIntent().getStringExtra("bucketId");
            }

            settingViews();
            settingControls();

            screenOrientation = getIntPrefs(VideoPlayerActivity.this, PREF_SCREEN_ORIENTATION);
            if (screenOrientation == 1) {
                //PORTRAIT
                screenIsLandscape = false;
                VideoPlayerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            } else if (screenOrientation == 2) {
                //LANDSCAPE
                screenIsLandscape = true;
                VideoPlayerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            } else {
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    screenIsLandscape = true;
                } else {
                    screenIsLandscape = false;
                }
            }

            setCommon();
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    private void setCommon() {
        try {
            startingVideo();
            setSeekbar();
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }


    private void getDropboxIMGSize(Uri uri) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(new File(uri.getPath()).getAbsolutePath(), options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;

    }


    private DisplayMetrics displayMetrics;

    private void settingViews() {

        orientation = getResources().getConfiguration().orientation;

        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        this.videoHeight = displayMetrics.heightPixels;
        this.videoWidth = displayMetrics.widthPixels;

        Log.e("TAG-SIZE", videoWidth + "x" + videoHeight);

        try {
            MediaMetadataRetriever mRetriever = new MediaMetadataRetriever();
            mRetriever.setDataSource(mVideoPath);
            Bitmap frame = mRetriever.getFrameAtTime();
            if (frame != null) {
                imgWidth = frame.getWidth();
                imgHeight = frame.getHeight();

                Log.e("TAG-IMAGE-SIZE", imgWidth + "x" + imgHeight);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        tvPlayerErrorMsg = (TextView) findViewById(R.id.tvPlayerErrorMsg);

        mVideoView = (VideoView) findViewById(R.id.videoView);
        mVideoView.setLayoutParams(new FrameLayout.LayoutParams(videoWidth, videoHeight, Gravity.CENTER));

        mPausePlay = (ImageView) findViewById(R.id.fsPause);
        mNext = (ImageView) findViewById(R.id.fsNext);
        ivVideoList = (ImageView) findViewById(R.id.ivVideoList);
        mPrevious = (ImageView) findViewById(R.id.fsPrev);
        mSeekBar = (SeekBar) findViewById(R.id.fsSeekbar);
        mSongName = (TextView) findViewById(R.id.fsSongName);
        mFirstText = (TextView) findViewById(R.id.fsFirstText);
        mSecondText = (TextView) findViewById(R.id.fsSecondText);
        mFrame = (FrameLayout) findViewById(R.id.frameLayout);
        llSleep = (LinearLayout) findViewById(R.id.llSleep);
        llPlayList = (LinearLayout) findViewById(R.id.llPlayList);
        llSpeed = (LinearLayout) findViewById(R.id.llSpeed);
        mControls = (LinearLayout) findViewById(R.id.fsControls);
        mRightBar = (LinearLayout) findViewById(R.id.rightBar);
        mLeftBar = (LinearLayout) findViewById(R.id.leftBar);
        mTopBar = (LinearLayout) findViewById(R.id.fsTopBar);
        llSettings = (LinearLayout) findViewById(R.id.llSettings);
        mSettings = (ImageView) findViewById(R.id.settings);
        mLockScreen = (ImageView) findViewById(R.id.lockScr);
        mUnLockScreen = (ImageView) findViewById(R.id.unlockScr);
        mRotate = (ImageView) findViewById(R.id.rotateScr);
        mScreenShot = (ImageView) findViewById(R.id.screenShot);
        mOverlay = (ImageView) findViewById(R.id.overlay);
        mCurrentBri = (TextView) findViewById(R.id.fsBrigh);
        mBrightSeekBar = (SeekBar) findViewById(R.id.brightnesSeelBar);
        mBrightLayout = (LinearLayout) findViewById(R.id.fsBright);
        mSoundShow = (LinearLayout) findViewById(R.id.fsSound);
        mVolumeSeek = (SeekBar) findViewById(R.id.volumeSeelBar);
        mCurrentVol = (TextView) findViewById(R.id.fsVolum);
        mShowBrightness = (ImageView) findViewById(R.id.brightShow);
        mSoundImageShow = (ImageView) findViewById(R.id.soundShow);
        tv0_5x = (TextView) findViewById(R.id.tv0_5x);
        tv0_75x = (TextView) findViewById(R.id.tv0_75x);
        tv1_0x = (TextView) findViewById(R.id.tv1_0x);
        tv1_25x = (TextView) findViewById(R.id.tv1_25x);
        tv1_5x = (TextView) findViewById(R.id.tv1_5x);
        tv2_0x = (TextView) findViewById(R.id.tv2_0x);
        playSpeed = (TextView) findViewById(R.id.playSpeed);
        llSpeedDisplay = (LinearLayout) findViewById(R.id.llSpeedDisplay);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            llSpeedDisplay.setVisibility(View.VISIBLE);
        } else {
            llSpeedDisplay.setVisibility(View.GONE);
        }
        this.mInrement = (TextView) findViewById(R.id.fsIncre);
        this.mDecre = (TextView) findViewById(R.id.fsDecre);
        this.mMute = (ImageView) findViewById(R.id.mute);
        this.mBackBtn = (ImageView) findViewById(R.id.fsGoBack);
        this.mEqualizerLayout = (FrameLayout) findViewById(R.id.eqFrame);
        this.mFrame.setOnTouchListener(this);
        getWindow().addFlags(128);

        rvPlayList = (RecyclerView) findViewById(R.id.rvPlayList);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        rvPlayList.setLayoutManager(manager);


        this.mGestureDetector = new GestureDetector(this, this);
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        this.mAudioManager = audioManager;
        if (audioManager != null) {
            int streamVolume = audioManager.getStreamVolume(3);
            this.getVolume = streamVolume;
            if (streamVolume > 0) {
                this.mMute.setImageResource(R.drawable.ic_volumeup);
            } else {
                this.mMute.setImageResource(R.drawable.ic_volume_off);
            }
        }
    }

    private void startingVideo() {
        mVideoView.setVisibility(View.VISIBLE);
        if (MainActivity.isStartedFromNotificationCLick) {
            mVideoView.setVideoPath(mVideoPath);
            mVideoView.seekTo(BackgroundPlay.mediaPlayer.getCurrentPosition());
            mVideoView.start();
            mediaWasPlayed = true;
        } else {
            String stringExtra = mVideoPath;
            mVideoPath = stringExtra;
            if (stringExtra != null) {
                BackgroundPlay.songPath = stringExtra;
                mediaWasPlayed = true;
            }
            mVideoView.setVideoPath(mVideoPath);
            mVideoView.seekTo(seekPosition);
            mVideoView.start();
        }
        if (!videoIsPlaying) {
            mPausePlay.performClick();
        }
        hideViewsAfterSomeTime();
        mSongName.setText(mVideoName);
        MainActivity.mediaIsPlayed = true;
        int i = mWhereFrom;
        arrayListVideos = new ArrayList<>();
        videoUri = new ArrayList<>();
        if (i == 1) {
            getMusic();
        } else if (i == 2) {
            getMusicInsideFragments();
        } else if (i == 3) {
            arrayListVideos.clear();
            arrRecentList = getRecentVideoList(this, PREF_RECENT_VIDEO_LIST);
            arrayListVideos = (ArrayList<Object>) arrRecentList;
        }
        if (arrayListVideos != null && arrayListVideos.size() > 0) {
            rvPlayList.setVisibility(View.VISIBLE);
            tvPlayerErrorMsg.setVisibility(View.GONE);

            mAdapter = new VideoAdapter(VideoPlayerActivity.this, arrayListVideos, "player", this);
            rvPlayList.setAdapter(mAdapter);
            rvPlayList.setHasFixedSize(true);

            rvPlayList.smoothScrollToPosition(mPosition);
        } else {
            rvPlayList.setVisibility(View.GONE);
            tvPlayerErrorMsg.setVisibility(View.VISIBLE);
        }


        setBrightness();
        volumeSeekBar();
    }

    private void settingControls() {
        mPausePlay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (VideoPlayerActivity.videoIsPlaying) {
                    if (cdt != null && cdt.isRunning()) {
                        cdt.pause();
                    }
                    VideoPlayerActivity.mVideoView.pause();
                    mPausePlay.setImageResource(R.drawable.ic_play);
                    VideoPlayerActivity.videoIsPlaying = false;
                    VideoPlayerActivity.this.playCycler();
                    return;
                }
                if (cdt != null && cdt.isPaused()) {
                    cdt.resume();
                }
                VideoPlayerActivity.mVideoView.start();
                mPausePlay.setImageResource(R.drawable.ic_pause);
                VideoPlayerActivity.videoIsPlaying = true;
                VideoPlayerActivity.this.playCycler();
            }
        });
        this.mNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                VideoPlayerActivity.mVideoView.stopPlayback();
                if (arrayListVideos != null && arrayListVideos.size() > 0) {
                    if (cdt != null && cdt.isRunning()) {
                        cdt.cancel();
                        isScreenSleep = false;
                        setStringPrefs(VideoPlayerActivity.this, PREF_SLEEP, tvSleepOff.getText().toString());
                    }
                    flag = "next_press";
                    if (VideoPlayerActivity.mPosition < arrayListVideos.size() - 1) {
                        VideoPlayerActivity.mVideoPath = ((ModelClassForVideo) arrayListVideos.get(mPosition + 1)).getSongPath();
                        VideoPlayerActivity.mPosition++;
                        VideoPlayerActivity.mVideoView.setVideoPath(mVideoPath);
                        seekPosition = 0;
                        VideoPlayerActivity.mVideoView.seekTo(seekPosition);
                        VideoPlayerActivity.mVideoView.start();
                        BackgroundPlay.songPath = VideoPlayerActivity.mVideoPath;
                        mPausePlay.setImageResource(R.drawable.ic_pause);
                        VideoPlayerActivity.videoIsPlaying = true;
                    } else {
                        VideoPlayerActivity.mPosition = 0;
                        VideoPlayerActivity.mVideoPath = ((ModelClassForVideo) arrayListVideos.get(mPosition)).getSongPath();
                        VideoPlayerActivity.mVideoView.setVideoPath(VideoPlayerActivity.mVideoPath);
                        seekPosition = 0;
                        VideoPlayerActivity.mVideoView.seekTo(seekPosition);
                        VideoPlayerActivity.mVideoView.start();
                        BackgroundPlay.songPath = VideoPlayerActivity.mVideoPath;
                        mPausePlay.setImageResource(R.drawable.ic_pause);
                        VideoPlayerActivity.videoIsPlaying = true;
                    }
                    VideoPlayerActivity.mSongN = ((ModelClassForVideo) arrayListVideos.get(mPosition)).getSongName();
                    VideoPlayerActivity.mSongName.setText(VideoPlayerActivity.mSongN);
                    mAdapter.notifyDataSetChanged();
                } else {
                    mPausePlay.setImageResource(R.drawable.ic_play);
                    VideoPlayerActivity.videoIsPlaying = false;
                }
            }
        });
        this.mPrevious.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                VideoPlayerActivity.mVideoView.stopPlayback();
                if (arrayListVideos != null && arrayListVideos.size() > 0) {
                    if (cdt != null && cdt.isRunning()) {
                        cdt.cancel();
                        isScreenSleep = false;
                        setStringPrefs(VideoPlayerActivity.this, PREF_SLEEP, tvSleepOff.getText().toString());
                    }
                    flag = "previous_press";
                    if (VideoPlayerActivity.mPosition >= arrayListVideos.size() || VideoPlayerActivity.mPosition <= 0) {
                        VideoPlayerActivity.mPosition = arrayListVideos.size() - 1;
                        VideoPlayerActivity.mVideoPath = ((ModelClassForVideo) arrayListVideos.get(VideoPlayerActivity.mPosition)).getSongPath();
                        VideoPlayerActivity.mVideoView.setVideoPath(VideoPlayerActivity.mVideoPath);
                        seekPosition = 0;
                        VideoPlayerActivity.mVideoView.seekTo(seekPosition);
                        VideoPlayerActivity.mVideoView.start();
                        BackgroundPlay.songPath = VideoPlayerActivity.mVideoPath;
                        mPausePlay.setImageResource(R.drawable.ic_pause);
                        VideoPlayerActivity.videoIsPlaying = true;
                    } else {
                        VideoPlayerActivity.mVideoPath = ((ModelClassForVideo) arrayListVideos.get(VideoPlayerActivity.mPosition - 1)).getSongPath();
                        VideoPlayerActivity.mPosition--;
                        VideoPlayerActivity.mVideoView.setVideoPath(VideoPlayerActivity.mVideoPath);
                        seekPosition = 0;
                        VideoPlayerActivity.mVideoView.seekTo(seekPosition);
                        VideoPlayerActivity.mVideoView.start();
                        BackgroundPlay.songPath = VideoPlayerActivity.mVideoPath;
                        mPausePlay.setImageResource(R.drawable.ic_pause);
                        VideoPlayerActivity.videoIsPlaying = true;
                    }
                    VideoPlayerActivity.mSongN = ((ModelClassForVideo) arrayListVideos.get(VideoPlayerActivity.mPosition)).getSongName();
                    VideoPlayerActivity.mSongName.setText(VideoPlayerActivity.mSongN);
                    mAdapter.notifyDataSetChanged();
                } else {
                    mPausePlay.setImageResource(R.drawable.ic_play);
                    VideoPlayerActivity.videoIsPlaying = false;
                }
            }
        });


        ivVideoList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                llPlayList.setLayoutParams(new FrameLayout.LayoutParams((int) Math.round((double) videoWidth / 1.5), ViewGroup.LayoutParams.MATCH_PARENT, Gravity.END));//videoHeight   //Integer.parseInt(String.valueOf((double) videoWidth / 1.5))
                llPlayList.setClickable(true);
                llPlayList.setEnabled(true);
                llPlayList.setVisibility(View.VISIBLE);
                llPlayList.startAnimation(AnimationUtils.loadAnimation(VideoPlayerActivity.this, R.anim.slide_in_right));//inFromRightAnimation()
            }
        });


        this.mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideViews();
                setSettingDialog();
            }
        });

        this.mLockScreen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (VideoPlayerActivity.this.screenIsNotLocked) {
                    VideoPlayerActivity.this.hideViews();
                    mUnLockScreen.setVisibility(View.VISIBLE);
                    mUnLockScreen.startAnimation(AnimationUtils.loadAnimation(VideoPlayerActivity.this, R.anim.slide_in_right));
                    Toast.makeText(VideoPlayerActivity.this, "Screen is locked", Toast.LENGTH_SHORT).show();
                    VideoPlayerActivity.this.screenIsNotLocked = false;
                    return;
                }
            }
        });
        mUnLockScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VideoPlayerActivity.this.showViews();
                mUnLockScreen.startAnimation(AnimationUtils.loadAnimation(VideoPlayerActivity.this, R.anim.slide_out_right));
                mUnLockScreen.setVisibility(View.GONE);
                Toast.makeText(VideoPlayerActivity.this, "Screen is unlocked", Toast.LENGTH_SHORT).show();
                VideoPlayerActivity.this.screenIsNotLocked = true;
                VideoPlayerActivity.this.hideViewsAfterSomeTime();
            }
        });
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mediaPlayer) {
                Log.e("TAG", "onCompletion()");

                cancelSleepIfApplied();


                if (!isScreenSleep) {
                    if (VideoPlayerActivity.this.repeatThisSOng) {
                        seekPosition = 0;
                        VideoPlayerActivity.mVideoView.seekTo(seekPosition);
                        VideoPlayerActivity.mVideoView.start();
                        VideoPlayerActivity.this.playCycler();
                        return;
                    }
                    isPlayerAutoPlay = getBooleanPrefs(VideoPlayerActivity.this, PREF_IS_PLAYER_AUTO_PLAY);
                    if (isPlayerAutoPlay) {
                        if (arrayListVideos != null && arrayListVideos.size() > 0) {
                            if (VideoPlayerActivity.mPosition < arrayListVideos.size() - 1) {
                                VideoPlayerActivity.mVideoPath = ((ModelClassForVideo) arrayListVideos.get(mPosition + 1)).getSongPath();
                                VideoPlayerActivity.mPosition++;
                                VideoPlayerActivity.mVideoView.setVideoPath(VideoPlayerActivity.mVideoPath);
                                seekPosition = 0;
                                VideoPlayerActivity.mVideoView.seekTo(seekPosition);
                                VideoPlayerActivity.mVideoView.start();
                                BackgroundPlay.songPath = VideoPlayerActivity.mVideoPath;
                                mPausePlay.setImageResource(R.drawable.ic_pause);
                                VideoPlayerActivity.videoIsPlaying = true;
                            } else {
                                VideoPlayerActivity.mPosition = 0;
                                VideoPlayerActivity.mVideoPath = ((ModelClassForVideo) arrayListVideos.get(mPosition)).getSongPath();
                                VideoPlayerActivity.mVideoView.setVideoPath(VideoPlayerActivity.mVideoPath);
                                seekPosition = 0;
                                VideoPlayerActivity.mVideoView.seekTo(seekPosition);
                                VideoPlayerActivity.mVideoView.start();
                                BackgroundPlay.songPath = VideoPlayerActivity.mVideoPath;
                                mPausePlay.setImageResource(R.drawable.ic_pause);
                                VideoPlayerActivity.videoIsPlaying = true;
                            }
                            VideoPlayerActivity.mSongN = ((ModelClassForVideo) arrayListVideos.get(mPosition)).getSongName();
                            VideoPlayerActivity.mSongName.setText(VideoPlayerActivity.mSongN);
                            mAdapter.notifyDataSetChanged();
                        } else {
                            mPausePlay.setImageResource(R.drawable.ic_play);
                            VideoPlayerActivity.videoIsPlaying = false;
                        }
                    } else {
                        mPausePlay.setImageResource(R.drawable.ic_play);
                        VideoPlayerActivity.videoIsPlaying = false;
                    }
                } else {
                    isScreenSleep = false;
                }
            }
        });
        this.mRotate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (VideoPlayerActivity.this.screenIsLandscape) {
                    VideoPlayerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    VideoPlayerActivity.this.screenIsLandscape = false;
                    return;
                }
                VideoPlayerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                VideoPlayerActivity.this.screenIsLandscape = true;
            }
        });
        this.mMute.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (VideoPlayerActivity.this.isVolumeMute) {
                    VideoPlayerActivity.this.mAudioManager.setStreamVolume(3, VideoPlayerActivity.this.getVolume, 0);
                    VideoPlayerActivity.this.isVolumeMute = false;
                    VideoPlayerActivity.this.mMute.setImageResource(R.drawable.ic_volumeup);
                    return;
                }
                VideoPlayerActivity.this.mAudioManager.setStreamVolume(3, 0, 0);
                VideoPlayerActivity.this.isVolumeMute = true;
                VideoPlayerActivity.this.mMute.setImageResource(R.drawable.ic_volume_off);
            }
        });
        this.mBackBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onBackPressed();
            }
        });


        mScreenShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (videoIsPlaying) {
                    mPausePlay.performClick();
                }
                save_screenshot();
            }
        });

        mOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

                    if (VideoPlayerActivity.this.screenIsLandscape) {
                        VideoPlayerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        VideoPlayerActivity.this.screenIsLandscape = false;
                    }

                    if (!Settings.canDrawOverlays(VideoPlayerActivity.this)) {

                        setOverlayPermissionDialog();

                    } else {
//                        floatingService.setArrayData(videoUri, arrayListVideos);
                        Intent intent = new Intent(VideoPlayerActivity.this, FloatService.class);

                        Bundle bundle = new Bundle();
                        bundle.putSerializable("videoUriList", videoUri);
                        bundle.putSerializable("videoDataList", arrayListVideos);
                        bundle.putInt("position", mPosition);
                        bundle.putInt("seekPositionFloat", seekPosition);
                        bundle.putInt("whereFrom", VideoPlayerActivity.mWhereFrom);
                        bundle.putString("bucketName", mBucketName);
                        intent.putExtras(bundle);

//                        intent.putExtra("videoUriList", videoUri);
//                        intent.putExtra("videoDataList", arrayListVideos);
//                        intent.putExtra("position", mPosition);
//                        intent.putExtra("seekPositionFloat", seekPosition);
//                        intent.putExtra("whereFrom", VideoPlayerActivity.mWhereFrom);
//                        intent.putExtra("bucketName", mBucketName);
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            startForegroundService(intent);
                        } else {
                            startService(intent);
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 50);
                    }
                } else {
                    Intent intent = new Intent(VideoPlayerActivity.this, FloatService.class);
                    intent.putExtra("videoUriList", videoUri);
                    intent.putExtra("videoDataList", arrayListVideos);
                    intent.putExtra("position", mPosition);
                    intent.putExtra("seekPositionFloat", mVideoView.getCurrentPosition());
                    intent.putExtra("whereFrom", VideoPlayerActivity.mWhereFrom);
                    intent.putExtra("bucketName", mBucketName);
                    startService(intent);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 50);
                }


            }
        });


        this.playSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideViews();
                if (speedVal == 0.5f) {
                    tv0_5x.setTextColor(getResources().getColor(R.color.colorAccent));
                } else if (speedVal == 0.75f) {
                    tv0_75x.setTextColor(getResources().getColor(R.color.colorAccent));
                } else if (speedVal == 1.0f) {
                    tv1_0x.setTextColor(getResources().getColor(R.color.colorAccent));
                } else if (speedVal == 1.25f) {
                    tv1_25x.setTextColor(getResources().getColor(R.color.colorAccent));
                } else if (speedVal == 1.5f) {
                    tv1_5x.setTextColor(getResources().getColor(R.color.colorAccent));
                } else if (speedVal == 2.0f) {
                    tv2_0x.setTextColor(getResources().getColor(R.color.colorAccent));
                }
                VideoPlayerActivity.this.llSpeed.startAnimation(AnimationUtils.loadAnimation(VideoPlayerActivity.this, R.anim.slide_in_bottom));//inFromBottomAnimation()
                VideoPlayerActivity.this.llSpeed.setVisibility(View.VISIBLE);
                VideoPlayerActivity.this.isSpeedVisible = true;
            }
        });

        tv0_5x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speedVal = 0.5f;
                playSpeed.setText(speedVal + "X");
                tv0_5x.setTextColor(getResources().getColor(R.color.colorAccent));
                tv0_75x.setTextColor(getResources().getColor(R.color.colorWhite));
                tv1_0x.setTextColor(getResources().getColor(R.color.colorWhite));
                tv1_25x.setTextColor(getResources().getColor(R.color.colorWhite));
                tv1_5x.setTextColor(getResources().getColor(R.color.colorWhite));
                tv2_0x.setTextColor(getResources().getColor(R.color.colorWhite));

                setPlaySpeed(speedVal);
            }
        });

        tv0_75x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speedVal = 0.75f;
                playSpeed.setText(speedVal + "X");
                tv0_5x.setTextColor(getResources().getColor(R.color.colorWhite));
                tv0_75x.setTextColor(getResources().getColor(R.color.colorAccent));
                tv1_0x.setTextColor(getResources().getColor(R.color.colorWhite));
                tv1_25x.setTextColor(getResources().getColor(R.color.colorWhite));
                tv1_5x.setTextColor(getResources().getColor(R.color.colorWhite));
                tv2_0x.setTextColor(getResources().getColor(R.color.colorWhite));

                setPlaySpeed(speedVal);
            }
        });

        tv1_0x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speedVal = 1.0f;
                playSpeed.setText(speedVal + "X");
                tv0_5x.setTextColor(getResources().getColor(R.color.colorWhite));
                tv0_75x.setTextColor(getResources().getColor(R.color.colorWhite));
                tv1_0x.setTextColor(getResources().getColor(R.color.colorAccent));
                tv1_25x.setTextColor(getResources().getColor(R.color.colorWhite));
                tv1_5x.setTextColor(getResources().getColor(R.color.colorWhite));
                tv2_0x.setTextColor(getResources().getColor(R.color.colorWhite));

                setPlaySpeed(speedVal);
            }
        });

        tv1_25x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speedVal = 1.25f;
                playSpeed.setText(speedVal + "X");
                tv0_5x.setTextColor(getResources().getColor(R.color.colorWhite));
                tv0_75x.setTextColor(getResources().getColor(R.color.colorWhite));
                tv1_0x.setTextColor(getResources().getColor(R.color.colorWhite));
                tv1_25x.setTextColor(getResources().getColor(R.color.colorAccent));
                tv1_5x.setTextColor(getResources().getColor(R.color.colorWhite));
                tv2_0x.setTextColor(getResources().getColor(R.color.colorWhite));

                setPlaySpeed(speedVal);
            }
        });

        tv1_5x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speedVal = 1.5f;
                playSpeed.setText(speedVal + "X");
                tv0_5x.setTextColor(getResources().getColor(R.color.colorWhite));
                tv0_75x.setTextColor(getResources().getColor(R.color.colorWhite));
                tv1_0x.setTextColor(getResources().getColor(R.color.colorWhite));
                tv1_25x.setTextColor(getResources().getColor(R.color.colorWhite));
                tv1_5x.setTextColor(getResources().getColor(R.color.colorAccent));
                tv2_0x.setTextColor(getResources().getColor(R.color.colorWhite));

                setPlaySpeed(speedVal);
            }
        });

        tv2_0x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speedVal = 2.0f;
                playSpeed.setText(speedVal + "X");
                tv0_5x.setTextColor(getResources().getColor(R.color.colorWhite));
                tv0_75x.setTextColor(getResources().getColor(R.color.colorWhite));
                tv1_0x.setTextColor(getResources().getColor(R.color.colorWhite));
                tv1_25x.setTextColor(getResources().getColor(R.color.colorWhite));
                tv1_5x.setTextColor(getResources().getColor(R.color.colorWhite));
                tv2_0x.setTextColor(getResources().getColor(R.color.colorAccent));

                setPlaySpeed(speedVal);
            }
        });

    }


    private Dialog dialogFloatWindow;

    private void setOverlayPermissionDialog() {

        dialogFloatWindow = new Dialog(this);
        dialogFloatWindow.setContentView(R.layout.layout_delete);
        dialogFloatWindow.getWindow().setLayout(-1, -2);

        Button btnCancel = (Button) dialogFloatWindow.findViewById(R.id.btnCancel);
        Button btnDelete = (Button) dialogFloatWindow.findViewById(R.id.btnDelete);
        TextView tvDeleteMsg = (TextView) dialogFloatWindow.findViewById(R.id.tvDeleteMsg);
        TextView tvTitle = (TextView) dialogFloatWindow.findViewById(R.id.tvTitle);

        btnDelete.setText("Settings");
        tvTitle.setText("Floating window permission");
        tvDeleteMsg.setText("System prohibits Floating window display by default, click \"Settings\" to give permission");

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dialogFloatWindow.dismiss();
                    Intent request = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + VideoPlayerActivity.this.getPackageName()));
                    startActivityForResult(request, 111);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogFloatWindow.dismiss();
            }
        });
        dialogFloatWindow.show();

//        MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(this);
//        alert.setTitle("Permission setting");
//        alert.setMessage("Part of the system prohibit the float window display by default, please allow the pop-up window permission first to open the video.");
//        alert.setPositiveButton("Go to Open", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//            }
//        });
//        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//        alert.create();
//        alert.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 111) {
//            if(resultCode == RESULT_OK) {
            mOverlay.performClick();
//            }else{
//                dialogFloatWindow.dismiss();
//            }
        } else if (requestCode == 10000) {
            if (resultCode == Activity.RESULT_OK) {
                if (dDelete != null && dDelete.isShowing()) {
                    dDelete.dismiss();
                }
                deleteRecordAndNotify();
            } else {
                Toast.makeText(VideoPlayerActivity.this, "error", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private int deleteVal;
    private Dialog dDelete;

    private void deleteVideo() {

        String path = ((ModelClassForVideo) arrayListVideos.get(mPosition)).getSongPath();

        dDelete = new Dialog(this);
        dDelete.setContentView(R.layout.layout_delete);
        dDelete.getWindow().setLayout(-1, -2);

        Button btnCancel = (Button) dDelete.findViewById(R.id.btnCancel);
        Button btnDelete = (Button) dDelete.findViewById(R.id.btnDelete);
        TextView tvDeleteMsg = (TextView) dDelete.findViewById(R.id.tvDeleteMsg);

        tvDeleteMsg.setText(getString(R.string.delete_msg) + " this video?");

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    Log.e("TAG", path);
                    File vidFile = new File(path);
                    if (!vidFile.exists()) {
                        return;
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        ArrayList<Uri> list = new ArrayList<>();
                        list.add(getUriFromPath(vidFile.getAbsolutePath(), VideoPlayerActivity.this));
                        PendingIntent intent = MediaStore.createDeleteRequest(getContentResolver(), list);
                        try {
                            startIntentSenderForResult(intent.getIntentSender(), 10000, null, 0, 0, 0, null);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }

                    } else {

                        ContentResolver contentResolver = VideoPlayerActivity.this.getContentResolver();
                        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        deleteVal = contentResolver.delete(uri, MediaStore.Images.Media.DATA + "=?", new String[]{vidFile.getAbsolutePath()});
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + vidFile)));

                        dDelete.dismiss();
                        deleteRecordAndNotify();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dDelete.dismiss();
            }
        });
        dDelete.show();

//        new MaterialAlertDialogBuilder(this)
//                .setTitle("Delete")
//                .setMessage("Are you sure you want to delete this video?")
//                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        try {
////                            llSettings.setVisibility(View.GONE);
//
//                            Log.e("TAG", path);
//                            File vidFile = new File(path);
//                            if (!vidFile.exists()) {
//                                return;
//                            }
//
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                                ArrayList<Uri> list = new ArrayList<>();
//                                list.add(getUriFromPath(vidFile.getAbsolutePath(), VideoPlayerActivity.this));
//                                PendingIntent intent = MediaStore.createDeleteRequest(getContentResolver(), list);
//                                try {
//                                    startIntentSenderForResult(intent.getIntentSender(), 10000, null, 0, 0, 0, null);
//                                } catch (IntentSender.SendIntentException e) {
//                                    e.printStackTrace();
//                                }
//
//                            } else {
//
//                                ContentResolver contentResolver = VideoPlayerActivity.this.getContentResolver();
//                                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
//                                deleteVal = contentResolver.delete(uri, MediaStore.Images.Media.DATA + "=?", new String[]{vidFile.getAbsolutePath()});
////                                if (Build.VERSION.SDK_INT >= 19) {
////                                    VideoPlayerActivity.this.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE"));
////                                } else {
////                                    VideoPlayerActivity.this.sendBroadcast(new Intent("android.intent.action.MEDIA_MOUNTED", Uri.parse("file://" + Environment.getExternalStorageDirectory())));
////                                }
//                                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + vidFile)));
//
//                                deleteRecordAndNotify();
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                })
//                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//                    }
//                }).show

    }


    public void deleteRecordAndNotify() {

        arrRecentList = getRecentVideoList(VideoPlayerActivity.this, PREF_RECENT_VIDEO_LIST);
        for (int j = 0; j < arrRecentList.size(); j++) {
            if (((ModelClassForVideo) arrRecentList.get(j)).getSongPath().equals(((ModelClassForVideo) arrayListVideos.get(mPosition)).getSongPath())) {
                arrRecentList.remove(j);
                setRecentVideoList(VideoPlayerActivity.this, PREF_RECENT_VIDEO_LIST, arrRecentList);
                break;
            }
        }

        ModelClassForVideo latestData = getLatestVideo(VideoPlayerActivity.this, PREF_LATEST_PLAY);
        if (latestData != null) {
            if (latestData.getSongPath().equals(((ModelClassForVideo) arrayListVideos.get(mPosition)).getSongPath())) {
                removePrefsByKey(VideoPlayerActivity.this, PREF_LATEST_PLAY);
            }
        }

        llSettings.setVisibility(View.GONE);
        arrayListVideos.remove(mPosition);
        mAdapter.notifyDataSetChanged();
        if (mPosition < arrayListVideos.size() - 1) {
            mVideoPath = ((ModelClassForVideo) arrayListVideos.get(mPosition)).getSongPath();
            mVideoName = ((ModelClassForVideo) arrayListVideos.get(mPosition)).getSongName();
        }

        setCommon();
//                if (deleteVal == 1) {
        Toast.makeText(VideoPlayerActivity.this.getApplicationContext(), "File Deleted Successfully", Toast.LENGTH_SHORT).show();
//        ((BaseActivity) this).notifyAllApp();
        EventBus.getDefault().post(new MessageEvent("RefreshVideo", ""));
        EventBus.getDefault().post(new MessageEvent("RefreshFolder", ""));
        EventBus.getDefault().post(new MessageEvent("RefreshRecent", ""));
//                }

    }


    private void setSleepTime(int screenOffTime) {
        if (cdt != null && cdt.isRunning()) {
            cdt.cancel();
        }
        cdt = new CountDownTimerWithPause(screenOffTime, 1000, true) {
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                Log.e("TAG", "onFinish()");
                if (VideoPlayerActivity.videoIsPlaying) {
                    cancelSleepIfApplied();
                    VideoPlayerActivity.mVideoView.pause();
                    mPausePlay.setImageResource(R.drawable.ic_play);
                    VideoPlayerActivity.videoIsPlaying = false;
                }
                VideoPlayerActivity.this.hideViews();
                llSettings.setVisibility(View.GONE);
                if (dialogSettings != null && dialogSettings.isShowing()) {
                    dialogSettings.dismiss();
                }
                if (llPlayList.getVisibility() == View.VISIBLE) {
                    llPlayList.setVisibility(View.GONE);
                    llPlayList.startAnimation(AnimationUtils.loadAnimation(VideoPlayerActivity.this, R.anim.slide_out_right));
                }
                if (llSpeed.getVisibility() == View.VISIBLE) {
                    llSpeed.setVisibility(View.GONE);
                    llSpeed.startAnimation(AnimationUtils.loadAnimation(VideoPlayerActivity.this, R.anim.slide_out_right));
                }
                llSleep.setVisibility(View.VISIBLE);
                VideoPlayerActivity.this.playCycler();
            }
        };
        cdt.create();
        if (!videoIsPlaying) {
            cdt.pause();
        }
    }


    public void cancelSleepIfApplied() {
        String strSleepVal = getStringPrefs(VideoPlayerActivity.this, PREF_SLEEP);
        if (strSleepVal.equalsIgnoreCase("To end")) {
            isScreenSleep = true;
        }
        if (tvSleepOff != null) {
            setStringPrefs(VideoPlayerActivity.this, PREF_SLEEP, tvSleepOff.getText().toString());
        }
    }


    private void getSleepMilliseconds(String screenOffTimeout) {
        switch (screenOffTimeout) {
            case "0":
                tvSleepOff.setTextColor(getResources().getColor(R.color.colorAccent));
                tvSleep10.setTextColor(getResources().getColor(R.color.colorWhite));
                tvSleep30.setTextColor(getResources().getColor(R.color.colorWhite));
                tvSleep60.setTextColor(getResources().getColor(R.color.colorWhite));
                tvSleepToEnd.setTextColor(getResources().getColor(R.color.colorWhite));
                tvSleepCustom.setTextColor(getResources().getColor(R.color.colorWhite));
                isScreenSleep = false;
                Toast.makeText(VideoPlayerActivity.this, "Sleep mode is off", Toast.LENGTH_SHORT).show();
                if (cdt != null) {
                    cdt.cancel();
                }
                break;

            case "10":
                time = 600000;
                tvSleepOff.setTextColor(getResources().getColor(R.color.colorWhite));
                tvSleep10.setTextColor(getResources().getColor(R.color.colorAccent));
                tvSleep30.setTextColor(getResources().getColor(R.color.colorWhite));
                tvSleep60.setTextColor(getResources().getColor(R.color.colorWhite));
                tvSleepToEnd.setTextColor(getResources().getColor(R.color.colorWhite));
                tvSleepCustom.setTextColor(getResources().getColor(R.color.colorWhite));
                Toast.makeText(VideoPlayerActivity.this, "After 10 minutes into the sleep mode", Toast.LENGTH_SHORT).show();
                setSleepTime(time);
                break;

            case "30":
                time = 1800000;
                tvSleepOff.setTextColor(getResources().getColor(R.color.colorWhite));
                tvSleep10.setTextColor(getResources().getColor(R.color.colorWhite));
                tvSleep30.setTextColor(getResources().getColor(R.color.colorAccent));
                tvSleep60.setTextColor(getResources().getColor(R.color.colorWhite));
                tvSleepToEnd.setTextColor(getResources().getColor(R.color.colorWhite));
                tvSleepCustom.setTextColor(getResources().getColor(R.color.colorWhite));
                Toast.makeText(VideoPlayerActivity.this, "After 30 minutes into the sleep mode", Toast.LENGTH_SHORT).show();
                setSleepTime(time);
                break;

            case "60":
                time = 3600000;
                tvSleepOff.setTextColor(getResources().getColor(R.color.colorWhite));
                tvSleep10.setTextColor(getResources().getColor(R.color.colorWhite));
                tvSleep30.setTextColor(getResources().getColor(R.color.colorWhite));
                tvSleep60.setTextColor(getResources().getColor(R.color.colorAccent));
                tvSleepToEnd.setTextColor(getResources().getColor(R.color.colorWhite));
                tvSleepCustom.setTextColor(getResources().getColor(R.color.colorWhite));
                Toast.makeText(VideoPlayerActivity.this, "After 60 minutes into the sleep mode", Toast.LENGTH_SHORT).show();
                setSleepTime(time);
                break;

            case "TO_END":
                tvSleepOff.setTextColor(getResources().getColor(R.color.colorWhite));
                tvSleep10.setTextColor(getResources().getColor(R.color.colorWhite));
                tvSleep30.setTextColor(getResources().getColor(R.color.colorWhite));
                tvSleep60.setTextColor(getResources().getColor(R.color.colorWhite));
                tvSleepToEnd.setTextColor(getResources().getColor(R.color.colorAccent));
                tvSleepCustom.setTextColor(getResources().getColor(R.color.colorWhite));

                time = mVideoView.getDuration() - seekPosition;
                Toast.makeText(VideoPlayerActivity.this, "Will sleep when finish current video", Toast.LENGTH_SHORT).show();
                setSleepTime(time);
                break;

            case "CUSTOM":
                setTimePicker();
                break;

            default:
                break;
        }
    }


    TimePicker.OnTimeChangedListener onTimeChangedListener = new TimePicker.OnTimeChangedListener() {
        @Override
        public void onTimeChanged(TimePicker timePicker, int i, int i1) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, i);
            cal.set(Calendar.MINUTE, i1);
            totalMillis = cal.getTimeInMillis();
        }
    };


    private long totalMillis, currMillis, diffMillis;
    private Dialog dTimePicker;

    private void setTimePicker() {
        MaterialAlertDialogBuilder alertTime = new MaterialAlertDialogBuilder(this);
//        alertTime.setTitle("Select time");
        alertTime.setCancelable(false);
        View v = LayoutInflater.from(this).inflate(R.layout.layout_set_time, null);

        Button btnOk = v.findViewById(R.id.btnOk);
        Button btnCancel = v.findViewById(R.id.btnCancel);
        TimePicker sleepTimePicker = v.findViewById(R.id.sleepTimePicker);
        sleepTimePicker.setIs24HourView(true);

        sleepTimePicker.setOnTimeChangedListener(onTimeChangedListener);


        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                currMillis = calendar.getTimeInMillis();

                time = (int) (totalMillis - currMillis);
                tvSleepOff.setTextColor(getResources().getColor(R.color.colorWhite));
                tvSleep10.setTextColor(getResources().getColor(R.color.colorWhite));
                tvSleep30.setTextColor(getResources().getColor(R.color.colorWhite));
                tvSleep60.setTextColor(getResources().getColor(R.color.colorWhite));
                tvSleepToEnd.setTextColor(getResources().getColor(R.color.colorWhite));
                tvSleepCustom.setTextColor(getResources().getColor(R.color.colorAccent));
                Toast.makeText(VideoPlayerActivity.this, "Player sleeps on selected time", Toast.LENGTH_SHORT).show();
                setSleepTime(time);
                dTimePicker.dismiss();

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dTimePicker.dismiss();
            }
        });

        alertTime.setView(v);
        dTimePicker = alertTime.create();
        dTimePicker.show();
    }


    private void setSettingDialog() {

        dialogSettings = new Dialog(VideoPlayerActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        View settingView;

        if (orientation == Configuration.ORIENTATION_LANDSCAPE || VideoPlayerActivity.this.screenIsLandscape) {
            // In landscape
            settingView = LayoutInflater.from(this).inflate(R.layout.layout_settings_land, null);
        } else {
            // In portrait
            settingView = LayoutInflater.from(this).inflate(R.layout.layout_settings, null);
        }

        LinearLayout llSettingBackPlay = settingView.findViewById(R.id.llSettingBackPlay);
        LinearLayout llSettingLoop = settingView.findViewById(R.id.llSettingLoop);
        LinearLayout llSettingEqualizer = settingView.findViewById(R.id.llSettingEqualizer);
        LinearLayout llSettingDelete = settingView.findViewById(R.id.llSettingDelete);
        LinearLayout llSettingFloat = settingView.findViewById(R.id.llSettingFloat);

        ImageView ivLoop = settingView.findViewById(R.id.ivLoop);
        ImageView ivBackPlay = settingView.findViewById(R.id.ivBackPlay);
        ImageView ivEqualizer = settingView.findViewById(R.id.ivEqualizer);

        TextView tvLoop = settingView.findViewById(R.id.tvLoop);
        TextView tvBackPlay = settingView.findViewById(R.id.tvBackPlay);
        TextView tvEqualizer = settingView.findViewById(R.id.tvEqualizer);
        tvBestFit = settingView.findViewById(R.id.tvBestFit);
        tvFill = settingView.findViewById(R.id.tvFill);
        tvOriginal = settingView.findViewById(R.id.tvOriginal);
        tv18_9 = settingView.findViewById(R.id.tv18_9);
        tv16_9 = settingView.findViewById(R.id.tv16_9);
        tv4_3 = settingView.findViewById(R.id.tv4_3);
        tvFit_h = settingView.findViewById(R.id.tvFit_h);
        tvFit_v = settingView.findViewById(R.id.tvFit_v);

        tvSleepOff = settingView.findViewById(R.id.tvSleepOff);
        tvSleep10 = settingView.findViewById(R.id.tvSleep10);
        tvSleep30 = settingView.findViewById(R.id.tvSleep30);
        tvSleep60 = settingView.findViewById(R.id.tvSleep60);
        tvSleepToEnd = settingView.findViewById(R.id.tvSleepToEnd);
        tvSleepCustom = settingView.findViewById(R.id.tvSleepCustom);

        String ratioVal = getStringPrefs(this, PREF_SCREEN_RATIO);
        switch (ratioVal) {
            case "BEST FIT":
                tvBestFit.setTextColor(getResources().getColor(R.color.colorAccent));
                break;

            case "FILL":
                tvFill.setTextColor(getResources().getColor(R.color.colorAccent));
                break;

            case "ORIGINAL":
                tvOriginal.setTextColor(getResources().getColor(R.color.colorAccent));
                break;

            case "18:9":
                tv18_9.setTextColor(getResources().getColor(R.color.colorAccent));
                break;

            case "16:9":
                tv16_9.setTextColor(getResources().getColor(R.color.colorAccent));
                break;

            case "4:3":
                tv4_3.setTextColor(getResources().getColor(R.color.colorAccent));
                break;

            case "FIT-H":
                tvFit_h.setTextColor(getResources().getColor(R.color.colorAccent));
                break;

            case "FIT-V":
                tvFit_v.setTextColor(getResources().getColor(R.color.colorAccent));
                break;
        }

        String sleepVal = getStringPrefs(this, PREF_SLEEP);
        switch (sleepVal) {
            case "Off":
                tvSleepOff.setTextColor(getResources().getColor(R.color.colorAccent));
                break;

            case "10 mins":
                tvSleep10.setTextColor(getResources().getColor(R.color.colorAccent));
                break;

            case "30 mins":
                tvSleep30.setTextColor(getResources().getColor(R.color.colorAccent));
                break;

            case "60 mins":
                tvSleep60.setTextColor(getResources().getColor(R.color.colorAccent));
                break;

            case "To end":
                tvSleepToEnd.setTextColor(getResources().getColor(R.color.colorAccent));
                break;

            case "Custom":
                tvSleepCustom.setTextColor(getResources().getColor(R.color.colorAccent));
                break;
        }

        tvBestFit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvOriginal.setTextColor(getResources().getColor(R.color.colorWhite));
                tvBestFit.setTextColor(getResources().getColor(R.color.colorAccent));
                tvFill.setTextColor(getResources().getColor(R.color.colorWhite));
                tv18_9.setTextColor(getResources().getColor(R.color.colorWhite));
                tv16_9.setTextColor(getResources().getColor(R.color.colorWhite));
                tv4_3.setTextColor(getResources().getColor(R.color.colorWhite));
                tvFit_h.setTextColor(getResources().getColor(R.color.colorWhite));
                tvFit_v.setTextColor(getResources().getColor(R.color.colorWhite));

                mVideoView.setLayoutParams(new FrameLayout.LayoutParams(videoWidth, videoHeight, Gravity.CENTER));
                setStringPrefs(VideoPlayerActivity.this, PREF_SCREEN_RATIO, tvBestFit.getText().toString());
            }
        });

        tvFill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvOriginal.setTextColor(getResources().getColor(R.color.colorWhite));
                tvBestFit.setTextColor(getResources().getColor(R.color.colorWhite));
                tvFill.setTextColor(getResources().getColor(R.color.colorAccent));
                tv18_9.setTextColor(getResources().getColor(R.color.colorWhite));
                tv16_9.setTextColor(getResources().getColor(R.color.colorWhite));
                tv4_3.setTextColor(getResources().getColor(R.color.colorWhite));
                tvFit_h.setTextColor(getResources().getColor(R.color.colorWhite));
                tvFit_v.setTextColor(getResources().getColor(R.color.colorWhite));

                mVideoView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mVideoView.getLayoutParams().width = videoWidth;
                } else {
                    mVideoView.getLayoutParams().height = videoHeight;
                }
                setStringPrefs(VideoPlayerActivity.this, PREF_SCREEN_RATIO, tvFill.getText().toString());
            }
        });


        tvOriginal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvOriginal.setTextColor(getResources().getColor(R.color.colorAccent));
                tvBestFit.setTextColor(getResources().getColor(R.color.colorWhite));
                tvFill.setTextColor(getResources().getColor(R.color.colorWhite));
                tv18_9.setTextColor(getResources().getColor(R.color.colorWhite));
                tv16_9.setTextColor(getResources().getColor(R.color.colorWhite));
                tv4_3.setTextColor(getResources().getColor(R.color.colorWhite));
                tvFit_h.setTextColor(getResources().getColor(R.color.colorWhite));
                tvFit_v.setTextColor(getResources().getColor(R.color.colorWhite));

                mVideoView.setLayoutParams(new FrameLayout.LayoutParams(imgWidth, imgHeight, Gravity.CENTER));
                setStringPrefs(VideoPlayerActivity.this, PREF_SCREEN_RATIO, tvOriginal.getText().toString());
            }
        });

        tv18_9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvOriginal.setTextColor(getResources().getColor(R.color.colorWhite));
                tvBestFit.setTextColor(getResources().getColor(R.color.colorWhite));
                tvFill.setTextColor(getResources().getColor(R.color.colorWhite));
                tv18_9.setTextColor(getResources().getColor(R.color.colorAccent));
                tv16_9.setTextColor(getResources().getColor(R.color.colorWhite));
                tv4_3.setTextColor(getResources().getColor(R.color.colorWhite));
                tvFit_h.setTextColor(getResources().getColor(R.color.colorWhite));
                tvFit_v.setTextColor(getResources().getColor(R.color.colorWhite));

                mVideoView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    newHeight = (videoHeight * 9) / 18;
                } else {
                    newHeight = (videoWidth * 9) / 18;
                }
                Log.e("TAG", "NewHeight 18:9 = " + newHeight);
                mVideoView.getLayoutParams().width = videoWidth;
                mVideoView.getLayoutParams().height = newHeight;
                setStringPrefs(VideoPlayerActivity.this, PREF_SCREEN_RATIO, tv18_9.getText().toString());
            }
        });

        tv16_9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvOriginal.setTextColor(getResources().getColor(R.color.colorWhite));
                tvBestFit.setTextColor(getResources().getColor(R.color.colorWhite));
                tvFill.setTextColor(getResources().getColor(R.color.colorWhite));
                tv18_9.setTextColor(getResources().getColor(R.color.colorWhite));
                tv16_9.setTextColor(getResources().getColor(R.color.colorAccent));
                tv4_3.setTextColor(getResources().getColor(R.color.colorWhite));
                tvFit_h.setTextColor(getResources().getColor(R.color.colorWhite));
                tvFit_v.setTextColor(getResources().getColor(R.color.colorWhite));

                mVideoView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    newHeight = (videoHeight * 9) / 16;
                } else {
                    newHeight = (videoWidth * 9) / 16;
                }
                Log.e("TAG", "NewHeight 16:9 = " + newHeight);
                mVideoView.getLayoutParams().height = newHeight;
                setStringPrefs(VideoPlayerActivity.this, PREF_SCREEN_RATIO, tv16_9.getText().toString());
            }
        });

        tv4_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvOriginal.setTextColor(getResources().getColor(R.color.colorWhite));
                tvBestFit.setTextColor(getResources().getColor(R.color.colorWhite));
                tvFill.setTextColor(getResources().getColor(R.color.colorWhite));
                tv18_9.setTextColor(getResources().getColor(R.color.colorWhite));
                tv16_9.setTextColor(getResources().getColor(R.color.colorWhite));
                tv4_3.setTextColor(getResources().getColor(R.color.colorAccent));
                tvFit_h.setTextColor(getResources().getColor(R.color.colorWhite));
                tvFit_v.setTextColor(getResources().getColor(R.color.colorWhite));

                mVideoView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    newHeight = (videoHeight * 3) / 4;
                } else {
                    newHeight = (videoWidth * 3) / 4;
                }
                Log.e("TAG", "NewHeight 4:3 = " + newHeight);
                mVideoView.getLayoutParams().height = newHeight;
                setStringPrefs(VideoPlayerActivity.this, PREF_SCREEN_RATIO, tv4_3.getText().toString());
            }
        });

        tvFit_h.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvOriginal.setTextColor(getResources().getColor(R.color.colorWhite));
                tvBestFit.setTextColor(getResources().getColor(R.color.colorWhite));
                tvFill.setTextColor(getResources().getColor(R.color.colorWhite));
                tv18_9.setTextColor(getResources().getColor(R.color.colorWhite));
                tv16_9.setTextColor(getResources().getColor(R.color.colorWhite));
                tv4_3.setTextColor(getResources().getColor(R.color.colorWhite));
                tvFit_h.setTextColor(getResources().getColor(R.color.colorAccent));
                tvFit_v.setTextColor(getResources().getColor(R.color.colorWhite));

                mVideoView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mVideoView.setLayoutParams(new FrameLayout.LayoutParams(videoWidth, videoHeight, Gravity.CENTER));
                } else {
                    mVideoView.setLayoutParams(new FrameLayout.LayoutParams(2000, videoHeight, Gravity.CENTER));
                }
                setStringPrefs(VideoPlayerActivity.this, PREF_SCREEN_RATIO, tvFit_h.getText().toString());
            }
        });

        tvFit_v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvOriginal.setTextColor(getResources().getColor(R.color.colorWhite));
                tvBestFit.setTextColor(getResources().getColor(R.color.colorWhite));
                tvFill.setTextColor(getResources().getColor(R.color.colorWhite));
                tv18_9.setTextColor(getResources().getColor(R.color.colorWhite));
                tv16_9.setTextColor(getResources().getColor(R.color.colorWhite));
                tv4_3.setTextColor(getResources().getColor(R.color.colorWhite));
                tvFit_h.setTextColor(getResources().getColor(R.color.colorWhite));
                tvFit_v.setTextColor(getResources().getColor(R.color.colorAccent));

                mVideoView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mVideoView.setLayoutParams(new FrameLayout.LayoutParams(videoWidth, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
                } else {
                    mVideoView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, videoHeight, Gravity.CENTER));
                }
                setStringPrefs(VideoPlayerActivity.this, PREF_SCREEN_RATIO, tvFit_v.getText().toString());
            }
        });

        tvSleepOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSleepMilliseconds("0");
                setStringPrefs(VideoPlayerActivity.this, PREF_SLEEP, tvSleepOff.getText().toString());
            }
        });

        tvSleep10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSleepMilliseconds("10");
                setStringPrefs(VideoPlayerActivity.this, PREF_SLEEP, tvSleep10.getText().toString());
            }
        });

        tvSleep30.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSleepMilliseconds("30");
                setStringPrefs(VideoPlayerActivity.this, PREF_SLEEP, tvSleep30.getText().toString());
            }
        });

        tvSleep60.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSleepMilliseconds("60");
                setStringPrefs(VideoPlayerActivity.this, PREF_SLEEP, tvSleep60.getText().toString());
            }
        });

        tvSleepToEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSleepMilliseconds("TO_END");
                setStringPrefs(VideoPlayerActivity.this, PREF_SLEEP, tvSleepToEnd.getText().toString());
            }
        });

        tvSleepCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSleepMilliseconds("CUSTOM");
                setStringPrefs(VideoPlayerActivity.this, PREF_SLEEP, tvSleepCustom.getText().toString());
            }
        });


        if (playInBack) {
            ivBackPlay.setColorFilter(ContextCompat.getColor(VideoPlayerActivity.this, R.color.colorWhite), android.graphics.PorterDuff.Mode.SRC_IN);
            tvBackPlay.setTextColor(getResources().getColor(R.color.colorWhite));
        }

        if (playInBack) {
            ivBackPlay.setColorFilter(ContextCompat.getColor(VideoPlayerActivity.this, R.color.colorWhite), android.graphics.PorterDuff.Mode.SRC_IN);
            tvBackPlay.setTextColor(getResources().getColor(R.color.colorWhite));
            ivLoop.setColorFilter(ContextCompat.getColor(VideoPlayerActivity.this, R.color.colorWhite), android.graphics.PorterDuff.Mode.SRC_IN);
            tvLoop.setTextColor(getResources().getColor(R.color.colorWhite));
        }

        llSettingBackPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (VideoPlayerActivity.playInBack) {
                    ivBackPlay.setColorFilter(ContextCompat.getColor(VideoPlayerActivity.this, R.color.colorWhite), android.graphics.PorterDuff.Mode.SRC_IN);
                    tvBackPlay.setTextColor(getResources().getColor(R.color.colorWhite));
                    Toast.makeText(VideoPlayerActivity.this, "Background Play Is Disabled", Toast.LENGTH_SHORT).show();
                    VideoPlayerActivity.playInBack = false;
                    return;
                }
                ivBackPlay.setColorFilter(ContextCompat.getColor(VideoPlayerActivity.this, R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);
                tvBackPlay.setTextColor(getResources().getColor(R.color.colorAccent));
                Toast.makeText(VideoPlayerActivity.this, "Background Play Is Enabled", Toast.LENGTH_SHORT).show();
                VideoPlayerActivity.playInBack = true;
                llSettings.setVisibility(View.GONE);
                if (dialogSettings != null && dialogSettings.isShowing()) {
                    dialogSettings.dismiss();
                }
                finish();
            }
        });

        llSettingFloat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogSettings != null && dialogSettings.isShowing()) {
                    dialogSettings.dismiss();
                }
                mOverlay.performClick();
            }
        });


        llSettingLoop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!VideoPlayerActivity.this.repeatThisSOng) {
                    ivLoop.setColorFilter(ContextCompat.getColor(VideoPlayerActivity.this, R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);
                    tvLoop.setTextColor(getResources().getColor(R.color.colorAccent));
                    Toast.makeText(VideoPlayerActivity.this, "Repeat This Song", Toast.LENGTH_SHORT).show();
                    VideoPlayerActivity.this.repeatThisSOng = true;
                    return;
                }
                ivLoop.setColorFilter(ContextCompat.getColor(VideoPlayerActivity.this, R.color.colorWhite), android.graphics.PorterDuff.Mode.SRC_IN);
                tvLoop.setTextColor(getResources().getColor(R.color.colorWhite));
                Toast.makeText(VideoPlayerActivity.this, "Repeat Is Disabled", Toast.LENGTH_SHORT).show();
                VideoPlayerActivity.this.repeatThisSOng = false;
            }
        });

        llSettingDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogSettings != null && dialogSettings.isShowing()) {
                    dialogSettings.dismiss();
                }
                deleteVideo();
            }
        });

        llSettingEqualizer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (!VideoPlayerActivity.this.isEqualizerShowed) {
//                    VideoPlayerActivity.this.mEqualizerLayout.setVisibility(View.VISIBLE);
//                    VideoPlayerActivity.this.showEqualizer();
////                    VideoPlayerActivity.this.isEqualizerShowed = true;
//                }
            }
        });


        dialogSettings.setContentView(settingView);
        dialogSettings.getWindow().setBackgroundDrawableResource(R.color.colorBlackTransparent);
        if (dialogSettings != null && !dialogSettings.isShowing()) {
            dialogSettings.show();
        }
    }


    private void setPlaySpeed(float speed) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            myPlayBackParams = new PlaybackParams();
            myPlayBackParams.setSpeed(speed); //you can set speed here
            mp.setPlaybackParams(myPlayBackParams);
            if (!videoIsPlaying) {
                mVideoView.pause();
            }
        }
    }

    private void save_screenshot() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setCancelable(false);


//        if (builder.getWindow() != null)
//            builder.getWindow().getAttributes().windowAnimations = R.style.SlidingDialogAnimation;

        View v = getLayoutInflater().inflate(R.layout.screen_shot, null);
        builder.setView(v);
        dialogSS = builder.create();
        dialogSS.show();

        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(((ModelClassForVideo) arrayListVideos.get(VideoPlayerActivity.mPosition)).getSongPath());
//        Log.e("TAG", "==> " + VideoPlayerActivity.mVideoView.getCurrentPosition());
        Bitmap frameAtTime = mediaMetadataRetriever.getFrameAtTime(TimeUnit.MILLISECONDS.toMicros(VideoPlayerActivity.mVideoView.getCurrentPosition()), MediaMetadataRetriever.OPTION_CLOSEST);
        MediaPlayer.create(this, R.raw.ss_sound).start();

        ((ImageView) v.findViewById(R.id.di_ss)).setImageBitmap(frameAtTime);

        ((ImageView) v.findViewById(R.id.ivCloseSS)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!videoIsPlaying) {
                    mPausePlay.performClick();
                }
                dialogSS.dismiss();
            }
        });

        ((TextView) v.findViewById(R.id.tvSaveSS)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveImage(frameAtTime, "save");
                dialogSS.dismiss();
            }
        });

        ((TextView) v.findViewById(R.id.tvShareSS)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveImage(frameAtTime, "share");
//                d.dismiss();
            }
        });

        if (VideoPlayerActivity.this.screenIsLandscape) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialogSS.getWindow().getAttributes());
            lp.width = (int) getResources().getDimension(R.dimen._200sdp);
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            dialogSS.getWindow().setAttributes(lp);
        } else {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialogSS.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            dialogSS.getWindow().setAttributes(lp);
        }
    }


    private void saveImage(Bitmap bitmap, String flag) {
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        File dir = new File(externalStorageDirectory.getAbsolutePath() + "/" + getResources().getString(R.string.app_name));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg";
        File file2 = new File(dir, fileName);
        file2.renameTo(file2);
        MediaScannerConnection.scanFile(this, new String[]{externalStorageDirectory.getAbsolutePath() + "/" + getResources().getString(R.string.app_name) + "/" + fileName}, null, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String s, Uri uri) {
            }
        });
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file2);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            Uri uri;
            if (flag.equalsIgnoreCase("share")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(VideoPlayerActivity.this, BuildConfig.APPLICATION_ID + ".fileprovider", file2);
                } else {
                    uri = Uri.fromFile(file2);
                }

                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("image/png");
                startActivity(intent);

            } else if (flag.equalsIgnoreCase("save")) {
                Toast.makeText(this, file2.getAbsolutePath(), Toast.LENGTH_LONG).show();
            }
            mPausePlay.performClick();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void onPause() {
        super.onPause();
        this.mCurrentSongPosition = mVideoView.getCurrentPosition();
        if (playInBack && videoIsPlaying) {
            startService();
        }
        MainActivity.playedSongCurrentPosition = mVideoView.getCurrentPosition();
        isFullScreenActivityVisible = false;
        mVideoView.pause();
        this.onResumeHasCalled = false;

        ModelClassForVideo data;
        if (arrayListVideos.size() > 0) {
            data = (ModelClassForVideo) arrayListVideos.get(mPosition);
        } else {
            data = getLatestVideo(this, PREF_LATEST_PLAY);
        }

        ModelClassForVideo modelClassForVideo = new ModelClassForVideo();
        modelClassForVideo.setBoolean_selected(false);
        modelClassForVideo.setSongPath(data.getSongPath());
        modelClassForVideo.setSongName(data.getSongName());
        modelClassForVideo.setSongAlbum(data.getSongAlbum());
        modelClassForVideo.setSongThumb(data.getSongThumb());
        modelClassForVideo.setSongDuration(data.getSongDuration());
        modelClassForVideo.setSongSize(data.getSongSize());
        modelClassForVideo.setDateAdded(data.getDateAdded());
        modelClassForVideo.setDateModified(data.getDateModified());
        modelClassForVideo.setCurrDuration(seekPosition);
        modelClassForVideo.setTypeVal(0);
        setLatestVideo(VideoPlayerActivity.this, PREF_LATEST_PLAY, modelClassForVideo);

        modelClassForVideo.setTypeVal(1);
        checkAndAddToRecentList(VideoPlayerActivity.this, modelClassForVideo);

    }

    public void onResume() {
        super.onResume();
        if (!this.onResumeHasCalled) {
            if (MainActivity.isStartedFromNotificationCLick) {
                MainActivity.isStartedFromNotificationCLick = false;
            } else if (!MainActivity.mediaIsPlayed || !MainActivity.playedSongName.equals(mVideoName)) {
                mVideoView.seekTo(this.mCurrentSongPosition);
                MainActivity.playedSongName = mVideoName;
            } else {
                mVideoView.seekTo(seekPosition);
            }
            videoIsPlaying = true;
            this.backPressed = false;
            mPausePlay.setImageResource(R.drawable.ic_pause);
            if (playInBack) {
                if (BackgroundPlay.mediaPlayer != null) {
                    int currentPosition = BackgroundPlay.mediaPlayer.getCurrentPosition();
                    this.mCurrentSongPosition = currentPosition;
                    mVideoView.seekTo(currentPosition);
                    if (MainActivity.nextWasPressedInVideo || MainActivity.PreviousWasPressedInVideo) {
                        mVideoView.setVideoPath(mVideoPath);
                        mVideoView.seekTo(BackgroundPlay.mediaPlayer.getCurrentPosition());
                        mVideoView.start();
                        MainActivity.nextWasPressedInVideo = false;
                        MainActivity.PreviousWasPressedInVideo = false;
                    }
                    BackgroundPlay.mediaPlayer.pause();
                    BackgroundPlay.mediaPlayer.stop();
                    BackgroundPlay.mediaPlayer.release();
                    BackgroundPlay.mediaPlayer = null;
                    stopService();
                    BackgroundPlay.mediaPlayerIsPlaying = false;
                    playCycler();
                    playInBack = false;
                }
                playInBack = false;
            }
            isFullScreenActivityVisible = true;
            this.onResumeHasCalled = true;
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (BackgroundPlay.mediaPlayer == null || !this.backPressed) {
            this.backPressed = true;
        } else if (BackgroundPlay.mediaPlayerIsPlaying) {
            BackgroundPlay.mediaPlayer.stop();
            BackgroundPlay.mediaPlayer.release();
            BackgroundPlay.mediaPlayer = null;
            stopService();
        }
        if (!playInBack) {
            stopService();
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(10);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llMain:
                int pos = (int) v.getTag();
                mPosition = pos;
                seekPosition = 0;

                if (cdt != null && cdt.isRunning()) {
                    cdt.cancel();
                    isScreenSleep = false;
                    setStringPrefs(VideoPlayerActivity.this, PREF_SLEEP, tvSleepOff.getText().toString());
                }

                mVideoPath = ((ModelClassForVideo) arrayListVideos.get(pos)).getSongPath();
                mVideoName = ((ModelClassForVideo) arrayListVideos.get(pos)).getSongName();
                setCommon();
                mAdapter.notifyDataSetChanged();
                rvPlayList.smoothScrollToPosition(pos);
                break;
        }
    }

    public static class MyBroadCastReciever extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                VideoPlayerActivity.playInBack = false;
            } else {
                intent.getAction().equals("android.intent.action.SCREEN_ON");
                VideoPlayerActivity.playInBack = true;
            }
        }
    }

    private void setSeekbar() {
        this.mHandler = new Handler();
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mediaPlayer) {
                mp = mediaPlayer;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mp.seekTo(seekPosition, SEEK_CLOSEST);
                } else {
                    VideoPlayerActivity.mVideoView.seekTo(seekPosition);
                }
                VideoPlayerActivity.mSeekBar.setMax(VideoPlayerActivity.mVideoView.getDuration());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    myPlayBackParams = new PlaybackParams();
                    myPlayBackParams.setSpeed(speedVal);
                    mp.setPlaybackParams(myPlayBackParams);
                }
                VideoPlayerActivity.mVideoView.start();
                VideoPlayerActivity.this.playCycler();
            }
        });
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.e("video", "setOnErrorListener ");
                if (flag.equals("previous_press")) {
                    mPrevious.performClick();
                } else {
                    mNext.performClick();
                }
                flag = "";
                return true;
            }
        });


        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                if (z) {
                    VideoPlayerActivity.mVideoView.seekTo(i);

                    if (cdt != null && cdt.isRunning()) {
                        if (cdt.timeLeft() > 0 && cdt.timeLeft() < 1000) {
                            isScreenSleep = true;
                        } else {
                            isScreenSleep = false;
                        }
                    }

                    VideoPlayerActivity.this.playCycler();
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                VideoPlayerActivity.mVideoView.pause();
                mPausePlay.setImageResource(R.drawable.ic_play);
                VideoPlayerActivity.this.playCycler();
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                if (VideoPlayerActivity.videoIsPlaying) {
                    mPausePlay.setImageResource(R.drawable.ic_pause);
                    VideoPlayerActivity.mVideoView.start();
                    VideoPlayerActivity.this.playCycler();
                }
            }
        });
    }

    private void setBrightness() {
        int i = Settings.System.getInt(getApplicationContext().getContentResolver(), "screen_brightness", 0);
        this.Brightness = i;
        this.mBrightSeekBar.setProgress(i);
        this.mBrightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                float f = ((float) i) / 100.0f;
                if (VideoPlayerActivity.this.Brightness < 0) {
                    VideoPlayerActivity.this.Brightness = 0;
                } else if (VideoPlayerActivity.this.Brightness > 255) {
                    VideoPlayerActivity.this.Brightness = 255;
                }
                WindowManager.LayoutParams attributes = VideoPlayerActivity.this.getWindow().getAttributes();
                attributes.screenBrightness = f;
                VideoPlayerActivity.this.getWindow().setAttributes(attributes);
                if (i <= 30) {
                    VideoPlayerActivity.this.mShowBrightness.setImageResource(R.drawable.ic_brightness_low);
                } else if (i <= 55) {
                    VideoPlayerActivity.this.mShowBrightness.setImageResource(R.drawable.ic_brightness_2);
                } else if (i <= 70) {
                    VideoPlayerActivity.this.mShowBrightness.setImageResource(R.drawable.ic_brightness_3);
                } else if (i > 70) {
                    VideoPlayerActivity.this.mShowBrightness.setImageResource(R.drawable.ic_brightness_high);
                }
            }
        });
        this.mBrightSeekBar.setProgress(this.mBrightSeekBar.getMax() / 2);
    }

    private void volumeSeekBar() {
        try {
            this.mVolumeSeek.setMax(this.mAudioManager.getStreamMaxVolume(3));
            this.mVolumeSeek.setProgress(this.mAudioManager.getStreamVolume(3));
            this.mVolumeSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                }

                public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                    VideoPlayerActivity.this.mAudioManager.setStreamVolume(3, i, 0);
                    if (i == 0) {
                        VideoPlayerActivity.this.mSoundImageShow.setImageResource(R.drawable.ic_volume_off);
                    } else if (i <= 4) {
                        VideoPlayerActivity.this.mSoundImageShow.setImageResource(R.drawable.ic_volume_low);
                    } else if (i <= 10) {
                        VideoPlayerActivity.this.mSoundImageShow.setImageResource(R.drawable.ic_volume_2);
                    } else {
                        VideoPlayerActivity.this.mSoundImageShow.setImageResource(R.drawable.ic_volumeup);
                    }
                }
            });
        } catch (Exception unused) {
            unused.printStackTrace();
        }
    }

    private void startService() {
        if (mVideoPath == null) {
            return;
        }

        Intent bgPlayIntent = new Intent(getApplicationContext(), BackgroundPlay.class);
//        Bundle b = new Bundle();
//        b.putSerializable("arrData", arrayListVideos);
//        b.putInt("position", mPosition);
//        b.putInt("mWhereFrom", mWhereFrom);
//        bgPlayIntent.putExtras(b);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(bgPlayIntent);
//            return;
//        }
//        startService(bgPlayIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(bgPlayIntent);
        } else {
            startService(bgPlayIntent);
        }
    }

    private void stopService() {
        stopService(new Intent(this, BackgroundPlay.class));
    }

    private void playCycler() {
        mSeekBar.setProgress(mVideoView.getCurrentPosition());
        if (videoIsPlaying) {
            mRunnable = new Runnable() {
                public void run() {
                    seekPosition = VideoPlayerActivity.mVideoView.getCurrentPosition();
                    VideoPlayerActivity.this.playCycler();
                    VideoPlayerActivity.this.mFirstText.setText(VideoPlayerActivity.this.msToString(VideoPlayerActivity.mVideoView.getCurrentPosition()));//createTimeLabel
                    String access$600 = VideoPlayerActivity.this.msToString(VideoPlayerActivity.mVideoView.getDuration() - VideoPlayerActivity.mVideoView.getCurrentPosition());//createTimeLabel
                    TextView textView = VideoPlayerActivity.this.mSecondText;
                    textView.setText("- " + access$600);
                }
            };
            mHandler.postDelayed(mRunnable, 1);
        } else {
            mHandler.removeCallbacks(mRunnable);
            mHandler.removeCallbacksAndMessages(null);
            Log.e("TAG", "Cancel Handler");
        }
    }

    private void showEqualizer() {
        if (Build.VERSION.SDK_INT >= 21) {
            int audioSessionId = mVideoView.getAudioSessionId();
//            Toast.makeText(this, "" + audioSessionId, Toast.LENGTH_SHORT).show();
            this.isEqualizerShowed = true;
            getSupportFragmentManager().beginTransaction().replace(R.id.eqFrame, EqualizerFragment.newBuilder().setAccentColor(Color.parseColor("#1088ff")).setAudioSessionId(audioSessionId).build()).commit();
        }
    }

    public void onBackPressed() {
        if (llSleep.getVisibility() == View.VISIBLE) {
            llSleep.setVisibility(View.GONE);
            isSpeedVisible = false;
            Toast.makeText(VideoPlayerActivity.this, "Close sleep mode", Toast.LENGTH_SHORT).show();
        } else if (llSpeed.getVisibility() == View.VISIBLE) {
            llSpeed.startAnimation(AnimationUtils.loadAnimation(VideoPlayerActivity.this, R.anim.slide_out_bottom));//outToRightAnimation()
            llSpeed.setVisibility(View.GONE);
            isSpeedVisible = false;
        } else if (llPlayList.getVisibility() == View.VISIBLE) {
            llPlayList.startAnimation(AnimationUtils.loadAnimation(VideoPlayerActivity.this, R.anim.slide_out_right));//outToRightAnimation()
            llPlayList.setVisibility(View.GONE);
        } else if (llSettings.getVisibility() == View.VISIBLE) {
            llSettings.setVisibility(View.GONE);
        } else if (this.screenIsNotLocked) {
            this.mCurrentSongPosition = mVideoView.getCurrentPosition();
            if (this.isEqualizerShowed) {
                this.mEqualizerLayout.setVisibility(View.GONE);
                this.isEqualizerShowed = false;
                return;
            }
            if (tvBestFit != null) {
                setStringPrefs(this, PREF_SCREEN_RATIO, tvBestFit.getText().toString());
            }
            if (tvSleepOff != null) {
                setStringPrefs(this, PREF_SLEEP, tvSleepOff.getText().toString());
            }
            super.onBackPressed();
        }
    }

    private String createTimeLabel(int i) {
        int i2 = i / 1000;
        int i3 = i2 / 60;
        int i4 = i2 % 60;
        String str = i3 + ":";
        if (i4 < 10) {
            str = str + "0";
        }
        return str + i4;
    }

    private static String msToString(long ms) {
        long totalSecs = ms / 1000;
        long hours = (totalSecs / 3600);
        long mins = (totalSecs / 60) % 60;
        long secs = totalSecs % 60;
        String minsString = (mins == 0) ? "00" : ((mins < 10) ? ("0" + mins) : ("" + mins));
        String secsString = (secs == 0) ? "00" : ((secs < 10) ? ("0" + secs) : ("" + secs));
        if (hours > 0)
            return hours + ":" + minsString + ":" + secsString;
        else //if (mins > 0)
            return minsString + ":" + secsString;
//        else
//            return ":" + secsString;
    }


    private void getMusicInsideFragments() {

        sortVal = getIntPrefs(VideoPlayerActivity.this, PREF_SORT_BY);
        if (sortVal == 0) {
            str = "title ASC";
        } else if (sortVal == 1) {
            str = "_size ASC";
        } else if (sortVal == 2) {
            str = "duration ASC";
        } else if (sortVal == 3) {
            str = "date_modified DESC";
        } else if (sortVal == 4) {
            str = "date_modified ASC";
        }

        if (mBucketName.equals("0")) {
            strCheckVal = mBucketId;
            strCheck = "bucket_id like?";
        } else {
            strCheckVal = mBucketName;
            strCheck = "bucket_display_name like?";
        }

        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Cursor query = getContentResolver().query(uri, new String[]{"_data", "title", "bucket_display_name", "duration", "_size", "date_added", "date_modified"}, strCheck, new String[]{strCheckVal}, str);
        Log.d("abc", str);
        if (query != null && query.getCount() > 0) {
            int columnIndexOrThrow = query.getColumnIndexOrThrow("_data");
            int columnIndexOrThrow3 = query.getColumnIndexOrThrow("title");
            int columnIndexOrThrow4 = query.getColumnIndexOrThrow("bucket_display_name");
            int vidDuration = query.getColumnIndexOrThrow("duration");
            int vidSize = query.getColumnIndexOrThrow("_size");
            int vidAddedDate = query.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED);
            int vidModifiedDate = query.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED);
            while (query.moveToNext()) {
                String path = query.getString(columnIndexOrThrow);
                ModelClassForVideo modelClassForVideo = new ModelClassForVideo();
                modelClassForVideo.setBoolean_selected(false);
                modelClassForVideo.setSongPath(path);
                modelClassForVideo.setSongName(query.getString(columnIndexOrThrow3));
                modelClassForVideo.setSongAlbum(query.getString(columnIndexOrThrow4));
                modelClassForVideo.setSongThumb(path);
                modelClassForVideo.setSongDuration(query.getInt(vidDuration));
                modelClassForVideo.setSongSize(query.getInt(vidSize));
                modelClassForVideo.setDateAdded(query.getLong(vidAddedDate));
                modelClassForVideo.setDateModified(query.getLong(vidModifiedDate));
                arrayListVideos.add(modelClassForVideo);

                Uri viduri;
                File f = new File(path);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    viduri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".fileprovider", f);
                } else {
                    viduri = Uri.fromFile(f);
                }
                videoUri.add(viduri);
            }


            if (sortVal == 5) {
                Collections.reverse(arrayListVideos);
                Collections.reverse(videoUri);
            }
        }
    }

    int sortVal;
    String str = "";

    private void getMusic() {

        sortVal = getIntPrefs(VideoPlayerActivity.this, PREF_SORT_BY);
        if (sortVal == 0) {
            str = "title ASC";
        } else if (sortVal == 1) {
            str = "_size ASC";
        } else if (sortVal == 2) {
            str = "duration ASC";
        } else if (sortVal == 3) {
            str = "date_modified DESC";
        } else if (sortVal == 4) {
            str = "date_modified ASC";
        }

        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Cursor query = getContentResolver().query(uri, new String[]{"_data", "title", "bucket_display_name", "duration", "_size", "date_added", "date_modified"}, null, null, str);
        if (query != null && query.getCount() > 0) {
            int columnIndexOrThrow = query.getColumnIndexOrThrow("_data");
            int columnIndexOrThrow3 = query.getColumnIndexOrThrow("title");
            int columnIndexOrThrow4 = query.getColumnIndexOrThrow("bucket_display_name");
            int vidDuration = query.getColumnIndexOrThrow("duration");
            int vidSize = query.getColumnIndexOrThrow("_size");
            int vidAddedDate = query.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED);
            int vidModifiedDate = query.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED);
            while (query.moveToNext()) {
                String path = query.getString(columnIndexOrThrow);
                ModelClassForVideo modelClassForVideo = new ModelClassForVideo();
                modelClassForVideo.setBoolean_selected(false);
                modelClassForVideo.setSongPath(path);
                modelClassForVideo.setSongName(query.getString(columnIndexOrThrow3));
                modelClassForVideo.setSongAlbum(query.getString(columnIndexOrThrow4));
                modelClassForVideo.setSongThumb(path);
                modelClassForVideo.setSongDuration(query.getInt(vidDuration));
                modelClassForVideo.setSongSize(query.getInt(vidSize));
                modelClassForVideo.setDateAdded(query.getLong(vidAddedDate));
                modelClassForVideo.setDateModified(query.getLong(vidModifiedDate));
                arrayListVideos.add(modelClassForVideo);

                Uri viduri;
                File f = new File(query.getString(columnIndexOrThrow));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    viduri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".fileprovider", f);
                } else {
                    viduri = Uri.fromFile(f);
                }
                videoUri.add(viduri);
            }

            if (sortVal == 5) {
                Collections.reverse(arrayListVideos);
                Collections.reverse(videoUri);
            }
        }

    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            orientation = Configuration.ORIENTATION_LANDSCAPE;
            this.screenIsLandscape = true;
            displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            videoHeight = displayMetrics.heightPixels;
            videoWidth = displayMetrics.widthPixels;
        } else {
            orientation = Configuration.ORIENTATION_PORTRAIT;
            this.screenIsLandscape = false;
            displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            videoHeight = displayMetrics.heightPixels;
            videoWidth = displayMetrics.widthPixels;
        }

        Log.e("TAG-SIZE", videoWidth + "x" + videoHeight);

        mVideoView.setLayoutParams(new FrameLayout.LayoutParams(videoWidth, videoHeight, Gravity.CENTER));

        if (dialogSettings != null && dialogSettings.isShowing()) {
            dialogSettings.dismiss();
            setSettingDialog();

            String ratioVal = getStringPrefs(this, PREF_SCREEN_RATIO);
            switch (ratioVal) {
                case "BEST FIT":
                    tvBestFit.performClick();
                    break;

                case "FILL":
                    tvFill.performClick();
                    break;

                case "ORIGINAL":
                    tvOriginal.performClick();
                    break;

                case "18:9":
                    tv18_9.performClick();
                    break;

                case "16:9":
                    tv16_9.performClick();
                    break;

                case "4:3":
                    tv4_3.performClick();
                    break;

                case "FIT-H":
                    tvFit_h.performClick();
                    break;

                case "FIT-V":
                    tvFit_v.performClick();
                    break;
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouch(View view, MotionEvent motionEvent) {
        this.mGestureDetector.onTouchEvent(motionEvent);
        if (MotionEventCompat.getActionMasked(motionEvent) != 1) {
            return true;
        }
        this.isXPressed = false;
        this.isYPressed = false;
        this.mBrightLayout.setVisibility(View.GONE);
        this.mSoundShow.setVisibility(View.GONE);
        if (videoIsPlaying) {
            mVideoView.start();
            mPausePlay.setImageResource(R.drawable.ic_pause);
            playCycler();
        }
        return true;
    }

    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {

        float width = (float) this.mFrame.getWidth();
        if (this.screenIsNotLocked) {
            if (f2 <= 11.0f || motionEvent.getX() <= width / 2.0f || this.isXPressed) {
                float f3 = -f2;
                if (f3 > 11.0f && motionEvent.getX() > width / 2.0f && !this.isXPressed) {
                    this.isYPressed = true;
                    this.mBrightLayout.setVisibility(View.GONE);
                    AudioManager audioManager = this.mAudioManager;
                    audioManager.setStreamVolume(3, audioManager.getStreamVolume(3) - 1, 0);
                    this.mVolumeSeek.setProgress(this.mAudioManager.getStreamVolume(3));
                    this.mSoundShow.setVisibility(View.VISIBLE);
                    TextView textView = this.mCurrentVol;
                    textView.setText("Volume : " + this.mVolumeSeek.getProgress());
                    return true;
                } else if (f2 <= 11.0f || motionEvent.getX() >= width / 2.0f || this.isXPressed) {
                    if (f3 > 11.0f && motionEvent.getX() < width / 2.0f && !this.isXPressed) {
                        this.isYPressed = true;
                        this.mSoundShow.setVisibility(View.GONE);
                        SeekBar seekBar = this.mBrightSeekBar;
                        seekBar.setProgress(seekBar.getProgress() - 3);
                        this.mBrightLayout.setVisibility(View.VISIBLE);
                        TextView textView2 = this.mCurrentBri;
                        textView2.setText("Brightness : " + (this.mBrightSeekBar.getProgress() / 7));
                    }
                    if (f > 1.0f && !this.isYPressed) {
                        VideoView videoView = mVideoView;
                        videoView.seekTo(videoView.getCurrentPosition() - 1300);
                        this.isXPressed = true;
                        showViews();
                        mVideoView.pause();
                        mPausePlay.setImageResource(R.drawable.ic_play);
                        this.mLockScreen.setVisibility(View.VISIBLE);
                        playCycler();
                        return true;
                    } else if ((-f) > 1.0f && !this.isYPressed) {
                        VideoView videoView2 = mVideoView;
                        videoView2.seekTo(videoView2.getCurrentPosition() + 1300);
                        this.isXPressed = true;
                        showViews();
                        mVideoView.pause();
                        mPausePlay.setImageResource(R.drawable.ic_play);
                        this.mLockScreen.setVisibility(View.VISIBLE);
                        playCycler();
                    }
                } else {
                    this.isYPressed = true;
                    this.mSoundShow.setVisibility(View.GONE);
                    SeekBar seekBar2 = this.mBrightSeekBar;
                    seekBar2.setProgress(seekBar2.getProgress() + 3);
                    this.mBrightLayout.setVisibility(View.VISIBLE);
                    TextView textView3 = this.mCurrentBri;
                    textView3.setText("Brightness : " + (this.mBrightSeekBar.getProgress() / 7));
                    return true;
                }
            } else {
                this.isYPressed = true;
                this.mBrightLayout.setVisibility(View.GONE);
                AudioManager audioManager2 = this.mAudioManager;
                audioManager2.setStreamVolume(3, audioManager2.getStreamVolume(3) + 1, 0);
                this.mVolumeSeek.setProgress(this.mAudioManager.getStreamVolume(3));
                this.mSoundShow.setVisibility(View.VISIBLE);
                TextView textView4 = this.mCurrentVol;
                textView4.setText("Volume : " + this.mVolumeSeek.getProgress());
                return true;
            }
        }
        return true;
    }

    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        if (llSleep.getVisibility() == View.VISIBLE) {
            llSleep.setVisibility(View.GONE);
            Toast.makeText(VideoPlayerActivity.this, "Close sleep mode", Toast.LENGTH_SHORT).show();
        } else if (llPlayList.getVisibility() == View.VISIBLE) {
            llPlayList.startAnimation(AnimationUtils.loadAnimation(VideoPlayerActivity.this, R.anim.slide_out_right));
            llPlayList.setVisibility(View.GONE);
        } else if (isSpeedVisible) {
            llSpeed.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_bottom));
            llSpeed.setVisibility(View.GONE);
            isSpeedVisible = false;
        } else if (controlsAreVisible) {
            hideViews();
            isScrolled = false;
            mViewsHandler.removeCallbacks(this.mViewsRunnable);
        } else if (screenIsNotLocked) {
            showViews();
        } else if (ScreenIsLockedAndLockIsVisible) {
            mUnLockScreen.startAnimation(AnimationUtils.loadAnimation(VideoPlayerActivity.this, R.anim.slide_out_right));
            mUnLockScreen.setVisibility(View.GONE);
            ScreenIsLockedAndLockIsVisible = false;
        } else {
            mUnLockScreen.setVisibility(View.VISIBLE);
            mUnLockScreen.startAnimation(AnimationUtils.loadAnimation(VideoPlayerActivity.this, R.anim.slide_in_right));
            ScreenIsLockedAndLockIsVisible = true;
        }
        return true;
    }

    public boolean onDoubleTap(MotionEvent motionEvent) {
        if (this.screenIsNotLocked) {
            float f = ((float) getResources().getDisplayMetrics().widthPixels) / 2.0f;
            if (motionEvent.getX() > f) {
                if (motionEvent.getX() > f + 200.0f) {
                    VideoView videoView = mVideoView;
                    videoView.seekTo(videoView.getCurrentPosition() + 10000);
                    this.mInrement.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            VideoPlayerActivity.this.mInrement.setVisibility(View.GONE);
                        }
                    }, 700);
                } else if (videoIsPlaying) {
                    mVideoView.pause();
                    mPausePlay.setImageResource(R.drawable.ic_play);
                    videoIsPlaying = false;
                    playCycler();
                } else {
                    mVideoView.start();
                    mPausePlay.setImageResource(R.drawable.ic_pause);
                    videoIsPlaying = true;
                    playCycler();
                }
            }
            if (motionEvent.getX() < f) {
                if (motionEvent.getX() < f - 200.0f) {
                    VideoView videoView2 = mVideoView;
                    videoView2.seekTo(videoView2.getCurrentPosition() - 10000);
                    this.mDecre.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            VideoPlayerActivity.this.mDecre.setVisibility(View.GONE);
                        }
                    }, 700);
                } else if (videoIsPlaying) {
                    mVideoView.pause();
                    mPausePlay.setImageResource(R.drawable.ic_play);
                    videoIsPlaying = false;
                    playCycler();
                } else {
                    mVideoView.start();
                    mPausePlay.setImageResource(R.drawable.ic_pause);
                    videoIsPlaying = true;
                    playCycler();
                }
            }
            if (this.controlsAreVisible) {
                this.mViewsHandler.removeCallbacks(this.mViewsRunnable);
                hideViewsAfterSomeTime();
            }
        }
        return true;
    }

    private void hideViews() {
        this.mControls.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_bottom));//outToBottomAnimation()
        this.mTopBar.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_top));//outToTopAnimation());
        this.mControls.setVisibility(View.GONE);
        this.mTopBar.setVisibility(View.GONE);

        mLeftBar.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_left));//outToLeftAnimation()
        mRightBar.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_right));//outToRightAnimation()
        mLeftBar.setVisibility(View.GONE);
        mRightBar.setVisibility(View.GONE);

        this.controlsAreVisible = false;
    }

    private void showViews() {
        this.mControls.setVisibility(View.VISIBLE);
        this.mTopBar.setVisibility(View.VISIBLE);
        this.mControls.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom));//inFromBottomAnimation()
        this.mTopBar.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_top));//inFromTopAnimation()

        mLeftBar.setVisibility(View.VISIBLE);
        mRightBar.setVisibility(View.VISIBLE);
        mLeftBar.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_left));//inFromLeftAnimation()
        mRightBar.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));//inFromRightAnimation()

        this.controlsAreVisible = true;
    }

    private static Animation inFromTopAnimation() {
        Animation inFromTop = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromTop.setDuration(500);
        inFromTop.setFillAfter(true);
        inFromTop.setFillEnabled(true);
        inFromTop.setInterpolator(new AccelerateInterpolator());
        return inFromTop;
    }

    private static Animation outToTopAnimation() {
        Animation outToTop = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, -1.0f);
        outToTop.setDuration(500);
        outToTop.setFillAfter(true);
        outToTop.setFillEnabled(true);
        outToTop.setInterpolator(new AccelerateInterpolator());
        return outToTop;
    }


    private static Animation inFromBottomAnimation() {
        Animation inFromBottom = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, +1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromBottom.setDuration(500);
        inFromBottom.setFillAfter(true);
        inFromBottom.setFillEnabled(true);
        inFromBottom.setInterpolator(new AccelerateInterpolator());
        return inFromBottom;
    }

    private static Animation outToBottomAnimation() {
        Animation outToBottom = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, +1.0f);
        outToBottom.setDuration(500);
        outToBottom.setFillAfter(true);
        outToBottom.setFillEnabled(true);
        outToBottom.setInterpolator(new AccelerateInterpolator());
        return outToBottom;
    }


    private static Animation inFromLeftAnimation() {
        Animation inFromLeft = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromLeft.setDuration(500);
        inFromLeft.setFillAfter(true);
        inFromLeft.setFillEnabled(true);
        inFromLeft.setInterpolator(new AccelerateInterpolator());
        return inFromLeft;
    }

    private static Animation outToLeftAnimation() {
        Animation outToLeft = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        outToLeft.setDuration(500);
        outToLeft.setFillAfter(true);
        outToLeft.setFillEnabled(true);
        outToLeft.setInterpolator(new AccelerateInterpolator());
        return outToLeft;
    }

    private static Animation inFromRightAnimation() {
        Animation inFromRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, +1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromRight.setDuration(500);
        inFromRight.setFillAfter(true);
        inFromRight.setFillEnabled(true);
        inFromRight.setInterpolator(new AccelerateInterpolator());
        return inFromRight;
    }

    private static Animation outToRightAnimation() {
        Animation outToRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, +1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        outToRight.setDuration(500);
        outToRight.setFillAfter(true);
        outToRight.setFillEnabled(true);
        outToRight.setInterpolator(new AccelerateInterpolator());
        return outToRight;
    }

    private void hideViewsAfterSomeTime() {
        Runnable r0 = new Runnable() {
            public void run() {
                VideoPlayerActivity.this.hideViews();
            }
        };
        this.mViewsRunnable = r0;
        this.mViewsHandler.postDelayed(r0, 6000);
    }
}
