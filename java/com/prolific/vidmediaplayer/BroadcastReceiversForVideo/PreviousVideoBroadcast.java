package com.prolific.vidmediaplayer.BroadcastReceiversForVideo;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import com.prolific.vidmediaplayer.Activities.VideoPlayerActivity;
import com.prolific.vidmediaplayer.Activities.MainActivity;
import com.prolific.vidmediaplayer.Models.ModelClassForVideo;
import com.prolific.vidmediaplayer.Services.BackgroundPlay;

public class PreviousVideoBroadcast extends BroadcastReceiver {
    private static final String MEDIA_CHANNEL_ID = "darkVideoSongService";
    Notification notification;
    private VideoPlayerActivity playerActivity;
    private BackgroundPlay backgroundPlay;
    public void onReceive(Context context, Intent intent) {
        NotificationManagerCompat.from(context);
        playerActivity = new VideoPlayerActivity();
        backgroundPlay = new BackgroundPlay();
        if (VideoPlayerActivity.mPosition >= VideoPlayerActivity.arrayListVideos.size() || VideoPlayerActivity.mPosition <= 0) {
            VideoPlayerActivity.mPosition = 0;
            VideoPlayerActivity.mVideoPath = ((ModelClassForVideo)VideoPlayerActivity.arrayListVideos.get(VideoPlayerActivity.mPosition)).getSongPath();
            BackgroundPlay.songPath = VideoPlayerActivity.mVideoPath;
            BackgroundPlay.mediaPlayer.stop();
            BackgroundPlay.mediaPlayer.release();
            BackgroundPlay.mediaPlayer = null;
        } else {
            VideoPlayerActivity.mVideoPath = ((ModelClassForVideo)VideoPlayerActivity.arrayListVideos.get(VideoPlayerActivity.mPosition - 1)).getSongPath();
            VideoPlayerActivity.mPosition++;
            BackgroundPlay.songPath = VideoPlayerActivity.mVideoPath;
            BackgroundPlay.mediaPlayer.stop();
            BackgroundPlay.mediaPlayer.release();
            BackgroundPlay.mediaPlayer = null;
        }
        if (VideoPlayerActivity.isFullScreenActivityVisible) {
            VideoPlayerActivity.mSongName.setText(((ModelClassForVideo)VideoPlayerActivity.arrayListVideos.get(VideoPlayerActivity.mPosition)).getSongName());
            VideoPlayerActivity.mSeekBar.setMax(BackgroundPlay.mediaPlayer.getDuration());
        }
        if (Build.VERSION.SDK_INT >= 21) {
            ContextCompat.startForegroundService(context, new Intent(context, BackgroundPlay.class));
        }
        MainActivity.PreviousWasPressedInVideo = true;
    }
}
