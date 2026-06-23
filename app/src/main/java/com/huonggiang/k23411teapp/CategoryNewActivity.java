package com.huonggiang.k23411teapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.huonggiang.models.Category;

import DALS.CategoryDAO;

public class CategoryNewActivity extends AppCompatActivity {

    /** Intent extras dùng cho chế độ sửa. */
    public static final String EXTRA_CATEGORY_ID   = "extra_category_id";
    public static final String EXTRA_CATEGORY_NAME = "extra_category_name";
    public static final String EXTRA_CATEGORY_DESC = "extra_category_desc";

    EditText edtCategoryId;
    EditText edtCategoryName;
    EditText edtCategoryDescription;

    /** true = đang sửa category có sẵn, false = thêm mới. */
    private boolean editMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_category_new);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        addViews();
        checkIntent();
    }

    private void addViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        edtCategoryId          = findViewById(R.id.edtCategoryId);
        edtCategoryName        = findViewById(R.id.edtCategoryName);
        edtCategoryDescription = findViewById(R.id.edtCategoryDescription);
    }

    /** Nếu Intent mang dữ liệu category → chuyển sang chế độ sửa và đổ dữ liệu. */
    private void checkIntent() {
        String id = getIntent().getStringExtra(EXTRA_CATEGORY_ID);
        if (id != null) {
            editMode = true;
            setTitle(R.string.str_category_edit);

            edtCategoryId.setText(id);
            edtCategoryId.setEnabled(false); // khóa khóa chính khi sửa
            edtCategoryName.setText(getIntent().getStringExtra(EXTRA_CATEGORY_NAME));
            edtCategoryDescription.setText(getIntent().getStringExtra(EXTRA_CATEGORY_DESC));
        } else {
            setTitle(R.string.str_category_new);
        }
    }

    public void processSaveCategory(View view) {
        String cateId      = edtCategoryId.getText().toString().trim();
        String cateName    = edtCategoryName.getText().toString().trim();
        String description = edtCategoryDescription.getText().toString().trim();

        // Validate: không để trống
        if (TextUtils.isEmpty(cateId) || TextUtils.isEmpty(cateName)) {
            Toast.makeText(this, R.string.str_msg_input_full, Toast.LENGTH_SHORT).show();
            return;
        }

        Category category = new Category(cateId, cateName, description);

        if (editMode) {
            int rows = CategoryDAO.updateCategory(this, category);
            if (rows > 0) {
                Toast.makeText(this, R.string.str_category_updated, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        } else {
            // Chặn trùng khóa chính
            if (CategoryDAO.isCategoryExists(this, cateId)) {
                Toast.makeText(this, R.string.str_category_id_exists, Toast.LENGTH_SHORT).show();
                return;
            }
            long result = CategoryDAO.saveNewCategory(this, category);
            if (result > 0) {
                Toast.makeText(this, R.string.str_category_added, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    public void processCancelCategory(View view) {
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
