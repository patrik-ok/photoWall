package com.rlk.feedback.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import java.io.File;
import java.util.ArrayList;

import com.rlk.feedback.R;
import com.rlk.feedback.domain.PhotoAlbumLVItem;
import com.rlk.feedback.util.SDCardImageLoader;
import com.rlk.feedback.util.ScreenUtils;

/**
 * 閫夋嫨鐩稿唽椤甸潰,ListView鐨刟dapter
 * Created by hanj on 14-10-14.
 */
public class PhotoAlbumLVAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<PhotoAlbumLVItem> list;

    private SDCardImageLoader loader;

    public PhotoAlbumLVAdapter(Context context, ArrayList<PhotoAlbumLVItem> list) {
        this.context = context;
        this.list = list;

        loader = new SDCardImageLoader((Activity)context);
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	PhotoAlbumLVItem album = list.get(position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).
                    inflate(R.layout.photo_album_lv_item, null);
            holder = new ViewHolder();

            holder.firstImageIV = (ImageView) convertView.findViewById(R.id.select_img_gridView_img);
            holder.pathNameTV = (TextView) convertView.findViewById(R.id.select_img_gridView_path);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //鍥剧墖锛堢缉鐣ュ浘锛�
        String filePath = album.getFirstImagePath();
        holder.firstImageIV.setTag(filePath);
        loader.loadImage(4, filePath, holder.firstImageIV);
        //鏂囧瓧
        holder.pathNameTV.setText(getPathNameToShow(list.get(position)));

        return convertView;
    }

    private class ViewHolder {
        ImageView firstImageIV;
        TextView pathNameTV;
    }

    /**鏍规嵁瀹屾暣璺緞锛岃幏鍙栨渶鍚庝竴绾ц矾寰勶紝骞舵嫾涓婃枃浠舵暟鐢ㄤ互鏄剧ず銆�*/
    private String getPathNameToShow(PhotoAlbumLVItem item) {
        String absolutePath = item.getPathName();
        int lastSeparator = absolutePath.lastIndexOf(File.separator);
        return absolutePath.substring(lastSeparator + 1) + "(" + item.getFileCount() + ")";
    }

}
