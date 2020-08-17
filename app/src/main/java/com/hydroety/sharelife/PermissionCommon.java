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
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.PermissionList;
import com.google.api.services.drive.model.Comment;
import com.google.api.services.drive.model.CommentList;
import com.google.api.services.drive.model.Reply;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PermissionCommon extends FileCommon {
    private static final String TAG = "PermissionCommon";
    static final int REQUEST_UPDATE_PERMISSION = 1051;

    public static List<PermissionInfo> mPermissionList=null;
    public static PermissionArrayAdapter mPermissionAdapter=null;

    protected String mFileId="";

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

    protected void updatePermissionList() {
        if (DBG) Log.i(TAG,"updatePermissionList()");
    }

    /**
     * An asynchronous task that handles the Drive API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    protected class PermissionListRequestTask extends AsyncTask<Void, Void, List<Permission>> {
        private com.google.api.services.drive.Drive mService = null;
        private Exception mLastError = null;

        PermissionListRequestTask(GoogleAccountCredential credential) {
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
        protected List<Permission> doInBackground(Void... params) {
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
        private List<Permission> getDataFromApi() throws IOException {


            return retrievePermissions();
        }

        /**
         * Retrieve a list of permissions.
         *
         * @return List of permissions.
         */
        private List<Permission> retrievePermissions() throws IOException {
            if (DBG) Log.d(TAG,"fileId:" + mFileId);
            PermissionList permissions = mService.permissions().list(mFileId)
                    .setPageSize(20)
                    .setFields("nextPageToken, permissions(id, type, emailAddress, role, displayName, photoLink, deleted)")
                    .execute();

            if (DBG) Log.d(TAG,"nextPageToken:" + permissions.getNextPageToken());

            return permissions.getPermissions();
        }


        @Override
        protected void onPreExecute() {
            if (mProgress != null) {
                mProgress.show();
            }
        }

        @Override
        protected void onPostExecute(List<Permission> output) {
            if (DBG) Log.i(TAG,"onPostExecute()");
            if (mProgress != null && mProgress.isShowing()) {
                mProgress.dismiss();
            }
            if (output == null) {
                if (DBG) Log.d(TAG,"No results returned.");
            }else{
                mPermissionList = null;
                mPermissionList = new ArrayList<PermissionInfo>();
                if (mAutoCompleteListAll == null) {
                    mAutoCompleteListAll = new ArrayList<PermissionInfo>();
                }

                for (Permission file : output) {
                    if (DBG) Log.d(TAG,"PermissionId:" + file.getId());
                    if (DBG) Log.d(TAG,"type:" + file.getType());
                    if (DBG) Log.d(TAG,"emailAddress:" + file.getEmailAddress());
                    if (DBG) Log.d(TAG,"role:" + file.getRole());
                    if (DBG) Log.d(TAG,"displayName:" + file.getDisplayName());
                    if (DBG) Log.d(TAG,"photoLink:" + file.getPhotoLink());
                    if (DBG) Log.d(TAG,"deleted:" + file.getDeleted());

                    if ((file.getDeleted() == null || !file.getDeleted()) && !file.getType().contentEquals("anyone")) {
                        PermissionInfo info = new PermissionInfo();
                        info.setPermissionId(file.getId());
                        info.setType(file.getType());
                        info.setAddress(file.getEmailAddress());
                        info.setName(file.getDisplayName());
                        info.setRole(file.getRole());
                        info.setPhotoLink(file.getPhotoLink());
//                        info.setDeleted(file.getDeleted());

                        mPermissionList.add(info);
                        if (!info.getRole().contentEquals("owner")) {

                            int index = mAutoCompleteListAll.indexOf(info);
                            if (index == -1) {
                                mAutoCompleteListAll.add(info);
                            } else {
                                PermissionInfo old = mAutoCompleteListAll.set(index, info);
                                if (DBG) Log.d(TAG,"old emailAddress:" + old.getAddress());
                                if (DBG) Log.d(TAG,"old displayName:" + old.getName());
                                if (DBG) Log.d(TAG,"old photoLink:" + old.getPhotoLink());
                            }
                        }

                    }

               }

                updatePermissionList();


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
