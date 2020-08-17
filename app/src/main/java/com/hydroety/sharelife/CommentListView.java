package com.hydroety.sharelife;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Button;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.Comment;
import com.google.api.services.drive.model.Reply;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class CommentListView extends CommentCommon  {
    private static final String TAG = "CommentListView";
    static final int REQUEST_OPEN_FILE = 1004;
    static final int REQUEST_GET_COMMENTLIST = 1005;
    static final int REQUEST_CREATE_COMMENT = 1006;
    static final int REQUEST_SHARE_FILE = 1007;
    static final int REQUEST_DELETE_FILE = 1008;

    static final int REQUEST_CREATE_REPLY = 1021;
    static final int REQUEST_DELETE_COMMENT = 1022;
    static final int REQUEST_GET_COMMENT = 1023;
    static final int REQUEST_UPDATE_COMMENT = 1024;
    static final int REQUEST_DELETE_REPLY = 1025;
    static final int REQUEST_GET_REPLY = 1026;
    static final int REQUEST_UPDATE_REPLY = 1027;

    static final int BORDER_WEIGHT = 2;
    static final int POPUPPADDING = 20;

    private String mCommentId;
    protected String mContent;
    private String mReplyId;
    private String mName;
    private String mTime;
    private String mPhotoLink;
    private boolean mIsReply;
    private boolean mIsResolved;
    private List<Button> mEditBtnList=null;
    private PopupWindow mPopupWindow=null;
    private TextView mPopupTitle;
    private EditText mReplyEdit;
    private EditText mCommentEdit;
    private EditText mUpdateEdit;
    private Reply mReply;
    private Comment mComment;
    private AdView mAdView;



    protected int mRequest=REQUEST_OPEN_FILE;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }

    @Override
    public void onStart() {
        super.onStart();
        if (DBG) Log.d(TAG, "onStart()");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DBG) Log.i(TAG, "onResume()");
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onPause() {
        if (DBG) Log.i(TAG, "onPause()");
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (DBG) Log.i(TAG, "onStop()");
    }

    @Override
    public void onDestroy() {
        if (DBG) Log.i(TAG, "onDestroy()");
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        if (DBG) Log.i(TAG, "finish()");
    }

    @Override
    protected void update() {
        if (DBG) Log.i(TAG,"update()");
        int i;

        TextView appInfoName = (TextView)findViewById(R.id.comments_text);
        if (mCommentListAll.size() > 0) {
            appInfoName.setVisibility(View.VISIBLE);
        }

        mEditBtnList = null;
        mEditBtnList = new ArrayList<Button>();

        LinearLayout ll = (LinearLayout) findViewById(R.id.layout_item);
        ll.setBackground(makelayerDrawable(false));
        ll.removeAllViews();

        for (i=0; i < mCommentListAll.size(); i++) {
            CommentInfo info = mCommentListAll.get(i);
            if (info.IsReply()) {
                ll.addView(makeReplyItem(info,i));
            } else {
                ll.addView(makeCommentItem(info,i));
            }

        }

        for (Button button : mEditBtnList) {
            // リスナーをボタンに登録
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (DBG) Log.d(TAG, "onClick() position=" + v.getTag() );
                    int position = (int)v.getTag();
                    CommentInfo info = mCommentListAll.get(position);

                    if (info.IsMe()) {
                        mCommentId = info.getCommentId();
                        mName = info.getName();
                        mTime = info.getModifiedTime();
                        mPhotoLink = info.getPhotoLink();
                        mIsReply = info.IsReply();
                        if (info.IsReply()) {
                            mReplyId = info.getId();
                            if (DBG) Log.d(TAG,"ReplyId=" + mReplyId);
                            mRequest=REQUEST_GET_REPLY;
                        }else{
                            mRequest=REQUEST_GET_COMMENT;
                        }

                        getResultsFromApi();

                    } else {

                        mCommentId = info.getCommentId();
                        mIsResolved = info.IsResolved();

                        makeReplyPopup();
                    }
                }
            });

        }


    }

    public void DeleteComment(View v) {
        String msg;
        String title;

        if (mIsReply) {
            mRequest = REQUEST_DELETE_REPLY;
            msg = getString(R.string.delete_reply_msg);
            title = getString(R.string.delete_reply_title);
        } else {
            mRequest = REQUEST_DELETE_COMMENT;
            msg = getString(R.string.delete_comment_msg);
            title = getString(R.string.delete_comment_title);
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // OK button pressed
                        if (mPopupWindow.isShowing()) {
                            mPopupWindow.dismiss();
                        }

                        getResultsFromApi();

                    }
                })
                .setNegativeButton(getString(android.R.string.cancel), null)
                .show();

    }

    private void makeUpdatePopup() {

        mPopupWindow = new PopupWindow(this);

        // レイアウト設定
        View popupView = getLayoutInflater().inflate(R.layout.update_popup, null);

        mPopupWindow.setContentView(popupView);

        // 背景設定
        mPopupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_background));

        // タップ時に他のViewでキャッチされないための設定
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);

        //デバイスサイズを取得しpopupwindowサイズ設定する場合
        Display d = getWindowManager().getDefaultDisplay();
        Point p2 = new Point();
        // ②ナビゲーションバーを除く画面サイズを取得
        d.getSize(p2);

        mPopupWindow.setWidth(p2.x-convertDpToPx(POPUPPADDING));
