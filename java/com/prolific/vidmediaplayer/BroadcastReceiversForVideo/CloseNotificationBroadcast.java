package com.prolific.vidmediaplayer.BroadcastReceiversForVideo;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;

import com.prolific.vidmediaplayer.Services.BackgroundPlay;

public class CloseNotificationBroadcast extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {

        MediaPlayer mediaPlayer2 = BackgroundPlay.mediaPlayer;
        if (mediaPlayer2 != null && mediaPlayer2.isPlaying()) {
            BackgroundPlay.mediaPlayer.stop();
            BackgroundPlay.mediaPlayer.release();
            BackgroundPlay.mediaPlayer = null;
        }

        context.stopService(new Intent(context, BackgroundPlay.class));
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(10);
    }
}
