package com.hydroety.sharelife;


import android.text.format.DateFormat;

import com.google.api.client.util.DateTime;

public class CommentInfo {
    private String mCommentId;
    private String mId;
    private DateTime mModifiedTime;
    private String mName;
    private boolean mIsMe=false;
    private String mPhotoLink;
    private String mContent;
    private boolean mIsReply=false;
    private boolean mIsResolved=false;

    public void setCommentId(String Id) {
        mCommentId = Id;
    }

    public String getCommentId() {
        return mCommentId;
    }


    public void setId(String Id) {
        mId = Id;
    }

    public String getId() {
        return mId;
    }

    public void setModifiedTime(DateTime ModifiedTime) {
        mModifiedTime = ModifiedTime;
    }

    public String getModifiedTime() {
        String time = (String) DateFormat.format("yyyy/MM/dd, E, kk:mm:ss",mModifiedTime.getValue());
        return time;
    }

    public DateTime getDataTime() { return  mModifiedTime;}

    public void setName(String Name) {
        mName = Name;
    }

    public String getName() {
        return mName;
    }

    public void setMe(boolean me) {
        mIsMe = me;
    }

    public boolean IsMe() {
        return mIsMe;
    }

    public void setPhotoLink(String PhotoLink) {
        mPhotoLink = PhotoLink;
    }

    public String getPhotoLink() {
        return mPhotoLink;
    }

    public void setContent(String Content) {
        mContent = Content;
    }

    public String getContent() {
        return mContent;
    }

    public void setReply() { mIsReply = true; }

    public boolean IsReply() { return mIsReply; }

    public void setResolved(boolean Resolved) {
        mIsResolved = Resolved;
    }

    public boolean IsResolved() {
        return mIsResolved;
    }

}
