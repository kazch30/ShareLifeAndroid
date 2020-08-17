package com.hydroety.sharelife;


import android.text.format.DateFormat;

import com.google.api.client.util.DateTime;

public class PopUpInfo {
    private String mCommentId;
    private DateTime mCommentModifiedTime;
    private String mCommentName;
    private String mCommentContent;
    private String mPhotoLink;
    private boolean mIsReply=false;
    private String mReplyId;
    private DateTime mReplyModifiedTime;
    private String mReplyName;
    private String mReplyContent;
    private String mReplyPhotoLink;

    public void setCommentId(String Id) {
        mCommentId = Id;
    }

    public String getCommentId() {
        return mCommentId;
    }


    public void setCommentModifiedTime(DateTime ModifiedTime) {
        mCommentModifiedTime = ModifiedTime;
    }

    public String getCommentModifiedTime() {
        String time = (String) DateFormat.format("yyyy/MM/dd, E, kk:mm:ss",mCommentModifiedTime.getValue());
        return time;
    }

    public void setCommentName(String Name) {
        mCommentName = Name;
    }

    public String getCommentName() {
        return mCommentName;
    }

    public void setCommentContent(String Content) {
        mCommentContent = Content;
    }

    public String getCommentContent() {
        return mCommentContent;
    }

    public void setReply() { mIsReply = true; }

    public boolean IsReply() { return mIsReply; }

    public void setReplyId(String Id) {
        mReplyId = Id;
    }

    public String getReplyId() {
        return mReplyId;
    }

    public void setReplyModifiedTime(DateTime ModifiedTime) {
        mReplyModifiedTime = ModifiedTime;
    }

    public String getReplyModifiedTime() {
        String time = (String) DateFormat.format("yyyy/MM/dd, E, kk:mm:ss",mReplyModifiedTime.getValue());
        return time;
    }

    public void setReplyName(String Name) {
        mReplyName = Name;
    }

    public String getReplyName() {
        return mReplyName;
    }

    public void setReplyContent(String Content) {
        mReplyContent = Content;
    }

    public String getReplyContent() {
        return mReplyContent;
    }

    public void setPhotoLink(String PhotoLink) {
        mPhotoLink = PhotoLink;
    }

    public String getPhotoLink() {
        return mPhotoLink;
    }

    public void setReplyPhotoLink(String PhotoLink) {
        mReplyPhotoLink = PhotoLink;
    }

    public String getReplyPhotoLink() {
        return mReplyPhotoLink;
    }

}