//        mPopupWindow.setWidth(p2.x);
        mPopupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        mPopupTitle = (TextView) popupView.findViewById(R.id.popupTitle);
        mPopupTitle.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (DBG) Log.i(TAG, "onTouch()");

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (DBG) Log.i(TAG, "MotionEvent.ACTION_UP");
                    //To get left side click
//                    if(event.getRawX() >= mPopupTitle.getRight() - mPopupTitle.getTotalPaddingRight()) {
                    if(event.getRawX() <= (mPopupTitle.getTotalPaddingLeft() + convertDpToPx(POPUPPADDING))) {
                        if (DBG) Log.d(TAG, "close icon click!!");

                        v.performClick();

                        if (mPopupWindow.isShowing()) {
                            mPopupWindow.dismiss();
                        }

                        return true;

                    }

                }
                return true;
            }
        });

        mUpdateEdit = (EditText) popupView.findViewById(R.id.comment_edit);
        mUpdateEdit.setText(mContent);
        mUpdateEdit.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (DBG) Log.i(TAG, "onTouch()");
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (mUpdateEdit.getRight() - mUpdateEdit.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                        if (DBG) Log.d(TAG, "send icon click!!");
                        mContent = mUpdateEdit.getText().toString();
                        if (!mContent.isEmpty()) {
                            if (mIsReply) {
                                mRequest = REQUEST_UPDATE_REPLY;
                            } else {
                                mRequest = REQUEST_UPDATE_COMMENT;
                            }
                            getResultsFromApi();
                        }

                        v.performClick();

                        if (mPopupWindow.isShowing()) {
                            mPopupWindow.dismiss();
                        }

                        return true;
                    }
                /* To get left side click
                if(event.getRawX() <= (mUpdateEdit.getCompoundDrawables()[DRAWABLE_LEFT].getBounds().width())){

                }
                */
                }
                return false;
            }
        });

        TextView appInfoName = (TextView)popupView.findViewById(R.id.update_name);
        appInfoName.setText(mName);
        TextView appInfoTime = (TextView)popupView.findViewById(R.id.update_time);
        appInfoTime.setText(mTime);
        CircleImageView appInfoImage = (CircleImageView)popupView.findViewById(R.id.update_image);

        LoardImgFile(mPhotoLink, appInfoImage);


    }

    private void makeReplyPopup() {

        mPopupWindow = new PopupWindow(this);

        // レイアウト設定
        View popupView = getLayoutInflater().inflate(R.layout.reply_popup, null);

        mPopupWindow.setContentView(popupView);

        // 背景設定
        mPopupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_background));

        // タップ時に他のViewでキャッチされないための設定
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);

        //デバイスサイズを取得しpopupwindowサイズ設定する場合
        Display d = getWindowManager().getDefaultDisplay();
        Point p2 = new Point();
        // ②ナビゲーションバーを除く画面サイズを取得
        d.getSize(p2);

        mPopupWindow.setWidth(p2.x-convertDpToPx(POPUPPADDING));
//        mPopupWindow.setWidth(p2.x);
        mPopupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        mPopupTitle = (TextView) popupView.findViewById(R.id.popupTitle);
        mPopupTitle.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (DBG) Log.i(TAG, "onTouch()");

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (DBG) Log.i(TAG, "MotionEvent.ACTION_UP");
                    //To get left side click
