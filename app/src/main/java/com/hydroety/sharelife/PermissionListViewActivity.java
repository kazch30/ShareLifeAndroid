package com.hydroety.sharelife;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.model.Permission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class PermissionListViewActivity extends PermissionCommon implements AdapterView.OnItemClickListener {
    private static final String TAG = "PermissionListView";

    private static final String FILEID_STATE = "FileId";
    private static final String PNGFILEID_STATE = "PngFileId";
    static final int REQUEST_CREATE_PERMISSION = 1050;
    static final int REQUEST_DELETE_PERMISSION = 1052;
//    private EditText mGmailEdit;
    private AutoCompleteTextView mGmailEdit;
    private EditText mMsgEdit;
    private String mGmailAdr;
    private String mPermissionMsg;
    private String mPngFileId="";
    private String mPermissionId="";
    private List<PermissionInfo> mAutoCompleteList=null;
    private AdView mAdView;


    private int mRequest=REQUEST_CREATE_PERMISSION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permission_list_view);

        if (savedInstanceState != null) {
            if (DBG) Log.d(TAG, "savedInstanceState != null");
            // Restore value of members from saved state
            if (savedInstanceState.containsKey(FILEID_STATE)) {
                mFileId = savedInstanceState.getString(FILEID_STATE);
                if (DBG) Log.d(TAG, "FileId=" + mFileId);
            }
            if (savedInstanceState.containsKey(PNGFILEID_STATE)) {
                mPngFileId = savedInstanceState.getString(PNGFILEID_STATE);
                if (DBG) Log.d(TAG, "PngFileId=" + mPngFileId);
            }
        } else {
            Intent intent = getIntent();
            mFileId = intent.getStringExtra("FileId");
            if (DBG) Log.d(TAG, "FileId=" + mFileId);
            mPngFileId = intent.getStringExtra("PngFileId");
            if (DBG) Log.d(TAG, "PngFileId=" + mPngFileId);
        }

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // adapterのインスタンスを作成
        mPermissionAdapter = new PermissionArrayAdapter(this, R.layout.permission_list_view_item, mPermissionList);

        ListView lv = (ListView) findViewById(R.id.permissionlist);
        lv.setAdapter(mPermissionAdapter);
        lv.setOnItemClickListener(this);

        mMsgEdit = (EditText) findViewById(R.id.gmail_msg);
        mGmailEdit = (AutoCompleteTextView) findViewById(R.id.gmail_edit);

        mAutoCompleteList = new ArrayList<PermissionInfo>();
        mAutoCompleteList.addAll(mAutoCompleteListAll);

        PermissionAutoCompleteAdapter autoCompleteAdapter = new PermissionAutoCompleteAdapter(this, R.layout.permission_autocomplete_item, mAutoCompleteList);

        mGmailEdit.setAdapter(autoCompleteAdapter);
        mGmailEdit.setThreshold(1);
