package com.huonggiang.k23411teapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.huonggiang.models.ListUserAccount;
import com.huonggiang.models.UserAccount;
import com.huonggiang.utils.NetworkMonitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LoginActivity extends AppCompatActivity {

    // ── Views ─────────────────────────────────────────────────────────────────

    EditText edtUsername;
    EditText edtPassword;
    TextView txtMessage;
    TextView tvNetworkStatus;
    CheckBox chkSaveInfo;
    RadioButton radAdministrator, radEmployee;
    Button btnLogin;

    private NetworkMonitor networkMonitor;

    // ── Constants / SharedPreferences ─────────────────────────────────────────

    static final String NAME_SHARE_REF  = "LoginInfo";
    static final String DATABASE_NAME   = "K23411TEsales.sqlite";
    static final String DB_PATH_SUFFIX  = "/databases/";

    /** Tăng số này mỗi khi DB trong assets thay đổi để app copy đè bản cũ. */
    static final int DB_VERSION = 2;

    public static SQLiteDatabase database = null;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        copyDataBase();
        addViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences(NAME_SHARE_REF, MODE_PRIVATE);
        String username = prefs.getString("Username", "");
        String password = prefs.getString("Password", "");
        boolean saved   = prefs.getBoolean("SaveInfo", false);

        if (saved) {
            edtUsername.setText(username);
            edtPassword.setText(password);
        } else {
            edtUsername.setText("admin");
            edtPassword.setText("123");
            radAdministrator.setChecked(true);
        }
        chkSaveInfo.setChecked(saved);
    }

    // ── Network monitoring ─────────────────────────────────────────────────────

    @Override
    protected void onStart() {
        super.onStart();
        networkMonitor.start();
        // Hiển thị ngay trạng thái hiện tại nếu đang offline
        if (!networkMonitor.isOnline()) {
            showOffline();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        networkMonitor.stop();
    }

    /** Banner đỏ: mất kết nối. */
    private void showOffline() {
        tvNetworkStatus.setText(R.string.str_network_lost);
        tvNetworkStatus.setBackgroundColor(
                ContextCompat.getColor(this, R.color.network_offline));
        tvNetworkStatus.setVisibility(View.VISIBLE);
    }

    /** Banner xanh: đã kết nối / đổi mạng. Tự ẩn sau 2,5 giây. */
    private void showOnline(int transportType) {
        int msg;
        if (transportType == NetworkCapabilities.TRANSPORT_WIFI) {
            msg = R.string.str_network_connected_wifi;
        } else if (transportType == NetworkCapabilities.TRANSPORT_CELLULAR) {
            msg = R.string.str_network_connected_mobile;
        } else {
            msg = R.string.str_network_connected;
        }
        tvNetworkStatus.setText(msg);
        tvNetworkStatus.setBackgroundColor(
                ContextCompat.getColor(this, R.color.network_online));
        tvNetworkStatus.setVisibility(View.VISIBLE);
        tvNetworkStatus.postDelayed(() -> tvNetworkStatus.setVisibility(View.GONE), 2500);
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private void addViews() {
        edtUsername      = findViewById(R.id.edtUsername);
        edtPassword      = findViewById(R.id.edtPassword);
        txtMessage       = findViewById(R.id.txtMessage);
        tvNetworkStatus  = findViewById(R.id.tvNetworkStatus);
        chkSaveInfo      = findViewById(R.id.chkSaveInfo);
        radAdministrator = findViewById(R.id.radAdministrator);
        radEmployee      = findViewById(R.id.radEmployee);
        btnLogin = findViewById(R.id.btnLogin);

        // Giám sát mạng: mất mạng → banner đỏ; có mạng/đổi mạng → banner xanh + Toast
        networkMonitor = new NetworkMonitor(this, new NetworkMonitor.Listener() {
            @Override
            public void onConnected(int transportType) {
                showOnline(transportType);
            }

            @Override
            public void onDisconnected() {
                showOffline();
                Toast.makeText(LoginActivity.this,
                        R.string.str_network_lost, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Button handlers (wired via android:onClick in XML) ────────────────────

    public void loginSystem(View view) {
        String username = edtUsername.getText().toString();
        String pwd      = edtPassword.getText().toString();
        UserAccount acc = ListUserAccount.login(username, pwd);

        if (acc != null) {
            // Persist login info
            SharedPreferences.Editor editor =
                    getSharedPreferences(NAME_SHARE_REF, MODE_PRIVATE).edit();
            editor.putString("Username", username);
            editor.putString("Password", pwd);
            editor.putBoolean("SaveInfo", chkSaveInfo.isChecked());
            editor.apply();

            // Show success message
            txtMessage.setVisibility(View.VISIBLE);
            txtMessage.setBackgroundColor(Color.parseColor("#E8F5E9"));
            txtMessage.setTextColor(Color.parseColor("#2E7D32"));
            txtMessage.setText(getString(R.string.str_login_successful));

            // Navigate based on selected role
            // TODO: replace radio-button check with role returned from server
            Class<?> target = radAdministrator.isChecked()
                    ? MainActivity.class
                    : EmployeeAdvancedActivity.class;
            Intent intent = new Intent(this, target);
            intent.putExtra("USER_ACCOUNT", acc);
            startActivity(intent);

        } else {
            // Show failure message
            txtMessage.setVisibility(View.VISIBLE);
            txtMessage.setBackgroundColor(Color.parseColor("#FFEBEE"));
            txtMessage.setTextColor(Color.parseColor("#C62828"));
            txtMessage.setText(getString(R.string.str_login_failed));
        }
    }

    public void exitSystem(View view) {
        finish();
    }

    // ── Database helpers ──────────────────────────────────────────────────────

    /**
     * Copies the bundled SQLite database from assets to the app's database
     * directory. Copies (overwriting) when the DB does not yet exist OR when
     * DB_VERSION is newer than the version stored in SharedPreferences.
     */
    private void copyDataBase() {
        try {
            SharedPreferences prefs = getSharedPreferences(NAME_SHARE_REF, MODE_PRIVATE);
            int savedVersion = prefs.getInt("DbVersion", 0);

            File dbFile = getDatabasePath(DATABASE_NAME);
            boolean needCopy = !dbFile.exists() || savedVersion < DB_VERSION;

            if (needCopy) {
                if (copyDBFromAsset()) {
                    prefs.edit().putInt("DbVersion", DB_VERSION).apply();
                    Toast.makeText(this, "Database updated (v" + DB_VERSION + ")", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Copy database fail!", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Log.e("LoginActivity", "copyDataBase error: " + e.toString());
        }
    }

    private boolean copyDBFromAsset() {
        String dbPath = getApplicationInfo().dataDir + DB_PATH_SUFFIX + DATABASE_NAME;
        try {
            InputStream  input  = getAssets().open(DATABASE_NAME);
            File         dir    = new File(getApplicationInfo().dataDir + DB_PATH_SUFFIX);
            if (!dir.exists()) dir.mkdir();

            OutputStream output = new FileOutputStream(dbPath);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            output.flush();
            output.close();
            input.close();
            return true;
        } catch (IOException e) {
            Log.e("LoginActivity", "copyDBFromAsset error: " + e.toString());
            return false;
        }
    }
}
