package com.huonggiang.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Tải ảnh từ URL về hiển thị lên {@link ImageView} một cách bất đồng bộ.
 *
 * - Tải trên luồng nền (pool 4 luồng), hiển thị kết quả ở main thread.
 * - Có bộ nhớ đệm {@link LruCache} để không tải lại ảnh đã có.
 * - Chống lỗi tái sử dụng view trong ListView: gắn URL vào tag của ImageView,
 *   chỉ gán bitmap nếu khi tải xong tag vẫn đúng URL đó.
 */
public class ImageLoader {

    private static final ExecutorService POOL = Executors.newFixedThreadPool(4);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    // Đệm tối đa ~12MB ảnh
    private static final LruCache<String, Bitmap> CACHE = new LruCache<String, Bitmap>(12 * 1024 * 1024) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };

    private ImageLoader() {
    }

    public static void load(ImageView imageView, String url) {
        if (imageView == null) return;
        imageView.setTag(url);

        if (url == null || url.isEmpty()) {
            imageView.setImageBitmap(null);
            return;
        }

        Bitmap cached = CACHE.get(url);
        if (cached != null) {
            imageView.setImageBitmap(cached);
            return;
        }

        imageView.setImageBitmap(null); // xoá ảnh cũ trong lúc chờ tải
        POOL.execute(() -> {
            Bitmap bmp = download(url);
            if (bmp == null) return;
            CACHE.put(url, bmp);
            MAIN.post(() -> {
                // chỉ gán nếu view chưa bị tái sử dụng cho item khác
                if (url.equals(imageView.getTag())) {
                    imageView.setImageBitmap(bmp);
                }
            });
        });
    }

    private static Bitmap download(String urlStr) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            try (InputStream is = conn.getInputStream()) {
                return BitmapFactory.decodeStream(is);
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
