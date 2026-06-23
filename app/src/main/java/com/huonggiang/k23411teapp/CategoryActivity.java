package com.huonggiang.k23411teapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.huonggiang.adapters.CategoryAdapter;
import com.huonggiang.models.Category;

import java.util.ArrayList;

import DALS.CategoryDAO;

public class CategoryActivity extends AppCompatActivity {

    ListView lvCategory;
    ArrayList<Category> categories;
    CategoryAdapter categoryAdapter;

    /** Mở CategoryNewActivity (thêm/sửa) và tự refresh danh sách khi quay lại. */
    private final ActivityResultLauncher<Intent> categoryFormLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            loadCategories();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_category);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
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

        lvCategory = findViewById(R.id.lvCategory);
        loadCategories();

        // Click → mở danh sách sản phẩm của category
        lvCategory.setOnItemClickListener((parent, view, position, id) -> {
            Category selected = categories.get(position);
            Intent intent = new Intent(this, ProductManagementActivity.class);
            intent.putExtra(ProductManagementActivity.EXTRA_CATEGORY_ID,   selected.getCategoryId());
            intent.putExtra(ProductManagementActivity.EXTRA_CATEGORY_NAME, selected.getCategoryName());
            startActivity(intent);
        });

        // Long-click → menu Sửa / Xóa
        lvCategory.setOnItemLongClickListener((parent, view, position, id) -> {
            showEditDeleteDialog(categories.get(position));
            return true;
        });
    }

    /** Truy vấn lại DB và refresh ListView. */
    private void loadCategories() {
        categories = CategoryDAO.getCategories(this);
        categoryAdapter = new CategoryAdapter(this, R.layout.category_custom_item);
        categoryAdapter.addAll(categories);
        lvCategory.setAdapter(categoryAdapter);
    }

    private void showEditDeleteDialog(Category category) {
        String[] options = {
                getString(R.string.str_edit),
                getString(R.string.str_delete)
        };
        new AlertDialog.Builder(this)
                .setTitle(category.getCategoryName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openEditForm(category);
                    } else {
                        confirmDelete(category);
                    }
                })
                .show();
    }

    private void openEditForm(Category category) {
        Intent intent = new Intent(this, CategoryNewActivity.class);
        intent.putExtra(CategoryNewActivity.EXTRA_CATEGORY_ID,   category.getCategoryId());
        intent.putExtra(CategoryNewActivity.EXTRA_CATEGORY_NAME, category.getCategoryName());
        intent.putExtra(CategoryNewActivity.EXTRA_CATEGORY_DESC, category.getDescription());
        categoryFormLauncher.launch(intent);
    }

    private void confirmDelete(Category category) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.str_confirm_delete_title)
                .setMessage(R.string.str_confirm_delete_category_msg)
                .setPositiveButton(R.string.str_yes, (dialog, which) -> {
                    int rows = CategoryDAO.deleteCategory(this, category.getCategoryId());
                    if (rows > 0) {
                        Toast.makeText(this, R.string.str_category_deleted, Toast.LENGTH_SHORT).show();
                        loadCategories();
                    }
                })
                .setNegativeButton(R.string.str_no, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.category_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.mnu_category_new) {
            categoryFormLauncher.launch(new Intent(this, CategoryNewActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
