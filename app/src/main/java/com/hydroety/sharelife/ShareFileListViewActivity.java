package com.hydroety.sharelife;

import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import com.google.gson.Gson;
import com.google.common.reflect.TypeToken;
import java.lang.reflect.Type;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;


public class ShareFileListViewActivity extends FileCommon implements OnItemClickListener {
    private static final String TAG = "ShareFileListView";
    public static final int MY_PERMISSIONS_REQUEST_GET_ACCOUNTS = 100;

    private List<ShareFileInfo> mList;
    private boolean mupdateFileList = false;
    private AdView mAdView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DBG) Log.d(TAG,"onCreate()");

        setContentView(R.layout.sharefile_list_view);

        if (mShareFileList == null) {

            CheckAccounts();
        } else {
            setAdpter();
        }
/*
        MobileAds.initialize(this, getString(R.string.banner_ad_unit_id));

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
*/
    }

    @Override
    public void onStart() {
        super.onStart();
        if (DBG) Log.d(TAG,"onStart()");
    }

    @Override
    public void onResume() {
        if (DBG) Log.i(TAG,"onResume()");
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onPause() {
        if (DBG) Log.i(TAG,"onPause()");
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (DBG) Log.i(TAG,"onStop()");
    }

    @Override
    public void onDestroy() {
        if (DBG) Log.i(TAG,"onDestroy()");
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();

        mShareFileList = null;
        saveAutoCompleteList();

    }

    @Override
    public void finish()
    {
        super.finish();
        if (DBG) Log.i(TAG,"finish()");
    }

    private void loadAdRequest() {
        if (mAdView == null) {
            if (DBG) Log.i(TAG,"loadAdRequest()");
            MobileAds.initialize(this, getString(R.string.admob_app_iD));

            mAdView = findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
    }

    private void getAutoCompleteList() {
        if (DBG) Log.i(TAG, "getAutoCompleteList()");
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.pref_name), Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(getString(R.string.list_tag), null);
        Type type = new TypeToken<ArrayList<PermissionInfo>>() {
        }.getType();
        mAutoCompleteListAll = gson.fromJson(json, type);
        if (mAutoCompleteListAll != null) {
            if (DBG) Log.d(TAG, "mAutoCompleteListAll size:" + mAutoCompleteListAll.size());

            for (PermissionInfo info : mAutoCompleteListAll) {
                if (DBG) Log.d(TAG, "PermissionId:" + info.getPermissionId());
                if (DBG) Log.d(TAG, "type:" + info.getType());
                if (DBG) Log.d(TAG, "emailAddress:" + info.getAddress());
                if (DBG) Log.d(TAG, "role:" + info.getRole());
                if (DBG) Log.d(TAG, "displayName:" + info.getName());
                if (DBG) Log.d(TAG, "photoLink:" + info.getPhotoLink());
            }
        }
    }

    private void saveAutoCompleteList() {
        if (DBG) Log.i(TAG,"saveAutoCompleteList()");
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.pref_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mAutoCompleteListAll);
        editor.putString(getString(R.string.list_tag), json);
        editor.commit();
    }

    private void setAdpter() {
        if (DBG) Log.i(TAG,"setAdpter()");

        mList = mShareFileList;

        // adapterのインスタンスを作成
        mShareFileAdapter =
                new ShareFileArrayAdapter(this, R.layout.sharefile_list_view_item, mList);

        ListView lv = (ListView) findViewById(R.id.listview);
        lv.setAdapter(mShareFileAdapter);
        lv.setOnItemClickListener(this);

    }

    private void CheckAccounts() {
        if (DBG) Log.d(TAG,"CheckAccounts() ->");

        if (checkPermissionAccounts()) {
            getResultsFromApi();
        }
        if (DBG) Log.d(TAG,"<- CheckAccounts()");

    }

    private boolean checkPermissionAccounts() {
        if (DBG) Log.d(TAG,"checkPermissionAccounts() ->");
        boolean ret = false;

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.GET_ACCOUNTS)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                if (DBG) Log.d(TAG,"checkPermissionAccounts() user *asynchronously*");

                if (DBG) Log.d(TAG,"checkPermissionAccounts() request");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.GET_ACCOUNTS},
                        MY_PERMISSIONS_REQUEST_GET_ACCOUNTS);

            } else {

                // No explanation needed, we can request the permission.


                if (DBG) Log.d(TAG,"checkPermissionAccounts() request");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.GET_ACCOUNTS},
                        MY_PERMISSIONS_REQUEST_GET_ACCOUNTS);

                // MY_PERMISSIONS_REQUEST_GET_ACCOUNTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // permission was granted, yay!
            if (DBG) Log.d(TAG,"checkPermissionAccounts() permission was granted, yay!");
            ret = true;
        }

        if (DBG) Log.d(TAG,"<- checkPermissionAccounts()");
        return ret;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (DBG) Log.i(TAG, "onRequestPermissionsResult()");

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_GET_ACCOUNTS: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    if (DBG) Log.d(TAG,"onRequestPermissionsResult() PERMISSIONS_REQUEST_GET_ACCOUNTS was granted, yay! ");
                    CheckAccounts();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    if (DBG) Log.d(TAG,"onRequestPermissionsResult() PERMISSIONS_REQUEST_GET_ACCOUNTS denied, boo!");
                }
            }

        }
    }

    @Override
    protected void updateFileList() {
        if (DBG) Log.i(TAG, "updateFileList()");

        if (mShareFileList != null) {

            setAdpter();

            if (!mupdateFileList) {
                mupdateFileList = true;
                new ThumbnailRequestTask(mCredential).execute();
                getAutoCompleteList();
            }
        }
    }

    @Override
    protected void getResultsFromApi() {
        if (DBG) Log.i(TAG,"getResultsFromApi()");
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            if (DBG) Log.d(TAG,"chooseAccount()");
            chooseAccount();
        } else if (! isDeviceOnline()) {
            if (DBG) Log.d(TAG,"No network connection available.");
        } else {
            if (DBG) Log.d(TAG,"FileListRequestTask()");
            new FileListRequestTask(mCredential).execute();
            loadAdRequest();
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        if (DBG) Log.d(TAG,"onItemClick() position=" + position);
        Intent intent = new Intent(this.getApplicationContext(), ShareActivity.class);

        ShareFileInfo item = this.mList.get(position);
        String fileId = item.getFileId();
        String pngFileId = item.getPngFileId();
        boolean ownedByMe = item.IsOwnedByMe();

        intent.putExtra("FileId", fileId);
        intent.putExtra("PngFileId", pngFileId);
        intent.putExtra("ImgName", "");
        intent.putExtra("OwnedByMe", ownedByMe);
        intent.putExtra("Position", position);

        startActivity(intent);

    }

    private void CreateDocsActivity() {
        Intent startActivity = new Intent();
        startActivity.setClass(this,CreateDocs.class);
        startActivity.putExtra("CaptureImage", true);
        startActivity(startActivity);

    }

    private void GalleryImageActivity() {
        Intent startActivity = new Intent();
        startActivity.setClass(this,CreateDocs.class);
        startActivity.putExtra("CaptureImage", false);
        startActivity(startActivity);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.filelist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.filemenu1:
                CreateDocsActivity();
                return true;
            case R.id.filemenu2:
                GalleryImageActivity();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
