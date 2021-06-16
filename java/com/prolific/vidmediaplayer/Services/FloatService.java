package com.prolific.vidmediaplayer.Services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.core.app.NotificationCompat;

import com.prolific.vidmediaplayer.Activities.BaseActivity;
import com.prolific.vidmediaplayer.Activities.VideoPlayerActivity;
import com.prolific.vidmediaplayer.Models.ModelClassForVideo;
import com.prolific.vidmediaplayer.R;

import java.util.ArrayList;

import static com.prolific.vidmediaplayer.Services.AppClass.CHANNEL_ID;

public class FloatService extends Service implements View.OnClickListener {

    public static WindowManager windowManager;
    public static LinearLayout linearLayout, llTopControls, llCenterControlls, llBottomControls;
    public static ImageView ivClose, ivResize, ivOpenPlayer, ivPrev, ivPlayPause, ivNext;
    TextView tvSongName;
    public static final String TAG = "FloatService";
    LayoutInflater inflater;
    public static VideoView vvVideo;
    public SeekBar mSeekBar;
    public ArrayList<Uri> videoUriList;
    public ArrayList<ModelClassForVideo> videoDataList;
    public static View floatingView;
    public static WindowManager.LayoutParams wParams;
    boolean isFirstView = true;
    private WindowManager.LayoutParams prevParams;
    public static int videoPosition = 0;
    public static int seekPosition1 = 0;
    public static int wherefrom;
    private GestureDetector gestureDetector;
//    public static SharedPreferences sharedPreferences;
//    public static SharedPreferences.Editor editor;
    private static int pauseTime = 0; //in milliseconds
    private Notification notification;
    public static boolean overlayControlsAreVisible = true;
    private Handler mViewsHandler;
    private Runnable mViewsRunnable;
    public static boolean videoIsPlaying = true;
    private String dirName;

    private Float oldDraggableRawEventY = 0f;
    private Float oldDraggableRawEventX = 0f;

    private Float oldResizeRawEventY = 0f;
    private Float oldResizeRawEventX = 0f;

    private Point initialWindowSize = new Point(0, 0);
    private Point minimumWindowSize = new Point(0, 0);
    private Point maximumWindowSize = new Point(2000, 4000);

    private Point screenSize = new Point(0, 0);

    @Override
    public void onCreate() {
        super.onCreate();
        mViewsHandler = new Handler();
        //initilaising the shared preference
//        sharedPreferences = getSharedPreferences(Constants.COMMON_SHARED_PREF, MODE_PRIVATE);
//        editor = sharedPreferences.edit();
    }

    public FloatService() {

    }

    public void setArrayData(ArrayList<Uri> arrUriData, ArrayList<ModelClassForVideo> arrData){
        this.videoUriList = new ArrayList<>();
        videoDataList = new ArrayList<>();
        videoUriList.addAll(arrUriData);
        videoDataList.addAll(arrData);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Notification build = new NotificationCompat.Builder(this, CHANNEL_ID).build();
            notification = build;
            startForeground(10, notification);
            stopForeground(true);
        }
        if(intent != null && intent.getExtras() != null) {
            videoUriList = (ArrayList<Uri>) intent.getExtras().getSerializable("videoUriList");
            videoDataList = (ArrayList<ModelClassForVideo>) intent.getExtras().getSerializable("videoDataList");
            videoPosition = intent.getExtras().getInt("position", 0);
            seekPosition1 = intent.getExtras().getInt("seekPositionFloat", 0);
            wherefrom = intent.getExtras().getInt("whereFrom", 0);
            dirName = intent.getExtras().getString("bucketName");
        }

//        videoUriList = (ArrayList<Uri>) intent.getSerializableExtra("videoUriList");
//        videoDataList = (ArrayList<ModelClassForVideo>) intent.getSerializableExtra("videoDataList");
//        videoPosition = intent.getIntExtra("position", 0);
//        seekPosition1 = intent.getIntExtra("seekPositionFloat", 0);
//        wherefrom = intent.getIntExtra("whereFrom", 0);
//        dirName = intent.getStringExtra("bucketName");

