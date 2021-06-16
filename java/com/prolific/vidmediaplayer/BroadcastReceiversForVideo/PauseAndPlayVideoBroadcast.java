package com.prolific.vidmediaplayer.BroadcastReceiversForVideo;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.prolific.vidmediaplayer.R;
import com.prolific.vidmediaplayer.Services.BackgroundPlay;

public class PauseAndPlayVideoBroadcast extends BroadcastReceiver {

    private BackgroundPlay backgroundPlay;

    public void onReceive(Context context, Intent intent) {
        backgroundPlay = new BackgroundPlay();
        Class<PauseAndPlayVideoBroadcast> cls = PauseAndPlayVideoBroadcast.class;
        if (BackgroundPlay.mediaPlayer.isPlaying()) {
            BackgroundPlay.mediaPlayer.pause();
            BackgroundPlay.mediaPlayerIsPlaying = false;
            if (Build.VERSION.SDK_INT >= 21) {
                PendingIntent.getBroadcast(context, 10, new Intent(context, cls), PendingIntent.FLAG_UPDATE_CURRENT);
                ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(10, BackgroundPlay.notification);
                context.sendBroadcast(new Intent("play"));
            }
        } else {
            BackgroundPlay.mediaPlayer.start();
            BackgroundPlay.mediaPlayerIsPlaying = true;
            if (Build.VERSION.SDK_INT >= 21) {
                PendingIntent.getBroadcast(context, 10, new Intent(context, cls), PendingIntent.FLAG_UPDATE_CURRENT);
                ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(10, BackgroundPlay.notification);
                context.sendBroadcast(new Intent("pause"));
            }
        }
        if (BackgroundPlay.mediaPlayer.isPlaying()) {
//            BackgroundPlay.expandedViews.setImageViewResource(R.id.notificationPause, R.drawable.ic_pause_black);
            BackgroundPlay.collapsedViews.setImageViewResource(R.id.notificationPause, R.drawable.ic_pause_black);
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(10, BackgroundPlay.notification);
            context.sendBroadcast(new Intent("pause"));
            return;
        }
//        BackgroundPlay.expandedViews.setImageViewResource(R.id.notificationPause, R.drawable.ic_play_black);
        BackgroundPlay.collapsedViews.setImageViewResource(R.id.notificationPause, R.drawable.ic_play_black);
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(10, BackgroundPlay.notification);
        context.sendBroadcast(new Intent("pause"));
    }
}
