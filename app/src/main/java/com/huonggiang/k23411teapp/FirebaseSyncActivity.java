package com.huonggiang.k23411teapp;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.huonggiang.adapters.FbContactAdapter;
import com.huonggiang.models.FbContact;
import com.huonggiang.utils.NetworkMonitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import DALS.FbContactDAO;

/**
 * Màn hình HỌC FIREBASE: CRUD danh bạ theo kiến trúc "offline-first" + đồng bộ.
 *
 *  - Remote DB : Firebase Realtime Database, node "contacts".
 *  - Local DB  : SQLite ({@link FbContactDAO}) làm bộ đệm offline + hàng đợi đồng bộ.
 *  - {@link NetworkMonitor}: phát hiện online/offline để tự kích hoạt đồng bộ.
 *
 * Ba trạng thái theo yêu cầu:
 *  1) ONLINE   : ghi xuống Local DB (đánh dấu) rồi đẩy NGAY lên Firebase → SYNCED.
 *  2) OFFLINE  : chỉ ghi xuống Local DB, đánh dấu PENDING_UPSERT / PENDING_DELETE.
 *  3) ONLINE trở lại: quét các bản PENDING trong Local DB và đẩy tuần tự lên Firebase.
 *
 * Đọc (Read) luôn lấy từ Local DB (offline-first); khi online, một ValueEventListener
 * lắng nghe Firebase realtime và cập nhật ngược lại Local DB.
 */
public class FirebaseSyncActivity extends AppCompatActivity {

    // URL Realtime Database theo vùng asia-southeast1 — BẮT BUỘC dùng URL đầy đủ.
    private static final String DATABASE_URL =
            "https://codemobile-79754-default-rtdb.asia-southeast1.firebasedatabase.app";
    private static final String NODE = "contacts";

    private EditText etName, etEmail, etPhone;
    private Button btnSave, btnDelete, btnSeed, btnSync;
    private TextView tvStatus, tvCount;
    private ListView lvContacts;

    private final List<FbContact> contacts = new ArrayList<>();
    private FbContactAdapter adapter;

    private FbContactDAO dao;
    private NetworkMonitor monitor;

    private DatabaseReference contactsRef;     // null nếu Firebase chưa cấu hình được
    private ValueEventListener serverListener;

    private String editingId = null;           // != null khi đang sửa một liên hệ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_firebase_sync);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        dao = new FbContactDAO(this);
        initFirebase();
        addViews();
        addEvents();

        monitor = new NetworkMonitor(this, new NetworkMonitor.Listener() {
            @Override public void onConnected(int transportType) {
                showStatus(true);
                syncNow(false);   // có mạng trở lại → tự đồng bộ
            }
            @Override public void onDisconnected() {
                showStatus(false);
            }
        });

