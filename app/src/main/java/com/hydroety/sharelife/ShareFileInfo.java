package com.hydroety.sharelife;


import com.google.api.client.util.DateTime;

import android.graphics.Bitmap;
import android.text.format.DateFormat;

public class ShareFileInfo {
    private String mFileId;
    private String mPngFileId="";
    private DateTime mModifiedTime;
    private String mName;
    private String mIconLink="";
    private boolean mOwnedByMe=true;
    private Bitmap mThumbnail=null;

    public void setFileId(String fileId) {
        mFileId = fileId;
    }

    public String getFileId() {
        return mFileId;
    }

    public void setPngFileId(String fileId) {
        mPngFileId = fileId;
    }

    public String getPngFileId() {
        return mPngFileId;
    }

    public void setModifiedTime(DateTime ModifiedTime) {
        mModifiedTime = ModifiedTime;
    }

    public String getModifiedTime() {
        String time = (String) DateFormat.format("yyyy/MM/dd, E, kk:mm:ss",mModifiedTime.getValue());
        return time;
    }

    public void setName(String Name) {
        mName = Name;
    }

    public String getName() {
        return mName;
    }

    public void setIconLink(String IconLink) {
        mIconLink = IconLink;
    }

    public String getIconLink() {
        return mIconLink;
    }

    public void setOwnedByMe(boolean ownedByMe) {
        mOwnedByMe = ownedByMe;
    }

    public boolean IsOwnedByMe() {
        return mOwnedByMe;
    }

    public void setThumbnail(Bitmap bmp) {
        mThumbnail = bmp;
    }

    public Bitmap getThumbnail() {
        return mThumbnail;
    }

}
