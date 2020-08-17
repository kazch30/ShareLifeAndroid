package com.hydroety.sharelife;


import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.Comment;
import com.google.api.services.drive.model.CommentList;
import com.google.api.services.drive.model.Reply;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommentCommon extends PermissionCommon {
    private static final String TAG = "CommentCommon";
    static final int REQUEST_UPDATE_LIST = 1030;

    public static List<CommentInfo> mCommentListAll=null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DBG) Log.i(TAG, "onCreate()");
    }

    @Override
    public void onStart() {
        super.onStart();
        if (DBG) Log.i(TAG, "onStart()");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DBG) Log.i(TAG,"onResume()");
    }

    @Override
    public void onPause() {
        super.onPause();
        if (DBG) Log.i(TAG,"onPause()");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (DBG) Log.i(TAG,"onStop()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (DBG) Log.i(TAG,"onDestroy()");
    }

    @Override
    public void finish()
    {
        super.finish();
        if (DBG) Log.i(TAG,"finish()");
    }

    protected void update() {
        if (DBG) Log.i(TAG,"update()");
    }

    protected void showpopup() {
        if (DBG) Log.i(TAG,"showpopup()");
    }


    /**
         * An asynchronous task that handles the Drive API call.
         * Placing the API calls in their own task ensures the UI stays responsive.
         */
    protected class CommentListRequestTask extends AsyncTask<Void, Void, List<Comment>> {
        private com.google.api.services.drive.Drive mService = null;
        private Exception mLastError = null;

        CommentListRequestTask(GoogleAccountCredential credential) {
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
        protected List<Comment> doInBackground(Void... params) {
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
        private List<Comment> getDataFromApi() throws IOException {


            return retrieveComments();
        }

        /**
         * Retrieve a list of comments.
         *
         * @return List of comments.
         */
        private List<Comment> retrieveComments() throws IOException {
            if (DBG) Log.d(TAG,"fileId:" + mFileId);
            CommentList comments = mService.comments().list(mFileId)
                    .setPageSize(20)
                    .setFields("nextPageToken, comments(id, modifiedTime, author, content, resolved, replies(id, modifiedTime, author, content))")
                    .execute();

            if (DBG) Log.d(TAG,"nextPageToken:" + comments.getNextPageToken());

            return comments.getComments();
        }


        @Override
        protected void onPreExecute() {
            if (mProgress != null) {
                mProgress.show();
            }
        }

        @Override
        protected void onPostExecute(List<Comment> output) {
            if (DBG) Log.i(TAG,"onPostExecute()");
            if (mProgress != null && mProgress.isShowing()) {
                mProgress.dismiss();
            }
            if (output == null) {
//                if (output == null || output.size() == 0) {
                if (DBG) Log.d(TAG,"No results returned.");
            }else{
                mCommentListAll = null;
                mCommentListAll = new ArrayList<CommentInfo>();

                for (Comment file : output) {
                    if (DBG) Log.d(TAG,"commentId:" + file.getId());
                    if (DBG) Log.d(TAG,"name:" + file.getAuthor().getDisplayName());
                    if (DBG) Log.d(TAG,"address:" + file.getAuthor().getEmailAddress());
                    if (DBG) Log.d(TAG,"me:" + file.getAuthor().getMe());
                    if (DBG) Log.d(TAG,"link:" + file.getAuthor().getPhotoLink());
                    if (DBG) Log.d(TAG,"date:" + file.getModifiedTime().toString());
                    if (DBG) Log.d(TAG,"comment:" + file.getContent());
                    if (DBG) Log.d(TAG,"resolved:" + file.getResolved());

                    CommentInfo info = new CommentInfo();
                    info.setCommentId(file.getId());
                    info.setId(file.getId());
                    info.setName(file.getAuthor().getDisplayName());
                    info.setMe(file.getAuthor().getMe());
                    info.setPhotoLink(file.getAuthor().getPhotoLink());
                    info.setModifiedTime(file.getModifiedTime());
                    info.setContent(file.getContent());
                    if (file.getResolved() != null) {
                        info.setResolved(file.getResolved());
                    }
                    mCommentListAll.add(info);


                    for (Reply reply : file.getReplies()){
                        if (DBG) Log.d(TAG,"replyId:" + reply.getId());
                        if (DBG) Log.d(TAG,"reply name:" + reply.getAuthor().getDisplayName());
                        if (DBG) Log.d(TAG,"reply address:" + reply.getAuthor().getEmailAddress());
                        if (DBG) Log.d(TAG,"reply me:" + reply.getAuthor().getMe());
                        if (DBG) Log.d(TAG,"reply link:" + reply.getAuthor().getPhotoLink());
                        if (DBG) Log.d(TAG,"reply date:" + reply.getModifiedTime().toString());
                        if (DBG) Log.d(TAG,"reply comment:" + reply.getContent());

                        CommentInfo replyinfo = new CommentInfo();
                        replyinfo.setReply();
                        replyinfo.setCommentId(file.getId());
                        replyinfo.setId(reply.getId());
                        replyinfo.setName(reply.getAuthor().getDisplayName());
                        replyinfo.setMe(reply.getAuthor().getMe());
                        replyinfo.setPhotoLink(reply.getAuthor().getPhotoLink());
                        replyinfo.setModifiedTime(reply.getModifiedTime());
                        replyinfo.setContent(reply.getContent());
                        mCommentListAll.add(replyinfo);

                    }

                }

                update();
                showpopup();


            }


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
