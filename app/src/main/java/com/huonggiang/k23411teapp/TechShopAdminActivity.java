package com.huonggiang.k23411teapp;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import com.huonggiang.models.TechShopData;
import com.huonggiang.utils.CurrencyUtils;
import com.huonggiang.utils.TechShopRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Màn Admin: Dashboard thống kê doanh số + Top khách hàng + Top sản phẩm.
 * Đọc toàn bộ dữ liệu từ Firebase qua {@link TechShopRepository} rồi tính tại app.
 */
public class TechShopAdminActivity extends AppCompatActivity {

    private static final int TOP_N = 5;

    private ProgressBar pbLoading;
    private TextView tvError, tvTotalRevenue, tvRevenueSub;
    private LinearLayout llStatus, llTopCustomers, llTopProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tech_shop_admin);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        pbLoading = findViewById(R.id.pbLoading);
        tvError = findViewById(R.id.tvError);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvRevenueSub = findViewById(R.id.tvRevenueSub);
        llStatus = findViewById(R.id.llStatus);
        llTopCustomers = findViewById(R.id.llTopCustomers);
        llTopProducts = findViewById(R.id.llTopProducts);

        TechShopRepository.loadAll(new TechShopRepository.Callback() {
            @Override public void onLoaded(TechShopData data) {
                pbLoading.setVisibility(View.GONE);
                render(data);
            }
            @Override public void onError(String message) {
                pbLoading.setVisibility(View.GONE);
                tvError.setText(getString(R.string.str_ts_load_error, message));
                tvError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void render(TechShopData data) {
        renderRevenue(data);
        renderTopCustomers(data);
        renderTopProducts(data);
    }

    // ── Doanh số ────────────────────────────────────────────────────────────────

    private void renderRevenue(TechShopData data) {
        double total = 0, completed = 0;
        Map<String, Double> byStatus = new LinkedHashMap<>();
        for (TechShopData.Order o : data.orders) {
            total += o.totalAmount;
            if ("Completed".equalsIgnoreCase(o.status)) completed += o.totalAmount;
            Double cur = byStatus.get(o.status);
            byStatus.put(o.status, (cur == null ? 0 : cur) + o.totalAmount);
        }

        tvTotalRevenue.setText(CurrencyUtils.formatVnd(total));
        tvRevenueSub.setText(getString(R.string.str_ts_revenue_sub,
                data.orders.size(), CurrencyUtils.formatVnd(completed)));

        llStatus.removeAllViews();
        for (Map.Entry<String, Double> e : byStatus.entrySet()) {
            addRow(llStatus, e.getKey(), CurrencyUtils.formatVnd(e.getValue()));
        }
    }

    // ── Top khách hàng (theo tổng chi tiêu) ─────────────────────────────────────

    private void renderTopCustomers(TechShopData data) {
        Map<String, Double> spend = new HashMap<>();
        for (TechShopData.Order o : data.orders) {
            Double cur = spend.get(o.customerId);
            spend.put(o.customerId, (cur == null ? 0 : cur) + o.totalAmount);
        }

        List<Map.Entry<String, Double>> ranked = new ArrayList<>(spend.entrySet());
        Collections.sort(ranked, (a, b) -> Double.compare(b.getValue(), a.getValue()));

        llTopCustomers.removeAllViews();
        int rank = 1;
        for (Map.Entry<String, Double> e : ranked) {
            if (rank > TOP_N) break;
            addRow(llTopCustomers, rank + ". " + customerName(data, e.getKey()),
                    CurrencyUtils.formatVnd(e.getValue()));
            rank++;
        }
    }

    // ── Top sản phẩm (theo số lượng bán) ────────────────────────────────────────

    private void renderTopProducts(TechShopData data) {
        Map<String, Integer> qty = new HashMap<>();
        for (TechShopData.OrderDetail d : data.orderDetails) {
            Integer cur = qty.get(d.productId);
            qty.put(d.productId, (cur == null ? 0 : cur) + d.quantity);
        }

        List<Map.Entry<String, Integer>> ranked = new ArrayList<>(qty.entrySet());
        Collections.sort(ranked, (a, b) -> Integer.compare(b.getValue(), a.getValue()));

        llTopProducts.removeAllViews();
        int rank = 1;
        for (Map.Entry<String, Integer> e : ranked) {
            if (rank > TOP_N) break;
            addRow(llTopProducts, rank + ". " + productName(data, e.getKey()),
                    getString(R.string.str_ts_sold, e.getValue()));
            rank++;
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private String customerName(TechShopData data, String id) {
        for (TechShopData.Customer c : data.customers) {
            if (c.id.equals(id)) return c.fullName;
        }
        return id;
    }

    private String productName(TechShopData data, String id) {
        for (TechShopData.Product p : data.products) {
            if (p.id.equals(id)) return p.name;
        }
        return id;
    }

    /** Thêm một dòng "nhãn ... giá trị" vào container. */
    private void addRow(LinearLayout parent, String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        int pad = dp(7);
        row.setPadding(0, pad, 0, pad);

        TextView left = new TextView(this);
        left.setText(label);
        left.setTextColor(0xFF212121);
        left.setTextSize(15);
        left.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView right = new TextView(this);
        right.setText(value);
        right.setTextColor(0xFF0D47A1);
        right.setTextSize(15);
        right.setTypeface(null, Typeface.BOLD);

        row.addView(left);
        row.addView(right);
        parent.addView(row);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
