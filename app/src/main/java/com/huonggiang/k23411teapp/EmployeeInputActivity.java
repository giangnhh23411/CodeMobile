package com.huonggiang.k23411teapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.huonggiang.models.Employee;

public class EmployeeInputActivity extends AppCompatActivity {

    private EditText edtId, edtName, edtPhone;
    private AutoCompleteTextView autoBirthPlace;
    private ImageButton btnSave, btnCancel;

    private boolean isEditMode = false;
    private Employee employeeToEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.employee_input_activity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initViews();
        setupAutoComplete();
        checkIntent();
        setupListeners();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void initViews() {
        edtId = findViewById(R.id.edtId);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        autoBirthPlace = findViewById(R.id.autoBirthPlace);
        btnSave = findViewById(R.id.imgSave);
        btnCancel = findViewById(R.id.imgCancel);
    }

    private void setupAutoComplete() {
        String[] provinces = getResources().getStringArray(R.array.provinces_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, provinces);
        autoBirthPlace.setAdapter(adapter);
        autoBirthPlace.setThreshold(1);
    }

    private void checkIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            isEditMode = intent.getBooleanExtra("isEditMode", false);
            employeeToEdit = (Employee) intent.getSerializableExtra("EDIT_EMPLOYEE");

            if (employeeToEdit != null) {
                isEditMode = true;
                if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.str_edit_employee);
                edtId.setText(employeeToEdit.getId());
                edtId.setEnabled(false);
                edtName.setText(employeeToEdit.getName());
                edtPhone.setText(employeeToEdit.getPhone());
                autoBirthPlace.setText(employeeToEdit.getBirthPlace());
            } else if (isEditMode) {
                if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.str_edit_employee);
                edtId.setEnabled(false);
            } else {
                if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.str_add_employee);
            }
        }
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveEmployee());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void saveEmployee() {
        String id = edtId.getText().toString().trim();
        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String birthPlace = autoBirthPlace.getText().toString().trim();

        if (id.isEmpty()) {
            edtId.setError(getString(R.string.str_msg_empty));
            edtId.requestFocus();
            return;
        }
        if (name.isEmpty()) {
            edtName.setError(getString(R.string.str_msg_empty));
            edtName.requestFocus();
            return;
        }
        if (phone.isEmpty()) {
            edtPhone.setError(getString(R.string.str_msg_empty));
            edtPhone.requestFocus();
            return;
        }
        if (!phone.matches("\\d{10,11}")) {
            edtPhone.setError(getString(R.string.str_msg_invalid_phone));
            edtPhone.requestFocus();
            return;
        }
        if (birthPlace.isEmpty()) {
            autoBirthPlace.setError(getString(R.string.str_msg_empty));
            autoBirthPlace.requestFocus();
            return;
        }

        Employee employee = new Employee(id, name, phone, birthPlace);
        Intent resultIntent = new Intent();
        resultIntent.putExtra("EMPLOYEE_RESULT", employee);
        resultIntent.putExtra("isEditMode", isEditMode);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