        loadLocal();
    }

    /** Lấy tham chiếu tới node Firebase; nếu chưa cấu hình thì chạy chế độ local-only. */
    private void initFirebase() {
        try {
            contactsRef = FirebaseDatabase.getInstance(DATABASE_URL).getReference(NODE);
        } catch (Exception e) {
            contactsRef = null;
        }
    }

    private void addViews() {
        etName  = findViewById(R.id.etFbName);
        etEmail = findViewById(R.id.etFbEmail);
        etPhone = findViewById(R.id.etFbPhone);
        btnSave   = findViewById(R.id.btnFbSave);
        btnDelete = findViewById(R.id.btnFbDelete);
        btnSeed   = findViewById(R.id.btnFbSeed);
        btnSync   = findViewById(R.id.btnFbSync);
        tvStatus  = findViewById(R.id.tvFbStatus);
        tvCount   = findViewById(R.id.tvFbCount);
        lvContacts = findViewById(R.id.lvFbContacts);

        adapter = new FbContactAdapter(this, R.layout.item_fb_contact, contacts);
        lvContacts.setAdapter(adapter);
        btnDelete.setEnabled(false);
    }

    private void addEvents() {
        btnSave.setOnClickListener(v -> saveContact());
        btnDelete.setOnClickListener(v -> { if (editingId != null) deleteContact(editingId); });
        btnSeed.setOnClickListener(v -> seedSampleData());
        btnSync.setOnClickListener(v -> syncNow(true));
        lvContacts.setOnItemClickListener((parent, view, position, id) -> startEdit(contacts.get(position)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        monitor.start();
        showStatus(monitor.isOnline());
        attachServerListener();
        if (monitor.isOnline()) syncNow(false);
    }

    @Override
    protected void onStop() {
        detachServerListener();
        monitor.stop();
        super.onStop();
    }

    // ── Local DB → UI (đọc offline-first) ───────────────────────────────────────

    private void loadLocal() {
        contacts.clear();
        contacts.addAll(dao.getVisible());
        adapter.notifyDataSetChanged();
        tvCount.setText(getString(R.string.str_fb_count, contacts.size()));
    }

    // ── CRUD ────────────────────────────────────────────────────────────────────

    private void saveContact() {
        String name  = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            etName.setError(getString(R.string.str_fb_need_name));
            return;
        }

        String id = (editingId != null) ? editingId : newId();
        FbContact c = new FbContact(id, name, email, phone, FbContact.PENDING_UPSERT);
        dao.upsert(c);          // B1: luôn ghi Local DB trước (offline-first)
        loadLocal();
        clearForm();

        if (isOnline()) pushUpsert(c);  // B2: nếu online thì đẩy ngay lên Firebase
    }

    private void deleteContact(String id) {
        if (FbContact.PENDING_UPSERT.equals(dao.getStatus(id))) {
            // Bản ghi chưa từng lên Firebase → xoá hẳn ở Local DB
            dao.delete(id);
        } else {
            // Đã từng đồng bộ → đánh dấu chờ xoá, để khi online sẽ xoá trên Firebase
            dao.updateStatus(id, FbContact.PENDING_DELETE);
        }
        loadLocal();
        clearForm();
        if (isOnline()) pushDelete(id);
    }

    private void startEdit(FbContact c) {
        editingId = c.getId();
        etName.setText(c.getName());
        etEmail.setText(c.getEmail());
        etPhone.setText(c.getPhone());
        btnSave.setText(R.string.str_fb_update);
        btnDelete.setEnabled(true);
    }

    private void clearForm() {
        editingId = null;
        etName.setText("");
        etEmail.setText("");
        etPhone.setText("");
        btnSave.setText(R.string.str_fb_add);
        btnDelete.setEnabled(false);
    }

    // ── Đẩy thay đổi lên Firebase ───────────────────────────────────────────────

    private void pushUpsert(FbContact c) {
        if (contactsRef == null) return;
        contactsRef.child(c.getId()).setValue(toMap(c))
                .addOnSuccessListener(x -> { dao.updateStatus(c.getId(), FbContact.SYNCED); loadLocal(); });
    }

    private void pushDelete(String id) {
        if (contactsRef == null) { dao.delete(id); loadLocal(); return; }
        contactsRef.child(id).removeValue()
                .addOnSuccessListener(x -> { dao.delete(id); loadLocal(); });
    }

    /** Quét hàng đợi trong Local DB và đẩy mọi thay đổi đang chờ lên Firebase. */
    private void syncNow(boolean showToast) {
        if (!isOnline() || contactsRef == null) {
            if (showToast) toast(getString(R.string.str_fb_offline_cannot_sync));
            return;
        }
        List<FbContact> upserts = dao.getByStatus(FbContact.PENDING_UPSERT);
        List<FbContact> deletes = dao.getByStatus(FbContact.PENDING_DELETE);
        for (FbContact c : upserts) pushUpsert(c);
        for (FbContact c : deletes) pushDelete(c.getId());
        if (showToast) {
            int n = upserts.size() + deletes.size();
            toast(n == 0 ? getString(R.string.str_fb_all_synced)
                         : getString(R.string.str_fb_syncing, n));
        }
    }

    // ── Đọc realtime từ Firebase (chỉ chạy khi online) ──────────────────────────

    private void attachServerListener() {
        if (contactsRef == null || serverListener != null) return;
        serverListener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> serverIds = new ArrayList<>();
                for (DataSnapshot s : snapshot.getChildren()) {
                    String id = s.getKey();
                    serverIds.add(id);
                    // Không ghi đè bản đang chờ đồng bộ ở Local DB
                    String localStatus = dao.getStatus(id);
                    if (localStatus == null || FbContact.SYNCED.equals(localStatus)) {
                        dao.upsert(new FbContact(id,
                                str(s, "name"), str(s, "email"), str(s, "phone"),
                                FbContact.SYNCED));
                    }
                }
                // Bản đã SYNCED ở local nhưng không còn trên server → đã bị xoá nơi khác
                for (FbContact local : dao.getByStatus(FbContact.SYNCED)) {
                    if (!serverIds.contains(local.getId())) dao.delete(local.getId());
                }
                loadLocal();
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                toast(getString(R.string.str_fb_read_error));
            }
        };
        contactsRef.addValueEventListener(serverListener);
    }

    private void detachServerListener() {
        if (contactsRef != null && serverListener != null) {
            contactsRef.removeEventListener(serverListener);
            serverListener = null;
        }
    }

    // ── Dữ liệu mẫu (theo slide) ────────────────────────────────────────────────

    private void seedSampleData() {
        dao.upsert(new FbContact("contact1", "Trần Duy Thanh",      "thanhtd@uel.edu.vn",       "987773061",  FbContact.PENDING_UPSERT));
        dao.upsert(new FbContact("contact2", "Phạm Thị Xuân Diều",   "dieuptx@uel.edu.vn",       "911111111",  FbContact.PENDING_UPSERT));
        dao.upsert(new FbContact("contact3", "Trần Phạm Thanh Trà",  "thanhtra@gmail.com",       "1112223334", FbContact.PENDING_UPSERT));
        dao.upsert(new FbContact("contact4", "Trần Phạm Mẫn Nhi",    "tranphammannhi@gmail.com", "3334446667", FbContact.PENDING_UPSERT));
        loadLocal();
        if (isOnline()) syncNow(false);
        toast(getString(R.string.str_fb_seeded));
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private boolean isOnline() {
        return monitor != null && monitor.isOnline();
    }

    private void showStatus(boolean online) {
        tvStatus.setText(online ? R.string.str_fb_online : R.string.str_fb_offline);
        tvStatus.setBackgroundColor(Color.parseColor(online ? "#2E7D32" : "#C62828"));
        btnSync.setEnabled(online && contactsRef != null);
    }

    /** Tạo key duy nhất cho liên hệ mới (push().getKey() chạy được cả khi offline). */
    private String newId() {
        if (contactsRef != null) {
            String key = contactsRef.push().getKey();
            if (key != null) return key;
        }
        return "local-" + System.currentTimeMillis();
    }

    private Map<String, Object> toMap(FbContact c) {
        Map<String, Object> m = new HashMap<>();
        m.put("name", c.getName());
        m.put("email", c.getEmail());
        m.put("phone", c.getPhone());
        return m;
    }

    private static String str(DataSnapshot snapshot, String key) {
        Object value = snapshot.child(key).getValue();
        return value == null ? "" : value.toString();
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