        initialise();//intent
        hideViewsAfterSomeTime();
        addVideoToVideoView();
        setSeekbar();
        addWindowManager();

        return START_REDELIVER_INTENT;

    }


    public void changeVideo(int mPosition, int seekposition) {
        videoPosition = mPosition;
        seekPosition1 = seekposition;
        addVideoToVideoView();
    }


    public void hideViews() {
        llBottomControls.setVisibility(View.GONE);
        llTopControls.setVisibility(View.GONE);
        llCenterControlls.setVisibility(View.GONE);

        overlayControlsAreVisible = false;
    }

    public void showViews() {
        llBottomControls.setVisibility(View.VISIBLE);
        llTopControls.setVisibility(View.VISIBLE);
        llCenterControlls.setVisibility(View.VISIBLE);

        overlayControlsAreVisible = true;
    }


    public void hideViewsAfterSomeTime() {
        Runnable r0 = new Runnable() {
            public void run() {
                hideViews();
            }
        };
        this.mViewsRunnable = r0;
        this.mViewsHandler.postDelayed(r0, 6000);
    }


    public void initialise() {

        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        floatingView = inflater.inflate(R.layout.layout_service_overlay, null);
        vvVideo = (VideoView) floatingView.findViewById(R.id.vvVideo);
        mSeekBar = (SeekBar) floatingView.findViewById(R.id.fsSeekbar);
        linearLayout = (LinearLayout) floatingView.findViewById(R.id.ll);
        llTopControls = (LinearLayout) floatingView.findViewById(R.id.llTopControls);
        llCenterControlls = (LinearLayout) floatingView.findViewById(R.id.llCenterControlls);
        llBottomControls = (LinearLayout) floatingView.findViewById(R.id.llBottomControls);
        ivClose = (ImageView) floatingView.findViewById(R.id.ivClose);
        ivResize = (ImageView) floatingView.findViewById(R.id.ivResize);
        ivOpenPlayer = (ImageView) floatingView.findViewById(R.id.ivOpenPlayer);
        ivPrev = (ImageView) floatingView.findViewById(R.id.ivPrev);
        ivPlayPause = (ImageView) floatingView.findViewById(R.id.ivPlayPause);
        ivNext = (ImageView) floatingView.findViewById(R.id.ivNext);
        tvSongName = (TextView) floatingView.findViewById(R.id.tvSongName);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        ivClose.setOnClickListener(this);
        ivOpenPlayer.setOnClickListener(this);
        ivPrev.setOnClickListener(this);
        ivPlayPause.setOnClickListener(this);
        ivNext.setOnClickListener(this);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDoubleTap(MotionEvent e) {
//                openActivity();
                return super.onDoubleTap(e);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {

                if (overlayControlsAreVisible) {
                    hideViews();
//                    mViewsHandler.removeCallbacks(mViewsRunnable);
                } else {
                    showViews();
                }

//                if (vvVideo != null && vvVideo.isPlaying()) {
//
//                    pauseTime = vvVideo.getCurrentPosition();
//                    vvVideo.pause();
//                    Toast.makeText(FloatService.this, "Paused Video", Toast.LENGTH_SHORT).show();
//                } else if (vvVideo != null && !vvVideo.isPlaying()) {
//
//                    vvVideo.start();
//                    vvVideo.seekTo(pauseTime);
//                    Toast.makeText(FloatService.this, "Resumed Video", Toast.LENGTH_SHORT).show();
//                }
                return true;
            }
        });
    }

    public void addVideoToVideoView() {

        if (videoUriList != null && videoUriList.size() > 0) {

            tvSongName.setText(videoDataList.get(videoPosition).getSongName());
            vvVideo.setVideoURI(videoUriList.get(videoPosition));
            vvVideo.requestFocus();
            vvVideo.seekTo(seekPosition1);
            vvVideo.start();
            vvVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    try {
                        setNextVideoOnComplete();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });

        } else {

            stopSelf();
            Toast.makeText(this, "No video in Playlist", Toast.LENGTH_SHORT).show();
        }
    }


    public void setNextVideoOnComplete(){
        //prevVideoIndex++;
        videoPosition = videoPosition + 1;//sharedPreferences.getInt(Constants.CURRENT_PLAYING_VIDEO_NUMBER, -1)
        int temp = videoPosition % videoUriList.size();
        vvVideo.setVideoURI(videoUriList.get(temp));


        /*PlaylistAdapter.changeActiveItemBackground(temp); //change background colour*/

//                    editor.putInt(Constants.CURRENT_PLAYING_VIDEO_NUMBER, temp);
//                    editor.commit();
        seekPosition1 = 0;
        vvVideo.seekTo(seekPosition1);

        if(videoFileIsCorrupted(videoDataList.get(temp).getSongPath())) {
            vvVideo.start();
        }else{
            Toast.makeText(this, "Video is currupted or malformated", Toast.LENGTH_SHORT).show();
            setNextVideoOnComplete();
        }
    }



    private boolean videoFileIsCorrupted(String path){

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        try {
            retriever.setDataSource(FloatService.this, Uri.parse(path));
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

        String hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO);
        return "yes".equals(hasVideo);
    }


    private void setSeekbar() {
        this.mHandler = new Handler();
        vvVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mediaPlayer) {
//                mp = mediaPlayer;
//                vvVideo.seekTo(seekPosition1);
                mSeekBar.setMax(vvVideo.getDuration());
                mSeekBar.setProgress(seekPosition1);
                vvVideo.start();
                playCycler();
            }
        });
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                if (z) {
                    playCycler();
                    vvVideo.seekTo(i);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                vvVideo.pause();
                ivPlayPause.setImageResource(R.drawable.ic_play);
                playCycler();
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                if (videoIsPlaying) {
                    ivPlayPause.setImageResource(R.drawable.ic_pause);
                    vvVideo.start();
                    playCycler();
                }
            }
        });
    }

    Runnable mRunnable;
    Handler mHandler;

    public void playCycler() {
//        mSeekBar.setProgress(vvVideo.getCurrentPosition());
        mSeekBar.setProgress(seekPosition1);
        if (videoIsPlaying) {//vvVideo.isPlaying()
            Runnable r0 = new Runnable() {
                public void run() {
                    seekPosition1 = vvVideo.getCurrentPosition();
                    playCycler();
                }
            };
            this.mRunnable = r0;
            this.mHandler.postDelayed(r0, 1);
        }
    }


    public Point getInitialWindowSize() {
        return new Point(Math.round(getResources().getDimension(R.dimen._180sdp)), Math.round(getResources().getDimension(R.dimen._120sdp)));//200, 200.toDp())
    }


    public Point getMinimumWindowSize() {
        return new Point(Math.round(getResources().getDimension(R.dimen._150sdp)), Math.round(getResources().getDimension(R.dimen._100sdp)));
    }

    public Point getMaximumWindowSize() {
        return new Point(screenSize.x, screenSize.y);
    }


    @SuppressLint("ClickableViewAccessibility")
    public void addWindowManager() {

        int flagType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            flagType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            flagType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        initialWindowSize = getInitialWindowSize();

        wParams = new WindowManager.LayoutParams(initialWindowSize.x, initialWindowSize.y, flagType, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        //ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
//        wParams.x = 0;//sharedPreferences.getInt(Constants.CURRENT_X, -1);
//        wParams.y = 0;//sharedPreferences.getInt(Constants.CURRENT_Y, -1);

        wParams.gravity = Gravity.TOP | Gravity.LEFT;
        wParams.x = 100;
        wParams.y = 200;

        Display display = windowManager.getDefaultDisplay();
        display.getSize(screenSize);

        minimumWindowSize = getMinimumWindowSize();
        maximumWindowSize = getMaximumWindowSize();


        if (!isFirstView) {
            isFirstView = false;
            windowManager.removeView(floatingView);//floatingView
        }
        windowManager.addView(floatingView, wParams);//floatingView

        ivResize.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_MOVE:


                        float changeDistanceX = (motionEvent.getRawX() - oldResizeRawEventX);
                        oldResizeRawEventX = motionEvent.getRawX();
                        int width;
                        if (wParams.width > 0) {
                            width = wParams.width;
                        } else {
                            width = floatingView.getMeasuredWidth();//floatingView
                        }

                        float changeDistanceY = (motionEvent.getRawY() - oldResizeRawEventY);
                        oldResizeRawEventY = motionEvent.getRawY();
                        int height;
                        if (wParams.height > 0) {
                            height = wParams.height;
                        } else {
                            height = floatingView.getMeasuredHeight();//floatingView
                        }

                        float heightResize = height + (changeDistanceX / 2);
                        float widthResize = width + changeDistanceX;

                        if (screenSize.x > widthResize) {

                            if (heightResize < minimumWindowSize.y) {
                                heightResize = (float) minimumWindowSize.y;
                            }
                            if (maximumWindowSize.y < heightResize) {
                                heightResize = (float) maximumWindowSize.y;
                            }
                            if (screenSize.y < heightResize) {
                                heightResize = (float) screenSize.y;
                            }


                            if (widthResize < minimumWindowSize.x) {
                                widthResize = (float) minimumWindowSize.x;
                            }
                            if (maximumWindowSize.x < widthResize) {
                                widthResize = (float) maximumWindowSize.x;
                            }
                            if (screenSize.x < widthResize) {
                                widthResize = (float) screenSize.x;
                            }

                            wParams.height = (int) heightResize;
                            wParams.width = (int) widthResize;

                            windowManager.updateViewLayout(floatingView, wParams);//floatingView
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        oldResizeRawEventY = motionEvent.getRawY();
                        oldResizeRawEventX = motionEvent.getRawX();
                        break;

                    case MotionEvent.ACTION_DOWN:
                        oldResizeRawEventY = motionEvent.getRawY();
                        oldResizeRawEventX = motionEvent.getRawX();
                        break;

                    default:
                        oldResizeRawEventY = motionEvent.getRawY();
                        oldResizeRawEventX = motionEvent.getRawX();
                        break;
                }
                return true;
            }
        });

        floatingView.setOnTouchListener(new View.OnTouchListener() {

            WindowManager.LayoutParams updatedParams = wParams;
            int x, y;
            float touchedX, touchedY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:

                        x = updatedParams.x;
                        y = updatedParams.y;

                        touchedX = event.getRawX();
                        touchedY = event.getRawY();

                        break;

                    case MotionEvent.ACTION_MOVE:

                        updatedParams.x = (int) (x + (event.getRawX() - touchedX));
                        updatedParams.y = (int) (y + (event.getRawY()) - touchedY);

                        windowManager.updateViewLayout(floatingView, updatedParams);
                        prevParams = updatedParams;

//                        editor.putInt(Constants.CURRENT_X, updatedParams.x);
//                        editor.putInt(Constants.CURRENT_Y, updatedParams.y);
//                        editor.commit();

//                        AppGlobals.setIntPrefs(getApplicationContext(), AppGlobals.CURRENT_X, updatedParams.x);
//                        AppGlobals.setIntPrefs(getApplicationContext(), AppGlobals.CURRENT_Y, updatedParams.y);

                        break;

                    default:
                        break;

                }

                gestureDetector.onTouchEvent(event);

                return false;
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.ivClose:

                ModelClassForVideo modelClassForVideo = new ModelClassForVideo();
                modelClassForVideo.setBoolean_selected(false);
                modelClassForVideo.setSongPath(videoDataList.get(videoPosition).getSongPath());
                modelClassForVideo.setSongName(videoDataList.get(videoPosition).getSongName());
                modelClassForVideo.setSongAlbum(videoDataList.get(videoPosition).getSongAlbum());
                modelClassForVideo.setSongThumb(videoDataList.get(videoPosition).getSongThumb());
                modelClassForVideo.setSongDuration(videoDataList.get(videoPosition).getSongDuration());
                modelClassForVideo.setSongSize(videoDataList.get(videoPosition).getSongSize());
                modelClassForVideo.setDateAdded(videoDataList.get(videoPosition).getDateAdded());
                modelClassForVideo.setDateModified(videoDataList.get(videoPosition).getDateModified());
                modelClassForVideo.setCurrDuration(seekPosition1);
                modelClassForVideo.setTypeVal(0);
                BaseActivity.setLatestVideo(getApplicationContext(), BaseActivity.PREF_LATEST_PLAY, modelClassForVideo);

                modelClassForVideo.setTypeVal(1);
                BaseActivity.checkAndAddToRecentList(getApplicationContext(), modelClassForVideo);

                stopSelf();
                break;

            case R.id.ivOpenPlayer:
                openActivity();
                break;

            case R.id.ivPlayPause:
                if (videoIsPlaying) {
                    vvVideo.pause();
                    ivPlayPause.setImageResource(R.drawable.ic_play);
                    seekPosition1 = vvVideo.getCurrentPosition();
                    videoIsPlaying = false;
                    playCycler();
                    return;
                }
                vvVideo.seekTo(seekPosition1);
                vvVideo.start();
                ivPlayPause.setImageResource(R.drawable.ic_pause);
                videoIsPlaying = true;
                playCycler();
                break;

            case R.id.ivPrev:
                setPreviousVideo();
                break;

            case R.id.ivNext:
                setNextVideo();
                break;
        }

    }


    public void setNextVideo(){
        if (videoPosition < videoUriList.size()) {
            if (vvVideo != null && vvVideo.isPlaying()) {
                vvVideo.pause();
            }
            ivPlayPause.setImageResource(R.drawable.ic_pause);
            videoIsPlaying = true;
            videoPosition = videoPosition + 1;
            int temp = videoPosition;
            tvSongName.setText(videoDataList.get(videoPosition).getSongName());
//                    editor.putInt(Constants.CURRENT_PLAYING_VIDEO_NUMBER, videoPosition);
//                    editor.commit();
            seekPosition1 = 0;
            if (videoFileIsCorrupted(videoDataList.get(temp).getSongPath())) {
                addVideoToVideoView();
            } else {
                Toast.makeText(this, "Video is currupted or malformated", Toast.LENGTH_SHORT).show();
                setNextVideo();
            }
        }
    }


    public void setPreviousVideo(){

        if (videoPosition > 0) {
            if (vvVideo != null && vvVideo.isPlaying()) {
                vvVideo.pause();
            }
            ivPlayPause.setImageResource(R.drawable.ic_pause);
            videoIsPlaying = true;
            videoPosition = videoPosition - 1;
            int temp = videoPosition;
            tvSongName.setText(videoDataList.get(videoPosition).getSongName());
//                    editor.putInt(Constants.CURRENT_PLAYING_VIDEO_NUMBER, videoPosition);
//                    editor.commit();
            seekPosition1 = 0;

            if (videoFileIsCorrupted(videoDataList.get(temp).getSongPath())) {
                addVideoToVideoView();
            } else {
                Toast.makeText(this, "Video is currupted or malformated", Toast.LENGTH_SHORT).show();
                setPreviousVideo();
            }
        }
    }


    public void openActivity() {
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            stopForeground(STOP_FOREGROUND_REMOVE);
//        }else{
//            stopSelf();
//        }
        Intent intent = new Intent(this, VideoPlayerActivity.class);
        intent.putExtra("path", videoDataList.get(videoPosition).getSongPath());
        intent.putExtra("name", videoDataList.get(videoPosition).getSongName());
        intent.putExtra("position", videoPosition);
        intent.putExtra("seekPosition", seekPosition1);
        intent.putExtra("whereFrom", wherefrom);
        intent.putExtra("bucketName", dirName);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        stopSelf();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: inside service");

        windowManager.removeView(floatingView);
    }


   /* @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(new Intent(getApplication(), FloatService.class));
        }else{
            startService(new Intent(getApplication(), FloatService.class));
        }

    }*/



    @Override
    public IBinder onBind(Intent intent) {

        throw new UnsupportedOperationException("Not yet implemented");
    }
}