/*
        mGmailEdit.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mGmailEdit.showDropDown();
                }
            }
        });
        */
        mGmailEdit.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (DBG) Log.i(TAG, "onItemClick()");
                if (DBG) Log.d(TAG, "position=" + position);
                PermissionInfo item = (PermissionInfo)parent.getItemAtPosition(position);
                mGmailEdit.setText(item.getAddress());

            }
        });

        mGmailEdit.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (DBG) Log.i(TAG, "onTouch()");
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (mGmailEdit.getRight() - mGmailEdit.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                        if (DBG) Log.d(TAG, "send icon click!!");
                        mPermissionMsg = mMsgEdit.getText().toString();

                        mGmailAdr = mGmailEdit.getText().toString();
                        if (!mGmailAdr.isEmpty()) {
                            mRequest=REQUEST_CREATE_PERMISSION;
                            getResultsFromApi();
                        }

                        v.performClick();

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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onSaveInstanceState()");

        if (mFileId != null) {
            savedInstanceState.putString(FILEID_STATE, mFileId);
        }
        if (mPngFileId != null) {
            savedInstanceState.putString(PNGFILEID_STATE, mPngFileId);
        }

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void  onRestoreInstanceState(Bundle savedInstanceState) {
        if (DBG) Log.i(TAG, "onRestoreInstanceState()");
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(FILEID_STATE)) {
            mFileId = savedInstanceState.getString(FILEID_STATE);
            if (DBG) Log.d(TAG, "FileId=" + mFileId);
        }
        if (savedInstanceState.containsKey(PNGFILEID_STATE)) {
            mPngFileId = savedInstanceState.getString(PNGFILEID_STATE);
            if (DBG) Log.d(TAG, "PngFileId=" + mPngFileId);
        }
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
    protected void updatePermissionList() {
        if (DBG) Log.i(TAG, "updatePermissionList()");

        mPermissionAdapter.clear();
        mPermissionAdapter.addAll(mPermissionList);
        mPermissionAdapter.notifyDataSetChanged();
    }

    @Override
    protected void updateFileList() {
        if (DBG) Log.i(TAG,"updateFileList()");

        mShareFileAdapter.clear();
        mShareFileAdapter.addAll(mShareFileList);
        mShareFileAdapter.notifyDataSetChanged();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        if (DBG) Log.d(TAG, "onItemClick() position=" + position);

    }

    public void DeleteUser(View v) {
        if (DBG) Log.i(TAG,"DeleteUser()");
        if (DBG) Log.d(TAG, "DeleteUser() position=" + v.getTag() );
        int position = (int)v.getTag();
        PermissionInfo item = mPermissionList.get(position);
        mPermissionId = item.getPermissionId();
        mRequest = REQUEST_DELETE_PERMISSION;
        getResultsFromApi();

        if (mAutoCompleteListAll.indexOf(item) != -1) {
            mAutoCompleteListAll.remove(item);
        }
    }

    private void updateList(){
        if (mRequest == REQUEST_CREATE_PERMISSION || mRequest == REQUEST_DELETE_PERMISSION) {
            mRequest = REQUEST_UPDATE_PERMISSION;
            getResultsFromApi();

            mGmailEdit.setText("");
            mMsgEdit.setText("");
        }

    }

    @Override
    protected void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            if (DBG) Log.d(TAG,"No network connection available.");
        } else {
            if (mRequest == REQUEST_UPDATE_PERMISSION) {
                new PermissionListRequestTask(mCredential).execute();
            } else {
                new MakeRequestTask(mCredential).execute();
            }
        }
    }

    /**
     * An asynchronous task that handles the Drive API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, String> {
        private com.google.api.services.drive.Drive mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
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
        protected String doInBackground(Void... params) {

            getDataFromApi();

            return "OK";

        }

        JsonBatchCallback<Permission> callback = new JsonBatchCallback<Permission>() {
            @Override
            public void onFailure(GoogleJsonError e,
                                  HttpHeaders responseHeaders)
                    throws IOException {
                // Handle error
                System.err.println(e.getMessage());
            }

            @Override
            public void onSuccess(Permission permission,
                                  HttpHeaders responseHeaders)
                    throws IOException {
                System.out.println("Permission ID: " + permission.getId());
            }
        };

        private void permissionmodification() throws IOException {
            BatchRequest batch = mService.batch();
            Permission userPermission = new Permission()
                    .setType("user")
                    .setRole("commenter")

                    .setEmailAddress(mGmailAdr);
            mService.permissions().create(mFileId, userPermission)
                    .setEmailMessage(mPermissionMsg)
                    .setFields("id")
                    .queue(batch, callback);

            if (!mPngFileId.isEmpty()) {
                Permission pngPermission = new Permission()
                        .setType("user")
                        .setRole("reader")

                        .setEmailAddress(mGmailAdr);
                mService.permissions().create(mPngFileId, pngPermission)
                        .setFields("id")
                        .queue(batch, callback);
            }

            batch.execute();
        }

        private void permissiondelete() throws IOException {
            mService.permissions().delete(mFileId,mPermissionId).execute();
        }
        /**
             * Fetch a list of up to 10 file names and IDs.
             *
             * @return List of Strings describing files, or an empty list if no files
             * found.
             * @throws IOException
             */
        private void getDataFromApi() {
            // Get a list of up to 10 files.
            try {
                if (mRequest == REQUEST_CREATE_PERMISSION) {
                    permissionmodification();
                } else {
                    if (mRequest == REQUEST_DELETE_PERMISSION) {
                        permissiondelete();
                    }
                }
                return;
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return;
            }
        }


        @Override
        protected void onPreExecute() {
            if (mProgress != null) {
                mProgress.show();
            }
        }

        @Override
        protected void onPostExecute(String out) {
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

}
