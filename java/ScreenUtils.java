package com.rlk.feedback.util;

import android.app.Activity;
import android.util.DisplayMetrics;

/**
 * ScreenUtils
 * 
 */
public class ScreenUtils {
    private static int screenW;
    private static int screenH;
    private static float screenDensity;
    private static ScreenUtils mInstance = null;
    private ScreenUtils(){};
    public static ScreenUtils getInstance(Activity activity){
    	if(mInstance == null){
    		synchronized (ScreenUtils.class) {
    			if(mInstance == null){
    				mInstance =new ScreenUtils();
    				DisplayMetrics metric = new DisplayMetrics();
    				activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
    		        screenW = metric.widthPixels;
    		        screenH = metric.heightPixels;
    		        screenDensity = metric.density;
    			}
			}
    	}
    	return mInstance;
    }

    public static int getScreenW(){
        return screenW;
    }

    public static int getScreenH(){
        return screenH;
    }

    public static float getScreenDensity(){
        return screenDensity;
    }

    /** dp to px*/
    public static int dp2px(float dpValue) {
        return (int) (dpValue * getScreenDensity() + 0.5f);
    }

    /** px to dp */
    public static int px2dp(float pxValue) {
        return (int) (pxValue / getScreenDensity() + 0.5f);
    }
}
