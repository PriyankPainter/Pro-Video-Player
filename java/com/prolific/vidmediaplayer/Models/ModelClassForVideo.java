package com.prolific.vidmediaplayer.Models;

import java.io.Serializable;

public class ModelClassForVideo implements Serializable {
    private boolean boolean_selected;
    private String songAlbum;
    private int songDuration, currDuration, vidCount, typeVal;
    private long songSize;
    private long dateAdded, dateModified, totalSize;
    private String songName;
    private String songPath;
    private String songThumb;
    private String songResolution;
    private String vidBucketId;
//    private int vidBucketId;

//    public int getVidBucketId() {
//        return vidBucketId;
//    }
//
//    public void setVidBucketId(int vidBucketId) {
//        this.vidBucketId = vidBucketId;
//    }


    public String getVidBucketId() {
        return vidBucketId;
    }

    public void setVidBucketId(String vidBucketId) {
        this.vidBucketId = vidBucketId;
    }

    public int getVidCount() {
        return vidCount;
    }

    public void setVidCount(int vidCount) {
        this.vidCount = vidCount;
    }

    public String getSongName() {
        return this.songName;
    }

    public void setSongName(String str) {
        this.songName = str;
    }

    public String getSongAlbum() {
        return this.songAlbum;
    }

    public void setSongAlbum(String str) {
        this.songAlbum = str;
    }

    public String getSongPath() {
        return this.songPath;
    }

    public void setSongPath(String str) {
        this.songPath = str;
    }

    public String getSongThumb() {
        return this.songThumb;
    }

    public void setSongThumb(String str) {
        this.songThumb = str;
    }

    public boolean isBoolean_selected() {
        return this.boolean_selected;
    }

    public void setBoolean_selected(boolean z) {
        this.boolean_selected = z;
    }

    public int getSongDuration() {
        return songDuration;
    }

    public void setSongDuration(int songDuration) {
        this.songDuration = songDuration;
    }

    public int getCurrDuration() {
        return currDuration;
    }

    public void setCurrDuration(int currDuration) {
        this.currDuration = currDuration;
    }

    public long getSongSize() {
        return songSize;
    }

    public void setSongSize(long songSize) {
        this.songSize = songSize;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    public long getDateModified() {
        return dateModified;
    }

    public void setDateModified(long dateModified) {
        this.dateModified = dateModified;
    }

    public String getSongResolution() {
        return songResolution;
    }

    public void setSongResolution(String songResoution) {
        this.songResolution = songResoution;
    }

    public int getTypeVal() {
        return typeVal;
    }

    public void setTypeVal(int typeVal) {
        this.typeVal = typeVal;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }
}
