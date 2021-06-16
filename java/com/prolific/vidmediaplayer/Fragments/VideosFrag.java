package com.prolific.vidmediaplayer.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.prolific.vidmediaplayer.Activities.BaseActivity;
import com.prolific.vidmediaplayer.Activities.MainActivity;
import com.prolific.vidmediaplayer.Activities.SelectionActivity;
import com.prolific.vidmediaplayer.Activities.SettingActivity;
import com.prolific.vidmediaplayer.Activities.VideoPlayerActivity;
import com.prolific.vidmediaplayer.Adapter.VideoAdapter;
import com.prolific.vidmediaplayer.BuildConfig;
import com.prolific.vidmediaplayer.Models.MessageEvent;
import com.prolific.vidmediaplayer.Models.ModelClassForVideo;
import com.prolific.vidmediaplayer.Others.CustomTypefaceSpan;
import com.prolific.vidmediaplayer.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import static com.prolific.vidmediaplayer.Others.AdLoader.initInterstitialAds;

public class VideosFrag extends Fragment implements View.OnClickListener {

    private ArrayList<ModelClassForVideo> arrayListVideosSearch;
    private VideoAdapter mAdapter;
    private RecyclerView mVideoRecyclerView;
    private LinearLayout llEmpty;
    public ArrayList<Object> videosArrayList, videoListWOLastPlay;
    private ArrayList<Uri> videosUriList;
    private String fileOriginalName;
    private RecyclerView.LayoutManager mLayoutManager;
    private int mPosition = 0;
    private long totalSize = 0;
    private int sortVal;
    private boolean isLastPlayVisible;
    private ModelClassForVideo latestVideoData;
    private String str = "";
    private boolean isAlreadyReverse = false;
    private MainActivity mainActivity;
    private ShimmerFrameLayout shimmerViewContainer;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = layoutInflater.inflate(R.layout.fragment_videos, viewGroup, false);
        initView(view);
        return view;
    }


    private void initView(View view) {
        mainActivity = new MainActivity();

        mVideoRecyclerView = view.findViewById(R.id.videosRecycler);
        shimmerViewContainer = view.findViewById(R.id.shimmerViewContainer);
        shimmerViewContainer.startShimmer();
        llEmpty = view.findViewById(R.id.llEmpty);

        videoListWOLastPlay = new ArrayList<>();
        videosArrayList = new ArrayList<>();
        videosUriList = new ArrayList<>();

//        getAllVideos();

        int viewTypeVal = ((MainActivity) requireActivity()).getIntPrefs(requireActivity(), ((MainActivity) requireActivity()).PREF_VIEW_AS);
        if (viewTypeVal == 0) {
            mLayoutManager = new LinearLayoutManager(getContext());
            mVideoRecyclerView.setLayoutManager(mLayoutManager);
        } else if (viewTypeVal == 1) {
            mLayoutManager = new GridLayoutManager(getContext(), 2);
            GridLayoutManager manager = (GridLayoutManager) mLayoutManager;
            manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int i) {
                    switch (i) {
                        case 0:
                            return 2;

                        default:
                            return 1;
                    }
                }
            });
            mVideoRecyclerView.setLayoutManager(manager);
        } else {
            mLayoutManager = new LinearLayoutManager(getContext());
            mVideoRecyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL));
            mVideoRecyclerView.setLayoutManager(mLayoutManager);
        }

        mVideoRecyclerView.setHasFixedSize(true);
        new GetAllVideos().execute();

        mAdapter = new VideoAdapter(getActivity(), videosArrayList, "main_list", VideosFrag.this::onClick);
        mVideoRecyclerView.setAdapter(mAdapter);

    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void OnMessageEvent(MessageEvent event) {
        if (event.name.equalsIgnoreCase("RefreshVideo")) {
//            new GetAllVideos().execute();
            getAllVideos();
            if (videosArrayList != null && videosArrayList.size() > 0) {
                addRemoveLatestPlayData();
//                loadNativeAds();
                mVideoRecyclerView.setVisibility(View.VISIBLE);
                llEmpty.setVisibility(View.GONE);
                mAdapter.notifyDataSetChanged();
            } else {
                mVideoRecyclerView.setVisibility(View.GONE);
                llEmpty.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        ((MainActivity) getActivity()).restartApp();
        addRemoveLatestPlayData();
    }

    private void addRemoveLatestPlayData() {
        if(videosArrayList.size() > 0) {
            isLastPlayVisible = ((MainActivity) requireActivity()).getBooleanPrefs(requireActivity(), ((MainActivity) requireActivity()).PREF_IS_LAST_PLAY_VISIBLE);
            if (isLastPlayVisible) {
                latestVideoData = ((MainActivity) requireActivity()).getLatestVideo(getActivity(), BaseActivity.PREF_LATEST_PLAY);
                if (latestVideoData != null && latestVideoData.getSongPath() != null && !latestVideoData.getSongPath().equals("")) {
                    videosArrayList.set(0, latestVideoData);
                    mAdapter.notifyItemChanged(0);
                }
            } else {
                mAdapter.notifyItemChanged(0);
            }
        }
    }


    private class GetAllVideos extends AsyncTask<Void, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e("Tag", "---------[ OnPreExecute ]-----");
//            addRemoveLatestPlayData();
        }

        @Override
        protected String doInBackground(Void... voids) {
            requireActivity().runOnUiThread(new Runnable() {
                public void run() {
                    getAllVideos();
                }
            });
            return "";
        }

        @Override
        protected void onPostExecute(String aVoid) {
            super.onPostExecute(aVoid);
            Log.e("Tag", "---------[ OnPostExecute ]-----");
            if (videosArrayList != null && videosArrayList.size() > 0) {
                addRemoveLatestPlayData();
//                loadNativeAds();
                mVideoRecyclerView.setVisibility(View.VISIBLE);
                llEmpty.setVisibility(View.GONE);
                shimmerViewContainer.stopShimmer();
                shimmerViewContainer.setVisibility(View.GONE);
                mAdapter.notifyDataSetChanged();
            } else {
                mVideoRecyclerView.setVisibility(View.GONE);
                llEmpty.setVisibility(View.VISIBLE);
                shimmerViewContainer.stopShimmer();
            }
//            mAdapter.notifyDataSetChanged();
        }
    }


    private void getAllVideos() {
        totalSize = 0;
        videosArrayList.clear();
        videoListWOLastPlay.clear();
        videosUriList.clear();

        sortVal = ((MainActivity) requireActivity()).getIntPrefs(requireActivity(), ((MainActivity) requireActivity()).PREF_SORT_BY);
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
        String[] strArr = {"_data", "title", "bucket_display_name", "duration", "_size", "date_added", "date_modified"};//, "resolution" //,MediaStore.Video.VideoColumns.RESOLUTION
        Cursor cursor = Objects.requireNonNull(getContext()).getContentResolver().query(uri, strArr, null, null, str);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                int vidSize = cursor.getColumnIndexOrThrow("_size");
                String path = cursor.getString(cursor.getColumnIndexOrThrow("_data"));

                ModelClassForVideo modelClassForVideo = new ModelClassForVideo();
                modelClassForVideo.setBoolean_selected(false);
                modelClassForVideo.setSongPath(path);
                modelClassForVideo.setSongName(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                modelClassForVideo.setSongAlbum(cursor.getString(cursor.getColumnIndexOrThrow("bucket_display_name")));
                modelClassForVideo.setSongThumb(path);
                modelClassForVideo.setSongDuration(cursor.getInt(cursor.getColumnIndexOrThrow("duration")));
                modelClassForVideo.setSongSize(cursor.getInt(vidSize));
                modelClassForVideo.setDateAdded(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)));
                modelClassForVideo.setDateModified(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)));
                modelClassForVideo.setTypeVal(1);
                videosArrayList.add(modelClassForVideo);

                Uri vidUri;
                File f = new File(path);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    vidUri = FileProvider.getUriForFile(requireActivity(), BuildConfig.APPLICATION_ID + ".fileprovider", f);
                } else {
                    vidUri = Uri.fromFile(f);
                }
                videosUriList.add(vidUri);

                totalSize = totalSize + cursor.getInt(vidSize);
                Log.e("TAG", "getMusic-path :" + path + "----  Count : " + cursor.getCount());
                ((MainActivity) requireActivity()).totalVidSize = totalSize;
            }
            if (videosArrayList.size() > 0) {

                videoListWOLastPlay.addAll(videosArrayList);

                if (((ModelClassForVideo)videosArrayList.get(0)).getTypeVal() != 0) {
                    ModelClassForVideo modelClassForVideo = new ModelClassForVideo();
                    modelClassForVideo.setBoolean_selected(false);
                    modelClassForVideo.setTypeVal(0);
                    videosArrayList.add(0, modelClassForVideo);
                }
                if (sortVal == 5) {
                    if (isAlreadyReverse) {

                        ModelClassForVideo data0 = (ModelClassForVideo)videosArrayList.get(0);
                        videosArrayList.remove(0);

                        Collections.reverse(videosArrayList);
                        Collections.reverse(videosUriList);
                        Collections.reverse(videoListWOLastPlay);

                        videosArrayList.add(0, data0);

                        isAlreadyReverse = true;
                    }
                }
            }
        }
        cursor.close();
    }

    private AdLoader adLoader;
    private void loadNativeAds() {

        AdLoader.Builder builder = new AdLoader.Builder(requireContext(), getString(R.string.app_nativeAd_id));
        adLoader = builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
            @Override
            public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                // A native ad loaded successfully, check if the ad loader has finished loading
                // and if so, insert the ads into the list.
                if (!adLoader.isLoading()) {
                    insertAdsInMenuItems(unifiedNativeAd);
                }
            }
        }).withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                // A native ad failed to load, check if the ad loader has finished loading
                // and if so, insert the ads into the list.
                Log.e("TAG~~~", "Filed to load Native Ad : " + errorCode);
            }
        }).build();

        // Load the Native ads.
        adLoader.loadAds(new AdRequest.Builder().build(), 1);
    }

    private int c=0;
    private void insertAdsInMenuItems(UnifiedNativeAd ad) {
        if (ad == null) {
            return;
        }

        int index = 2;
        for (int i = 0; i < videosArrayList.size(); i++) {
            c++;
            if(c == 10){
                //TODO: add logic to add unified ad to array
                videosArrayList.add(i, ad);
                c = 0;
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Log.e("TAG", "ResultCode = " + resultCode);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 20000) {//Rename in android 11 only
                if (dRename != null && dRename.isShowing()) {
                    dRename.dismiss();
                }
                updateTag2(requireActivity());
            } else if (requestCode == 10000) {//Delete in android 11 only
                if (dDelete != null && dDelete.isShowing()) {
                    dDelete.dismiss();
                }
                deleteRecordAndNotify();
            }
        } else {
            Toast.makeText(mainActivity, "error", Toast.LENGTH_SHORT).show();
        }
    }

    private int updateRows;

    private void updateTag2(Activity activity) {
        updateRows = 0;

        Uri uri = ((MainActivity) requireActivity()).getUriFromPath(from.getAbsolutePath(), requireActivity());
        ContentValues values = new ContentValues();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Video.Media.IS_PENDING, 1);
            requireActivity().getContentResolver().update(uri, values, null, null);

            values.put(MediaStore.Video.Media.IS_PENDING, 0);
            values.put(MediaStore.Video.Media.DISPLAY_NAME, to.getName());

            updateRows = requireActivity().getContentResolver().update(uri, values, null, null);
            requireActivity().getContentResolver().notifyChange(uri, null);

        } else {
            updateRows = requireActivity().getContentResolver().update(uri, values, null, null);

            values.put(MediaStore.Video.Media.DISPLAY_NAME, to.getName());
            requireActivity().getContentResolver().notifyChange(uri, null);
        }

        MediaScannerConnection.scanFile(activity, new String[]{from.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
            public void onScanCompleted(String path, Uri uri) {
                Log.e("ExternalStorage", "-> Scanned " + path + ":");
                Log.e("ExternalStorage", "-> uri=" + uri);
            }
        });

        if (updateRows > 0) {
            notifyOtherTabData();
        }
    }


    private File from, to;
    private Dialog dRename;

    private void renameFile() {

        String path = ((ModelClassForVideo)videosArrayList.get(mPosition)).getSongPath();

        dRename = new Dialog(requireActivity());
        dRename.setContentView(R.layout.layout_rename);
        dRename.getWindow().setLayout(-1, -2);

        Button cancel = (Button) dRename.findViewById(R.id.btnCancel);
        Button btnRename = (Button) dRename.findViewById(R.id.btnRename);
        EditText editText = (EditText) dRename.findViewById(R.id.etRename);
        editText.setHint("Please enter video name");
        File oldFile = new File(path);
        String name = oldFile.getName();
        fileOriginalName = name;
        String substring = name.substring(0, name.lastIndexOf("."));
        fileOriginalName = substring;
        editText.setText(substring);
        editText.setSelection(0, editText.getText().length());
        editText.setHighlightColor(ContextCompat.getColor(requireActivity(), R.color.colorAccent));
        editText.requestFocus();
        editText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable editable) {
            }

            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (editText.getText().toString().equals(fileOriginalName) || editText.getText().toString().equals("")) {
                    btnRename.setAlpha(0.4f);
                    btnRename.setEnabled(false);
                    return;
                }
                btnRename.setAlpha(1.0f);
                btnRename.setEnabled(true);
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dRename.dismiss();
            }
        });
        btnRename.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String absolutePath = oldFile.getParentFile().getAbsolutePath();
                String absolutePath2 = oldFile.getAbsolutePath();
                String substring = absolutePath2.substring(absolutePath2.lastIndexOf("."));
                File file = new File(absolutePath + "/" + editText.getText().toString() + substring);// + substring
                if (oldFile.exists()) {
                    from = oldFile;
                    to = file;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        ArrayList<Uri> list = new ArrayList<>();
                        list.add(((MainActivity) requireActivity()).getUriFromPath(from.getAbsolutePath(), requireActivity()));
                        PendingIntent intent = MediaStore.createWriteRequest(requireActivity().getContentResolver(), list);
                        try {
                            startIntentSenderForResult(intent.getIntentSender(), 20000, null, 0, 0, 0, null);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (oldFile.renameTo(file)) {
                            requireActivity().getApplicationContext().getContentResolver().delete(MediaStore.Files.getContentUri("external"), "_data=?", new String[]{oldFile.getAbsolutePath()});
                            Intent intent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
                            intent.setData(Uri.fromFile(file));
                            requireActivity().getApplicationContext().sendBroadcast(intent);

                            notifyOtherTabData();

                        } else {
                            Toast.makeText(requireActivity(), getResources().getString(R.string.rename_fail), Toast.LENGTH_SHORT).show();
                            dRename.dismiss();
                        }
                    }
                } else {
                    Toast.makeText(requireActivity(), getResources().getString(R.string.rename_file_not_exist), Toast.LENGTH_SHORT).show();
                }
            }
        });
        dRename.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dRename.show();
    }


    public void notifyOtherTabData() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                ModelClassForVideo modelClassForVideo = new ModelClassForVideo();
                modelClassForVideo.setBoolean_selected(false);
                modelClassForVideo.setSongPath(to.getAbsolutePath());
                modelClassForVideo.setSongThumb(to.getAbsolutePath());
                String name = to.getName();
                if (name.contains(".")) {
                    String[] strName = name.split("\\.");
                    modelClassForVideo.setSongName(strName[0]);
                } else {
                    modelClassForVideo.setSongName(name);
                }
                modelClassForVideo.setSongAlbum(((ModelClassForVideo)videosArrayList.get(mPosition)).getSongAlbum());
                modelClassForVideo.setSongDuration(((ModelClassForVideo)videosArrayList.get(mPosition)).getSongDuration());
                modelClassForVideo.setSongSize(((ModelClassForVideo)videosArrayList.get(mPosition)).getSongSize());
                modelClassForVideo.setDateAdded(((ModelClassForVideo)videosArrayList.get(mPosition)).getDateAdded());
                modelClassForVideo.setDateModified(((ModelClassForVideo)videosArrayList.get(mPosition)).getDateModified());

                BaseActivity.arrRecentList = BaseActivity.getRecentVideoList(requireActivity(), BaseActivity.PREF_RECENT_VIDEO_LIST);
                for (int j = 0; j < BaseActivity.arrRecentList.size(); j++) {
                    if (((ModelClassForVideo)BaseActivity.arrRecentList.get(j)).getSongPath().equals(((ModelClassForVideo)videosArrayList.get(mPosition)).getSongPath())) {
                        modelClassForVideo.setTypeVal(((ModelClassForVideo)BaseActivity.arrRecentList.get(j)).getTypeVal());
                        BaseActivity.arrRecentList.set(j, modelClassForVideo);
                        BaseActivity.setRecentVideoList(requireActivity(), BaseActivity.PREF_RECENT_VIDEO_LIST, BaseActivity.arrRecentList);
                    }
                }

                videosArrayList.set(mPosition, modelClassForVideo);

                ModelClassForVideo latestData = ((MainActivity) requireActivity()).getLatestVideo(requireActivity(), BaseActivity.PREF_LATEST_PLAY);
                if (latestData != null) {
                    if (latestData.getSongPath().equals(from.getAbsolutePath())) {
                        latestData.setBoolean_selected(false);
                        latestData.setSongPath(to.getAbsolutePath());
                        latestData.setSongThumb(to.getAbsolutePath());
                        name = to.getName();
                        if (name.contains(".")) {
                            String[] strName = name.split("\\.");
                            latestData.setSongName(strName[0]);
                        } else {
                            latestData.setSongName(name);
                        }
                        latestData.setSongAlbum(latestData.getSongAlbum());
                        latestData.setSongDuration(latestData.getSongDuration());
                        latestData.setSongSize(latestData.getSongSize());
                        latestData.setDateAdded(latestData.getDateAdded());
                        latestData.setDateModified(latestData.getDateModified());
                        latestData.setCurrDuration(latestData.getCurrDuration());
                        latestData.setDateModified(latestData.getDateModified());
                        latestData.setTypeVal(latestData.getTypeVal());
                    }
                    BaseActivity.setLatestVideo(requireActivity(), BaseActivity.PREF_LATEST_PLAY, latestData);
                }

                videoListWOLastPlay.clear();
                videoListWOLastPlay.addAll(videosArrayList);
                mAdapter.notifyDataSetChanged();
                Toast.makeText(requireActivity(), getResources().getString(R.string.rename_success), Toast.LENGTH_SHORT).show();
                dRename.dismiss();
