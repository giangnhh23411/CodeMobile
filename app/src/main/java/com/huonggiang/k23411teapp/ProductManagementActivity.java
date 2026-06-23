package com.huonggiang.k23411teapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.huonggiang.adapters.ProductAdapter;
import com.huonggiang.models.Product;

import java.util.ArrayList;

import DALS.ProductDAO;

public class ProductManagementActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY_ID   = "extra_category_id";
    public static final String EXTRA_CATEGORY_NAME = "extra_category_name";

    ListView lvProducts;
    TextView tvNoProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_management);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        addViews();
    }

    private void addViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        lvProducts   = findViewById(R.id.lvProducts);
        tvNoProducts = findViewById(R.id.tvNoProducts);

        // Nhận category từ intent
        String categoryId   = getIntent().getStringExtra(EXTRA_CATEGORY_ID);
        String categoryName = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);

        // Đặt tên category làm tiêu đề toolbar
        if (categoryName != null && getSupportActionBar() != null) {
            getSupportActionBar().setTitle(categoryName);
        }

        // Load sản phẩm từ DB thật
        ArrayList<Product> products = (categoryId != null)
                ? ProductDAO.getProductsByCategory(this, categoryId)
                : ProductDAO.getAllProducts(this);

        if (products.isEmpty()) {
            lvProducts.setVisibility(View.GONE);
            tvNoProducts.setVisibility(View.VISIBLE);
        } else {
            ProductAdapter adapter = new ProductAdapter(this, R.layout.product_item, products);
            lvProducts.setAdapter(adapter);
            lvProducts.setVisibility(View.VISIBLE);
            tvNoProducts.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
