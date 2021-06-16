package com.prolific.vidmediaplayer.BroadcastReceiversForVideo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.prolific.vidmediaplayer.Activities.VideoPlayerActivity;
import com.prolific.vidmediaplayer.Activities.MainActivity;
import com.prolific.vidmediaplayer.Services.BackgroundPlay;

public class NotificationClickBroadcast extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {

        MainActivity.isStartedFromNotificationCLick = true;
        VideoPlayerActivity.mVideoPath = BackgroundPlay.songPath;
        Intent intent2 = new Intent(context, VideoPlayerActivity.class);
        intent2.putExtra("path",intent.getStringExtra("path"));
        intent2.putExtra("name", intent.getStringExtra("name"));
        intent2.putExtra("position", intent.getIntExtra("position", 0));
        intent2.putExtra("seekPosition", intent.getIntExtra("seekPosition", 0));
        if (VideoPlayerActivity.mWhereFrom == 2) {
            intent2.putExtra("bucketName", intent.getStringExtra("bucketName"));
        }
        intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent2);
    }
}