//                ((BaseActivity) requireActivity()).notifyAllApp();
                EventBus.getDefault().post(new MessageEvent("RefreshVideo", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshFolder", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshRecent", ""));
            }
        }, 300);
    }

    private Dialog dDelete;

    private void deleteFile() {

        String path = ((ModelClassForVideo)videosArrayList.get(mPosition)).getSongPath();

        dDelete = new Dialog(requireActivity());
        dDelete.setContentView(R.layout.layout_delete);
        dDelete.getWindow().setLayout(-1, -2);

        Button btnCancel = (Button) dDelete.findViewById(R.id.btnCancel);
        Button btnDelete = (Button) dDelete.findViewById(R.id.btnDelete);
        TextView tvDeleteMsg = (TextView) dDelete.findViewById(R.id.tvDeleteMsg);

        tvDeleteMsg.setText(getString(R.string.delete_msg) + " video file?");

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("TAG", path);
                File vidFile = new File(path);
                if (!vidFile.exists()) {
                    Toast.makeText(requireActivity(), getResources().getString(R.string.delete_file_not_exist), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    ArrayList<Uri> list = new ArrayList<>();
                    list.add(((MainActivity) requireActivity()).getUriFromPath(vidFile.getAbsolutePath(), requireActivity()));
                    PendingIntent intent = MediaStore.createDeleteRequest(requireActivity().getContentResolver(), list);
                    try {
                        startIntentSenderForResult(intent.getIntentSender(), 10000, null, 0, 0, 0, null);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }

                } else {

                    vidFile.delete();
                    requireActivity().getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA + "=?", new String[]{vidFile.getAbsolutePath()});
                    requireActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + vidFile)));

                    deleteRecordAndNotify();
                    dDelete.dismiss();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dDelete.dismiss();
            }
        });

