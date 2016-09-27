package com.rlk.feedback;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import com.rlk.feedback.adapter.PhotoAlbumLVAdapter;
import com.rlk.feedback.domain.PhotoAlbumLVItem;
import com.rlk.feedback.util.Utility;

public class PhotoAlbumActivity extends BaseActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_album);

		if (!Utility.isSDcardOK()) {
			Utility.showToast(this, "SD卡不可用");
			return;
		}
		setTitle("选择相册");
		ListView listView = (ListView) findViewById(R.id.select_img_listView);

		final ArrayList<PhotoAlbumLVItem> list = new ArrayList<PhotoAlbumLVItem>();
		list.addAll(getImagePathsByContentProvider());
		final int LeftCount = getIntent().getIntExtra("LeftCount", 0);
		PhotoAlbumLVAdapter adapter = new PhotoAlbumLVAdapter(this, list);
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(PhotoAlbumActivity.this, PhotoWallActivity.class);
				intent.putExtra("folderPath", list.get(position).getPathName());
				intent.putExtra("LeftCount", LeftCount);
				startActivity(intent);
			}
		});

	}

	private int getImageCount(File folder) {
		int count = 0;
		File[] files = folder.listFiles();
		for (File file : files) {
			if (Utility.isImage(file.getName())) {
				count++;
			}
		}

		return count;
	}

	private String getFirstImagePath(File folder) {
		File[] files = folder.listFiles();
		for (int i = files.length - 1; i >= 0; i--) {
			File file = files[i];
			if (Utility.isImage(file.getName())) {
				return file.getAbsolutePath();
			}
		}

		return null;
	}

	private ArrayList<PhotoAlbumLVItem> getImagePathsByContentProvider() {
		Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

		String key_MIME_TYPE = MediaStore.Images.Media.MIME_TYPE;
		String key_DATA = MediaStore.Images.Media.DATA;

		ContentResolver mContentResolver = getContentResolver();

		Cursor cursor = mContentResolver.query(mImageUri, new String[] { key_DATA },
				key_MIME_TYPE + "=? or " + key_MIME_TYPE + "=? or " + key_MIME_TYPE + "=?",
				new String[] { "image/jpg", "image/jpeg", "image/png" }, MediaStore.Images.Media.DATE_MODIFIED);

		ArrayList<PhotoAlbumLVItem> list = null;
		if (cursor != null) {
			if (cursor.moveToLast()) {
				HashSet<String> cachePath = new HashSet<String>();
				list = new ArrayList<PhotoAlbumLVItem>();

				while (true) {
					String imagePath = cursor.getString(0);

					File parentFile = new File(imagePath).getParentFile();
					String parentPath = parentFile.getAbsolutePath();

					if (!cachePath.contains(parentPath)) {
						list.add(new PhotoAlbumLVItem(parentPath, getImageCount(parentFile),
								getFirstImagePath(parentFile)));
						cachePath.add(parentPath);
					}

					if (!cursor.moveToPrevious()) {
						break;
					}
				}
			}

			cursor.close();
		}

		return list;
	}
}
