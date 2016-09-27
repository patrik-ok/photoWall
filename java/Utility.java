package com.rlk.feedback.util;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;

/**
 * Created by hanj on 14-10-14.
 */
public class Utility {

    /**
     * 鍒ゆ柇SD鍗℃槸鍚﹀彲鐢�
     */
    public static boolean isSDcardOK() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 鑾峰彇SD鍗¤窡璺緞銆係D鍗′笉鍙敤鏃讹紝杩斿洖null
     */
    public static String getSDcardRoot() {
        if (isSDcardOK()) {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        }

        return null;
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context context, int msgId) {
        Toast.makeText(context, msgId, Toast.LENGTH_SHORT).show();
    }

    /**鑾峰彇瀛楃涓蹭腑鏌愪釜瀛楃涓插嚭鐜扮殑娆℃暟銆�*/
    public static int countMatches(String res, String findString) {
        if (res == null) {
            return 0;
        }

        if (findString == null || findString.length() == 0) {
            throw new IllegalArgumentException("The param findString cannot be null or 0 length.");
        }

        return (res.length() - res.replace(findString, "").length()) / findString.length();
    }

    /**鍒ゆ柇璇ユ枃浠舵槸鍚︽槸涓�涓浘鐗囥��*/
    public static boolean isImage(String fileName) {
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png");
    }

}