//        new MaterialAlertDialogBuilder(requireActivity())//, R.style.AlertDialogTheme
//                .setTitle(getResources().getString(R.string.lbl_delete))
//                .setMessage(getResources().getString(R.string.msg_delete))
//                .setPositiveButton(getResources().getString(R.string.lbl_ok), new DialogInterface.OnClickListener() {//17039379
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        Log.e("TAG", path);
//                        File vidFile = new File(path);
//                        if (!vidFile.exists()) {
//                            Toast.makeText(requireActivity(), getResources().getString(R.string.delete_file_not_exist), Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                            ArrayList<Uri> list = new ArrayList<>();
//                            list.add(((MainActivity) requireActivity()).getUriFromPath(vidFile.getAbsolutePath(), requireActivity()));
//                            PendingIntent intent = MediaStore.createDeleteRequest(requireActivity().getContentResolver(), list);
//                            try {
//                                startIntentSenderForResult(intent.getIntentSender(), 10000, null, 0, 0, 0, null);
//                            } catch (IntentSender.SendIntentException e) {
//                                e.printStackTrace();
//                            }
//
//                        } else {
//
//                            vidFile.delete();
//                            requireActivity().getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA + "=?", new String[]{vidFile.getAbsolutePath()});
//                            requireActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + vidFile)));
//
//                            deleteRecordAndNotify();
//
//                        }
//                    }
//                })
//                .setNegativeButton(getResources().getString(R.string.lbl_cancel), new DialogInterface.OnClickListener() {//17039369
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//                    }
//                });
        dDelete.show();
    }


    public void deleteRecordAndNotify() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                BaseActivity.arrRecentList = BaseActivity.getRecentVideoList(requireActivity(), BaseActivity.PREF_RECENT_VIDEO_LIST);
                for (int j = 0; j < BaseActivity.arrRecentList.size(); j++) {
                    if (((ModelClassForVideo)BaseActivity.arrRecentList.get(j)).getSongPath().equals(((ModelClassForVideo)videosArrayList.get(mPosition)).getSongPath())) {
                        BaseActivity.arrRecentList.remove(j);
                        BaseActivity.setRecentVideoList(requireActivity(), BaseActivity.PREF_RECENT_VIDEO_LIST, BaseActivity.arrRecentList);
                        break;
                    }
                }

                ModelClassForVideo latestData = ((MainActivity) requireActivity()).getLatestVideo(requireActivity(), BaseActivity.PREF_LATEST_PLAY);
                if (latestData != null) {
                    if (latestData.getSongPath().equals(((ModelClassForVideo)videosArrayList.get(mPosition)).getSongPath())) {
                        ((MainActivity) requireActivity()).removePrefsByKey(requireActivity(), BaseActivity.PREF_LATEST_PLAY);
                    }
                }

                videosArrayList.remove(mPosition);
                videoListWOLastPlay.clear();
                videoListWOLastPlay.addAll(videosArrayList);
