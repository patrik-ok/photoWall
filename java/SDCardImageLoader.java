package com.rlk.feedback.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.rlk.feedback.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

/**
 *
 * @author
 */
public class SDCardImageLoader {
	private ScreenUtils  screen;
    //缂撳瓨
    private LruCache<String, Bitmap> imageCache;
    // 鍥哄畾2涓嚎绋嬫潵鎵ц浠诲姟
    private ExecutorService executorService = Executors.newFixedThreadPool(2);
    private Handler handler = new Handler();

    public SDCardImageLoader(Activity activity) {
    	screen = ScreenUtils.getInstance(activity);
        // 鑾峰彇搴旂敤绋嬪簭鏈�澶у彲鐢ㄥ唴瀛�
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;

        // 璁剧疆鍥剧墖缂撳瓨澶у皬涓虹▼搴忔渶澶у彲鐢ㄥ唴瀛樼殑1/8
        imageCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
    }

    private Bitmap loadDrawable(final int smallRate, final String filePath,
                                final ImageCallback callback) {
        // 濡傛灉缂撳瓨杩囧氨浠庣紦瀛樹腑鍙栧嚭鏁版嵁
        if (imageCache.get(filePath) != null) {
            return imageCache.get(filePath);
        }

        // 濡傛灉缂撳瓨娌℃湁鍒欒鍙朣D鍗�
        executorService.submit(new Runnable() {
            public void run() {
                try {
                    BitmapFactory.Options opt = new BitmapFactory.Options();
                    opt.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(filePath, opt);

                    // 鑾峰彇鍒拌繖涓浘鐗囩殑鍘熷瀹藉害鍜岄珮搴�
                    int picWidth = opt.outWidth;
                    int picHeight = opt.outHeight;

                    //璇诲彇鍥剧墖澶辫触鏃剁洿鎺ヨ繑鍥�
                    if (picWidth == 0 || picHeight == 0) {
                        return;
                    }

                    //鍒濆鍘嬬缉姣斾緥
                    opt.inSampleSize = smallRate;
                    // 鏍规嵁灞忕殑澶у皬鍜屽浘鐗囧ぇ灏忚绠楀嚭缂╂斁姣斾緥
                    if (picWidth > picHeight) {
                        if (picWidth > screen.getScreenW())
                            opt.inSampleSize *= picWidth / screen.getScreenW();
                    } else {
                        if (picHeight > screen.getScreenH())
                            opt.inSampleSize *= picHeight / screen.getScreenH();
                    }

                    //杩欐鍐嶇湡姝ｅ湴鐢熸垚涓�涓湁鍍忕礌鐨勶紝缁忚繃缂╂斁浜嗙殑bitmap
                    opt.inJustDecodeBounds = false;
                    final Bitmap bmp = BitmapFactory.decodeFile(filePath, opt);
                    //瀛樺叆map
                    imageCache.put(filePath, bmp);

                    handler.post(new Runnable() {
                        public void run() {
                            callback.imageLoaded(bmp);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return null;
    }

    /**
     * 寮傛璇诲彇SD鍗″浘鐗囷紝骞舵寜鎸囧畾鐨勬瘮渚嬭繘琛屽帇缂╋紙鏈�澶т笉瓒呰繃灞忓箷鍍忕礌鏁帮級
     *
     * @param smallRate 鍘嬬缉姣斾緥锛屼笉鍘嬬缉鏃惰緭鍏�1锛屾鏃跺皢鎸夊睆骞曞儚绱犳暟杩涜杈撳嚭
     * @param filePath  鍥剧墖鍦⊿D鍗＄殑鍏ㄨ矾寰�
     * @param imageView 缁勪欢
     */
    public void loadImage(int smallRate, final String filePath, final ImageView imageView) {

        Bitmap bmp = loadDrawable(smallRate, filePath, new ImageCallback() {

            @Override
            public void imageLoaded(Bitmap bmp) {
                if (imageView.getTag().equals(filePath)) {
                    if (bmp != null) {
                        imageView.setImageBitmap(bmp);
                    } else {
                        imageView.setImageResource(R.drawable.ic_empty);
                    }
                }
            }
        });
        if (bmp != null) {
            if (imageView.getTag().equals(filePath)) {
                imageView.setImageBitmap(bmp);
            }
        } else {
            imageView.setImageResource(R.drawable.ic_empty);
        }
    }


    // 瀵瑰鐣屽紑鏀剧殑鍥炶皟鎺ュ彛
    public interface ImageCallback {
        // 娉ㄦ剰 姝ゆ柟娉曟槸鐢ㄦ潵璁剧疆鐩爣瀵硅薄鐨勫浘鍍忚祫婧�
        public void imageLoaded(Bitmap imageDrawable);
    }
}
