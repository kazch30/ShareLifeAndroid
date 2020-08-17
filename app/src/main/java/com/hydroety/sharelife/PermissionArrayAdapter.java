package com.hydroety.sharelife;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class PermissionArrayAdapter extends ArrayAdapter<PermissionInfo> {
    private static final String TAG = "PermissionArrayAdapter";
    private int resourceId;
    private List<PermissionInfo> items;
    private LayoutInflater inflater;

    public PermissionArrayAdapter(Context context, int resourceId, List<PermissionInfo> items) {
        super(context, resourceId, items);

        this.resourceId = resourceId;
        this.items = items;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        String fileId;
        int id = -1;

        if (convertView != null) {
            view = convertView;
        } else {
            view = this.inflater.inflate(this.resourceId, null);
        }

        PermissionInfo item = this.items.get(position);

        // テキストをセット
        TextView appInfoName = (TextView)view.findViewById(R.id.permission_name);
        appInfoName.setText(item.getName());

        TextView appInfoAddress = (TextView)view.findViewById(R.id.permission_address);
        appInfoAddress.setText(item.getAddress());

        TextView appInfoRole = (TextView)view.findViewById(R.id.permission_role);
        appInfoRole.setText(item.getRole());

        LinearLayout group_permission = (LinearLayout)view.findViewById(R.id.permission_item);
        LayoutParams lp = group_permission.getLayoutParams();
        MarginLayoutParams mlp = (MarginLayoutParams)lp;

        if (!item.getRole().contentEquals("owner")) {
            Button appInfoBtn = (Button)view.findViewById(R.id.permission_delete);
            appInfoBtn.setVisibility(View.VISIBLE);
            appInfoBtn.setTag(position);

            mlp.setMargins(10,mlp.topMargin, mlp.rightMargin, mlp.bottomMargin);
        }else{
            mlp.setMargins(110,mlp.topMargin, mlp.rightMargin, mlp.bottomMargin);
        }

        group_permission.setLayoutParams(mlp);

        // アイコンをセット
//        ImageView appInfoImage = (ImageView)view.findViewById(R.id.permission_image);
//        LoardImgFile(item.getPhotoLink(), appInfoImage);

        return view;

    }

    private void LoardImgFile(String imgName, ImageView appInfoImage) {
        if (BaseActivity.DBG) Log.i(TAG,"LoardImgFile()");
        if (BaseActivity.DBG) Log.d(TAG,"imgName=" + imgName);
        String path;
        if (imgName.indexOf("https:") == -1){
            path = "https:" + imgName;
        }else {
            path = imgName;
        }
        if (BaseActivity.DBG) Log.d(TAG,"path=" + path);

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
                if (BaseActivity.DBG) Log.d(TAG,"The following error occurred:\n"
                        + e.getMessage());
                return null;
            } catch (IOException e) {
                if (BaseActivity.DBG) Log.d(TAG,"The following error occurred:\n"
                        + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            image.setImageBitmap(result);
        }
    }

}