//                mAdapter.notifyItemRemoved(mPosition);
                mAdapter.notifyDataSetChanged();
                Toast.makeText(requireActivity(), getResources().getString(R.string.delete_success), Toast.LENGTH_SHORT).show();
//                ((BaseActivity) requireActivity()).notifyAllApp();
                EventBus.getDefault().post(new MessageEvent("RefreshVideo", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshFolder", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshRecent", ""));
            }
        }, 300);
    }

    private void shareFile() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, videosUriList.get(mPosition));
        intent.setType("video/*");
        startActivity(Intent.createChooser(intent, getResources().getString(R.string.lbl_share_using)));
    }

    @SuppressLint("SetTextI18n")
    private void setDetails() {
        try {
            final Dialog dialog = new Dialog(requireActivity(), R.style.AlertDialogTheme1);//, R.style.AlertDialogTheme
            dialog.setContentView(R.layout.layout_details);
            dialog.getWindow().setLayout(-1, -2);

            TextView tvNameVal = dialog.findViewById(R.id.tvNameVal);
            TextView tvDurationVal = dialog.findViewById(R.id.tvDurationVal);
            TextView tvSizeVal = dialog.findViewById(R.id.tvSizeVal);
            TextView tvResolutionVal = dialog.findViewById(R.id.tvResolutionVal);
            TextView tvFormatVal = dialog.findViewById(R.id.tvFormatVal);
            TextView tvDateModified = dialog.findViewById(R.id.tvDateModified);
            TextView tvPathVal = dialog.findViewById(R.id.tvPathVal);
            Button btnOk = dialog.findViewById(R.id.btnOk);

            String songPath = ((ModelClassForVideo)videosArrayList.get(mPosition)).getSongPath();
            String format = songPath.substring(songPath.lastIndexOf(".") + 1, songPath.length());

            Log.e("TAG--->", songPath);

            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(songPath);
                int width = Integer.parseInt(Objects.requireNonNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)));
                int height = Integer.parseInt(Objects.requireNonNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)));
                retriever.release();
                tvResolutionVal.setText(width + " x " + height);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("TAG - DETAILS", e.getMessage() + "");
                tvResolutionVal.setText(0 + " x " + 0);
            }

            tvNameVal.setText(((ModelClassForVideo)videosArrayList.get(mPosition)).getSongName());
            tvDurationVal.setText(BaseActivity.convertDuration(((ModelClassForVideo)videosArrayList.get(mPosition)).getSongDuration()));
            tvSizeVal.setText(((BaseActivity) requireActivity()).readableFileSize(((ModelClassForVideo)videosArrayList.get(mPosition)).getSongSize()));
            tvDateModified.setText(((BaseActivity) requireActivity()).changeMillisToDate(((ModelClassForVideo)videosArrayList.get(mPosition)).getDateModified(), "yyyy-MM-dd"));
            tvPathVal.setText(songPath);
            tvFormatVal.setText(format);

            btnOk.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TAG", e.getMessage() + "");
        }
    }


    public void loadInterstitialAds(int position) {
        try {
            if (com.prolific.vidmediaplayer.Others.AdLoader.getAd().isLoaded()) {
                com.prolific.vidmediaplayer.Others.AdLoader.interstitialAd.show();
//                new AdLoader.LoadingAds(context).execute();
                com.prolific.vidmediaplayer.Others.AdLoader.getAd().setAdListener(new AdListener() {

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        Log.e("TAG", "FAIL AD LOAD : " + loadAdError);
                        initInterstitialAds();
                        loadLastPlay(position);
                    }

                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        initInterstitialAds();
                        loadLastPlay(position);
                    }
                });
            } else {
                initInterstitialAds();
                loadLastPlay(position);
            }
        } catch (Exception e) {
            e.printStackTrace();
            loadLastPlay(position);
        }
    }


    public void loadLastPlay(int position){
        latestVideoData = ((MainActivity) requireActivity()).getLatestVideo(requireActivity(), BaseActivity.PREF_LATEST_PLAY);
        if (((ModelClassForVideo)videosArrayList.get(position)).getTypeVal() == 0) {
            int pos = 0;
            for (int j = 0; j < videoListWOLastPlay.size(); j++) {
                if (latestVideoData.getSongPath().equals(((ModelClassForVideo)videoListWOLastPlay.get(j)).getSongPath())) {
                    pos = j;
                    break;
                }
            }
            VideoPlayerActivity.mWhereFrom = 1;
            ((MainActivity) requireActivity()).playVideo(getActivity(), pos, videoListWOLastPlay, latestVideoData, "latestPlay", "", "");
        } else {
            for (int j = 0; j < videoListWOLastPlay.size(); j++) {
                if (((ModelClassForVideo)videosArrayList.get(position)).getSongPath().equals(((ModelClassForVideo)videoListWOLastPlay.get(j)).getSongPath())) {
                    mPosition = j;
                    break;
                }
            }
            VideoPlayerActivity.mWhereFrom = 1;
            ((MainActivity) requireActivity()).playVideo(getActivity(), mPosition, videoListWOLastPlay, ((ModelClassForVideo)videoListWOLastPlay.get(mPosition)), "", "", "");
        }
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int position;
        switch (v.getId()) {
            case R.id.cvMain:
                position = (int) v.getTag();
                loadInterstitialAds(position);
                break;


            case R.id.iv_more:
                View moreView = (View) v.getTag(R.string.view);
                position = (int) v.getTag(R.string.pos);

                PopupMenu popup = new PopupMenu(getContext(), moreView);
                popup.inflate(R.menu.menu_item_more);

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.actionPlay:

                                latestVideoData = ((MainActivity) requireActivity()).getLatestVideo(requireActivity(), BaseActivity.PREF_LATEST_PLAY);
                                if (((ModelClassForVideo)videosArrayList.get(mPosition)).getTypeVal() == 0) {
                                    for (int j = 0; j < videoListWOLastPlay.size(); j++) {
                                        if (latestVideoData.getSongPath().equals(((ModelClassForVideo)videoListWOLastPlay.get(j)).getSongPath())) {
                                            mPosition = j;
                                            break;
                                        }
                                    }
                                    VideoPlayerActivity.mWhereFrom = 1;
                                    ((MainActivity) requireActivity()).playVideo(getActivity(), mPosition, videoListWOLastPlay, latestVideoData, "latestPlay", "", "");
                                } else {
                                    for (int j = 0; j < videoListWOLastPlay.size(); j++) {
                                        if (((ModelClassForVideo)videosArrayList.get(mPosition)).getSongPath().equals(((ModelClassForVideo)videoListWOLastPlay.get(j)).getSongPath())) {
                                            mPosition = j;
                                            break;
                                        }
                                    }
                                    VideoPlayerActivity.mWhereFrom = 1;
                                    ((MainActivity) requireActivity()).playVideo(getActivity(), mPosition, videoListWOLastPlay, (ModelClassForVideo)videoListWOLastPlay.get(mPosition), "", "", "");
                                }

                                break;

                            case R.id.actionRename:
                                mPosition = position;
                                renameFile();
                                break;

                            case R.id.actionDelete:
                                mPosition = position;
                                deleteFile();
                                break;

                            case R.id.actionShare:
                                mPosition = position;
                                shareFile();
                                break;

                            case R.id.actionDetails:
                                mPosition = position;
                                setDetails();
                                break;

                            default:
                                break;
                        }
                        return true;
                    }
                });
                popup.show();
                break;

            case R.id.actionLastPlay:
                Toast.makeText(getActivity(), "Last Play Clicked", Toast.LENGTH_SHORT).show();
                break;
        }
    }


    public void onPrepareOptionsMenu(Menu menu) {

        int customFontId = R.font.worksans_regular;
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            String menuTitle = menuItem.getTitle().toString();
            Typeface typeface = ResourcesCompat.getFont(requireActivity(), customFontId);
            SpannableString spannableString = new SpannableString(menuTitle);
// For demonstration purposes only, if you need to support < API 28 just use the CustomTypefaceSpan class only.
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                TypefaceSpan typefaceSpan = typeface != null ? new TypefaceSpan(typeface) : new TypefaceSpan("sans-serif");
                spannableString.setSpan(typefaceSpan,
                        0,
                        menuTitle.length(),
                        Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            } else {
                CustomTypefaceSpan customTypefaceSpan = typeface != null ? new CustomTypefaceSpan(typeface) : new CustomTypefaceSpan(Typeface.defaultFromStyle(Typeface.NORMAL));
                spannableString.setSpan(customTypefaceSpan,
                        0,
                        menuTitle.length(),
                        Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            }
            menuItem.setTitle(spannableString);
        }
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_top_more, menu);//menu_top_more //custom_top_option
        menu.findItem(R.id.actionRemoveAll).setVisible(false);
        menu.findItem(R.id.actionViewAs).setVisible(false);
        menu.findItem(R.id.actionSortBy).getSubMenu().findItem(R.id.actionDuration).setVisible(true);
        menu.findItem(R.id.actionSortBy).getSubMenu().findItem(R.id.actionReverseAll).setVisible(false);
        if (videoListWOLastPlay != null && videoListWOLastPlay.size() > 0) {
            menu.findItem(R.id.actionSelect).setVisible(true);
        } else {
            menu.findItem(R.id.actionSelect).setVisible(false);
        }
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionLastPlay:
                int pos = 0;
                latestVideoData = ((MainActivity) requireActivity()).getLatestVideo(requireActivity(), BaseActivity.PREF_LATEST_PLAY);
                if (latestVideoData != null) {
                    for (int i = 0; i < videoListWOLastPlay.size(); i++) {
                        if (latestVideoData.getSongPath().equalsIgnoreCase(((ModelClassForVideo)videoListWOLastPlay.get(i)).getSongPath())) {
                            pos = i;
                            break;
                        }
                    }
                    VideoPlayerActivity.mWhereFrom = 1;
                    ((MainActivity) requireActivity()).playVideo(getActivity(), pos, videoListWOLastPlay, latestVideoData, "latestPlay", "", "");
                } else {
                    Toast.makeText(requireActivity(), getResources().getString(R.string.last_play_error), Toast.LENGTH_LONG).show();
                }
                MainActivity.currTab = 0;
                break;

            case R.id.actionSelect:
                MainActivity.currTab = 0;
                if (videosArrayList != null && videosArrayList.size() > 0) {
                    startActivity(new Intent(getActivity(), SelectionActivity.class));
                } else {
                    Toast.makeText(requireActivity(), getResources().getString(R.string.lbl_select_not_found), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.actionList:
                ((MainActivity) requireActivity()).setIntPrefs(requireActivity(), ((MainActivity) requireActivity()).PREF_VIEW_AS, 0);
                MainActivity.currTab = 0;
//                ((BaseActivity) requireActivity()).notifyAllApp();
                EventBus.getDefault().post(new MessageEvent("RefreshVideo", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshFolder", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshRecent", ""));
                break;

            case R.id.actionGrid:
                ((MainActivity) requireActivity()).setIntPrefs(requireActivity(), ((MainActivity) requireActivity()).PREF_VIEW_AS, 1);
                MainActivity.currTab = 0;
//                ((BaseActivity) requireActivity()).notifyAllApp();
                EventBus.getDefault().post(new MessageEvent("RefreshVideo", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshFolder", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshRecent", ""));
                break;

            case R.id.actionFullTitle:
                ((MainActivity) requireActivity()).setIntPrefs(requireActivity(), ((MainActivity) requireActivity()).PREF_VIEW_AS, 2);
                MainActivity.currTab = 0;
//                ((BaseActivity) requireActivity()).notifyAllApp();
                EventBus.getDefault().post(new MessageEvent("RefreshVideo", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshFolder", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshRecent", ""));
                break;


            case R.id.actionName:
                ((MainActivity) requireActivity()).setIntPrefs(requireActivity(), ((MainActivity) requireActivity()).PREF_SORT_BY, 0);
                MainActivity.currTab = 0;
//                ((BaseActivity) requireActivity()).notifyAllApp();
                EventBus.getDefault().post(new MessageEvent("RefreshVideo", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshFolder", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshRecent", ""));
                break;

            case R.id.actionSize:
                ((MainActivity) requireActivity()).setIntPrefs(requireActivity(), ((MainActivity) requireActivity()).PREF_SORT_BY, 1);
                MainActivity.currTab = 0;
//                ((BaseActivity) requireActivity()).notifyAllApp();
                EventBus.getDefault().post(new MessageEvent("RefreshVideo", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshFolder", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshRecent", ""));
                break;

            case R.id.actionDuration:
                ((MainActivity) requireActivity()).setIntPrefs(requireActivity(), ((MainActivity) requireActivity()).PREF_SORT_BY, 2);
                MainActivity.currTab = 0;
//                ((BaseActivity) requireActivity()).notifyAllApp();
                EventBus.getDefault().post(new MessageEvent("RefreshVideo", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshFolder", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshRecent", ""));
                break;

//            case R.id.actionDate:
//                ((MainActivity) requireActivity()).setIntPrefs(requireActivity(), ((MainActivity) requireActivity()).PREF_SORT_BY, 3);
//                MainActivity.currTab = 0;
////                ((BaseActivity) requireActivity()).notifyAllApp();
//                EventBus.getDefault().post(new MessageEvent("RefreshVideo", ""));
//                EventBus.getDefault().post(new MessageEvent("RefreshFolder", ""));
//                EventBus.getDefault().post(new MessageEvent("RefreshRecent", ""));
//                break;

            case R.id.actionNewToOld:
                ((BaseActivity) requireActivity()).setIntPrefs(requireActivity(), ((BaseActivity) requireActivity()).PREF_SORT_BY, 3);
                MainActivity.currTab = 1;
//                ((BaseActivity)requireActivity()).notifyAllApp();
                EventBus.getDefault().post(new MessageEvent("RefreshVideo", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshFolder", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshRecent", ""));
                break;

            case R.id.actionOldToNew:
                ((BaseActivity) requireActivity()).setIntPrefs(requireActivity(), ((BaseActivity) requireActivity()).PREF_SORT_BY, 4);
                MainActivity.currTab = 1;
//                ((BaseActivity)requireActivity()).notifyAllApp();
                EventBus.getDefault().post(new MessageEvent("RefreshVideo", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshFolder", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshRecent", ""));
                break;

            case R.id.actionReverseAll:
                ((MainActivity) requireActivity()).setIntPrefs(requireActivity(), ((MainActivity) requireActivity()).PREF_SORT_BY, 5);
                MainActivity.currTab = 0;

                if (!isAlreadyReverse) {
//                    Collections.reverse(videosUriList);
//                    Collections.reverse(videosArrayList);
//                    Collections.reverse(videoListWOLastPlay);
//                    mAdapter.notifyDataSetChanged();
                    isAlreadyReverse = true;
                } else {
                    isAlreadyReverse = false;
//                    ((BaseActivity) requireActivity()).notifyAllApp();
                    EventBus.getDefault().post(new MessageEvent("RefreshVideo", ""));
                    EventBus.getDefault().post(new MessageEvent("RefreshFolder", ""));
                    EventBus.getDefault().post(new MessageEvent("RefreshRecent", ""));
                }
                break;


            case R.id.actionRefresh:
                MainActivity.currTab = 0;
//                ((MainActivity) getActivity()).restartApp();
//                ((BaseActivity) requireActivity()).notifyAllApp();
                EventBus.getDefault().post(new MessageEvent("RefreshVideo", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshFolder", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshRecent", ""));
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        Toast.makeText(getActivity(), getResources().getString(R.string.msg_refresh), Toast.LENGTH_SHORT).show();
                    }
                }, 100);
                break;

            case R.id.actionSetting:
                startActivity(new Intent(getActivity(), SettingActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
