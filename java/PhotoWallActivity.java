package com.rlk.feedback;

import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.GridView;


import java.io.File;
import java.util.ArrayList;

import com.rlk.feedback.adapter.PhotoWallAdapter;
import com.rlk.feedback.util.Utility;


public class PhotoWallActivity extends BaseActivity {
    private ArrayList<String> list;
    private GridView mPhotoWall;
    private PhotoWallAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_wall);
        mPhotoWall = (GridView) findViewById(R.id.photo_wall_grid);
        String folderPath = getIntent().getStringExtra("folderPath");
        int lastSeparator = folderPath.lastIndexOf(File.separator);
        String folderName = folderPath.substring(lastSeparator + 1);
        setTitle(folderName);
        int LeftCount = getIntent().getIntExtra("LeftCount", 0);
        list = getAllImagePathsByFolder(folderPath);
        adapter = new PhotoWallAdapter(this, list,LeftCount);
        mPhotoWall.setAdapter(adapter);
        mbn.setVisibility(View.VISIBLE);
        mbn.setText(android.R.string.yes);
        mbn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> paths = getSelectImagePaths();
                Intent intent = new Intent(PhotoWallActivity.this, FeedBackEditActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putStringArrayListExtra("paths", paths);
                startActivity(intent);
            }
        });
    }



    /**
     * get picture from  specified path
     */
    private ArrayList<String> getAllImagePathsByFolder(String folderPath) {
        File folder = new File(folderPath);
        String[] allFileNames = folder.list();
        if (allFileNames == null || allFileNames.length == 0) {
            return null;
        }

        ArrayList<String> imageFilePaths = new ArrayList<String>();
        for (int i = allFileNames.length - 1; i >= 0; i--) {
            if (Utility.isImage(allFileNames[i])) {
                imageFilePaths.add(folderPath + File.separator + allFileNames[i]);
            }
        }

        return imageFilePaths;
    }

    /**
     * useing ContentProvider to read  recently picture from sdcard;
     */
//    private ArrayList<String> getLatestImagePaths(int maxCount) {
//        Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//
//        String key_MIME_TYPE = MediaStore.Images.Media.MIME_TYPE;
//        final String[] STORE_IMAGES = {
//                MediaStore.Images.Media.DATA
//        };
//
//        ContentResolver mContentResolver = getContentResolver();
//
//        // only jpg/jpeg/png   orderby date.modified
//        Cursor cursor = mContentResolver.query(mImageUri, STORE_IMAGES,
//                key_MIME_TYPE + "=? or " + key_MIME_TYPE + "=? or " + key_MIME_TYPE + "=?",
//                new String[]{"image/jpg", "image/jpeg", "image/png"},
//                MediaStore.Images.Media.DATE_MODIFIED);
//
//        ArrayList<String> latestImagePaths = null;
//        if (cursor != null) {
//            //reading from recently picture;
//            //when cursor has not data, cursor.moveToLast() will retuan false;
//            if (cursor.moveToLast()) {
//                latestImagePaths = new ArrayList<String>();
//
//                while (true) {
//                    // get picture path
//                    String path = cursor.getString(0);
//                    latestImagePaths.add(path);
//
//                    if (latestImagePaths.size() >= maxCount || !cursor.moveToPrevious()) {
//                        break;
//                    }
//                }
//            }
//            cursor.close();
//        }
//
//        return latestImagePaths;
//    }

    //get selected ImgsPath;
    private ArrayList<String> getSelectImagePaths() {
        SparseBooleanArray map = adapter.getSelectionMap();
        if (map.size() == 0) {
            return null;
        }

        ArrayList<String> selectedImageList = new ArrayList<String>();

        for (int i = 0; i < list.size(); i++) {
            if (map.get(i)) {
                selectedImageList.add(list.get(i));
            }
        }

        return selectedImageList;
    }
}
