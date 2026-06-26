package com.huonggiang.k23411teapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import com.huonggiang.adapters.TechProductAdapter;
import com.huonggiang.models.TechShopData;
import com.huonggiang.utils.CurrencyUtils;
import com.huonggiang.utils.MajorSearch;
import com.huonggiang.utils.TechShopRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Màn Khách hàng: xem sản phẩm theo danh mục (category) + tìm kiếm (search) + thêm
 * vào giỏ hàng. Giỏ hàng tạm trong phiên, xem nhanh qua hộp thoại.
 */
public class TechShopClientActivity extends AppCompatActivity {

    private EditText etSearch;
    private Spinner spCategory;
    private Button btnCart;
    private ProgressBar pbLoading;
    private TextView tvEmpty;
    private ListView lvProducts;

    private final List<TechShopData.Product> allActive = new ArrayList<>(); // sản phẩm đang bán
    private final List<TechShopData.Product> shown = new ArrayList<>();      // đang hiển thị
    private final Map<String, TechShopData.Product> productById = new HashMap<>();
    private final List<String> categoryIds = new ArrayList<>();  // song song với spinner; phần tử đầu = null (Tất cả)
    private TechProductAdapter adapter;

    /** Giỏ hàng tạm: productId -> số lượng. */
    private final LinkedHashMap<String, Integer> cart = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tech_shop_client);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        etSearch = findViewById(R.id.etSearch);
        spCategory = findViewById(R.id.spCategory);
        btnCart = findViewById(R.id.btnCart);
        pbLoading = findViewById(R.id.pbLoading);
        tvEmpty = findViewById(R.id.tvEmpty);
        lvProducts = findViewById(R.id.lvProducts);

        adapter = new TechProductAdapter(this, R.layout.item_tech_product, shown, this::addToCart);
        lvProducts.setAdapter(adapter);
        updateCartButton();
        addEvents();
        loadData();
    }

    private void addEvents() {
        btnCart.setOnClickListener(v -> showCart());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { applyFilter(); }
            @Override public void afterTextChanged(Editable s) { }
        });

        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                applyFilter();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void loadData() {
        pbLoading.setVisibility(View.VISIBLE);
        TechShopRepository.loadAll(new TechShopRepository.Callback() {
            @Override public void onLoaded(TechShopData data) {
                pbLoading.setVisibility(View.GONE);
                setup(data);
            }
            @Override public void onError(String message) {
                pbLoading.setVisibility(View.GONE);
                showEmpty(getString(R.string.str_ts_load_error, message));
            }
        });
    }

    private void setup(TechShopData data) {
        allActive.clear();
        productById.clear();
        for (TechShopData.Product p : data.products) {
            productById.put(p.id, p);
            if (p.active) allActive.add(p);
        }

        // Spinner danh mục: "Tất cả" + từng category
        List<String> names = new ArrayList<>();
        names.add(getString(R.string.str_ts_all_categories));
        categoryIds.clear();
        categoryIds.add(null);
        for (TechShopData.Category c : data.categories) {
            names.add(c.name);
            categoryIds.add(c.id);
        }
        spCategory.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, names));

        applyFilter();
    }

    /** Lọc theo danh mục đang chọn + từ khoá tìm kiếm (bỏ dấu tiếng Việt). */
    private void applyFilter() {
        String query = MajorSearch.normalize(etSearch.getText().toString());
        int pos = spCategory.getSelectedItemPosition();
        String categoryId = (pos >= 0 && pos < categoryIds.size()) ? categoryIds.get(pos) : null;

        shown.clear();
        for (TechShopData.Product p : allActive) {
            if (categoryId != null && !categoryId.equals(p.categoryId)) continue;
            if (!query.isEmpty() && !MajorSearch.normalize(p.name).contains(query)) continue;
            shown.add(p);
        }
        adapter.notifyDataSetChanged();

        if (shown.isEmpty()) {
            showEmpty(getString(R.string.str_ts_no_products));
        } else {
            tvEmpty.setVisibility(View.GONE);
            lvProducts.setVisibility(View.VISIBLE);
        }
    }

    private void showEmpty(String message) {
        tvEmpty.setText(message);
        tvEmpty.setVisibility(View.VISIBLE);
        lvProducts.setVisibility(View.GONE);
    }

    // ── Giỏ hàng ────────────────────────────────────────────────────────────────

    private void addToCart(TechShopData.Product p) {
        Integer cur = cart.get(p.id);
        cart.put(p.id, (cur == null ? 0 : cur) + 1);
        updateCartButton();
        Toast.makeText(this, getString(R.string.str_ts_added, p.name), Toast.LENGTH_SHORT).show();
    }

    private void updateCartButton() {
        int count = 0;
        for (int q : cart.values()) count += q;
        btnCart.setText(getString(R.string.str_ts_cart_count, count));
    }

    private void showCart() {
        if (cart.isEmpty()) {
            Toast.makeText(this, R.string.str_ts_cart_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder sb = new StringBuilder();
        double total = 0;
        for (Map.Entry<String, Integer> e : cart.entrySet()) {
            TechShopData.Product p = productById.get(e.getKey());
            if (p == null) continue;
            double line = p.price * e.getValue();
            total += line;
            sb.append(p.name).append("  x").append(e.getValue())
                    .append(" = ").append(CurrencyUtils.formatVnd(line)).append('\n');
        }
        sb.append('\n').append(getString(R.string.str_ts_cart_total, CurrencyUtils.formatVnd(total)));

        new AlertDialog.Builder(this)
                .setTitle(R.string.str_ts_cart_title)
                .setMessage(sb.toString())
                .setPositiveButton(R.string.str_ts_close, null)
                .setNeutralButton(R.string.str_ts_cart_clear, (d, w) -> {
                    cart.clear();
                    updateCartButton();
                })
                .show();
    }
}
