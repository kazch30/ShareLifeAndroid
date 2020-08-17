package com.hydroety.sharelife;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileCommon extends BaseActivity {
    private static final String TAG = "FileCommon";
    static final int REQUEST_UPDATE_FILE_LIST = 10020;

    public static List<ShareFileInfo> mShareFileList = null;
    public static ShareFileArrayAdapter mShareFileAdapter = null;
    public static List<PermissionInfo> mAutoCompleteListAll=null;

    public static List<String> mEntryList = null;
    public static List<String> mDirList = null;
    public static String mFolderId="";


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
        if (DBG) Log.i(TAG, "onResume()");
    }

    @Override
    public void onPause() {
        super.onPause();
        if (DBG) Log.i(TAG, "onPause()");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (DBG) Log.i(TAG, "onStop()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (DBG) Log.i(TAG, "onDestroy()");
    }

    @Override
    public void finish() {
        super.finish();
        if (DBG) Log.i(TAG, "finish()");
    }

    protected void update() {
        if (DBG) Log.i(TAG, "update()");

    }

    protected void showpopup() {
        if (DBG) Log.i(TAG,"showpopup()");
    }

    protected void updateFileList() {
        if (DBG) Log.i(TAG,"updateFileList()");

    }

    /**
     * An asynchronous task that handles the Drive API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    protected class FileListRequestTask extends AsyncTask<Void, Void, List<ShareFileInfo>> {
        private com.google.api.services.drive.Drive mService = null;
        private Exception mLastError = null;

        FileListRequestTask(GoogleAccountCredential credential) {
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
        protected List<ShareFileInfo> doInBackground(Void... params) {
            try {
                if (!getFolderId()) {
                    makeFolder();
                }
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private void makeFolder() throws IOException {
            if (DBG) Log.i(TAG, "makeFolder()");
            File fileMetadata = new File();
            fileMetadata.setName(getString(R.string.app_name));
            fileMetadata.setMimeType("application/vnd.google-apps.folder");

            File file = mService.files().create(fileMetadata)
                    .setFields("id, name")
                    .execute();

            if (DBG) Log.d(TAG, "folder id=" + file.getId());
            if (DBG) Log.d(TAG, "folder name=" + file.getName());
            mFolderId = file.getId();
        }

        private boolean getFolderId() throws IOException {
            if (DBG) Log.i(TAG, "getFolderId()");
            FileList result = mService.files().list()
                    .setQ("trashed = false and name contains '" + getString(R.string.app_name) + "' and mimeType='application/vnd.google-apps.folder'")
                    .setFields("nextPageToken, files(id, name, parents)")
                    .execute();
            List<File> files = result.getFiles();
            if (files != null) {
                for (File file : files) {
                    if (DBG) Log.d(TAG, "folder id=" + file.getId());
                    if (DBG) Log.d(TAG, "folder name=" + file.getName());
                    if (DBG) Log.d(TAG, "folder parents=" + file.getParents());
                    if (file.getName().equals(getString(R.string.app_name))){
                        if (DBG) Log.d(TAG, "folder exists.");
                        mFolderId = file.getId();
                        return true;
                    }
                }

            }

            return false;

        }

        /**
         * Fetch a list of up to 10 file names and IDs.
         *
         * @return List of Strings describing files, or an empty list if no files
         * found.
         * @throws IOException
         */
        private List<ShareFileInfo> getDataFromApi() throws IOException {
            // Get a list of up to 10 files.
            mShareFileList = null;
            mShareFileList = new ArrayList<ShareFileInfo>();

            FileList result = mService.files().list()
                    .setQ("name contains '" + getString(R.string.app_name) + "' and trashed = false and mimeType='application/vnd.google-apps.document'")
///                    .setQ("trashed = false and mimeType='application/vnd.google-apps.document'")
                    .setOrderBy("modifiedTime")
                    .setFields("nextPageToken, files(id, name, modifiedTime, iconLink, ownedByMe)")
                    .execute();
            List<File> files = result.getFiles();
            if (files != null) {
                for (File file : files) {
                    ShareFileInfo info = new ShareFileInfo();

                    info.setFileId(file.getId());
                    String name = file.getName();
                    if (DBG) Log.d(TAG, "originalFilename=" + name);

                    int i = name.indexOf("-");
                    if (i > 1) {
                        info.setPngFileId(name.substring(i+1, name.length() ));
                        if (DBG) Log.d(TAG, "PngFileId=" + info.getPngFileId());

                        name = name.substring(0, i);

                    }

                    if (!file.getOwnedByMe()) {
                        name = name + " ( " + getString(R.string.ownedByOther) + " )";
                    }

                    info.setName(name);
                    if (DBG) Log.d(TAG, "name=" + info.getName());

                    info.setModifiedTime(file.getModifiedTime());
                    info.setIconLink(file.getIconLink());
                    info.setOwnedByMe(file.getOwnedByMe());


                    String cacheFile= getCacheDir().getPath() + "/" + info.getFileId() + ".png";
                    if (DBG) Log.d(TAG,"cacheFile=" + cacheFile);

                    if (new java.io.File(cacheFile).exists()) {
                        if (DBG) Log.d(TAG,"cacheFile exists. = " + cacheFile);
                        FileInputStream fin = new FileInputStream(cacheFile);
                        Bitmap bmp = BitmapFactory.decodeStream(fin);
                        info.setThumbnail(bmp);
                    }


                    if (DBG) Log.d(TAG, info.getName() + "(" + info.getFileId() + ")");
                    if (DBG) Log.d(TAG, "更新日" + info.getModifiedTime());
                    if (DBG) Log.d(TAG, "iconLink:" + file.getIconLink());
                    if (DBG) Log.d(TAG, "ownedByMe:" + file.getOwnedByMe());
                    mShareFileList.add(info);

                }
            }
            return mShareFileList;
        }

        @Override
        protected void onPreExecute() {
            if (mProgress != null) {
                mProgress.show();
            }
        }

        @Override
        protected void onPostExecute(List<ShareFileInfo> output) {
            if (mProgress != null && mProgress.isShowing()) {
                mProgress.dismiss();
            }

            updateFileList();

//            new ThumbnailRequestTask(mCredential).execute();

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
                    if (DBG) Log.d(TAG, "The following error occurred:\n"
                            + mLastError.getMessage());

                }
            } else {
                if (DBG) Log.d(TAG, "Request cancelled.");
            }
        }


    }





    /**
     * An asynchronous task that handles the Drive API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    protected class ThumbnailRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.drive.Drive mService = null;
        private Exception mLastError = null;
        private String mFileId="";
        private String mPngFileId="";
        private String mImgName="";
        private List<String> mEntryList = null;
        private List<String> mDirList = null;


        ThumbnailRequestTask(GoogleAccountCredential credential) {
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
        protected List<String> doInBackground(Void... params) {
            try {

                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private void DownloadToImg() throws IOException {
            if (DBG) Log.i(TAG,"DownloadToImg()");
            String dir= getFilesDir().getPath() + "/" + getString(R.string.workdir) + "/";

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            mService.files().get(mPngFileId)
                    .executeMediaAndDownloadTo(outputStream);
//            FileOutputStream out = openFileOutput( getString(R.string.pngfile), MODE_PRIVATE );
            FileOutputStream out = new FileOutputStream(dir + getString(R.string.pngfile));
            out.write(outputStream.toByteArray());
            out.close();

            mImgName = getString(R.string.pngfile);

        }

        private void UnZipFile()  throws IOException {
            if (DBG) Log.i(TAG,"UnZipFile()");

            ZipEntry zipEntry=null;
            ZipInputStream in=null;
            BufferedOutputStream out=null;
            int len = 0;
            String dir= getFilesDir().getPath() + "/" + getString(R.string.workdir) + "/";

//            FileInputStream fin = openFileInput(getString(R.string.zipfile));
            FileInputStream fin = new FileInputStream(dir + getString(R.string.zipfile));

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
                    new java.io.File(dir + entry.substring(0, i+1)).mkdirs();
                }
                if (DBG) Log.d(TAG,"after entry=" + entry);
                mEntryList.add(entry);
//                FileOutputStream fout = openFileOutput(entry,MODE_PRIVATE);
                if (DBG) Log.d(TAG,"path=" + dir);
//                FileOutputStream fout = new FileOutputStream(getFilesDir().getPath()+ "/" + entry);
                out = new BufferedOutputStream(new FileOutputStream(dir + entry));

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
            String dir= getFilesDir().getPath() + "/" + getString(R.string.workdir) + "/";

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            if (DBG) Log.d(TAG,"executeMediaAndDownloadTo()->");
            mService.files().export(mFileId, "application/zip")
                    .executeMediaAndDownloadTo(outputStream);
            if (DBG) Log.d(TAG,"<-executeMediaAndDownloadTo()");
            if (DBG) Log.d(TAG,"openFileOutput()->");
//            FileOutputStream out = openFileOutput( getString(R.string.zipfile), MODE_PRIVATE );
            FileOutputStream out = new FileOutputStream(dir + getString(R.string.zipfile));

            if (DBG) Log.d(TAG,"<-openFileOutput()");
            if (DBG) Log.d(TAG,"out.write()->");
            out.write(outputStream.toByteArray());
            if (DBG) Log.d(TAG,"<-out.write()");

        }

        private void deleteFile() {
            if (DBG) Log.i(TAG,"deleteFile()");
            String dirpatrh= getFilesDir().getPath() + "/" + getString(R.string.workdir) + "/";

            for (String file : mEntryList) {
                if (DBG) Log.d(TAG,"entry=" + file);
                new java.io.File(dirpatrh + file).delete();

            }
            for (String dir : mDirList) {
                if (DBG) Log.d(TAG,"dir=" + dir);
                new java.io.File(dirpatrh + dir).delete();
            }

            mEntryList.clear();
            mDirList.clear();

            new java.io.File(dirpatrh + getString(R.string.zipfile)).delete();
            new java.io.File(dirpatrh + getString(R.string.pngfile)).delete();

        }

        /**
         * Fetch a list of up to 10 file names and IDs.
         *
         * @return List of Strings describing files, or an empty list if no files
         * found.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {

            if (mShareFileList != null) {
                if (DBG) Log.i(TAG,"ThumbnailRequestTask ->");

                mEntryList = new ArrayList<String>();
                mDirList = new ArrayList<String>();

                String dir= getFilesDir().getPath() + "/" + getString(R.string.workdir) + "/";
                new java.io.File(dir).mkdirs();

                for (ShareFileInfo info : mShareFileList) {
                    if (info.getThumbnail() != null) {
                        continue;
                    }
                    mFileId = info.getFileId();
                    mPngFileId = info.getPngFileId();
                    mImgName = "";

                    if (DBG) Log.d(TAG, "mFileId=" + mFileId);
                    if (DBG) Log.d(TAG, "mPngFileId=" + mPngFileId);

                    ExportToImg();
                    UnZipFile();
                    if (mImgName.isEmpty()) {
                        if (!mPngFileId.isEmpty()) {
                            DownloadToImg();
                        }
                    }

                    if (!mImgName.isEmpty()) {
                        if (DBG) Log.d(TAG, "mImgName=" + mImgName);

                        FileInputStream fin = new FileInputStream(dir + mImgName);
                        Bitmap bmp = BitmapFactory.decodeStream(fin);
                        bmp = BitmapHelper.shrinkBitmap(bmp, 100);
                        info.setThumbnail(bmp);

                    }

                    deleteFile();
                }

                new java.io.File(dir).delete();


                if (DBG) Log.i(TAG,"<- ThumbnailRequestTask");

            }

            return null;
        }

        @Override
        protected void onPreExecute() {
//            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
//            mProgress.dismiss();

            updateFileList();

        }

        @Override
        protected void onCancelled() {
//            mProgress.dismiss();
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
                    if (DBG) Log.d(TAG, "The following error occurred:\n"
                            + mLastError.getMessage());

                }
            } else {
                if (DBG) Log.d(TAG, "Request cancelled.");
            }
        }


    }

}
