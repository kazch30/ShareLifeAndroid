package com.hydroety.sharelife;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.os.Environment;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.view.Display;
import android.support.media.ExifInterface;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.Collections;


public class CreateDocs extends FileCommon {
    private static final String TAG = CreateDocs.class.getName();
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    private static final int REQUEST_TAKE_PHOTO = 2;
    private static final int READ_REQUEST_CODE = 3;

    private static final int BITMAP_MAX_HEIGHT = 1800;
    private static Date mDateCameraIntentStarted = null;
    private static int mRotateXDegrees = 0;


    protected boolean mFinish = false;
    protected Bitmap mBitmapSrc=null;
    protected boolean mCaptureImage;
    private String mFileId="";
    private String mPngFileId="";
    private String mCurrentPhotoPath="";
    private ShareFileInfo mFileInfo;

    private Uri mPhotoURI;

    private int mRequest=REQUEST_CODE_CAPTURE_IMAGE;

    private String mImgName="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DBG) Log.i(TAG, "onCreate()");

        Intent intent = getIntent();
        mCaptureImage = intent.getBooleanExtra("CaptureImage", true);
        if (DBG) Log.d(TAG,"mCaptureImage=" + mCaptureImage);

        mEntryList = new ArrayList<String>();
        mDirList = new ArrayList<String>();

        if (mCaptureImage) {
            String manufacturer = android.os.Build.MANUFACTURER.toLowerCase();
            if (!(manufacturer.contains("samsung")) && !(manufacturer.contains("sony"))) {

                dispatchTakePictureIntent();

            } else {
                if (DBG) Log.d(TAG, "\"samsung\" || \"sony\"");

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                mDateCameraIntentStarted = new Date();
                startActivityForResult(takePictureIntent,
                        REQUEST_CODE_CAPTURE_IMAGE);

            }
        } else {
            openDocument();
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        if (DBG) Log.i(TAG, "onStart()");
        if (mFinish) {
            finish();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (DBG) Log.i(TAG, "onResume()");

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

    private void openDocument() {
        if (DBG) Log.i(TAG,"openDocument()");

        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/jpeg");

        }

        startActivityForResult(intent, READ_REQUEST_CODE);

    }

    private void deleteFile(){
        if (DBG) Log.i(TAG,"deleteFile()");

        if (!mCurrentPhotoPath.isEmpty()) {
            if (DBG) Log.d(TAG,"deleteFile = " + mCurrentPhotoPath);

            new java.io.File(mCurrentPhotoPath).delete();
        }

    }

    private java.io.File createImageFile() throws IOException {
        if (DBG) Log.i(TAG,"createImageFile()");
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
//        String imageFileName = "SharePhoto";
//        java.io.File storageDir = android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        java.io.File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        if (DBG) Log.d(TAG,"getExternalFilesDir = " + storageDir.getPath() );
        // Make sure the Pictures directory exists.
//        storageDir.mkdir();
//        java.io.File storageDir = new java.io.File(getFilesDir().getPath());
        java.io.File image = java.io.File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
//                ".png",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        if (DBG) Log.d(TAG,"mCurrentPhotoPath = " + mCurrentPhotoPath);

        return image;
    }

    private void dispatchTakePictureIntent() {
        if (DBG) Log.i(TAG,"dispatchTakePictureIntent()");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            java.io.File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();///
                if (DBG) Log.d(TAG, "createImageFile() failed.");
                return;
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                if (DBG) Log.d(TAG, "FileProvider.getUriForFile->");
                mPhotoURI = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".fileprovider",
                        photoFile);
                if (DBG) Log.d(TAG, "<-FileProvider.getUriForFile");
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoURI);

                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void update() {
        if (DBG) Log.i(TAG,"update()");

        if (mShareFileAdapter != null && mFileInfo != null) {
            mShareFileList.add(mFileInfo);
            mShareFileAdapter.notifyDataSetChanged();
        }

        finish();

    }

