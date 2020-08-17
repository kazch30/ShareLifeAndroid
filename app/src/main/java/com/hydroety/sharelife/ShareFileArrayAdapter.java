package com.hydroety.sharelife;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ShareFileArrayAdapter extends ArrayAdapter<ShareFileInfo> {
    private static final String TAG = "ShareFileArrayAdapter";
    private int resourceId;
    private List<ShareFileInfo> items;
    private LayoutInflater inflater;

    public ShareFileArrayAdapter(Context context, int resourceId, List<ShareFileInfo> items) {
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

        ShareFileInfo item = this.items.get(position);

        // テキストをセット
        TextView appInfoName = (TextView)view.findViewById(R.id.item_name);
        appInfoName.setText(item.getName());

        TextView appInfoTime = (TextView)view.findViewById(R.id.item_time);
        appInfoTime.setText(item.getModifiedTime());

        // アイコンをセット
        ImageView appInfoImage = (ImageView)view.findViewById(R.id.item_image);
        Bitmap bmp = item.getThumbnail();
        if (bmp != null) {
//            LoardImgFile(item.getIconLink(), appInfoImage);
//        } else {
            appInfoImage.setImageBitmap(bmp);
        }

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