//                    if(event.getRawX() >= mPopupTitle.getRight() - mPopupTitle.getTotalPaddingRight()) {
                    if(event.getRawX() <= (mPopupTitle.getTotalPaddingLeft() + convertDpToPx(POPUPPADDING))) {
                        if (DBG) Log.d(TAG, "close icon click!!");

                        v.performClick();

                        if (mPopupWindow.isShowing()) {
                            mPopupWindow.dismiss();
                        }

                        return true;

                    }

                }
                return true;
            }
        });

        mReplyEdit = (EditText) popupView.findViewById(R.id.reply_edit);
        mReplyEdit.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (DBG) Log.i(TAG, "onTouch()");
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (mReplyEdit.getRight() - mReplyEdit.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                        if (DBG) Log.d(TAG, "send icon click!!");
                        mContent = mReplyEdit.getText().toString();
                        if (!mContent.isEmpty()) {
                            mRequest=REQUEST_CREATE_REPLY;
                            getResultsFromApi();
                        }

                        v.performClick();

                        if (mPopupWindow.isShowing()) {
                            mPopupWindow.dismiss();
                        }

                        return true;
                    }
                /* To get left side click
                if(event.getRawX() <= (mReplyEdit.getCompoundDrawables()[DRAWABLE_LEFT].getBounds().width())){

                }
                */
                }
                return false;
            }
        });


    }

    protected void makeCommentPopup() {

        mPopupWindow = new PopupWindow(this);

        // レイアウト設定
        View popupView = getLayoutInflater().inflate(R.layout.comment_popup, null);

        mPopupWindow.setContentView(popupView);

        // 背景設定
        mPopupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_background));

        // タップ時に他のViewでキャッチされないための設定
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);

        //デバイスサイズを取得しpopupwindowサイズ設定する場合
        Display d = getWindowManager().getDefaultDisplay();
        Point p2 = new Point();
        // ②ナビゲーションバーを除く画面サイズを取得
        d.getSize(p2);

        mPopupWindow.setWidth(p2.x-convertDpToPx(POPUPPADDING));
//        mPopupWindow.setWidth(p2.x);
        mPopupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        mPopupTitle = (TextView) popupView.findViewById(R.id.popupTitle);
        mPopupTitle.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (DBG) Log.i(TAG, "onTouch()");

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (DBG) Log.i(TAG, "MotionEvent.ACTION_UP");
                    //To get left side click
