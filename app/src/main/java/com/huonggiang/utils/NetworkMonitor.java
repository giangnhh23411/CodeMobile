package com.huonggiang.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

/**
 * Giám sát kết nối mạng mặc định của thiết bị và báo về cho UI qua {@link Listener}.
 * Tự xử lý: mất mạng (onDisconnected) và đổi loại mạng Wi-Fi ↔ di động (onConnected).
 *
 * Mọi callback được đẩy về main thread nên có thể cập nhật View trực tiếp.
 */
public class NetworkMonitor {

    public interface Listener {
        /** transportType là một trong NetworkCapabilities.TRANSPORT_* (WIFI / CELLULAR / ETHERNET). */
        void onConnected(int transportType);
        void onDisconnected();
    }

    private final ConnectivityManager cm;
    private final Listener listener;
    private final Handler main = new Handler(Looper.getMainLooper());

    private ConnectivityManager.NetworkCallback callback;
    private int lastTransport = -1; // -1 = chưa xác định / đang offline

    public NetworkMonitor(@NonNull Context context, @NonNull Listener listener) {
        this.cm = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        this.listener = listener;
    }

    /** Bắt đầu lắng nghe. Gọi trong onStart(). */
    public void start() {
        if (cm == null || callback != null) return;

        callback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onCapabilitiesChanged(@NonNull Network network,
                                              @NonNull NetworkCapabilities caps) {
                boolean hasInternet =
                        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                     && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                if (!hasInternet) return;

                int transport = resolveTransport(caps);
                // Chỉ báo khi loại mạng thay đổi (kết nối mới hoặc đổi Wi-Fi ↔ di động)
                if (transport != lastTransport) {
                    lastTransport = transport;
                    main.post(() -> listener.onConnected(transport));
                }
            }

            @Override
            public void onLost(@NonNull Network network) {
                lastTransport = -1;
                main.post(listener::onDisconnected);
            }
        };

        cm.registerDefaultNetworkCallback(callback);
    }

    /** Ngừng lắng nghe. Gọi trong onStop(). */
    public void stop() {
        if (cm != null && callback != null) {
            cm.unregisterNetworkCallback(callback);
            callback = null;
        }
    }

    /** Kiểm tra nhanh hiện đang online hay không. */
    public boolean isOnline() {
        if (cm == null) return false;
        Network network = cm.getActiveNetwork();
        if (network == null) return false;
        NetworkCapabilities caps = cm.getNetworkCapabilities(network);
        return caps != null
                && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    private int resolveTransport(NetworkCapabilities caps) {
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return NetworkCapabilities.TRANSPORT_WIFI;
        }
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            return NetworkCapabilities.TRANSPORT_CELLULAR;
        }
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            return NetworkCapabilities.TRANSPORT_ETHERNET;
        }
        return NetworkCapabilities.TRANSPORT_WIFI; // mặc định
    }
}
