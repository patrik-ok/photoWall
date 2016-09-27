package com.rlk.feedback.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.rlk.feedback.Configure;
import com.rlk.feedback.FeedBackEditActivity;
import com.rlk.feedback.R;
import com.rlk.feedback.util.SDCardImageLoader;

public class GridImageAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<String> dataList;
	private DisplayMetrics dm;
	private LayoutInflater mInflater;
	private SDCardImageLoader loader;

	public GridImageAdapter(Context c, ArrayList<String> dataList) {

		mContext = c;
		this.dataList = dataList;
		mInflater = LayoutInflater.from(c);
		dm = new DisplayMetrics();
		((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
		loader = new SDCardImageLoader((Activity) c);
	}

	@Override
	public int getCount() {
		return dataList.size();
	}

	@Override
	public Object getItem(int position) {
		return dataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if (convertView == null) {
			holder = new Holder();
			convertView = mInflater.inflate(R.layout.item_image_grid_layout, null);
			holder.iv = (ImageView) convertView.findViewById(R.id.image);
			holder.selected = (ImageView) convertView.findViewById(R.id.isselected);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		holder.iv.setAdjustViewBounds(true);
		holder.iv.setScaleType(ImageView.ScaleType.FIT_XY);

		String path = (String) getItem(position);
		holder.iv.setTag(path);
		if (path.contains(Configure.ADD_PICTURE_DEFAULT)) {
			holder.iv.setImageResource(R.drawable.camera_default);
			holder.selected.setVisibility(View.GONE);
			holder.selected.setImageBitmap(null);
		} else {
			// Bitmap b=BitmapFactory.decodeFile(path);
			// Bitmap bitmap=ThumbnailUtils.extractThumbnail(b, 60, 60);
			// holder.iv.setImageBitmap(bitmap);
			loader.loadImage(4, path, holder.iv);
			holder.selected.setVisibility(View.VISIBLE);
			holder.selected.setImageResource(R.drawable.ic_delete_n);
		}

		holder.selected.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dataList.toString().contains(Configure.ADD_PICTURE_DEFAULT)) {
					if (!dataList.get(position).contains(Configure.ADD_PICTURE_DEFAULT)) {
						dataList.remove(position);
						notifyDataSetChanged();
					}
				} else {
					if (!dataList.get(position).contains(Configure.ADD_PICTURE_DEFAULT)) {
						dataList.remove(position);
						if (getCount() < FeedBackEditActivity.PHOTO_COUNT) {
							dataList.add(Configure.ADD_PICTURE_DEFAULT);
						}
						notifyDataSetChanged();
					}
				}
			}
		});
		return convertView;
	}

	public int dipToPx(int dip) {
		return (int) (dip * dm.density + 0.5f);
	}

	class Holder {
		private ImageView iv;
		private ImageView selected;
	}

	public void add(String picUrl) {
		this.dataList.add(picUrl);
		notifyDataSetChanged();
	}

	public void addAll(ArrayList<String> paths) {
		if (dataList != null) {
			int index = dataList.indexOf(Configure.ADD_PICTURE_DEFAULT);
			if (index != -1) {
				dataList.remove(index);
			}
		}
		this.dataList.addAll(paths);
		notifyDataSetChanged();
	}
}