    private void ViewDocs() {
        Intent intent = new Intent(this.getApplicationContext(), ShareActivity.class);
        if (!mFileId.isEmpty()) {
            intent.putExtra("FileId", mFileId);
            intent.putExtra("PngFileId", mPngFileId);
            intent.putExtra("ImgName", mImgName);
            intent.putExtra("OwnedByMe", true);
            intent.putExtra("Position", mShareFileList.size());

            startActivity(intent);

        }

        mFinish = true;

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if (DBG) Log.i(TAG, "onActivityResult");
        switch (requestCode) {
            case REQUEST_CODE_CAPTURE_IMAGE:
                /*
                if (DBG) Log.d(TAG, "requestCode = REQUEST_CODE_CAPTURE_IMAGE");
                // Called after a photo has been taken.
                if (resultCode == Activity.RESULT_OK) {
                    if (DBG) Log.d(TAG, "resultCode == Activity.RESULT_OK");
                    // Store the image data as a bitmap for writing later.
                    mBitmapSrc = (Bitmap) data.getExtras().get("data");
                    getResultsFromApi();

                } else if (resultCode == Activity.RESULT_CANCELED) {
                    if (DBG) Log.d(TAG, "resultCode == Activity.RESULT_CANCELED");
                    mFinish = true;

                } else {
                    if (DBG) Log.d(TAG, "resultCode == failed!!");
                    mFinish = true;
                }
                */
                onTakePhotoActivityResult(requestCode, resultCode, data);
                break;
            case REQUEST_TAKE_PHOTO:
                if (DBG) Log.d(TAG, "requestCode = REQUEST_TAKE_PHOTO");
                if (resultCode == Activity.RESULT_OK) {
                    if (DBG) Log.d(TAG, "resultCode == Activity.RESULT_OK");

                    getResultsFromApi();

                } else if (resultCode == Activity.RESULT_CANCELED) {
                    if (DBG) Log.d(TAG, "resultCode == Activity.RESULT_CANCELED");
                    mFinish = true;

                } else {
                    if (DBG) Log.d(TAG, "resultCode == failed!!");
                    mFinish = true;
                }
                break;
            case READ_REQUEST_CODE:
                if (DBG) Log.d(TAG, "requestCode = READ_REQUEST_CODE");
                if (resultCode == Activity.RESULT_OK) {
                    if (DBG) Log.d(TAG, "resultCode == Activity.RESULT_RESULT_OK");
                    Uri uri = null;
                    Cursor myCursor = null;

                    if (data != null) {
                        uri = data.getData();
                        if (DBG) Log.d(TAG, "uri =" + uri);
                        try {
                            // Create a Cursor to obtain the file Path for the large image
                            String[] projection = {MediaStore.Images.ImageColumns.ORIENTATION};
                            myCursor = getContentResolver().query(uri,
                                    projection, null, null, null);

                            myCursor.moveToFirst();

                            mRotateXDegrees = myCursor.getInt(myCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION));
                            if (DBG) Log.d(TAG, "mRotateXDegrees = " + mRotateXDegrees);



                            mBitmapSrc = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            getResultsFromApi();

                        }catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (myCursor != null) {
                                myCursor.close();
                            }
                        }
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    if (DBG) Log.d(TAG, "resultCode == Activity.RESULT_CANCELED");
                    mFinish = true;

                } else {
                    if (DBG) Log.d(TAG, "resultCode == failed!!");
                    mFinish = true;
                }
                break;

        }

    }

    private void onTakePhotoActivityResult(int requestCode, int resultCode, Intent intent) {
        if (DBG) Log.i(TAG,"onTakePhotoActivityResult()");
        if (resultCode == RESULT_OK) {
            Cursor myCursor = null;
            Date dateOfPicture = null;
            try {
                // Create a Cursor to obtain the file Path for the large image
                String[] largeFileProjection = {MediaStore.Images.ImageColumns._ID,
                        MediaStore.Images.ImageColumns.DATA,
                        MediaStore.Images.ImageColumns.ORIENTATION,
                        MediaStore.Images.ImageColumns.DATE_TAKEN};
                String largeFileSort = MediaStore.Images.ImageColumns._ID + " DESC";
                myCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        largeFileProjection, null, null, largeFileSort);
                myCursor.moveToFirst();
                // This will actually give you the file path location of the image.
                String largeImagePath = myCursor.getString(myCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
                Uri tempCameraPicUri = Uri.fromFile(new java.io.File(largeImagePath));
                if (tempCameraPicUri != null) {
                    if (DBG) Log.d(TAG, "tempCameraPicUri=" + tempCameraPicUri);

                    dateOfPicture = new Date(myCursor.getLong(myCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN)));
                    if (dateOfPicture != null && dateOfPicture.after(mDateCameraIntentStarted)) {
                        if (DBG) Log.d(TAG, "dateOfPicture != null");
                        mPhotoURI = tempCameraPicUri;
                        mRotateXDegrees = myCursor.getInt(myCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION));
                    }

                }
            } catch (Exception e) {
                if (DBG) Log.w("TAG", "Exception - optaining the picture's uri failed: " + e.toString());
                mFinish = true;

            } finally {
                if (myCursor != null) {
                    myCursor.close();
                }
            }

            if (mPhotoURI == null) {
                try {
                    if (DBG) Log.d(TAG, "cameraPicUri = intent.getData()");
                    if (intent != null) {
                        mPhotoURI = intent.getData();
                    }
                } catch (Exception e){
                    if (DBG) Log.d(TAG,"error_could_not_take_photo.");
                }
            }

            if (mPhotoURI != null) {
                if (DBG) Log.d(TAG,"cameraPicUri=" + mPhotoURI);
                if (DBG) Log.d(TAG,"mRotateXDegrees=" + mRotateXDegrees);

            } else {
                if (DBG) Log.d(TAG,"intent.getExtras().get(\"data\")");
                // Store the image data as a bitmap for writing later.
                if (intent != null) {
                    mBitmapSrc = (Bitmap) intent.getExtras().get("data");
                }

            }

            getResultsFromApi();

        } else if (resultCode == Activity.RESULT_CANCELED) {
            if (DBG) Log.d(TAG, "resultCode == Activity.RESULT_CANCELED");
            mFinish = true;
        } else {
            if (DBG) Log.d(TAG, "resultCode == failed!!");
            mFinish = true;
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
//            if (mRequest == REQUEST_UPDATE_FILE_LIST) {
//                new FileListRequestTask(mCredential).execute();
//            } else {
                new MakeRequestTask(mCredential).execute();
//            }
        }
    }

    /**
         * An asynchronous task that handles the Drive API call.
         * Placing the API calls in their own task ensures the UI stays responsive.
         */
    private class MakeRequestTask extends AsyncTask<Void, Void, String> {
        private com.google.api.services.drive.Drive mService = null;
        private Exception mLastError = null;
        private FileContent mMediaContent;
        private Bitmap mBitmapToSave=null;
        private Bitmap mBitmapThumbnail=null;

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
        protected String doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private boolean saveFileToDrive() {
            if (DBG) Log.i(TAG, "saveFileToDrive()->");
            final Bitmap image = mBitmapToSave;

            if (image == null) {
                return false;
            }
            if (DBG) Log.d(TAG, "New contents created.");
            ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);
            try {
                FileOutputStream out = openFileOutput( getString(R.string.pngfile), MODE_PRIVATE );///
                out.write(bitmapStream.toByteArray());///
            } catch (IOException e1) {
                e1.printStackTrace();///
                if (DBG) Log.d(TAG, "Unable to write file contents.");
                return false;
            }

            if (DBG) Log.i(TAG, "<- saveFileToDrive()");
            return true;
        }

        private void saveThumbnail(Bitmap image) {
            if (DBG) Log.i(TAG, "saveThumbnail()");

            String cacheFile= getCacheDir().getPath() + "/" + mFileInfo.getFileId() + ".png";
            if (DBG) Log.i(TAG,"cacheFile=" + cacheFile);

            ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);
            try {
                FileOutputStream out = new FileOutputStream(cacheFile);
                out.write(bitmapStream.toByteArray());///
            } catch (IOException e1) {
                e1.printStackTrace();///
                if (DBG) Log.d(TAG, "Unable to write file contents.");
            }

        }

        private boolean makeFileToDrive() throws IOException{
            if (DBG) Log.i(TAG,"makeFileToDrive()");

            int x= 30;
            int y= 30;

            mFileInfo = new ShareFileInfo();

            if (mPhotoURI != null && !mCurrentPhotoPath.isEmpty()) {
                getrotateXDegrees(mCurrentPhotoPath);
            }

            Bitmap bmp = getCaptureImage();
            if (bmp != null) {
                bmp = addTextToImage(bmp, getString(R.string.app_name), x, y, Color.GREEN, 100, 24, false);
                bmp = addTextToImage(bmp, getString(R.string.app_name), x, (y+=30), Color.BLUE, 100, 24, false);
                bmp = addTextToImage(bmp, getString(R.string.app_name), x, (y+=30), Color.RED, 100, 24, false);
                bmp = addTextToImage(bmp, getString(R.string.app_name), x, (y+=30), Color.WHITE, 100, 24, false);
                mBitmapToSave = addTextToImage(bmp, getString(R.string.app_name), x, (y+=30), Color.BLACK, 100, 24, false);

                mBitmapThumbnail = BitmapHelper.shrinkBitmap(mBitmapToSave, 100);

                return true;
            }

            return false;
        }

        private void getrotateXDegrees(String absolutePath) throws IOException {
            if (DBG) Log.i(TAG,"getrotateXDegrees()");
            if (mPhotoURI != null) {

                ExifInterface exif = new ExifInterface(absolutePath);
                int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                mRotateXDegrees = 0;
                switch (exifOrientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        mRotateXDegrees = 90;
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_180:
                        mRotateXDegrees = 180;
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_270:
                        mRotateXDegrees = 270;
                        break;
                }

                if (DBG) Log.d(TAG,"mRotateXDegrees = " + mRotateXDegrees);

            }

        }

        private Bitmap getCaptureImage()  throws IOException  {
            if (DBG) Log.i(TAG,"getCaptureImage() ->");

            Bitmap bmp = null;

            if (DBG) Log.i(TAG,"mPhotoURI=" + mPhotoURI);
            if (mPhotoURI != null) {
                bmp = BitmapHelper.readBitmap(CreateDocs.this, mPhotoURI);
            } else {
                bmp = mBitmapSrc;
            }

            if (bmp != null) {
                int w = bmp.getWidth();
                int h = bmp.getHeight();
                if (DBG) Log.d(TAG,"Width = " + w);
                if (DBG) Log.d(TAG,"Height = " + h);

                //デバイスサイズを取得
                Display d = getWindowManager().getDefaultDisplay();
                Point p2 = new Point();

                d.getSize(p2);
                if (DBG) Log.d(TAG,"p2.x = " + p2.x);
                if (DBG) Log.d(TAG,"p2.y = " + p2.y);
/*                if (h > (p2.y / 2)) {
                    h = p2.y / 2;
                }*/
                if (h > (p2.y < BITMAP_MAX_HEIGHT ? p2.y : BITMAP_MAX_HEIGHT)) {
                    h = (p2.y < BITMAP_MAX_HEIGHT ? p2.y : BITMAP_MAX_HEIGHT);
                }
                if (DBG) Log.d(TAG,"h = " + h);

                bmp = BitmapHelper.shrinkBitmap(bmp, h, mRotateXDegrees);


            }else{
                if (DBG) Log.d(TAG,"error_could_not_take_photo.");
            }


            if (DBG) Log.i(TAG,"<- getCaptureImage()");
            return bmp;
        }


        private Bitmap addTextToImage(Bitmap src, String textToAddOnImage, int x, int y, int color, int alpha, int size, boolean underline) {
            if (DBG) Log.i(TAG,"addTextToImage()");

            int w = src.getWidth();
            int h = src.getHeight();
            if (DBG) Log.d(TAG,"w = " + w);
            if (DBG) Log.d(TAG,"h = " + h);

            Bitmap result = Bitmap.createBitmap(w, h, src.getConfig());

            Canvas canvas = new Canvas(result);
            canvas.drawBitmap(src, 0, 0, null);

            Paint paint = new Paint();
            paint.setColor(color);
            paint.setAlpha(alpha);
            paint.setTextSize(size);
            paint.setAntiAlias(true);
            paint.setUnderlineText(underline);
            canvas.drawText(textToAddOnImage, x, y, paint);

            return result;
        }


        private void uploads()  throws IOException {
            if (DBG) Log.i(TAG,"uploads()");

            String path = getFilesDir().getPath() + "/" + getString(R.string.pngfile);
            if (DBG) Log.d(TAG,"path: " + path);
            File fileMetadata = new File();
            fileMetadata.setName(getString(R.string.pngfile));
            if (!mFolderId.isEmpty()) {
                fileMetadata.setParents(Collections.singletonList(mFolderId));
            }
            fileMetadata.setMimeType("image/png");

            java.io.File filePath = new java.io.File(path);

            mMediaContent = new FileContent("image/png", filePath);
            File file = mService.files().create(fileMetadata, mMediaContent)
                    .setFields("id")
                    .execute();
            if (DBG) Log.d(TAG,"PNGFile ID: " + file.getId());
            mPngFileId = file.getId();

        }

        private void updateDocs()  throws IOException {
            if (DBG) Log.i(TAG,"updateDocs()");
            String path = getFilesDir().getPath() + "/" + getString(R.string.pngfile);
            String name = getString(R.string.app_name);
            if (!mPngFileId.isEmpty()){
                name = name + "-" + mPngFileId;
                mFileInfo.setPngFileId(mPngFileId);
            }
            File fileMetadata = new File();
            fileMetadata.setName(name);

            File file = mService.files().update(mFileId, fileMetadata)
                    .setFields("id, name, mimeType")
                    .execute();
            if (DBG) Log.d(TAG,"File ID: " + file.getId());
            if (DBG) Log.d(TAG,"name: " + file.getName());
            if (DBG) Log.d(TAG,"MimeType: " + file.getMimeType());

        }

        private void ImportingtoGoogleDocstypes()  throws IOException {
            if (DBG) Log.i(TAG,"ImportingtoGoogleDocstypes()");

            String path = getFilesDir().getPath() + "/" + getString(R.string.pngfile);
            if (DBG) Log.d(TAG,"path: " + path);
            File fileMetadata = new File();
            String name = getString(R.string.app_name);

            mFileInfo.setName(name);

            if (!mPngFileId.isEmpty()){
                name = name + "-" + mPngFileId;
                mFileInfo.setPngFileId(mPngFileId);
            }
            fileMetadata.setName(name);
            if (!mFolderId.isEmpty()) {
                fileMetadata.setParents(Collections.singletonList(mFolderId));
            }
            fileMetadata.setMimeType("application/vnd.google-apps.document");

            java.io.File filePath = new java.io.File(path);

            mMediaContent = new FileContent("image/png", filePath);
            File file = mService.files().create(fileMetadata, mMediaContent)
                    .setFields("id, name, modifiedTime, iconLink, ownedByMe")
                    .execute();
            if (DBG) Log.d(TAG,"File ID: " + file.getId());
            if (DBG) Log.d(TAG,"name: " + file.getName());
            mFileId = file.getId();

            mFileInfo.setFileId(file.getId());
            mFileInfo.setModifiedTime(file.getModifiedTime());
            mFileInfo.setIconLink(file.getIconLink());
            mFileInfo.setOwnedByMe(file.getOwnedByMe());
            if (mBitmapThumbnail != null) {
                mFileInfo.setThumbnail(mBitmapThumbnail);
                saveThumbnail(mBitmapThumbnail);
            }

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


        /**
         * Fetch a list of up to 10 file names and IDs.
         * @return List of Strings describing files, or an empty list if no files
         *         found.
         * @throws IOException
         */
        private String getDataFromApi() throws IOException {

            if (makeFileToDrive()) {
               if (saveFileToDrive()) {
                    ImportingtoGoogleDocstypes();
                    ExportToImg();
                    UnZipFile();

                    if (mImgName.isEmpty()) {
                        uploads();
                        updateDocs();
                    }
                }
            }

            return null;
        }


        @Override
        protected void onPreExecute() {
            if (mProgress != null) {
                mProgress.show();
            }
        }

        @Override
        protected void onPostExecute(String output) {
            if (DBG) Log.i(TAG,"onPostExecute()");

            if (mProgress != null && mProgress.isShowing()) {
                mProgress.dismiss();
            }

            ViewDocs();
            update();

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
