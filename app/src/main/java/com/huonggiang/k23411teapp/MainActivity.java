package com.huonggiang.k23411teapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.huonggiang.models.UserAccount;

public class MainActivity extends AppCompatActivity {

    TextView txtWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        addViews();
        addEvents();
    }

    private void addViews() {
        txtWelcome = findViewById(R.id.txtWelcome);
        Intent intent = getIntent();
        UserAccount acc = (UserAccount) intent.getSerializableExtra("USER_ACCOUNT");
        if (acc != null) {
            // Sử dụng String resource với placeholder đã thêm trước đó
            txtWelcome.setText(getString(R.string.str_welcome_user, acc.getDisplayName()));
        }
    }

    private void addEvents() {
        Button btnOpenCalculator = findViewById(R.id.btnOpenCalculator);
        if (btnOpenCalculator != null) {
            btnOpenCalculator.setOnClickListener(v -> openCalculator(v));
        }

        Button btnEmployeeManagement = findViewById(R.id.btnEmployeeManagement);
        if (btnEmployeeManagement != null) {
            btnEmployeeManagement.setOnClickListener(v -> openEmployeeManagement(v));
        }

        Button btnEmployeeAdvancedManagement = findViewById(R.id.btnEmployeeAdvancedManagement);
        if (btnEmployeeAdvancedManagement != null) {
            btnEmployeeAdvancedManagement.setOnClickListener(v -> openEmployeeAdvancedManagement(v));
        }
    }

    public void closeApp(View view) {
        finish();
    }

    public void openCalculator(View view) {
        Intent intent = new Intent(MainActivity.this, CalculatorActivity.class);
        startActivity(intent);
    }

    public void openEmployeeManagement(View view) {
        Intent intent = new Intent(MainActivity.this, EmployeeManagementActivity.class);
        startActivity(intent);
    }

    public void openEmployeeAdvancedManagement(View view) {
        Intent intent = new Intent(MainActivity.this, EmployeeAdvancedActivity.class);
        startActivity(intent);
    }

    public void openBillingManagement(View view) {
        Intent intent = new Intent(MainActivity.this, OrderManagementActivity.class);
        startActivity(intent);
    }

    public void openCategoryManagement(View view) {
        Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
        startActivity(intent);
    }

    public void openRaoVat(View view) {
        Intent intent = new Intent(MainActivity.this, RaoVatActivity.class);
        startActivity(intent);
    }

    public void openFontAndMusic(View view) {
        Intent intent = new Intent(MainActivity.this, FontAndMusicActivity.class);
        startActivity(intent);
    }

    public void openMyuelSearch(View view) {
        Intent intent = new Intent(MainActivity.this, MyuelSearchActivity.class);
        startActivity(intent);
    }

    public void openFirebaseSync(View view) {
        Intent intent = new Intent(MainActivity.this, FirebaseSyncActivity.class);
        startActivity(intent);
    }
}