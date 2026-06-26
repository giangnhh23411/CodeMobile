package com.huonggiang.k23411teapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

/**
 * Màn hình entry của TechShop: chọn vai trò Admin (thống kê) hoặc Khách hàng (mua sắm).
 */
public class TechShopActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tech_shop);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        Button btnAdmin = findViewById(R.id.btnTechAdmin);
        Button btnClient = findViewById(R.id.btnTechClient);
        btnAdmin.setOnClickListener(v ->
                startActivity(new Intent(this, TechShopAdminActivity.class)));
        btnClient.setOnClickListener(v ->
                startActivity(new Intent(this, TechShopClientActivity.class)));
    }
}
