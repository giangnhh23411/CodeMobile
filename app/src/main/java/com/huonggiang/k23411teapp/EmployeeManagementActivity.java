package com.huonggiang.k23411teapp;

import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.huonggiang.adapters.EmployeeAdapter;
import com.huonggiang.models.Employee;

import java.util.ArrayList;
import java.util.Random;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.huonggiang.k23411teapp.databinding.ActivityEmployeeManagementBinding;

public class EmployeeManagementActivity extends AppCompatActivity {

    private ActivityEmployeeManagementBinding binding;
    private ArrayList<Employee> employeeList;
    private EmployeeAdapter adapter;
    private int selectedIndex = -1;

    private static final String PREFS_NAME = "EmployeePrefs";
    private static final String KEY_SELECTED_INDEX = "selectedIndex";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        // Initialize View Binding
        binding = ActivityEmployeeManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        addViews();
        addEvents();
        
        // Load saved selection state
        loadSavedState();
    }

    /**
     * Loads the saved selection index from SharedPreferences and restores the UI.
     */
    private void loadSavedState() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        selectedIndex = prefs.getInt(KEY_SELECTED_INDEX, -1);
        
        if (selectedIndex != -1 && selectedIndex < employeeList.size()) {
            Employee selectedEmp = employeeList.get(selectedIndex);
            binding.edtEmpId.setText(selectedEmp.getId());
            binding.edtName.setText(selectedEmp.getName());
            binding.edtPhone.setText(selectedEmp.getPhone());
            
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
            binding.lvEmployee.setSelection(selectedIndex);
        }
    }

    /**
     * Saves the current selected index to SharedPreferences.
     */
    private void saveState() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_SELECTED_INDEX, selectedIndex);
        editor.apply();
    }

    /**
     * Initialize views, adapter and data.
     */
    private void addViews() {
        MaterialToolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        employeeList = new ArrayList<>();
        sampleData();
        adapter = new EmployeeAdapter(this, R.layout.item_custom_employee, employeeList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (position == selectedIndex) {
                    view.setBackgroundColor(Color.parseColor("#E3F2FD")); // Light blue highlight
                } else {
                    view.setBackgroundColor(Color.TRANSPARENT);
                }
                return view;
            }
        };
        binding.lvEmployee.setAdapter(adapter);
    }
    
    private void addEvents() {
        binding.btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, EmployeeInputActivity.class);
            intent.putExtra("isEditMode", false);
            startActivityForResult(intent, 100);
        });

        binding.lvEmployee.setOnItemClickListener((parent, view, position, id) -> {
            selectedIndex = position;
            Employee selectedEmp = employeeList.get(selectedIndex);
            binding.edtEmpId.setText(selectedEmp.getId());
            binding.edtName.setText(selectedEmp.getName());
            binding.edtPhone.setText(selectedEmp.getPhone());
            
            adapter.notifyDataSetChanged();
            saveState();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Employee emp = (Employee) data.getSerializableExtra("EMPLOYEE_RESULT");
            if (emp == null) return;

            if (requestCode == 100) { // Add
                employeeList.add(0, emp);
                adapter.notifyDataSetChanged();
                Toast.makeText(this, R.string.str_msg_added, Toast.LENGTH_SHORT).show();
            } else if (requestCode == 200) { // Edit (if needed in this screen)
                if (selectedIndex != -1) {
                    employeeList.set(selectedIndex, emp);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, R.string.str_msg_updated, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void clearInputs(View view) {
        binding.edtEmpId.setText("");
        binding.edtName.setText("");
        binding.edtPhone.setText("");
        binding.edtEmpId.requestFocus();
        selectedIndex = -1;
        adapter.notifyDataSetChanged();
        saveState();
    }

    private void sampleData() {
        String[] firstNames = {"Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Huỳnh", "Phan", "Vũ", "Võ", "Đặng"};
        String[] middleNames = {"Văn", "Thị", "Hoàng", "Minh", "Đức", "Hồng", "Kim", "Quang", "Thành", "Anh"};
        String[] lastNames = {"Anh", "Bình", "Chinh", "Dung", "Em", "Giang", "Hương", "Khanh", "Linh", "Minh", "Nam", "Oanh", "Phúc", "Quân", "Sơn", "Tâm", "Uyên", "Vinh", "Xuân", "Yến"};
        String[] prefixes = {"090", "098", "091", "032", "035", "038", "070", "077", "081", "085"};

        Random random = new Random();
        for (int i = 1; i <= 50; i++) {
            String empId = "NV" + String.format(java.util.Locale.getDefault(), "%03d", i);
            String name = firstNames[random.nextInt(firstNames.length)] + " " +
                         middleNames[random.nextInt(middleNames.length)] + " " +
                         lastNames[random.nextInt(lastNames.length)];
            
            StringBuilder phone = new StringBuilder(prefixes[random.nextInt(prefixes.length)]);
            for (int p = 0; p < 7; p++) {
                phone.append(random.nextInt(10));
            }
            employeeList.add(new Employee(empId, name, phone.toString()));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public void closeActivity(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.str_confirm_exit_title);
        builder.setMessage(R.string.str_confirm_exit_msg);
        builder.setPositiveButton(R.string.str_yes, (dialog, which) -> finish());
        builder.setNegativeButton(R.string.str_no, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    public void saveEmployee(View view) {
        String empId = binding.edtEmpId.getText().toString().trim();
        String name = binding.edtName.getText().toString().trim();
        String phone = binding.edtPhone.getText().toString().trim();

        if (empId.isEmpty() || name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, R.string.str_msg_input_full, Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.str_confirm_save_title);
        builder.setMessage(selectedIndex == -1 ? "Bạn có muốn thêm nhân viên mới?" : getString(R.string.str_confirm_save_msg));
        builder.setPositiveButton(R.string.str_yes, (dialog, which) -> {
            Employee emp = new Employee(empId, name, phone);
            if (selectedIndex == -1) {
                employeeList.add(0, emp); // Add to top
                Toast.makeText(this, "Đã thêm nhân viên mới", Toast.LENGTH_SHORT).show();
            } else {
                employeeList.set(selectedIndex, emp);
                Toast.makeText(this, R.string.str_msg_updated, Toast.LENGTH_SHORT).show();
            }
            
            adapter.notifyDataSetChanged();
            clearInputs(null);
        });
        builder.setNegativeButton(R.string.str_no, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    public void deleteEmployee(View view) {
        if (selectedIndex == -1) {
            Toast.makeText(this, R.string.str_msg_select_delete, Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.str_confirm_delete_title);
        builder.setMessage(R.string.str_confirm_delete_msg);
        builder.setPositiveButton(R.string.str_yes, (dialog, which) -> {
            employeeList.remove(selectedIndex);
            
            adapter.notifyDataSetChanged();

            clearInputs(null);
            Toast.makeText(this, R.string.str_msg_deleted, Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton(R.string.str_no, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
}
