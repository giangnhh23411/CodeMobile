package com.huonggiang.k23411teapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.huonggiang.adapters.EmployeeAdapter;
import com.huonggiang.models.Department;
import com.huonggiang.models.Employee;

import java.util.ArrayList;

public class EmployeeAdvancedActivity extends AppCompatActivity {
    ListView lvEmployee;
    ArrayList<Employee> listOfEmployee;
    EmployeeAdapter adapterEmployee;

    Spinner spDepartment;
    ArrayList<Department> listOfDepartment;
    ArrayAdapter<Department> adapterDepartment;

    MaterialCardView cardEmployeeDetail;
    EditText edtId, edtPhone;
    MaterialAutoCompleteTextView edtName;
    Button btnAdd, btnEdit, btnDelete;

    ArrayAdapter<String> adapterSuggestion;
    ArrayList<String> employeeNames = new ArrayList<>();
    
    Employee selectedEmployee = null;
    int lastSelectedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_advanced);
        
        addViews();
        sampleData();
        addEvents();
        
        View mainView = findViewById(R.id.main_advanced);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Employee emp = (Employee) data.getSerializableExtra("EMPLOYEE_RESULT");
            if (emp == null) return;

            if (requestCode == 100) { // Add mode
                Department targetDept = getTargetDepartmentForAdd();
                targetDept.addEmployee(emp);
                updateEmployeeList();
                updateSuggestions();
                Toast.makeText(this,
                        getString(R.string.str_msg_added_to_dept, targetDept.getDepartmentName()),
                        Toast.LENGTH_SHORT).show();
            } else if (requestCode == 200) { // Edit mode
                if (selectedEmployee != null) {
                    selectedEmployee.setId(emp.getId());
                    selectedEmployee.setName(emp.getName());
                    selectedEmployee.setPhone(emp.getPhone());
                    selectedEmployee.setBirthPlace(emp.getBirthPlace());
                    updateEmployeeInDepartment(selectedEmployee);
                    adapterEmployee.notifyDataSetChanged();
                    updateSuggestions();
                    Toast.makeText(this, R.string.str_msg_updated, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Trả về phòng ban sẽ nhận nhân viên mới:
     * - Nếu đang chọn một phòng cụ thể → dùng phòng đó
     * - Nếu không chọn hoặc đang chọn "Tất cả" → đưa vào Phòng Nhân Sự
     */
    private Department getTargetDepartmentForAdd() {
        Department selectedDept = (Department) spDepartment.getSelectedItem();
        if (selectedDept == null || "ALL".equals(selectedDept.getDepartmentId())) {
            for (Department dept : listOfDepartment) {
                if ("hr".equals(dept.getDepartmentId())) {
                    return dept;
                }
            }
        }
        return selectedDept;
    }

    private void addEvents() {
        spDepartment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updateEmployeeList();
                clearDetail();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        lvEmployee.setOnItemClickListener((adapterView, view, position, l) -> {
            selectedEmployee = adapterEmployee.getItem(position);
            lastSelectedPosition = position;
            adapterEmployee.setSelectedPosition(position);
            showDetail(selectedEmployee);
        });

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, EmployeeInputActivity.class);
            intent.putExtra("isEditMode", false);
            startActivityForResult(intent, 100); // 100 for Add
        });

        btnEdit.setOnClickListener(v -> {
            if (selectedEmployee == null) {
                Toast.makeText(this, R.string.str_msg_select_update, Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, EmployeeInputActivity.class);
            intent.putExtra("isEditMode", true);
            intent.putExtra("EDIT_EMPLOYEE", selectedEmployee);
            startActivityForResult(intent, 200); // 200 for Edit
        });

        btnDelete.setOnClickListener(v -> {
            if (selectedEmployee == null) return;
            // Tìm và xóa trong danh sách gốc của phòng ban
            for (Department dept : listOfDepartment) {
                if (dept.getListOfEmployee() != null && dept.getListOfEmployee().contains(selectedEmployee)) {
                    dept.getListOfEmployee().remove(selectedEmployee);
                    break;
                }
            }
            updateEmployeeList();
            updateSuggestions();
            clearDetail();
            Toast.makeText(this, R.string.str_msg_deleted, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateEmployeeInDepartment(Employee updatedEmp) {
        for (Department dept : listOfDepartment) {
            if (dept.getListOfEmployee() != null) {
                for (int i = 0; i < dept.getListOfEmployee().size(); i++) {
                    if (dept.getListOfEmployee().get(i).getId().equals(updatedEmp.getId())) {
                        dept.getListOfEmployee().set(i, updatedEmp);
                        return;
                    }
                }
            }
        }
    }

    private void updateSuggestions() {
        employeeNames.clear();
        for (Department dept : listOfDepartment) {
            if (!"ALL".equals(dept.getDepartmentId()) && dept.getListOfEmployee() != null) {
                for (Employee emp : dept.getListOfEmployee()) {
                    if (!employeeNames.contains(emp.getName())) {
                        employeeNames.add(emp.getName());
                    }
                }
            }
        }
        adapterSuggestion.clear();
        adapterSuggestion.addAll(employeeNames);
        adapterSuggestion.notifyDataSetChanged();
    }

    private void updateEmployeeList() {
        Department selectedDept = (Department) spDepartment.getSelectedItem();
        adapterEmployee.clear();
        if ("ALL".equals(selectedDept.getDepartmentId())) {
            for (Department d : listOfDepartment) {
                if (!"ALL".equals(d.getDepartmentId()) && d.getListOfEmployee() != null) {
                    adapterEmployee.addAll(d.getListOfEmployee());
                }
            }
        } else {
            if (selectedDept.getListOfEmployee() != null) {
                adapterEmployee.addAll(selectedDept.getListOfEmployee());
            }
        }
        adapterEmployee.notifyDataSetChanged();
    }

    private void showDetail(Employee emp) {
        cardEmployeeDetail.setVisibility(View.VISIBLE);
        edtId.setText(emp.getId());
        edtName.setText(emp.getName());
        edtPhone.setText(emp.getPhone());
        btnEdit.setEnabled(true);
        btnDelete.setEnabled(true);
    }

    private void clearDetail() {
        selectedEmployee = null;
        lastSelectedPosition = -1;
        adapterEmployee.setSelectedPosition(-1);
        cardEmployeeDetail.setVisibility(View.GONE);
        edtId.setText("");
        edtName.setText("");
        edtPhone.setText("");
    }

    private void sampleData() {
        listOfDepartment.add(new Department("ALL", getString(R.string.str_all_departments)));

        Department hr = new Department("hr", getString(R.string.str_dept_hr));
        listOfDepartment.add(hr);

        Department d1 = new Department("d1", getString(R.string.str_dept_1));
        d1.addEmployee(new Employee("e1", "Nguyễn Văn Tèo", "0981234567"));
        d1.addEmployee(new Employee("e2", "Lê Thị Tý", "0980000001"));
        listOfDepartment.add(d1);

        Department d2 = new Department("d2", getString(R.string.str_dept_2));
        d2.addEmployee(new Employee("e3", "Phạm Hồng Hoa", "0912345678"));
        listOfDepartment.add(d2);

        adapterDepartment.notifyDataSetChanged();
        updateSuggestions();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void addViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lvEmployee = findViewById(R.id.lvEmployee);
        listOfEmployee = new ArrayList<>();
        adapterEmployee = new EmployeeAdapter(this, R.layout.item_custom_employee, listOfEmployee);
        lvEmployee.setAdapter(adapterEmployee);

        spDepartment = findViewById(R.id.spDepartment);
        listOfDepartment = new ArrayList<>();
        adapterDepartment = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listOfDepartment);
        adapterDepartment.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDepartment.setAdapter(adapterDepartment);

        cardEmployeeDetail = findViewById(R.id.cardEmployeeDetail);
        edtId = findViewById(R.id.edtId);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        btnAdd = findViewById(R.id.btnAdd);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        adapterSuggestion = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, employeeNames);
        edtName.setAdapter(adapterSuggestion);
    }
}
