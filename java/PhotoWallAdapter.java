package com.rlk.feedback.adapter;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import com.rlk.feedback.R;
import com.rlk.feedback.util.SDCardImageLoader;
import com.rlk.feedback.util.ScreenUtils;

/**
 * PhotoWall涓璆ridView鐨勯�傞厤鍣�
 *
 * @author hanj
 */

public class PhotoWallAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> imagePathList = null;

    private SDCardImageLoader loader;

    private SparseBooleanArray selectionMap;
    
    private HashMap<Integer, Boolean> selectedStatus;
    
    private boolean isChecked = false;
    
    private int LeftCount;

    public PhotoWallAdapter(Context context, ArrayList<String> imagePathList,int LeftCount) {
        this.context = context;
        this.imagePathList = imagePathList;
        this.LeftCount = LeftCount;
        loader = new SDCardImageLoader((Activity)context);
        selectionMap = new SparseBooleanArray();
        selectedStatus = new HashMap<Integer, Boolean>();
    }

    @Override
    public int getCount() {
        return imagePathList == null ? 0 : imagePathList.size();
    }

    @Override
    public Object getItem(int position) {
        return imagePathList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String filePath = (String) getItem(position);

        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.photo_wall_item, null);
            holder = new ViewHolder();

            holder.imageView = (ImageView) convertView.findViewById(R.id.photo_wall_item_photo);
            holder.liner_photo_wall_item = convertView.findViewById(R.id.liner_photo_wall_item);
            holder.ImgCheckBox = (ImageView) convertView.findViewById(R.id.photo_wall_item_cb);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.liner_photo_wall_item.setTag(R.id.tag_first, position);
        holder.liner_photo_wall_item.setTag(R.id.tag_second, holder.imageView);
        holder.liner_photo_wall_item.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				 Integer position = (Integer) arg0.getTag(R.id.tag_first);
	             ImageView image = (ImageView) arg0.getTag(R.id.tag_second);
	             isChecked = !selectionMap.get(position);
	             if(isChecked){
	            	 if(selectedStatus.size()>=(LeftCount+1)){
	             		Toast.makeText(context, R.string.limit_picture, 0).show();
	             		return;
	             	 }
	            	 selectedStatus.put(position, isChecked);
	            	 image.setColorFilter(Color.parseColor("#66000000"));
	        		 holder.ImgCheckBox.setImageResource(R.drawable.btn_check_on_focused_holo_light);
	             }else{
	            	 selectedStatus.remove(position);
	            	 image.setColorFilter(null);
	            	 holder.ImgCheckBox.setImageResource(R.drawable.btn_check_on_disabled_focused_holo_light); 
	             }
	             selectionMap.put(position, isChecked);
			}
		});
   	 	holder.ImgCheckBox.setImageResource(R.drawable.btn_check_on_disabled_focused_holo_light); 
        holder.imageView.setTag(filePath);
        loader.loadImage(4, filePath, holder.imageView);
        return convertView;
    }
    private class ViewHolder {
    	View liner_photo_wall_item;
        ImageView imageView;
        ImageView ImgCheckBox;
    }
    public SparseBooleanArray getSelectionMap() {
        return selectionMap;
    }

    public void clearSelectionMap() {
        selectionMap.clear();
    }
}
