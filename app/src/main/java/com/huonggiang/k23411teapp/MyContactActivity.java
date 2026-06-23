package com.huonggiang.k23411teapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.huonggiang.adapters.ContactAdapter;
import com.huonggiang.models.Contact;

import java.util.ArrayList;

import DALS.MyContactDAO;

public class MyContactActivity extends AppCompatActivity {

    ListView lvMyContact;
    TextView tvEmptyContact;
    ContactAdapter adapter;
    final ArrayList<Contact> contacts = new ArrayList<>();

    /** Launcher xin quyền READ_CONTACTS lúc runtime. */
    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) {
                            loadContacts();
                        } else {
                            Toast.makeText(this,
                                    R.string.str_permission_contacts_denied,
                                    Toast.LENGTH_LONG).show();
                            updateEmptyState();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_contact);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainMyContact), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        addViews();
        requestContactsThenLoad();
    }

    private void addViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        lvMyContact    = findViewById(R.id.lvMyContact);
        tvEmptyContact = findViewById(R.id.tvEmptyContact);

        adapter = new ContactAdapter(this, R.layout.item_contact, contacts);
        lvMyContact.setAdapter(adapter);
    }

    /** Có quyền thì đọc luôn, chưa có thì xin. */
    private void requestContactsThenLoad() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            loadContacts();
        } else {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS);
        }
    }

    /** Đọc danh bạ qua DAO và đổ vào ListView. */
    private void loadContacts() {
        contacts.clear();
        contacts.addAll(MyContactDAO.getMyContacts(this));
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        boolean empty = contacts.isEmpty();
        lvMyContact.setVisibility(empty ? View.GONE : View.VISIBLE);
        tvEmptyContact.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
