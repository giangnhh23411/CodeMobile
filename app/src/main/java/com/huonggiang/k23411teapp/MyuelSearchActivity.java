package com.huonggiang.k23411teapp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import com.huonggiang.adapters.MajorAdapter;
import com.huonggiang.models.Major;
import com.huonggiang.utils.Http;
import com.huonggiang.utils.MajorSearch;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tra cứu ngành đào tạo từ MYUEL.
 *
 * Tải MỘT trang MYUEL (trang này chứa toàn bộ danh sách ngành), parse các link ngành
 * Đại học - Chính quy để dựng chỉ mục, rồi tìm kiếm bằng {@link MajorSearch} (chuẩn hoá
 * tiếng Việt + xếp hạng fuzzy). Bấm một ngành sẽ mở màn hình chương trình đào tạo
 * ({@link MyuelResultActivity}).
 *
 * Việc gọi mạng chạy ở luồng nền; cập nhật giao diện đẩy về main thread qua Handler.
 */
public class MyuelSearchActivity extends AppCompatActivity {

    private static final String HOST = "https://myuel.uel.edu.vn";
    // Trang bất kỳ của module này đều chứa toàn bộ cây ngành ở panel bên trái
    private static final String LIST_URL = HOST
            + "/Default.aspx?ModuleId=f92f39b2-dea3-4185-8cbb-56c1c49c5226"
            + "&OlogyID=406&DepartmentID=05&GraduateLevelID=DH&StudyTypeID=CQ";

    // <a ... href="/Default.aspx?...OlogyID=...">Tên ngành</a>
    private static final Pattern MAJOR_LINK = Pattern.compile(
            "<a[^>]*?href=\"(/Default\\.aspx\\?[^\"]*OlogyID=[^\"]*)\"[^>]*>([^<]+)</a>");

    private EditText edtSearch;
    private ListView lvMajors;
    private ProgressBar pbLoading;
    private TextView tvEmpty;
    private TextView tvCount;
    private ImageButton btnVoice;

    private final ArrayList<Major> allMajors = new ArrayList<>(); // chỉ mục đầy đủ
    private final ArrayList<Major> shown = new ArrayList<>();      // đang hiển thị
    private MajorAdapter adapter;

    private final Handler main = new Handler(Looper.getMainLooper());
    private volatile boolean destroyed = false;

    // Nhận kết quả nhận diện giọng nói (Recognition) và đổ vào ô tìm kiếm
    private final ActivityResultLauncher<Intent> voiceLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> matches = result.getData()
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty()) {
                        edtSearch.setText(matches.get(0));
                        edtSearch.setSelection(edtSearch.getText().length());
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_myuel_search);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        addViews();
        addEvents();
        loadMajors();
    }

    private void addViews() {
        edtSearch = findViewById(R.id.edtSearch);
        lvMajors = findViewById(R.id.lvMajors);
        pbLoading = findViewById(R.id.pbLoading);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvCount = findViewById(R.id.tvCount);
        btnVoice = findViewById(R.id.btnVoice);

        adapter = new MajorAdapter(this, R.layout.item_major, shown);
        lvMajors.setAdapter(adapter);
    }

    private void addEvents() {
        btnVoice.setOnClickListener(v -> startVoiceSearch());

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                applyQuery(s.toString());
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        lvMajors.setOnItemClickListener((parent, view, position, id) -> {
            if (position < 0 || position >= shown.size()) return;
            Major m = shown.get(position);
            Intent intent = new Intent(this, MyuelResultActivity.class);
            intent.putExtra(MyuelResultActivity.EXTRA_URL, m.getUrl());
            intent.putExtra(MyuelResultActivity.EXTRA_TITLE, m.getName());
            startActivity(intent);
        });
    }

    /** Mở hộp thoại nhận diện giọng nói (Recognition); kết quả đổ vào ô tìm kiếm. */
    private void startVoiceSearch() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.str_myuel_voice_prompt));
        try {
            voiceLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.str_myuel_voice_unavailable, Toast.LENGTH_SHORT).show();
        }
    }

    /** Tải & dựng chỉ mục ngành ở luồng nền. */
    private void loadMajors() {
        showLoading(true);
        new Thread(() -> {
            String html = Http.get(LIST_URL);
            ArrayList<Major> result = (html == null) ? null : parseMajors(html);
            main.post(() -> {
                if (destroyed) return;
                showLoading(false);
                if (result == null) {
                    showEmpty(getString(R.string.str_myuel_error));
                    return;
                }
                allMajors.clear();
                allMajors.addAll(result);
                applyQuery(edtSearch.getText().toString());
            });
        }).start();
    }

    /** Chạy thuật toán tìm kiếm và cập nhật danh sách hiển thị (main thread). */
    private void applyQuery(String query) {
        List<Major> matched = MajorSearch.search(allMajors, query);
        shown.clear();
        shown.addAll(matched);
        adapter.notifyDataSetChanged();

        tvCount.setText(getString(R.string.str_myuel_count, shown.size()));
        if (shown.isEmpty()) {
            showEmpty(getString(allMajors.isEmpty()
                    ? R.string.str_myuel_error : R.string.str_myuel_empty));
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void showLoading(boolean loading) {
        pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) tvEmpty.setVisibility(View.GONE);
    }

    private void showEmpty(String message) {
        tvEmpty.setText(message);
        tvEmpty.setVisibility(View.VISIBLE);
    }

    /** Parse tất cả link ngành từ HTML, loại trùng theo URL. */
    private ArrayList<Major> parseMajors(String html) {
        ArrayList<Major> list = new ArrayList<>();
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        Matcher matcher = MAJOR_LINK.matcher(html);
        while (matcher.find()) {
            String href = matcher.group(1);
            String name = unescape(matcher.group(2)).trim();
            if (name.isEmpty()) continue;

            // Chỉ lấy Đại học - Chính quy; bỏ các bậc/hệ khác.
            String level = param(href, "GraduateLevelID");
            String studyType = param(href, "StudyTypeID");
            if (!"DH".equals(level) || !"CQ".equals(studyType)) continue;

            String url = HOST + href;
            if (!seen.add(url)) continue; // loại trùng
            list.add(new Major(name, url, param(href, "OlogyID"), param(href, "DepartmentID")));
        }
        return list;
    }

    /** Lấy giá trị một tham số trong query string của href. */
    private static String param(String href, String key) {
        String marker = key + "=";
        int i = href.indexOf(marker);
        if (i < 0) return "";
        int start = i + marker.length();
        int end = href.indexOf('&', start);
        return end < 0 ? href.substring(start) : href.substring(start, end);
    }

    private static String unescape(String s) {
        return s.replace("&amp;", "&").replace("&nbsp;", " ").replace("&#39;", "'");
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        main.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
