package com.prolific.vidmediaplayer.Services;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

import com.onesignal.OneSignal;
import com.prolific.vidmediaplayer.BuildConfig;
import com.prolific.vidmediaplayer.Others.AdLoader;

public class AppClass extends Application {
    public static final String CHANNEL_ID = BuildConfig.APPLICATION_ID+"NotificationService";
    public static final String CHANNEL_ID_Back = BuildConfig.APPLICATION_ID+"NotificationServiceBack";
    private static final String ONESIGNAL_APP_ID = "ceae4c33-e6f1-4d5f-aa7d-686470a1ac2e";

    public void onCreate() {
        super.onCreate();
        new AdLoader(this);

        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);

        createNotificationChannel();
        createNotificationChannelBack();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Playing Audio", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setSound((Uri) null, (AudioAttributes) null);
            ((NotificationManager) getSystemService(NotificationManager.class)).createNotificationChannel(notificationChannel);
        }
    }

    private void createNotificationChannelBack() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID_Back, "Playing Audio", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setSound((Uri) null, (AudioAttributes) null);
            ((NotificationManager) getSystemService(NotificationManager.class)).createNotificationChannel(notificationChannel);
        }
    }
}
