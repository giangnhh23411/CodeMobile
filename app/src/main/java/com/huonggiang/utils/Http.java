package com.huonggiang.utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Tải nội dung text qua HTTP GET (UTF-8). Dùng cho các trang MYUEL.
 * Phải gọi ở luồng nền — không gọi trên main thread.
 */
public final class Http {

    private static final int TIMEOUT_MS = 15000;

    private Http() {
    }

    /** GET một URL, trả về body dạng text hoặc {@code null} nếu lỗi/không phải HTTP 200. */
    public static String get(String urlStr) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) return null;

            StringBuilder sb = new StringBuilder();
            try (InputStream is = conn.getInputStream();
                 Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                char[] buffer = new char[8192];
                int n;
                while ((n = reader.read(buffer)) != -1) sb.append(buffer, 0, n);
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