//                    if(event.getRawX() >= mPopupTitle.getRight() - mPopupTitle.getTotalPaddingRight()) {
                    if(event.getRawX() <= (mPopupTitle.getTotalPaddingLeft() + convertDpToPx(POPUPPADDING))) {
                        if (DBG) Log.d(TAG, "close icon click!!");

                        v.performClick();

                        if (mPopupWindow.isShowing()) {
                            mPopupWindow.dismiss();
                        }

                        return true;

                    }

                }
                return true;
            }
        });

        mCommentEdit = (EditText) popupView.findViewById(R.id.comment_edit);
        mCommentEdit.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (DBG) Log.i(TAG, "onTouch()");
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (mCommentEdit.getRight() - mCommentEdit.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                        if (DBG) Log.d(TAG, "send icon click!!");
                        mContent = mCommentEdit.getText().toString();
                        if (!mContent.isEmpty()) {
                            mRequest=REQUEST_CREATE_COMMENT;
                            getResultsFromApi();
                        }

                        v.performClick();

                        if (mPopupWindow.isShowing()) {
                            mPopupWindow.dismiss();
                        }

                        return true;
                    }
                /* To get left side click
                if(event.getRawX() <= (mCommentEdit.getCompoundDrawables()[DRAWABLE_LEFT].getBounds().width())){

                }
                */
                }
                return false;
            }
        });


    }

    private LayerDrawable makelayerDrawable(boolean isBottom) {
        GradientDrawable borderDrawable = new GradientDrawable();
        borderDrawable.setStroke(BORDER_WEIGHT, 0xffCCCCCC);

        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{borderDrawable});
        if (isBottom) {
            layerDrawable.setLayerInset(0, -BORDER_WEIGHT, -BORDER_WEIGHT, -BORDER_WEIGHT, 0);
        } else {
            layerDrawable.setLayerInset(0, -BORDER_WEIGHT, 0, -BORDER_WEIGHT, -BORDER_WEIGHT);
        }

        return layerDrawable;

    }

    private LinearLayout makeReplyItem(CommentInfo info, int position){

        LinearLayout group_reply = new LinearLayout(this);
        group_reply.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        group_reply.setBackground(makelayerDrawable(true));
//        group_reply.setBackgroundResource(R.drawable.border);
        group_reply.setOrientation(LinearLayout.VERTICAL);

        LayoutParams layout_params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layout_params.setMargins(convertDpToPx(30),0,0,0);
        LinearLayout group_reply1 = new LinearLayout(this);
        group_reply1.setLayoutParams(layout_params);
        group_reply1.setOrientation(LinearLayout.VERTICAL);


        LayoutParams layout_params1 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        LinearLayout group_reply2 = new LinearLayout(this);
        group_reply2.setLayoutParams(layout_params1);
        group_reply2.setOrientation(LinearLayout.HORIZONTAL);

//        CircleImageView appInfoImage = new CircleImageView(this);
        ImageView appInfoImage = new ImageView(this);
        appInfoImage.setLayoutParams(new LayoutParams(convertDpToPx(40), convertDpToPx(40)));
        appInfoImage.setPadding(convertDpToPx(3),convertDpToPx(3),convertDpToPx(3),convertDpToPx(3));
        group_reply2.addView(appInfoImage);
        LoardImgFile(info.getPhotoLink(), appInfoImage);


        LayoutParams layout_params2 = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layout_params2.setMargins(convertDpToPx(30),0,0,0);
        LinearLayout group_reply3 = new LinearLayout(this);
        group_reply3.setLayoutParams(layout_params2);
        group_reply3.setOrientation(LinearLayout.VERTICAL);

        // テキストをセット

        TextView appInfoName = new TextView(this);
        appInfoName.setText(info.getName());
        appInfoName.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        group_reply3.addView(appInfoName);

        TextView appInfoTime = new TextView(this);
        appInfoTime.setText(info.getModifiedTime());
        appInfoTime.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        group_reply3.addView(appInfoTime);

        group_reply2.addView(group_reply3);

        LayoutParams layout_params3 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layout_params3.gravity = Gravity.CENTER_VERTICAL;
        LinearLayout group_reply4 = new LinearLayout(this);
        group_reply4.setLayoutParams(layout_params3);
        group_reply4.setOrientation(LinearLayout.VERTICAL);

        if (info.IsMe()) {
            LayoutParams layout_params4 = new LayoutParams(convertDpToPx(40), convertDpToPx(40));
            layout_params4.gravity = Gravity.END;
            Button button = new Button(this);
//            button.setBackgroundResource(android.R.drawable.ic_menu_edit);
            button.setBackgroundResource(R.drawable.ic_edit_blue_24dp);
            button.setLayoutParams(layout_params4);
            button.setTag(position);
            group_reply4.addView(button);

            mEditBtnList.add(button);
        } else {
            LayoutParams layout_params4 = new LayoutParams(convertDpToPx(40), convertDpToPx(40));
            layout_params4.gravity = Gravity.END;
            Button button = new Button(this);
            button.setBackgroundResource(R.drawable.ic_reply_blue_24dp);
            button.setLayoutParams(layout_params4);
            button.setTag(position);
            group_reply4.addView(button);

            mEditBtnList.add(button);
        }

        group_reply2.addView(group_reply4);

        group_reply1.addView(group_reply2);

        LayoutParams layout_params5 = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layout_params5.setMargins(convertDpToPx(30),0,0,0);
        TextView appInfoContent = new TextView(this);
        appInfoContent.setText(info.getContent());
        appInfoContent.setLayoutParams(layout_params5);

        group_reply1.addView(appInfoContent);

        group_reply.addView(group_reply1);

        return group_reply;
    }

    private LinearLayout makeCommentItem(CommentInfo info, int position){
        LayoutParams layout_params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        LinearLayout group_comment = new LinearLayout(this);
        group_comment.setLayoutParams(layout_params);
        group_comment.setBackground(makelayerDrawable(true));
//        group_comment.setBackgroundResource(R.drawable.border);
        group_comment.setOrientation(LinearLayout.VERTICAL);


        LayoutParams layout_params1 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        LinearLayout group_comment1 = new LinearLayout(this);
        group_comment1.setLayoutParams(layout_params1);
        group_comment1.setOrientation(LinearLayout.HORIZONTAL);

//        CircleImageView appInfoImage = new CircleImageView(this);
        ImageView appInfoImage = new ImageView(this);
        appInfoImage.setLayoutParams(new LayoutParams(convertDpToPx(40), convertDpToPx(40)));
        appInfoImage.setPadding(convertDpToPx(3),convertDpToPx(3),convertDpToPx(3),convertDpToPx(3));
        group_comment1.addView(appInfoImage);
        LoardImgFile(info.getPhotoLink(), appInfoImage);


        LayoutParams layout_params2 = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layout_params2.setMargins(convertDpToPx(30),0,0,0);
        LinearLayout group_comment2 = new LinearLayout(this);
        group_comment2.setLayoutParams(layout_params2);
        group_comment2.setOrientation(LinearLayout.VERTICAL);

        // テキストをセット

        TextView appInfoName = new TextView(this);
        appInfoName.setText(info.getName());
        appInfoName.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        group_comment2.addView(appInfoName);

        TextView appInfoTime = new TextView(this);
        appInfoTime.setText(info.getModifiedTime());
        appInfoTime.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        group_comment2.addView(appInfoTime);

        group_comment1.addView(group_comment2);

        LayoutParams layout_params3 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layout_params3.gravity = Gravity.CENTER_VERTICAL;
        LinearLayout group_comment3 = new LinearLayout(this);
        group_comment3.setLayoutParams(layout_params3);
        group_comment3.setOrientation(LinearLayout.VERTICAL);

        if (info.IsMe()) {
            LayoutParams layout_params4 = new LayoutParams(convertDpToPx(40), convertDpToPx(40));
            layout_params4.gravity = Gravity.END;
            Button button = new Button(this);
//            button.setBackgroundResource(android.R.drawable.ic_menu_edit);
            button.setBackgroundResource(R.drawable.ic_edit_blue_24dp);
            button.setLayoutParams(layout_params4);
            button.setTag(position);
            group_comment3.addView(button);

            mEditBtnList.add(button);
        } else {
            LayoutParams layout_params4 = new LayoutParams(convertDpToPx(40), convertDpToPx(40));
            layout_params4.gravity = Gravity.END;
            Button button = new Button(this);
            button.setBackgroundResource(R.drawable.ic_reply_blue_24dp);
            button.setLayoutParams(layout_params4);
            button.setTag(position);
            group_comment3.addView(button);

            mEditBtnList.add(button);
        }

        group_comment1.addView(group_comment3);

        group_comment.addView(group_comment1);

        LayoutParams layout_params5 = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layout_params5.setMargins(convertDpToPx(30),0,0,0);
        TextView appInfoContent = new TextView(this);
        appInfoContent.setText(info.getContent());
        appInfoContent.setLayoutParams(layout_params5);

        group_comment.addView(appInfoContent);

        return group_comment;
    }

    private void setContent(String Content){
        if (DBG) Log.i(TAG,"setContent()");
        mContent = Content;
        if (mRequest == REQUEST_GET_REPLY || mRequest == REQUEST_GET_COMMENT) {
            makeUpdatePopup();
        }
    }

    private int convertDpToPx(int dp){
        float d = getResources().getDisplayMetrics().density;
        return (int)((dp * d) + 0.5);
    }

    private int convertPxToDp(int px){
        float d = getResources().getDisplayMetrics().density;
        return (int)((px / d) + 0.5);
    }

    private void LoardImgFile(String imgName, ImageView appInfoImage) {
        if (DBG) Log.i(TAG,"LoardImgFile()");
        if (DBG) Log.d(TAG,"imgName=" + imgName);
        String path = "https:" + imgName;
        //画像取得スレッド起動
        ImageGetTask task = new ImageGetTask(appInfoImage);
        task.execute(path);
    }

    class ImageGetTask extends AsyncTask<String,Void,Bitmap> {
        private ImageView image;

        public ImageGetTask(ImageView _image) {

            image = _image;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap image;
            try {
                URL imageUrl = new URL(params[0]);
                InputStream imageIs;
                imageIs = imageUrl.openStream();
                image = BitmapFactory.decodeStream(imageIs);
                return image;
            } catch (MalformedURLException e) {
                if (DBG) Log.d(TAG,"The following error occurred:\n"
                        + e.getMessage());
                return null;
            } catch (IOException e) {
                if (DBG) Log.d(TAG,"The following error occurred:\n"
                        + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            image.setImageBitmap(result);
        }
    }


    private void updateList(){
        switch (mRequest) {
            case REQUEST_CREATE_REPLY:
            case REQUEST_DELETE_REPLY:
            case REQUEST_UPDATE_REPLY:
            case REQUEST_DELETE_COMMENT:
            case REQUEST_UPDATE_COMMENT:
                mRequest = REQUEST_UPDATE_LIST;
                getResultsFromApi();
                return;
        }

    }

    /**
     * An asynchronous task that handles the Drive API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    protected class CreateReplyRequestTask extends AsyncTask<Void, Void, Reply> {
        private com.google.api.services.drive.Drive mService = null;
        private Exception mLastError = null;

        CreateReplyRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.drive.Drive.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName(getString(R.string.app_name))
                    .build();
        }

        /**
         * Background task to call Drive API.
         *
         * @param params no parameters needed for this task.
         */
        @Override
        protected Reply doInBackground(Void... params) {

            return getDataFromApi();

        }

        /**
         * Fetch a list of up to 10 file names and IDs.
         *
         * @return List of Strings describing files, or an empty list if no files
         * found.
         * @throws IOException
         */
        private Reply getDataFromApi() {
            // Get a list of up to 10 files.
            try {
                Reply reply = createReply();
                return reply;
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Retrieve a list of comments.
         *
         * @return Reply.
         */
        private Reply createReply() throws IOException {
            if (DBG) Log.i(TAG,"createReply()");
            if (DBG) Log.d(TAG,"fileId:" + mFileId);
            if (DBG) Log.d(TAG,"CommentId:" + mCommentId);
            if (DBG) Log.d(TAG,"content:" + mContent);

            Reply newReply = new Reply();
            DateTime dt = new DateTime(new Date());

            newReply.setContent(mContent);
            newReply.setCreatedTime(dt);
            newReply.setModifiedTime(dt);

            String action = "reopen";
            if (mIsResolved) {
                newReply.setAction(action);
            }

            Reply reply = mService.replies().create(mFileId, mCommentId, newReply)
                    .setFields("id, modifiedTime, author, content")
                    .execute();

            return reply;
        }

        @Override
        protected void onPreExecute() {
            if (mProgress != null) {
                mProgress.show();
            }
        }

        @Override
        protected void onPostExecute(Reply reply) {
            if (DBG) Log.i(TAG, "onPostExecute()");
            if (mProgress != null && mProgress.isShowing()) {
                mProgress.dismiss();
            }

            updateList();

        }

        @Override
        protected void onCancelled() {
            if (mProgress != null && mProgress.isShowing()) {
                mProgress.dismiss();
            }
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            REQUEST_AUTHORIZATION);
                } else {
                    if (DBG) Log.d(TAG,"The following error occurred:\n"
                            + mLastError.getMessage());

                }
            } else {
                if (DBG) Log.d(TAG,"Request cancelled.");
            }
        }
    }

    /**
     * An asynchronous task that handles the Drive API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    protected class RrplyRequestTask extends AsyncTask<Void, Void, Reply> {
        private com.google.api.services.drive.Drive mService = null;
        private Exception mLastError = null;

        RrplyRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.drive.Drive.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName(getString(R.string.app_name))
                    .build();
        }

        /**
         * Background task to call Drive API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected Reply doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of up to 10 file names and IDs.
         * @return List of Strings describing files, or an empty list if no files
         *         found.
         * @throws IOException
         */
        private Reply getDataFromApi() throws IOException {

            switch (mRequest) {
                case REQUEST_DELETE_REPLY:
                    deleteReply();
                    return null;
                case REQUEST_GET_REPLY:
                    return getReply();
                case REQUEST_UPDATE_REPLY:
                    return updateReply();
            }

            return null;
        }

        private void deleteReply() throws IOException {
            if (DBG) Log.i(TAG,"deleteReply()");

            mService.replies().delete(mFileId, mCommentId,mReplyId).execute();

        }

        private Reply getReply() throws IOException {
            if (DBG) Log.i(TAG,"getReply()");

            return mService.replies().get(mFileId, mCommentId,mReplyId)
                    .setFields("id, createdTime, modifiedTime, author, content")
                    .execute();
        }

        private Reply updateReply() throws IOException {
            if (DBG) Log.i(TAG,"updateReply()");
            if (DBG) Log.d(TAG, "new comment:" + mContent);

            DateTime dt = new DateTime(new Date());

            mReply.setContent(mContent);
            mReply.setModifiedTime(dt);

            return mService.replies().update(mFileId, mCommentId,mReplyId, mReply)
                    .setFields("id, createdTime, modifiedTime, author, content")
                    .execute();
        }


        @Override
        protected void onPreExecute() {
            if (mProgress != null) {
                mProgress.show();
            }
        }

        @Override
        protected void onPostExecute(Reply output) {
            if (DBG) Log.i(TAG,"onPostExecute()");
            String content;
            if (mProgress != null && mProgress.isShowing()) {
                mProgress.dismiss();
            }

            if (output != null) {
                if (DBG) Log.d(TAG, "Id:" + output.getId());
                if (DBG) Log.d(TAG, "name:" + output.getAuthor().getDisplayName());
                if (DBG) Log.d(TAG, "ModifiedTime:" + output.getModifiedTime().toString());
                if (DBG) Log.d(TAG, "comment:" + output.getContent());
                content = output.getContent();

                mReply = output;

                setContent(content);
            }

            updateList();

        }

        @Override
        protected void onCancelled() {
            if (mProgress != null && mProgress.isShowing()) {
                mProgress.dismiss();
            }
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            REQUEST_AUTHORIZATION);
                } else {
                    if (DBG) Log.d(TAG,"The following error occurred:\n"
                            + mLastError.getMessage());

                }
            } else {
                if (DBG) Log.d(TAG,"Request cancelled.");
            }
        }
    }

    /**
     * An asynchronous task that handles the Drive API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    protected class CommentRequestTask extends AsyncTask<Void, Void, Comment> {
        private com.google.api.services.drive.Drive mService = null;
        private Exception mLastError = null;

        CommentRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.drive.Drive.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName(getString(R.string.app_name))
                    .build();
        }

        /**
         * Background task to call Drive API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected Comment doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of up to 10 file names and IDs.
         * @return List of Strings describing files, or an empty list if no files
         *         found.
         * @throws IOException
         */
        private Comment getDataFromApi() throws IOException {

            switch (mRequest) {
                case REQUEST_DELETE_COMMENT:
                    deleteComment();
                    return null;
                case REQUEST_GET_COMMENT:
                    return getComment();
                case REQUEST_UPDATE_COMMENT:
                    return updateComment();
            }

            return null;
        }

        private void deleteComment() throws IOException {
            if (DBG) Log.i(TAG,"deleteComment()");

            mService.comments().delete(mFileId, mCommentId).execute();

        }

        private Comment getComment() throws IOException {
            if (DBG) Log.i(TAG,"getReply()");

            return mService.comments().get(mFileId, mCommentId)
                    .setFields("id, createdTime, modifiedTime, author, content")
                    .execute();
        }

        private Comment updateComment() throws IOException {
            if (DBG) Log.i(TAG,"updateReply()");
            if (DBG) Log.d(TAG, "new comment:" + mContent);

            DateTime dt = new DateTime(new Date());

            mComment.setContent(mContent);
            mComment.setModifiedTime(dt);

            return mService.comments().update(mFileId, mCommentId, mComment)
                    .setFields("id, createdTime, modifiedTime, author, content")
                    .execute();
        }


        @Override
        protected void onPreExecute() {
            if (mProgress != null) {
                mProgress.show();
            }
        }

        @Override
        protected void onPostExecute(Comment output) {
            if (DBG) Log.i(TAG,"onPostExecute()");
            String content;
            if (mProgress != null && mProgress.isShowing()) {
                mProgress.dismiss();
            }

            if (output != null) {
                if (DBG) Log.d(TAG, "Id:" + output.getId());
                if (DBG) Log.d(TAG, "name:" + output.getAuthor().getDisplayName());
                if (DBG) Log.d(TAG, "ModifiedTime:" + output.getModifiedTime().toString());
                if (DBG) Log.d(TAG, "comment:" + output.getContent());
                content = output.getContent();

                mComment = output;

                setContent(content);
            }

            updateList();

        }

        @Override
        protected void onCancelled() {
            if (mProgress != null && mProgress.isShowing()) {
                mProgress.dismiss();
            }
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            REQUEST_AUTHORIZATION);
                } else {
                    if (DBG) Log.d(TAG,"The following error occurred:\n"
                            + mLastError.getMessage());

                }
            } else {
                if (DBG) Log.d(TAG,"Request cancelled.");
            }
        }
    }

}
