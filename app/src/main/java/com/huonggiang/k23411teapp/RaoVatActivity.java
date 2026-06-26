package com.huonggiang.k23411teapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import com.huonggiang.adapters.RaoVatAdapter;
import com.huonggiang.models.RaoVatItem;
import com.huonggiang.utils.Http;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Hiển thị danh sách tin rao vặt lấy từ API raovat.tuoitre.vn.
 *
 * Việc gọi mạng (tải JSON) chạy ở luồng nền; kết quả được đẩy về main thread
 * qua {@link Handler} để cập nhật ListView. Bấm vào một tin sẽ mở trang chi tiết
 * trên trình duyệt.
 */
public class RaoVatActivity extends AppCompatActivity {

    private static final String API_URL = "https://raovat.tuoitre.vn/api/list/list-top-ttorv";

    private TextView tvCount;
    private TextView tvEmpty;
    private ProgressBar pbLoading;
    private ListView lvRaoVat;
    private Button btnReload;

    private final ArrayList<RaoVatItem> items = new ArrayList<>();
    private RaoVatAdapter adapter;

    private final Handler main = new Handler(Looper.getMainLooper());
    private volatile boolean destroyed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rao_vat);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        addViews();
        addEvents();
        loadData();
    }

    private void addViews() {
        tvCount = findViewById(R.id.tvCount);
        tvEmpty = findViewById(R.id.tvEmpty);
        pbLoading = findViewById(R.id.pbLoading);
        lvRaoVat = findViewById(R.id.lvRaoVat);
        btnReload = findViewById(R.id.btnReload);

        adapter = new RaoVatAdapter(this, R.layout.item_rao_vat, items);
        lvRaoVat.setAdapter(adapter);
    }

    private void addEvents() {
        if (btnReload != null) {
            btnReload.setOnClickListener(v -> loadData());
        }
        // Bấm một tin: mở trang chi tiết trên trình duyệt
        lvRaoVat.setOnItemClickListener((parent, view, position, id) -> {
            RaoVatItem item = adapter.getItem(position);
            if (item == null || item.getUrl() == null || item.getUrl().isEmpty()) return;
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(item.getUrl())));
            } catch (Exception e) {
                Toast.makeText(this, R.string.str_rv_cannot_open, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Tải danh sách tin trên luồng nền. */
    private void loadData() {
        showLoading(true);
        new Thread(() -> {
            String json = Http.get(API_URL);
            ArrayList<RaoVatItem> result = (json == null) ? null : parse(json);
            main.post(() -> {
                if (destroyed) return;
                showLoading(false);
                if (result == null) {
                    showEmpty(getString(R.string.str_rv_error));
                    Toast.makeText(this, R.string.str_rv_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                items.clear();
                items.addAll(result);
                adapter.notifyDataSetChanged();
                tvCount.setText(getString(R.string.str_rv_count, items.size()));
                if (items.isEmpty()) {
                    showEmpty(getString(R.string.str_rv_empty));
                } else {
                    tvEmpty.setVisibility(View.GONE);
                }
            });
        }).start();
    }

    private void showLoading(boolean loading) {
        pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) {
            tvEmpty.setVisibility(View.GONE);
            if (btnReload != null) btnReload.setEnabled(false);
        } else {
            if (btnReload != null) btnReload.setEnabled(true);
        }
    }

    private void showEmpty(String message) {
        tvEmpty.setText(message);
        tvEmpty.setVisibility(View.VISIBLE);
    }

    /** Parse JSON trả về danh sách tin, hoặc null nếu JSON không hợp lệ. */
    private ArrayList<RaoVatItem> parse(String json) {
        try {
            ArrayList<RaoVatItem> list = new ArrayList<>();
            JSONObject root = new JSONObject(json);
            JSONArray arr = root.optJSONArray("items");
            if (arr == null) return list;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new RaoVatItem(
                        o.optString("title"),
                        o.optString("thumb"),
                        o.optString("url"),
                        o.optString("price"),
                        o.optString("location")));
            }
            return list;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        main.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
