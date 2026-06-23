package com.huonggiang.k23411teapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.huonggiang.models.Employee;

public class AddEmployeeActivity extends AppCompatActivity {

    private EditText edtId, edtName, edtPhone;
    private AutoCompleteTextView autoBirthPlace;
    private boolean isEditMode = false;
    private Employee employeeToEdit;
    ImageView imgSave, imgCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_employee);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        addViews();
        setupAutoComplete();
        checkIntent();
        addEvents();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void addViews() {
        edtId = findViewById(R.id.edtId);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        autoBirthPlace = findViewById(R.id.autoBirthPlace);
        imgSave = findViewById(R.id.imgSave);
        imgCancel = findViewById(R.id.imgCancel);
    }

    private void addEvents() {
        imgSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processSaveEmployee();
            }
        });
        imgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void processSaveEmployee() {
        Employee emp = new Employee();
        emp.setId(edtId.getText().toString());
        emp.setName(edtName.getText().toString());
        emp.setPhone(edtPhone.getText().toString());
        emp.setBirthPlace(autoBirthPlace.getText().toString());

        Intent intent = getIntent();
        intent.putExtra("EMPLOYEE", emp);
        setResult(888, intent);
        finish();
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

    private void saveEmployee() {
        String id = edtId.getText().toString().trim();
        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String birthPlace = autoBirthPlace.getText().toString().trim();

        if (id.isEmpty()) { edtId.setError(getString(R.string.str_msg_empty)); return; }
        if (name.isEmpty()) { edtName.setError(getString(R.string.str_msg_empty)); return; }
        if (phone.isEmpty()) { edtPhone.setError(getString(R.string.str_msg_empty)); return; }
        if (!phone.matches("\\d{10,11}")) { edtPhone.setError(getString(R.string.str_msg_invalid_phone)); return; }
        if (birthPlace.isEmpty()) { autoBirthPlace.setError(getString(R.string.str_msg_empty)); return; }

        Employee employee = new Employee(id, name, phone, birthPlace);
        Intent resultIntent = new Intent();
        resultIntent.putExtra("EMPLOYEE_RESULT", employee);
        resultIntent.putExtra("IS_EDIT", isEditMode);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
