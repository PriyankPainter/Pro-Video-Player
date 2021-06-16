package com.prolific.vidmediaplayer.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdSize;
import com.prolific.vidmediaplayer.AudioClasses.PlayingAudioActivity;
import com.prolific.vidmediaplayer.AudioClasses.PlayingAudioService;
import com.prolific.vidmediaplayer.Models.MessageEvent;
import com.prolific.vidmediaplayer.Models.ModelClassForVideo;
import com.prolific.vidmediaplayer.Others.Utils;
import com.prolific.vidmediaplayer.Services.BackgroundPlay;
import com.prolific.vidmediaplayer.Services.FloatService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class BaseActivity extends AppCompatActivity {

    public final String PREF_SCREEN_RATIO = "prefScreenRatio";
    public final String PREF_SLEEP = "prefSleep";
    public static final String PREF_LATEST_PLAY = "prefLatestPlay";
    public static final String PREF_RECENT_VIDEO_LIST = "prefRecentVideoList";
    public final String PREF_VIEW_AS = "prefViewAs";
    public final String PREF_SORT_BY = "prefSortBy";
    public final String PREF_IS_LAST_PLAY_VISIBLE = "prefIsLastPlayVisible";
    public final String PREF_IS_PLAYER_AUTO_PLAY = "prefIsPlayerAutoPlay";
    public final String PREF_SCREEN_ORIENTATION = "prefScreenOrientation";
    public final String IS_ALREADY_OPENED = "prefIsAlresdyOpened";
    public final String CURRENT_X = "x-axis";
    public final String CURRENT_Y = "y-axis";

    public static ArrayList<Object> arrRecentList = new ArrayList<>();

    public static SharedPreferences sharedPref;
    public static final String PREF_NAME = "VideoPlayerPrefs";
    public long totalVidSize;



    public AdSize getAdSize(Activity activity) {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;
        int adWidth = (int) (widthPixels / density);
        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth);
    }


    public void notifyExceptFolder(){
        EventBus.getDefault().post(new MessageEvent("RefreshVideo", ""));
        EventBus.getDefault().post(new MessageEvent("RefreshRecent", ""));
    }

    public void notifyExceptVideo(){
        EventBus.getDefault().post(new MessageEvent("RefreshFolder", ""));
        EventBus.getDefault().post(new MessageEvent("RefreshRecent", ""));
    }

    public void notifyExceptRecent(){
        EventBus.getDefault().post(new MessageEvent("RefreshVideo", ""));
        EventBus.getDefault().post(new MessageEvent("RefreshFolder", ""));
    }

    public void notifyAllApp() {
        EventBus.getDefault().post(new MessageEvent("RefreshVideo", ""));
        EventBus.getDefault().post(new MessageEvent("RefreshFolder", ""));
        EventBus.getDefault().post(new MessageEvent("RefreshRecent", ""));
    }


    public String[] getStoragePath(Activity activity){
        Utils utils = new Utils(activity);
        String[] arrStoragePaths = new String[3];
        arrStoragePaths[0] = utils.StoragePath("InternalStorage");
        arrStoragePaths[1] = utils.StoragePath("ExternalStorage");
        arrStoragePaths[2] = utils.StoragePath("UsbStorage");
        return arrStoragePaths;
    }

    public Uri getUriFromPath(String pathMain, Context context) {
        Cursor cursor1 = context.getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media._ID},
                MediaStore.Video.Media.DATA + "=? ",
                new String[]{pathMain}, null);
        Uri mainUri = null;

        if (cursor1 != null && cursor1.moveToFirst()) {
            int id = cursor1.getInt(cursor1.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor1.close();
            mainUri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else {
        }

        Log.e("dasjdahsdkjsadsa", " ===== " + mainUri);

        return mainUri;
    }

    public Uri getBucketUriFromPath(String bucketName, Context context) {
        @SuppressLint("InlinedApi") Cursor cursor1 = context.getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media.BUCKET_ID},
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME + "=? ",
                new String[]{bucketName}, null);
        Uri mainUri = null;

        if (cursor1 != null && cursor1.moveToFirst()) {
            @SuppressLint("InlinedApi") int id = cursor1.getInt(cursor1.getColumnIndex(MediaStore.Video.Media.BUCKET_ID));
            cursor1.close();
            mainUri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else {
        }

        Log.e("dasjdahsdkjsadsa", " ===== " + mainUri);

        return mainUri;
    }





    public void setBooleanPrefs(Context context, String key, boolean val) {
        sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPref.edit();
        edit.putBoolean(key, val);
        edit.apply();
    }

    public boolean getBooleanPrefs(Context context, String key) {
        sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean val = sharedPref.getBoolean(key, false);
        return val;
    }

    public void setIntPrefs(Context context, String key, int val) {
        sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPref.edit();
        edit.putInt(key, val);
        edit.apply();
    }

    public int getIntPrefs(Context context, String key) {
        sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int val = sharedPref.getInt(key, 0);
        return val;
    }

    public void setStringPrefs(Context context, String key, String val) {
        sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPref.edit();
        edit.putString(key, val);
        edit.apply();
    }

    public String getStringPrefs(Context context, String key) {
        sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String val = sharedPref.getString(key, "");
        return val;
    }


    public static void setLatestVideo(Context context, String key, ModelClassForVideo myObject) {
        sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPref.edit();
        String json = new Gson().toJson(myObject);
        edit.putString(key, json);
        edit.apply();
    }


    public ModelClassForVideo getLatestVideo(Context context, String key) {
        sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = sharedPref.getString(key, "");
        Type type = new TypeToken<ModelClassForVideo>() {
        }.getType();
        ModelClassForVideo obj = null;
        if (json != null && !json.equals("")) {
            obj = new Gson().fromJson(json, type);
        }
        return obj;
    }

    public void removePrefsByKey(Context context, String key) {
        sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPref.edit();
        edit.remove(key);
        edit.apply();
    }


    public static void setRecentVideoList(Context context, String key, ArrayList<Object> arr) {
        sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPref.edit();
        String json = new Gson().toJson(arr);
        edit.putString(key, json);
        edit.apply();
    }


    public static ArrayList<Object> getRecentVideoList(Context context, String key) {
        sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = sharedPref.getString(key, "");
        Type type = new TypeToken<ArrayList<ModelClassForVideo>>() {
        }.getType();
        ArrayList<Object> arr = null;
        if (json != null && !json.equals("")) {
            arr = new Gson().fromJson(json, type);
        } else {
            arr = new ArrayList<>();
        }
        return arr;
    }


    @SuppressLint("DefaultLocale")
    public static String convertDuration(long songDuration) {

        NumberFormat f = new DecimalFormat("00");
        long hour = (songDuration / 3600000) % 24;
        long min = (songDuration / 60000) % 60;
        long sec = (songDuration / 1000) % 60;
        String strDuration;
        if(hour > 0){
            strDuration = f.format(hour) + ":" + f.format(min) + ":" + f.format(sec);
        }else{
            strDuration = f.format(min) + ":" + f.format(sec);
        }
        return strDuration;
    }

    public String readableFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }


    public String changeMillisToDate(long millis, String dateFormat) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis * 1000);
        @SuppressLint("SimpleDateFormat") String formattedDate = new SimpleDateFormat(dateFormat).format(calendar.getTime());
        return formattedDate;
    }

    public boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    public void playVideo(Activity context, int pos, ArrayList<Object> arr, ModelClassForVideo data, String flag, String dirName, String dirId) {
        if (isMyServiceRunning(context, FloatService.class)) {
            FloatService floatService = new FloatService();
            floatService.changeVideo(pos, 0);
        } else {
            Intent intent = new Intent(context, VideoPlayerActivity.class);
            if (flag.equals("folder")) {
                intent.putExtra("position", 0);
                VideoPlayerActivity.mPosition = 0;
            }else {
                intent.putExtra("position", pos);
                VideoPlayerActivity.mPosition = pos;
            }
            if(arr.size() > 0) {
                intent.putExtra("path", ((ModelClassForVideo)arr.get(pos)).getSongPath());
                intent.putExtra("name", ((ModelClassForVideo)arr.get(pos)).getSongName());
            }else{
                intent.putExtra("path", data.getSongPath());
                intent.putExtra("name", data.getSongName());
            }
            if (flag.equals("latestPlay")) {
                intent.putExtra("seekPosition", data.getCurrDuration());
            } else {
                intent.putExtra("seekPosition", 0);
            }
            if (flag.equals("insideVideos") || flag.equals("folder")) {
                intent.putExtra("bucketName", dirName);
                intent.putExtra("bucketId", dirId);
            }
            context.startActivity(intent);
        }
        if (BackgroundPlay.mediaPlayer != null) {
            PlayingAudioActivity.mHandler.removeCallbacks(PlayingAudioActivity.mRunnable);
            BackgroundPlay.mediaPlayer.stop();
            BackgroundPlay.mediaPlayer.release();
            BackgroundPlay.mediaPlayer = null;
        }
        removeNotificationWhenPlaying(context);
    }

    public void removeNotificationWhenPlaying(Activity context) {
        context.stopService(new Intent(context, BackgroundPlay.class));
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(10);
        context.stopService(new Intent(context, PlayingAudioService.class));
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(10);
        if (MainActivity.audioSongIsPlaying) {
            PlayingAudioActivity.mHandler.removeCallbacks(PlayingAudioActivity.mRunnable);
            PlayingAudioActivity.mediaPlayer.stop();
            PlayingAudioActivity.mediaPlayer.release();
            PlayingAudioActivity.mediaPlayer = null;
            MainActivity.audioSongIsPlaying = false;
        }
    }

    public static boolean isAlreadyAvailable = false;
    public static int arrPos;

    public static void checkAndAddToRecentList(Context context, ModelClassForVideo data) {

        arrRecentList.clear();
        arrRecentList = getRecentVideoList(context, PREF_RECENT_VIDEO_LIST);
        if (arrRecentList != null && arrRecentList.size() > 0) {
            for (int i = 0; i < arrRecentList.size(); i++) {
                if (data.getSongPath().equals(((ModelClassForVideo)arrRecentList.get(i)).getSongPath())) {
                    isAlreadyAvailable = true;
                    arrPos = i;
                    break;
                }
            }

            if (isAlreadyAvailable) {
                arrRecentList.remove(arrPos);
                arrRecentList.add(0, data);
                isAlreadyAvailable = false;
            } else {
                arrRecentList.add(0, data);
            }
        } else {
            arrRecentList.add(data);
        }
        setRecentVideoList(context, PREF_RECENT_VIDEO_LIST, arrRecentList);

    }


}
