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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prolific.vidmediaplayer.Activities.BaseActivity;
import com.prolific.vidmediaplayer.Activities.InsideVideos;
import com.prolific.vidmediaplayer.Activities.MainActivity;
import com.prolific.vidmediaplayer.Activities.SettingActivity;
import com.prolific.vidmediaplayer.Activities.VideoPlayerActivity;
import com.prolific.vidmediaplayer.Adapter.FolderAdapter;
import com.prolific.vidmediaplayer.Models.MessageEvent;
import com.prolific.vidmediaplayer.Models.ModelClassForVideo;
import com.prolific.vidmediaplayer.Others.CustomTypefaceSpan;
import com.prolific.vidmediaplayer.Others.GetAllFolders;
import com.prolific.vidmediaplayer.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FoldersFrag extends Fragment implements View.OnClickListener {
    public String mBucketOne, mBucketId;
    public ArrayList<Object> folderArrayList;
    private String fileOriginalName;
    private FolderAdapter mAdapter;
    public RecyclerView.LayoutManager mLayoutManager;
    private int mPosition = 0;
    public RecyclerView folderRecycler;
    private ArrayList<String> arrBucketId;
    private LinearLayout llEmpty;
    private VideosFrag vidFrag;
    private int sortVal;
    private String str, dirName;
    private Handler handler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view = layoutInflater.inflate(R.layout.fragment_folders, viewGroup, false);

        return view;
    }

    public void initView(View view) {
        vidFrag = new VideosFrag();

        this.folderRecycler = (RecyclerView) view.findViewById(R.id.folderRecycler);
        this.llEmpty = (LinearLayout) view.findViewById(R.id.llEmpty);

        folderArrayList = new ArrayList<>();
        arrBucketId = new ArrayList<>();

//        int viewTypeVal = ((BaseActivity) requireActivity()).getIntPrefs(requireActivity(), ((BaseActivity) requireActivity()).PREF_VIEW_AS);
//        if (viewTypeVal == 0) {
//            mLayoutManager = new LinearLayoutManager(getContext());
//        } else if (viewTypeVal == 1) {
            mLayoutManager = new GridLayoutManager(getContext(), 2);
//        } else {
//            mLayoutManager = new LinearLayoutManager(getContext());
//        }
        this.folderRecycler.setLayoutManager(this.mLayoutManager);
        this.folderRecycler.setHasFixedSize(true);

        mAdapter = new FolderAdapter(getContext(), folderArrayList, this);
        folderRecycler.setAdapter(mAdapter);

//        setAdapter();

        new GetAllFolders1().execute();

        mAdapter.setOnItemClickListener(new FolderAdapter.OnItemClickListener() {
            public void onItemClick(int i) {
                mBucketOne = ((ModelClassForVideo)folderArrayList.get(i)).getSongAlbum();
                mBucketId = ((ModelClassForVideo)folderArrayList.get(i)).getVidBucketId();
                Intent intent = new Intent(requireActivity(), InsideVideos.class);
                intent.putExtra("dirName", mBucketOne);
                intent.putExtra("dirId", mBucketId);
                MainActivity.mCurrentlyWhere = 2;
                FoldersFrag.this.startActivity(intent);
            }
        });
    }

    public void setAdapter() {
        fetchFolders();
        if (folderArrayList != null && folderArrayList.size() > 0) {
            folderRecycler.setVisibility(View.VISIBLE);
            llEmpty.setVisibility(View.GONE);
        } else {
            folderRecycler.setVisibility(View.GONE);
            llEmpty.setVisibility(View.VISIBLE);
        }
    }

    public class GetAllFolders1 extends AsyncTask<Void, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e("Tag", "---------[ OnPreExecute FOLDER]-----");
        }

        @Override
        protected String doInBackground(Void... voids) {
            requireActivity().runOnUiThread(new Runnable() {
                public void run() {
                    fetchFolders();
                }
            });
            return "";
        }

        @Override
        protected void onPostExecute(String aVoid) {
            super.onPostExecute(aVoid);
            Log.e("Tag", "---------[ OnPostExecute FOLDER]-----");
            if (folderArrayList != null && folderArrayList.size() > 0) {
                folderRecycler.setVisibility(View.VISIBLE);
                llEmpty.setVisibility(View.GONE);
                mAdapter.notifyDataSetChanged();
            } else {
                folderRecycler.setVisibility(View.GONE);
                llEmpty.setVisibility(View.VISIBLE);
            }
        }
    }



    public void fetchFolders1() {

        sortVal = ((BaseActivity) requireActivity()).getIntPrefs(getActivity(), ((BaseActivity) requireActivity()).PREF_SORT_BY);
        if (sortVal == 0) {
            str = "bucket_display_name ASC";
        } else if (sortVal == 1) {
            str = "_size ASC";
        } else if (sortVal == 2) {
            str = "duration ASC";
        } else if (sortVal == 3) {
            str = "date_modified DESC";
        } else if (sortVal == 4) {
            str = "date_modified ASC";
        }

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                if (message.what == 100) {
                    folderArrayList.clear();
                    folderArrayList.addAll((ArrayList<Object>) message.obj);

                    if (folderArrayList != null && folderArrayList.size() > 0) {
                        folderRecycler.setVisibility(View.VISIBLE);
                        llEmpty.setVisibility(View.GONE);

                        mAdapter = new FolderAdapter(getContext(), folderArrayList, FoldersFrag.this);
                        folderRecycler.setAdapter(mAdapter);
                        mAdapter.setOnItemClickListener(new FolderAdapter.OnItemClickListener() {
                            public void onItemClick(int i) {
                                mBucketOne = ((ModelClassForVideo)folderArrayList.get(i)).getSongAlbum();
                                Intent intent = new Intent(requireActivity(), InsideVideos.class);
                                intent.putExtra("dirName", mBucketOne);
                                MainActivity.mCurrentlyWhere = 2;
                                FoldersFrag.this.startActivity(intent);
                            }
                        });
                        mAdapter.notifyDataSetChanged();
                    } else {
                        folderRecycler.setVisibility(View.GONE);
                        llEmpty.setVisibility(View.VISIBLE);
                    }
                }
                return false;
            }
        });
        new GetAllFolders(requireActivity(), handler, folderArrayList).execute(((BaseActivity) requireActivity()).getStoragePath(requireActivity()));
    }


    public void fetchFolders() {
        folderArrayList.clear();
        arrBucketId.clear();

        sortVal = ((BaseActivity) requireActivity()).getIntPrefs(requireActivity(), ((BaseActivity) requireActivity()).PREF_SORT_BY);
        if (sortVal == 0) {
            str = "bucket_display_name ASC";
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
        String[] strArr = {"_data", "title", "bucket_id", "bucket_display_name", "duration", "_size", "date_added", "date_modified"};
        Cursor query = requireActivity().getContentResolver().query(uri, strArr, null, null, str);//"datetaken"
        if (query != null && query.getCount() > 0) {
            while (query.moveToNext()) {
                String vidPath = query.getString(query.getColumnIndexOrThrow("_data"));
                String vidBucketName = query.getString(query.getColumnIndexOrThrow("bucket_display_name"));
                if (vidBucketName == null) {
                    File f = new File(vidPath);
                    dirName = f.getParentFile().getName();
                } else {
                    dirName = vidBucketName;
                }
                String vidBucketId = query.getString(query.getColumnIndexOrThrow("bucket_id"));
                if (!arrBucketId.contains(vidBucketId)) {
                    ModelClassForVideo modelClassForVideo = new ModelClassForVideo();
                    modelClassForVideo.setBoolean_selected(false);
                    modelClassForVideo.setVidBucketId(vidBucketId);
                    modelClassForVideo.setSongPath(vidPath);
                    modelClassForVideo.setSongAlbum(dirName);
                    modelClassForVideo.setSongName(query.getString(query.getColumnIndexOrThrow("title")));
                    modelClassForVideo.setSongDuration(query.getInt(query.getColumnIndexOrThrow("duration")));
                    modelClassForVideo.setSongSize(query.getInt(query.getColumnIndexOrThrow("_size")));
                    modelClassForVideo.setDateAdded(query.getLong(query.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)));
                    modelClassForVideo.setDateModified(query.getLong(query.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)));
                    modelClassForVideo.setVidCount(getVideosAmmount(vidBucketId, str));
                    folderArrayList.add(modelClassForVideo);
                    arrBucketId.add(vidBucketId);

                    Log.e("TAG---FOLDER", vidPath + " ---- Count : " + folderArrayList.size());

                }
            }
            if (sortVal == 2) {
                Collections.sort(folderArrayList, new Comparator<Object>() {
                    @Override
                    public int compare(Object lhs, Object rhs) {
                        return Integer.valueOf(((ModelClassForVideo)lhs).getVidCount()).compareTo(((ModelClassForVideo)rhs).getVidCount());
                    }
                });
            } else if (sortVal == 5) {
                Collections.reverse(folderArrayList);
                Collections.reverse(arrBucketId);
            }
        }
        query.close();
    }

    private int getVideosAmmount(String bucketId, String sortVal) {
        int i = 0;
        String[] strArr = {bucketId};
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Cursor query = requireActivity().getContentResolver().query(uri, new String[]{"_data"}, "bucket_id like?", strArr, sortVal);
        if (query != null) {
            while (query.moveToNext()) {
                i++;
            }
        }
        return i;
    }

    private File fDir;
    private Dialog dDeleteDir;
    private void deleteFile() {

        String path = ((ModelClassForVideo)folderArrayList.get(mPosition)).getSongPath();

        dDeleteDir = new Dialog(requireActivity());
        dDeleteDir.setContentView(R.layout.layout_delete);
        dDeleteDir.getWindow().setLayout(-1, -2);

        Button btnCancel = (Button) dDeleteDir.findViewById(R.id.btnCancel);
        Button btnDelete = (Button) dDeleteDir.findViewById(R.id.btnDelete);
        TextView tvDeleteMsg = (TextView) dDeleteDir.findViewById(R.id.tvDeleteMsg);

        tvDeleteMsg.setText(getString(R.string.delete_msg) + " video folder, it will delete all videos from the folder?");

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dDeleteDir.dismiss();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("TAG", path);
                File f = new File(path);
                fDir = f.getParentFile();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    ArrayList<Uri> list = new ArrayList<>();
                    if (fDir.isDirectory()) {
                        String[] children = fDir.list();
                        for (int j = 0; j < children.length; j++) {
                            if (children[j].endsWith(".mp4") ||
                                    children[j].endsWith(".3gp") ||
                                    children[j].endsWith(".mpeg") ||
                                    children[j].endsWith(".avi") ||
                                    children[j].endsWith(".webm") ||
                                    children[j].endsWith(".mkv")) {
                                File file = new File(fDir, children[j]);
                                if (!file.exists()) {
                                    return;
                                }
//                                        pos = j;
                                File vidFile = new File(file.getAbsolutePath());
                                if (vidFile.exists()) {
                                    list.add(((BaseActivity) requireActivity()).getUriFromPath(vidFile.getAbsolutePath(), requireActivity()));
                                }
                            }
                        }

                    }
                    PendingIntent intent = MediaStore.createDeleteRequest(requireActivity().getContentResolver(), list);
                    try {
                        startIntentSenderForResult(intent.getIntentSender(), 10000, null, 0, 0, 0, null);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }

                } else {

                    if (fDir.exists()) {
                        if (!fDir.getParentFile().exists()) {
                            return;
                        } else {
                            if (fDir.isDirectory()) {
                                String[] children = fDir.list();
                                for (int j = 0; j < children.length; j++) {
                                    if (children[j].endsWith(".mp4") ||
                                            children[j].endsWith(".3gp") ||
                                            children[j].endsWith(".mpeg") ||
                                            children[j].endsWith(".avi") ||
                                            children[j].endsWith(".webm") ||
                                            children[j].endsWith(".mkv")) {
                                        File file = new File(fDir, children[j]);
                                        if (!file.exists()) {
                                            return;
                                        }
                                        file.delete();
                                        requireActivity().getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA + "=?", new String[]{file.getAbsolutePath()});
                                        requireActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file)));
                                    }
                                }
                                deleteRecordAndNotify();
                                dDeleteDir.dismiss();
                            }
                        }

                    } else {
                        Toast.makeText(requireActivity(), getResources().getString(R.string.delete_directory_not_exist), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

//        new MaterialAlertDialogBuilder(requireActivity())//, R.style.AlertDialogTheme
//                .setTitle(getResources().getString(R.string.lbl_delete))
//                .setMessage(getResources().getString(R.string.msg_delet_folder))
//                .setPositiveButton(getResources().getString(R.string.lbl_ok), new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        Log.e("TAG", path);
//                        File f = new File(path);
//                        fDir = f.getParentFile();
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                            ArrayList<Uri> list = new ArrayList<>();
//                            if (fDir.isDirectory()) {
//                                String[] children = fDir.list();
//                                for (int j = 0; j < children.length; j++) {
//                                    if (children[j].endsWith(".mp4") ||
//                                            children[j].endsWith(".3gp") ||
//                                            children[j].endsWith(".mpeg") ||
//                                            children[j].endsWith(".avi") ||
//                                            children[j].endsWith(".webm") ||
//                                            children[j].endsWith(".mkv")) {
//                                        File file = new File(fDir, children[j]);
//                                        if (!file.exists()) {
//                                            return;
//                                        }
////                                        pos = j;
//                                        File vidFile = new File(file.getAbsolutePath());
//                                        if (vidFile.exists()) {
//                                            list.add(((BaseActivity) requireActivity()).getUriFromPath(vidFile.getAbsolutePath(), requireActivity()));
//                                        }
//                                    }
//                                }
//
//                            }
//                            PendingIntent intent = MediaStore.createDeleteRequest(requireActivity().getContentResolver(), list);
//                            try {
//                                startIntentSenderForResult(intent.getIntentSender(), 10000, null, 0, 0, 0, null);
//                            } catch (IntentSender.SendIntentException e) {
//                                e.printStackTrace();
//                            }
//
//                        } else {
//
//                            if (fDir.exists()) {
//                                if (!fDir.getParentFile().exists()) {
//                                    return;
//                                } else {
//                                    if (fDir.isDirectory()) {
//                                        String[] children = fDir.list();
//                                        for (int j = 0; j < children.length; j++) {
//                                            if (children[j].endsWith(".mp4") ||
//                                                    children[j].endsWith(".3gp") ||
//                                                    children[j].endsWith(".mpeg") ||
//                                                    children[j].endsWith(".avi") ||
//                                                    children[j].endsWith(".webm") ||
//                                                    children[j].endsWith(".mkv")) {
//                                                File file = new File(fDir, children[j]);
//                                                if (!file.exists()) {
//                                                    return;
//                                                }
//                                                file.delete();
//                                                requireActivity().getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA + "=?", new String[]{file.getAbsolutePath()});
//                                                requireActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file)));
//                                            }
//                                        }
//                                        deleteRecordAndNotify();
//                                    }
//                                }
//
//                            } else {
//                                Toast.makeText(requireActivity(), getResources().getString(R.string.delete_directory_not_exist), Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    }
//                })
//                .setNegativeButton(getResources().getString(R.string.lbl_cancel), new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//                    }
//                })
        dDeleteDir.show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Log.e("TAG", "ResultCode = " + resultCode);

        if (resultCode == Activity.RESULT_OK) {
//            if (requestCode == 20000) {//Rename in android 11 only
//                if (dRename != null && dRename.isShowing()) {
//                    dRename.dismiss();
//                }
//                updateTag2(requireActivity());
//            } else
            if (requestCode == 10000) {//Delete in android 11 only
                if (dDeleteDir != null && dDeleteDir.isShowing()) {
                    dDeleteDir.dismiss();
                }
                deleteRecordAndNotify();
            }
        } else {
            Toast.makeText(requireActivity(), "error", Toast.LENGTH_SHORT).show();
        }
    }


    public void deleteRecordAndNotify() {

        File[] childs = fDir.listFiles();
        BaseActivity.arrRecentList = BaseActivity.getRecentVideoList(requireActivity(), BaseActivity.PREF_RECENT_VIDEO_LIST);
        for (int a = 0; a < childs.length; a++) {
            for (int j = 0; j < BaseActivity.arrRecentList.size(); j++) {
                if (((ModelClassForVideo)BaseActivity.arrRecentList.get(j)).getSongPath().equals(childs[a].getAbsolutePath())) {
                    BaseActivity.arrRecentList.remove(j);
                    BaseActivity.setRecentVideoList(requireActivity(), BaseActivity.PREF_RECENT_VIDEO_LIST, BaseActivity.arrRecentList);
                }
            }
        }

        ModelClassForVideo latestData = ((BaseActivity) requireActivity()).getLatestVideo(requireActivity(), BaseActivity.PREF_LATEST_PLAY);
        if (latestData != null) {
            if (latestData.getSongPath().equals(((ModelClassForVideo)folderArrayList.get(mPosition)).getSongPath())) {
                ((BaseActivity) requireActivity()).removePrefsByKey(requireActivity(), BaseActivity.PREF_LATEST_PLAY);
            }
        }

        folderArrayList.remove(mPosition);
        if (folderArrayList.size() > 0) {
            folderRecycler.setVisibility(View.VISIBLE);
            llEmpty.setVisibility(View.GONE);
        } else {
            folderRecycler.setVisibility(View.GONE);
            llEmpty.setVisibility(View.VISIBLE);
        }
        mAdapter.notifyItemRemoved(mPosition);
        Toast.makeText(FoldersFrag.this.getContext(), getResources().getString(R.string.delete_directory_success), Toast.LENGTH_SHORT).show();
        ((BaseActivity)requireActivity()).notifyExceptFolder();

    }


    private void shareFile(int i) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SEND");
        intent.putExtra("android.intent.extra.STREAM", Uri.parse(((ModelClassForVideo)folderArrayList.get(i)).getSongPath()));
        intent.setType("video/*");
        startActivity(Intent.createChooser(intent, getResources().getString(R.string.lbl_share_using)));
    }


    private Dialog dRename;

    private void renameFile() {

        String path = ((ModelClassForVideo)folderArrayList.get(mPosition)).getSongPath();

        dRename = new Dialog(requireActivity());
        dRename.setContentView(R.layout.layout_rename);
        dRename.getWindow().setLayout(-1, -2);

        Button cancel = (Button) dRename.findViewById(R.id.btnCancel);
        Button btnRename = (Button) dRename.findViewById(R.id.btnRename);
        EditText editText = (EditText) dRename.findViewById(R.id.etRename);
        editText.setHint("Please enter folder name");
        File oldFile = new File(path);
        String filename = oldFile.getPath().substring(oldFile.getPath().lastIndexOf("/") + 1, oldFile.getPath().length());
        String name = oldFile.getParentFile().getName();
        Log.e("TAG", name);
        fileOriginalName = name;
        editText.setText(fileOriginalName);
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
            @SuppressLint("NewApi")
            public void onClick(View view) {

                File dirFile = oldFile.getParentFile();
                String dirPath = dirFile.getAbsolutePath();
                String dirName = dirFile.getName();

                File dirParent = new File(dirPath);
                File file = new File(dirParent.getParentFile().getAbsolutePath(), editText.getText().toString());// + substring // + "/" +
                if (dirParent.renameTo(file)) {
                    ContentValues values = new ContentValues();
                    values.put("bucket_display_name", file.getAbsolutePath());
                    requireActivity().getContentResolver().update(MediaStore.Files.getContentUri("external"), values, "bucket_id=?", new String[]{((ModelClassForVideo)folderArrayList.get(mPosition)).getVidBucketId()});
                    requireActivity().sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(file)));

                    ModelClassForVideo modelClassForVideo = new ModelClassForVideo();
                    modelClassForVideo.setBoolean_selected(false);
                    modelClassForVideo.setSongPath(file.getAbsolutePath() + File.separator + filename);
                    modelClassForVideo.setSongThumb(file.getAbsolutePath() + File.separator + filename);
                    modelClassForVideo.setSongAlbum(editText.getText().toString());
                    modelClassForVideo.setSongDuration(((ModelClassForVideo)folderArrayList.get(mPosition)).getSongDuration());
                    modelClassForVideo.setSongSize(((ModelClassForVideo)folderArrayList.get(mPosition)).getSongSize());
                    modelClassForVideo.setDateAdded(((ModelClassForVideo)folderArrayList.get(mPosition)).getDateAdded());
                    modelClassForVideo.setDateModified(((ModelClassForVideo)folderArrayList.get(mPosition)).getDateModified());
                    modelClassForVideo.setVidCount(((ModelClassForVideo)folderArrayList.get(mPosition)).getVidCount());
                    folderArrayList.set(mPosition, modelClassForVideo);


                    mAdapter.notifyDataSetChanged();
                    Toast.makeText(requireActivity(), getResources().getString(R.string.rename_success), Toast.LENGTH_SHORT).show();

                    ((BaseActivity)requireActivity()).notifyExceptFolder();
                    dRename.dismiss();
                } else {
                    Toast.makeText(requireActivity(), getResources().getString(R.string.rename_fail), Toast.LENGTH_SHORT).show();
                    dRename.dismiss();
                }
            }
        });
        dRename.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dRename.show();
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_more_folder:
                View moreView = (View) v.getTag(R.string.view);
                int pos = (int) v.getTag(R.string.pos);

                PopupMenu popup = new PopupMenu(getContext(), moreView);
                popup.inflate(R.menu.menu_item_more);
                popup.getMenu().findItem(R.id.actionRemove).setVisible(false);
                popup.getMenu().findItem(R.id.actionShare).setVisible(false);
                popup.getMenu().findItem(R.id.actionDetails).setVisible(false);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.actionPlay:
                                mPosition = pos;
                                VideoPlayerActivity.mWhereFrom = 2;
                                ((BaseActivity) requireActivity()).playVideo(getActivity(), pos, folderArrayList, (ModelClassForVideo)folderArrayList.get(pos), "folder", ((ModelClassForVideo)folderArrayList.get(pos)).getSongAlbum(), ((ModelClassForVideo)folderArrayList.get(pos)).getVidBucketId());
                                break;

                            case R.id.actionRename:
                                mPosition = pos;
                                renameFile();
                                break;

                            case R.id.actionDelete:
                                mPosition = pos;
                                deleteFile();
                                break;

                            default:
                                break;
                        }
                        return true;
                    }
                });
                popup.show();
                break;
        }
    }

    public void filterRecords(String searchText){
        ArrayList<Object> arr = new ArrayList<>();
        for (int i = 0; i < folderArrayList.size(); i++) {
            if(((ModelClassForVideo)folderArrayList.get(i)).getSongAlbum().contains(searchText)){
                arr.add(folderArrayList.get(i));
            }
        }
        mAdapter.setData(arr);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        int customFontId = R.font.worksans_regular;
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            String menuTitle = menuItem.getTitle().toString();
            Typeface typeface = ResourcesCompat.getFont(requireActivity(), customFontId);
            SpannableString spannableString = new SpannableString(menuTitle);
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
        inflater.inflate(R.menu.menu_top_more, menu);

        menu.findItem(R.id.actionSelect).setVisible(false);
        menu.findItem(R.id.actionViewAs).setVisible(false);
        menu.findItem(R.id.actionRemoveAll).setVisible(false);
        menu.findItem(R.id.actionSortBy).getSubMenu().findItem(R.id.actionDuration).setVisible(false);
        menu.findItem(R.id.actionSortBy).getSubMenu().findItem(R.id.actionAmount).setVisible(true);
        menu.findItem(R.id.actionSortBy).getSubMenu().findItem(R.id.actionReverseAll).setVisible(false);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            /*case R.id.action_search:
                break;*/

            case R.id.actionLastPlay:
                int pos = 0;
                ModelClassForVideo latestVideoData = ((BaseActivity) requireActivity()).getLatestVideo(requireActivity(), BaseActivity.PREF_LATEST_PLAY);
                if (latestVideoData != null) {
                    for (int i = 0; i < vidFrag.videoListWOLastPlay.size(); i++) {
                        if (latestVideoData.getSongPath().equalsIgnoreCase(((ModelClassForVideo)vidFrag.videoListWOLastPlay.get(i)).getSongPath())) {
                            pos = i;
                            break;
                        }
                    }
                    VideoPlayerActivity.mWhereFrom = 1;
                    ((BaseActivity) requireActivity()).playVideo(getActivity(), pos, vidFrag.videoListWOLastPlay, latestVideoData, "latestPlay", "", "");
                } else {
                    Toast.makeText(requireActivity(), "There is no last played record found, please play from video list.", Toast.LENGTH_LONG).show();
                }
                MainActivity.currTab = 1;
                break;

            case R.id.actionName:
                ((BaseActivity) requireActivity()).setIntPrefs(requireActivity(), ((BaseActivity) requireActivity()).PREF_SORT_BY, 0);
                MainActivity.currTab = 1;
//                ((BaseActivity)requireActivity()).notifyAllApp();
                EventBus.getDefault().post(new MessageEvent("RefreshVideo", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshFolder", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshRecent", ""));
                break;

            case R.id.actionSize:
                ((BaseActivity) requireActivity()).setIntPrefs(requireActivity(), ((BaseActivity) requireActivity()).PREF_SORT_BY, 1);
                MainActivity.currTab = 1;
//                ((BaseActivity)requireActivity()).notifyAllApp();
                EventBus.getDefault().post(new MessageEvent("RefreshVideo", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshFolder", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshRecent", ""));
                break;

            case R.id.actionAmount:
                ((BaseActivity) requireActivity()).setIntPrefs(requireActivity(), ((BaseActivity) requireActivity()).PREF_SORT_BY, 2);
                MainActivity.currTab = 1;
//                ((BaseActivity)requireActivity()).notifyAllApp();
                EventBus.getDefault().post(new MessageEvent("RefreshVideo", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshFolder", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshRecent", ""));
                break;

//            case R.id.actionDate:
//                ((BaseActivity) requireActivity()).setIntPrefs(requireActivity(), ((BaseActivity) requireActivity()).PREF_SORT_BY, 3);
//                MainActivity.currTab = 1;
////                ((BaseActivity)requireActivity()).notifyAllApp();
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
                ((BaseActivity) requireActivity()).setIntPrefs(requireActivity(), ((BaseActivity) requireActivity()).PREF_SORT_BY, 5);
                MainActivity.currTab = 1;
//                ((BaseActivity)requireActivity()).notifyAllApp();
                EventBus.getDefault().post(new MessageEvent("RefreshVideo", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshFolder", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshRecent", ""));
                break;

            case R.id.actionRefresh:
                MainActivity.currTab = 1;
//                ((BaseActivity)requireActivity()).notifyAllApp();
                EventBus.getDefault().post(new MessageEvent("RefreshVideo", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshFolder", ""));
                EventBus.getDefault().post(new MessageEvent("RefreshRecent", ""));
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        Toast.makeText(getActivity(), "Refreshing", Toast.LENGTH_SHORT).show();
                    }
                }, 400);
                break;

            case R.id.actionSetting:
                startActivity(new Intent(getActivity(), SettingActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
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
        if (event.name.equals("RefreshFolder")) {
            fetchDataAndRefreshAdapter();
        }
    }


    public void fetchDataAndRefreshAdapter() {
        fetchFolders();
        if (folderArrayList != null && folderArrayList.size() > 0) {
            folderRecycler.setVisibility(View.VISIBLE);
            llEmpty.setVisibility(View.GONE);
            mAdapter.notifyDataSetChanged();
        } else {
            folderRecycler.setVisibility(View.GONE);
            llEmpty.setVisibility(View.VISIBLE);
        }
    }
}
