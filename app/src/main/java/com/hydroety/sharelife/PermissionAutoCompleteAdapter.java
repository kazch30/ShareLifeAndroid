package com.hydroety.sharelife;

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
import android.widget.Filter;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;

public class PermissionAutoCompleteAdapter extends ArrayAdapter<PermissionInfo> {
    private static final String TAG = "AutoCompleteAdapter";
    private int resourceId;
    private List<PermissionInfo> items, tempItems, suggestions;
    private LayoutInflater inflater;

    public PermissionAutoCompleteAdapter(Context context, int resourceId, List<PermissionInfo> items) {
        super(context, resourceId, items);

        this.resourceId = resourceId;
        this.items = items;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        tempItems = new ArrayList<PermissionInfo>(items); // this makes the difference.
        suggestions = new ArrayList<PermissionInfo>();
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
        TextView appInfoName = (TextView)view.findViewById(R.id.autocomplete_name);
        appInfoName.setText(item.getName());
        if (BaseActivity.DBG) Log.d(TAG,"name=" + item.getName());


        TextView appInfoAddress = (TextView)view.findViewById(R.id.autocomplete_address);
        appInfoAddress.setText(item.getAddress());
        if (BaseActivity.DBG) Log.d(TAG,"address=" + item.getAddress());

        // アイコンをセット
        ImageView appInfoImage = (ImageView) view.findViewById(R.id.autocomplete_image);
        if (item.getPhotoLink() != null) {
            LoardImgFile(item.getPhotoLink(), appInfoImage);
        }else {
            appInfoImage.setImageResource(R.drawable.ic_account_blue_24dp);
        }

        return view;

    }

    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    /**
     * Custom Filter implementation for custom suggestions we provide.
     */
    Filter nameFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            String str = ((PermissionInfo) resultValue).getName();
            return str;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null) {
                suggestions.clear();
                for (PermissionInfo info : tempItems) {
                    if (info.getAddress().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        suggestions.add(info);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            List<PermissionInfo> filterList = (ArrayList<PermissionInfo>) results.values;
            if (results != null && results.count > 0) {
                clear();
                for (PermissionInfo info : filterList) {
                    add(info);
                    notifyDataSetChanged();
                }
            }
        }
    };

    private void LoardImgFile(String imgName, ImageView appInfoImage) {
        if (BaseActivity.DBG) Log.i(TAG,"LoardImgFile()");
        if (BaseActivity.DBG) Log.d(TAG,"imgName=" + imgName);
        String path;
        if (imgName != null && imgName.indexOf("https:") == -1){
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
