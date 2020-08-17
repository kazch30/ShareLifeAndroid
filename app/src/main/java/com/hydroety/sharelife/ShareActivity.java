package com.hydroety.sharelife;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Display;
import android.view.Gravity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.AdapterView;
import android.widget.TextView;
import android.os.Handler;


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
import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.Comment;
import com.google.api.services.drive.model.CommentList;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.Reply;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class ShareActivity extends CommentListView {

    private static final String TAG = "share-activity";

    private static final String FILEID_STATE = "FileId";
    private static final String PNGFILEID_STATE = "PngFileId";
    private static final String IMGNAME_STATE = "ImgName";
    private static final String OWNEDBYME_STATE = "OwnedByMe";
    private static final String POSITION_STATE = "Position";
    private String mImgName="";
    private Handler mHandler;
    private EditText mCommentEdit;
    private TextView mPopupTitle;
    private boolean mExitWindow=false;
    private String mPngFileId="";
    private boolean mOwnedByMe;
    private int mPosition;

    private PopupWindow mPopupWindow=null;
    private ArrayList<PopUpInfo> mPopupList=null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DBG) Log.i(TAG,"onCreate()");

        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            if (DBG) Log.d(TAG,"savedInstanceState != null");
            // Restore value of members from saved state
            if (savedInstanceState.containsKey(FILEID_STATE)) {
                mFileId = savedInstanceState.getString(FILEID_STATE);
                if (DBG) Log.d(TAG, "FileId=" + mFileId);
            }
            if (savedInstanceState.containsKey(PNGFILEID_STATE)) {
                mPngFileId = savedInstanceState.getString(PNGFILEID_STATE);
                if (DBG) Log.d(TAG, "PngFileId=" + mPngFileId);
            }
            if (savedInstanceState.containsKey(OWNEDBYME_STATE)) {
                mOwnedByMe = savedInstanceState.getBoolean(OWNEDBYME_STATE);
                if (DBG) Log.d(TAG, "OwnedByMe=" + mOwnedByMe);
            }
            if (savedInstanceState.containsKey(POSITION_STATE)) {
                mPosition = savedInstanceState.getInt(POSITION_STATE);
                if (DBG) Log.d(TAG, "mPosition=" + mPosition);
            }

        } else {
            // Probably initialize members with default values for a new instance

            Intent intent = getIntent();
            mFileId = intent.getStringExtra("FileId");
            if (DBG) Log.d(TAG, "FileId=" + mFileId);
            mPngFileId = intent.getStringExtra("PngFileId");
            if (DBG) Log.d(TAG, "PngFileId=" + mPngFileId);
            mImgName = intent.getStringExtra("ImgName");
            if (DBG) Log.d(TAG, "ImgName=" + mImgName);
            mOwnedByMe = intent.getBooleanExtra("OwnedByMe", true);
            if (DBG) Log.d(TAG, "OwnedByMe=" + mOwnedByMe);
            mPosition = intent.getIntExtra("Position", 0);
            if (DBG) Log.d(TAG, "mPosition=" + mPosition);
        }

        mHandler = new Handler();

        if (mEntryList == null) {
            mEntryList = new ArrayList<String>();
        }
        if (mDirList == null) {
            mDirList = new ArrayList<String>();
        }

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
        savedInstanceState.putBoolean(OWNEDBYME_STATE, mOwnedByMe);
        savedInstanceState.putInt(POSITION_STATE, mPosition);

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
        if (savedInstanceState.containsKey(OWNEDBYME_STATE)) {
            mOwnedByMe = savedInstanceState.getBoolean(OWNEDBYME_STATE);
            if (DBG) Log.d(TAG, "OwnedByMe=" + mOwnedByMe);
        }
        if (savedInstanceState.containsKey(POSITION_STATE)) {
            mPosition = savedInstanceState.getInt(POSITION_STATE);
            if (DBG) Log.d(TAG, "mPosition=" + mPosition);
        }
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

        if (mImgName.isEmpty()) {
            getResultsFromApi();

        } else {
            LoardImgFile();

        }
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

        deleteFile();
    }

    @Override
    public void finish()
    {
        super.finish();
        if (DBG) Log.i(TAG,"finish()");
    }

    @Override
    protected void updateFileList() {
        if (DBG) Log.i(TAG,"updateFileList()");

        mShareFileAdapter.clear();
        mShareFileAdapter.addAll(mShareFileList);
        mShareFileAdapter.notifyDataSetChanged();

        if (mExitWindow) {
            finish();
        }

    }

    @Override
    protected void getResultsFromApi() {
        if (DBG) Log.i(TAG,"getResultsFromApi()");
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            if (DBG) Log.d(TAG,"No network connection available.");
        } else {
            if (mRequest == REQUEST_GET_COMMENTLIST || mRequest == REQUEST_UPDATE_LIST) {
                new CommentListRequestTask(mCredential).execute();
            }else {
                if (mRequest == REQUEST_SHARE_FILE) {
                    new PermissionListRequestTask(mCredential).execute();
                }else {
                    if (mRequest == REQUEST_UPDATE_FILE_LIST) {
                        new FileListRequestTask(mCredential).execute();
                    } else {
                        if (mRequest == REQUEST_CREATE_REPLY) {
                            new CreateReplyRequestTask(mCredential).execute();
                        } else {
                            if (mRequest == REQUEST_DELETE_REPLY || mRequest == REQUEST_GET_REPLY || mRequest == REQUEST_UPDATE_REPLY) {
                                new RrplyRequestTask(mCredential).execute();
                            } else {
                                if (mRequest == REQUEST_DELETE_COMMENT || mRequest == REQUEST_GET_COMMENT || mRequest == REQUEST_UPDATE_COMMENT) {
                                    new CommentRequestTask(mCredential).execute();
                                } else {
                                    new MakeRequestTask(mCredential).execute();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void deleteFile(){
        if (DBG) Log.i(TAG,"deleteFile()");

        for (String file : mEntryList) {
            if (DBG) Log.d(TAG,"entry=" + file);
            new java.io.File(getFilesDir().getPath() + "/" + file).delete();

        }
        for (String dir : mDirList) {
            if (DBG) Log.d(TAG,"dir=" + dir);
            new java.io.File(getFilesDir().getPath() + "/" + dir).delete();
        }

        mEntryList = null;
        mDirList = null;

        new java.io.File(getFilesDir().getPath() + "/" + getString(R.string.zipfile)).delete();
        new java.io.File(getFilesDir().getPath() + "/" + getString(R.string.pngfile)).delete();

    }


    @Override
    protected void updatePermissionList() {
        if (DBG) Log.i(TAG,"updatePermissionList()");

        if (mRequest == REQUEST_SHARE_FILE) {
            final Intent intent = new Intent(ShareActivity.this, PermissionListViewActivity.class);
            intent.putExtra("FileId", mFileId);
            intent.putExtra("PngFileId", mPngFileId);
            startActivity(intent);
        }

    }

    private void LoardImgFile() {
        if (DBG) Log.i(TAG,"LoardImgFile()");
        if (DBG) Log.d(TAG,"getFilesDir()=" + getFilesDir().getPath());
        if (DBG) Log.d(TAG,"mImgName=" + mImgName);
        try {
            FileInputStream fin = new FileInputStream(getFilesDir().getPath() + "/" + mImgName);
            Bitmap bmp = BitmapFactory.decodeStream(fin);
            ImageView appInfoImage = (ImageView) findViewById(R.id.selected_photo);
            if (bmp != null) {
                appInfoImage.setImageBitmap(bmp);
            }
        } catch (Exception e) {
            if (DBG) Log.d(TAG,"The following error occurred:\n"
                    + e.getMessage());
        }
    }

    /**
     * An asynchronous task that handles the Drive API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<Comment>> {
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
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<Comment> doInBackground(Void... params) {
            try {
                switch (mRequest){
                    case REQUEST_OPEN_FILE:
                        ExportToImg();
                        UnZipFile();
                        if (mImgName.isEmpty()) {
                            if (!mPngFileId.isEmpty()) {
                                DownloadToImg();
                            }
                        }
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                LoardImgFile();
                            }
                        });
                        return null;
                    case REQUEST_GET_COMMENTLIST:
                        return retrieveComments();
                    case REQUEST_CREATE_COMMENT:
                        createComment();
                        return null;
                    case REQUEST_DELETE_FILE:
                        deleteFile();
                        return null;
                }

                return null;

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
        private List<String> getDataFromApi() throws IOException {
            // Get a list of up to 10 files.
            List<String> fileInfo = new ArrayList<String>();
            FileList result = mService.files().list()
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
            List<File> files = result.getFiles();
            if (files != null) {
                for (File file : files) {
                    fileInfo.add(String.format("%s (%s)\n",
                            file.getName(), file.getId()));
                }
            }
            return fileInfo;
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
                    .setEmailAddress("grouphone30@gmail.com");
            mService.permissions().create(mFileId, userPermission)
                    .setFields("id")
                    .queue(batch, callback);

            Permission anyonePermission = new Permission()
                    .setType("anyone")
                    .setRole("commenter");
            mService.permissions().create(mFileId, anyonePermission)
                    .setFields("id")
                    .queue(batch, callback);

            batch.execute();
        }

        private void UnZipFile()  throws IOException {
            if (DBG) Log.i(TAG,"UnZipFile()");

            ZipEntry zipEntry=null;
            ZipInputStream in=null;
            BufferedOutputStream out=null;
            int len = 0;

            FileInputStream fin = openFileInput(getString(R.string.zipfile));

            in = new ZipInputStream(new BufferedInputStream(fin));

//            new java.io.File(getFilesDir().getPath() + "/images").mkdirs();

            while( (zipEntry = in.getNextEntry()) != null)
            {
                // 出力用ファイルストリームの生成
                String entry = zipEntry.getName();
                if (DBG) Log.d(TAG,"entry=" + entry);
                int i = entry.indexOf('/');
                if (i > 0) {
//                    entry = entry.substring(0, i);
                    if (DBG) Log.d(TAG,"dir=" + entry.substring(0, i+1));
                    mDirList.add(entry.substring(0, i+1));
                    new java.io.File(getFilesDir().getPath() + "/" + entry.substring(0, i+1)).mkdirs();
                }
                if (DBG) Log.d(TAG,"after entry=" + entry);
                mEntryList.add(entry);
//                FileOutputStream fout = openFileOutput(entry,MODE_PRIVATE);
                if (DBG) Log.d(TAG,"path=" + getFilesDir().getPath());
//                FileOutputStream fout = new FileOutputStream(getFilesDir().getPath()+ "/" + entry);
                out = new BufferedOutputStream(new FileOutputStream(getFilesDir().getPath()+ "/" + entry));

                i = entry.indexOf(".png");
                if (i > 0) {
                    mImgName = entry;
                }

                // エントリの内容を出力
                byte[] buffer = new byte[1024];
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }

                in.closeEntry();
                out.close();
                out = null;
            }

        }

        private void ExportToImg()  throws IOException {
            if (DBG) Log.i(TAG,"ExportToImg()");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            if (DBG) Log.d(TAG,"executeMediaAndDownloadTo()->");
            mService.files().export(mFileId, "application/zip")
                    .executeMediaAndDownloadTo(outputStream);
            if (DBG) Log.d(TAG,"<-executeMediaAndDownloadTo()");
            if (DBG) Log.d(TAG,"openFileOutput()->");
            FileOutputStream out = openFileOutput( getString(R.string.zipfile), MODE_PRIVATE );
            if (DBG) Log.d(TAG,"<-openFileOutput()");
            if (DBG) Log.d(TAG,"out.write()->");
            out.write(outputStream.toByteArray());
            if (DBG) Log.d(TAG,"<-out.write()");

        }

        private void DownloadToImg() throws IOException {
            if (DBG) Log.i(TAG,"DownloadToImg()");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            mService.files().get(mPngFileId)
                    .executeMediaAndDownloadTo(outputStream);
            FileOutputStream out = openFileOutput( getString(R.string.pngfile), MODE_PRIVATE );
            out.write(outputStream.toByteArray());

            mImgName = getString(R.string.pngfile);

        }

        private void deleteFile() throws IOException {
            if (DBG) Log.i(TAG,"deleteFile()");

            mService.files().delete(mFileId).execute();
            if (!mPngFileId.isEmpty()) {
                mService.files().delete(mPngFileId).execute();
            }

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
/*                List<Comment> files = comments.getComments();
                if (files != null) {
                    for (Comment file : files) {
                        if (DBG) Log.d(TAG,"commentId:" + file.getId());
                        if (DBG) Log.d(TAG,"name:" + file.getAuthor().getDisplayName());
                        if (DBG) Log.d(TAG,"date:" + file.getModifiedTime().toString());
                        if (DBG) Log.d(TAG,"comment:" + file.getContent());

                    }
                }
*/
            return comments.getComments();
        }

        private void createComment() throws IOException {
            if (DBG) Log.i(TAG,"createComment()");
            if (DBG) Log.d(TAG, "new comment:" + mContent);

            Comment newComment = new Comment();
            DateTime dt = new DateTime(new Date());

            newComment.setContent(mContent);
            newComment.setCreatedTime(dt);
            newComment.setModifiedTime(dt);

            mService.comments().create(mFileId, newComment)
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
        protected void onPostExecute(List<Comment> output) {
            if (DBG) Log.i(TAG,"onPostExecute()");
//            List<CommentInfo> CommentList;
//            mRequest = REQUEST_OPEN_FILE;
            if (mProgress != null && mProgress.isShowing()) {
                mProgress.dismiss();
            }

            if (mRequest == REQUEST_OPEN_FILE || mRequest == REQUEST_CREATE_COMMENT) {
                mRequest = REQUEST_UPDATE_LIST;
                getResultsFromApi();
            } else {
                if (mRequest == REQUEST_DELETE_FILE) {
//                    mRequest = REQUEST_UPDATE_FILE_LIST;
//                    getResultsFromApi();

                    if (DBG) Log.d(TAG, "mPosition=" + mPosition);
                    mShareFileList.remove(mPosition);
                    mShareFileAdapter.notifyDataSetChanged();

                    new java.io.File(getCacheDir().getPath() + "/" + mFileId + ".png").delete();

                    if (mExitWindow) {
                        finish();
                    }
//                    updateFileList();
                }
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



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.web, menu);
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);

            switch (item.getItemId()) {
                case R.id.item1:
                    item.setEnabled(mOwnedByMe);
                    if (!mOwnedByMe) {
                        item.setIcon(R.drawable.ic_person_add_black_24dp);
                    }
                    break;
                case R.id.item3:
                    item.setEnabled(mOwnedByMe);
                    if (!mOwnedByMe) {
                        item.setIcon(R.drawable.ic_delete_black_24dp);
                    }
                    break;

            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.item1:
                mRequest = REQUEST_SHARE_FILE;
                getResultsFromApi();
                return true;
            case R.id.item2:
//                mRequest = REQUEST_GET_COMMENTLIST;
//                getResultsFromApi();
                makeCommentPopup();
                return true;
            case R.id.item3:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.delete_file_title))
                        .setMessage(getString(R.string.delete_file_msg))
                        .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // OK button pressed
                                mRequest = REQUEST_DELETE_FILE;
                                getResultsFromApi();
                                mExitWindow = true;

                            }
                        })
                        .setNegativeButton(getString(android.R.string.cancel), null)
                        .show();


                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
