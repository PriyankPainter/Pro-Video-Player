package com.prolific.vidmediaplayer.Others;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.prolific.vidmediaplayer.Activities.BaseActivity;
import com.prolific.vidmediaplayer.Activities.MainActivity;
import com.prolific.vidmediaplayer.Models.ModelClassForVideo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class GetAllFolders extends AsyncTask<String, String, String> {

    private Handler handler;
    public String type = "", flag;
    @SuppressLint("StaticFieldLeak")
    private Activity c;
    private ArrayList<File> listFiles;
    private boolean boolVal;
    private String extension;
    private ArrayList<Object> arrFolder;
    private ArrayList<String> arrBucketName;

    public GetAllFolders(Activity c, Handler handler, ArrayList<Object> arrFolder) {
        this.c = c;
        this.handler = handler;
        this.arrFolder = arrFolder;
    }


    @Override
    protected void onPreExecute() {
        arrFolder = new ArrayList<>();
        arrBucketName = new ArrayList<>();
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            for (int i = 0; i < strings.length; i++) {
                getAllFileFolder(c, strings[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

   /* public ArrayList<File> getFilteredFiles(String dir, boolean isShowHiddenFile) {
        File file = new File(dir);
        File[] arrFileMain;
        ArrayList<File> arrDirs, arrFiles, arrAllFiles;
        arrFileMain = file.listFiles();
        arrDirs = new ArrayList<>();
        arrFiles = new ArrayList<>();

        if (arrFileMain != null && arrFileMain.length > 0) {
            for (int i = 0; i < arrFileMain.length; i++) {
                if (arrFileMain[i].isDirectory()) {
//                    if(isShowHiddenFile){
//                        arrDirs.add(arrFileMain[i]);
//                    }else{
//                        if(!arrFileMain[i].getName().startsWith(".")){
//                    File[] innerDir = arrFileMain[i].listFiles();
//                    if(innerDir != null && innerDir.length > 0){
//                        for (int j = 0; j < innerDir.length; j++) {
//
//                        }
//                        if(innerDir[i])
//                    }
                            arrDirs.add(arrFileMain[i]);
//                        }
//                    }
                }
//                else {
//                    if(isShowHiddenFile) {
//                        arrFiles.add(arrFileMain[i]);
//                    }else {
//                        if (!arrFileMain[i].getName().startsWith(".")) {
//                            arrFiles.add(arrFileMain[i]);
//                        }
//                    }
//                }
            }
        }

//        Collections.sort(arrDirs, OrderType.NAME.getComparator());
        if(arrDirs.size() > 0) {
            Collections.sort(arrDirs);
//            Collections.sort(arrDirs, new Comparator<File>() {
//                @Override
//                public int compare(File file, File t1) {
//                    return file.getName().toLowerCase().compareTo(t1.getName().toLowerCase());
//                }
//            });
        }

        if(arrFiles.size() > 0) {
            Collections.sort(arrFiles);
//            Collections.sort(arrFiles, new Comparator<File>() {
//                @Override
//                public int compare(File file, File t1) {
//                    return file.getName().toLowerCase().compareTo(t1.getName().toLowerCase());
//                }
//            });
        }
        arrAllFiles = new ArrayList<>(arrDirs);
        arrAllFiles.addAll(arrFiles);
        return arrAllFiles;
    }*/


    private void getAllFileFolder(Activity act, String string) {//=== /storage/emulated/0/
        try {
            File[] folderMain = new File(string).listFiles();
            if (folderMain != null && folderMain.length > 0) {
                for (File isImageMain : folderMain) {

                    if (isImageMain.isDirectory()) {
                        File[] isImages = isImageMain.listFiles();
                        if (isImages != null && isImages.length > 0) {
                            for (File isImage : isImages) {

                                if (isImage.isDirectory()) {
                                    if (!boolVal) {
                                        if (!isImage.getName().startsWith(".")) {
                                            File[] imageSub = isImage.listFiles();
                                            if (imageSub != null && imageSub.length > 0) {
                                                for (File isImageSub : imageSub) {

                                                    if (!isImageSub.isDirectory()) {
                                                        setData(act, isImageSub);
                                                    } else {
                                                        getAllFileFolder(act, isImage.toString());
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        File[] imageSub = isImage.listFiles();
                                        if (imageSub != null && imageSub.length > 0) {
                                            for (File isImageSub : imageSub) {

                                                if (!isImageSub.isDirectory()) {
                                                    setData(act, isImageSub);
                                                } else {
                                                    getAllFileFolder(act, isImageSub.toString());
                                                    break;
                                                }

                                            }
                                        }
                                    }
                                } else {
                                    setData(act, isImage);
                                }
                            }
                        }
                    } else {
                        setData(act, isImageMain);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setData(Activity act, File file) {
        extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString()).trim();
        if (extension.equalsIgnoreCase("mp4") ||
                extension.equalsIgnoreCase("3gp") ||
                extension.equalsIgnoreCase("mpeg") ||
                extension.equalsIgnoreCase("avi") ||
                extension.equalsIgnoreCase("webm") ||
                extension.equalsIgnoreCase("mkv")) {
            setLargeData(act, file);
        }
    }

    private int getVideosAmmount(String bucketName) {
        int i = 0;
        Log.e("TAG", bucketName);
        String[] strArr = {bucketName};
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            uri = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }
        Cursor query = c.getContentResolver().query(uri, new String[]{"_data", "title", "bucket_display_name", "_data"}, "bucket_display_name like?", strArr, null);//"bucket_display_name like?", strArr, sortVal
        if (query != null) {
            while (query.moveToNext()) {
                i++;
            }
        }
        return i;
    }

    private void setLargeData(Activity act, File file) {
        File dir = file.getParentFile();
        if (!arrBucketName.contains(dir.getName())) {
            ModelClassForVideo modelClassForVideo = new ModelClassForVideo();
            modelClassForVideo.setBoolean_selected(false);
//        modelClassForVideo.setVidBucketId(vidBucketId);
            modelClassForVideo.setSongName(file.getName());
            modelClassForVideo.setSongPath(file.getAbsolutePath());
            modelClassForVideo.setSongAlbum(dir.getName());
//        modelClassForVideo.setSongDuration(query.getInt(query.getColumnIndexOrThrow("duration")));
            modelClassForVideo.setSongSize(dir.length());
//        modelClassForVideo.setDateAdded(query.getLong(query.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)));
            modelClassForVideo.setDateModified(dir.lastModified());
            modelClassForVideo.setVidCount(getVideosAmmount(dir.getName()));
            arrFolder.add(modelClassForVideo);
            arrBucketName.add(file.getParentFile().getName());
        }
    }

    int sortVal;

    @Override
    protected void onPostExecute(String s) {
        try {
//            Log.e("TAG", "------------ON POST-------------");
            Message message = new Message();

            if (arrFolder.size() > 0) {
                sortVal = ((BaseActivity)c).getIntPrefs(c, ((BaseActivity)c).PREF_SORT_BY);
                if (sortVal == 0) {
                    Collections.sort(arrFolder, new Comparator<Object>() {
                        @Override
                        public int compare(Object file1, Object file2) {
                            return ((ModelClassForVideo) file1).getSongAlbum().compareTo(((ModelClassForVideo) file2).getSongAlbum());//lastModified()
                        }
                    });
                }else if(sortVal == 1){
                    Collections.sort(arrFolder, new Comparator<Object>() {
                        @Override
                        public int compare(Object file1, Object file2) {
                            return (int)(((ModelClassForVideo) file1).getSongSize()-((ModelClassForVideo) file2).getSongSize());
                        }
                    });
                } else if(sortVal == 2){
                    Collections.sort(arrFolder, new Comparator<Object>() {
                        @Override
                        public int compare(Object file1, Object file2) {
                            return (int)(((ModelClassForVideo) file1).getVidCount()-((ModelClassForVideo) file2).getSongSize());
                        }
                    });
                }else if(sortVal == 3){
                    Collections.sort(arrFolder, new Comparator<Object>() {
                        @Override
                        public int compare(Object file1, Object file2) {
                            return (int)(((ModelClassForVideo) file1).getDateModified()-((ModelClassForVideo) file2).getDateModified());
                        }
                    });
                } else if(sortVal == 4){
                    Collections.reverse(arrFolder);
                }


            }



            message.what = 100;
            message.obj = arrFolder;
            handler.sendMessage(message);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private Long getAlbumId(String path) {
        Cursor musicCursor;
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] mProjection = {MediaStore.Audio.Media.ALBUM_ID};
        String[] mArgs = {path};
        musicCursor = c.getContentResolver().query(uri, mProjection, MediaStore.Audio.Media.DATA + " = ?", mArgs, null);
        musicCursor.moveToFirst();
        Long albumId = musicCursor.getLong(musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
        return albumId;
    }

    private boolean checkExtension(File path) {
        if ((path.getName().endsWith(".png") || path.getName().endsWith(".jpeg") || path.getName().endsWith(".jpg") || path.getName().endsWith(".gif")))
            return true;
        return false;
    }
}