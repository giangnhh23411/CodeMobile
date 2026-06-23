package com.huonggiang.k23411teapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.huonggiang.adapters.CurriculumAdapter;
import com.huonggiang.models.CurriculumCourse;
import com.huonggiang.utils.CurriculumParser;
import com.huonggiang.utils.Http;

import java.util.ArrayList;
import java.util.List;

/**
 * Hiển thị chương trình đào tạo của ngành được chọn (Đại học - Chính quy).
 *
 * Nhận URL trang chi tiết ngành + tên ngành qua Intent. Tải HTML ở luồng nền, parse
 * bằng {@link CurriculumParser} thành danh sách học kỳ → học phần rồi đổ vào
 * {@link CurriculumAdapter}. Toàn bộ nội dung hiển thị ngay trong app (không mở WebView,
 * không dẫn link sang MYUEL).
 */
public class MyuelResultActivity extends AppCompatActivity {

    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_TITLE = "extra_title";

    private TextView tvSummary;
    private TextView tvEmpty;
    private ProgressBar pbResult;

    private final List<CurriculumAdapter.Row> rows = new ArrayList<>();
    private CurriculumAdapter adapter;

    private final Handler main = new Handler(Looper.getMainLooper());
    private volatile boolean destroyed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_myuel_result);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvTitle = findViewById(R.id.tvResultTitle);
        tvSummary = findViewById(R.id.tvResultSummary);
        pbResult = findViewById(R.id.pbResult);
        tvEmpty = findViewById(R.id.tvEmpty);
        ListView lvCurriculum = findViewById(R.id.lvCurriculum);

        adapter = new CurriculumAdapter(this, rows);
        lvCurriculum.setAdapter(adapter);

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        tvTitle.setText(title != null ? title : getString(R.string.str_myuel_result_title));

        loadCurriculum(getIntent().getStringExtra(EXTRA_URL));
    }

    /** Tải & parse chương trình đào tạo ở luồng nền, cập nhật giao diện trên main thread. */
    private void loadCurriculum(String url) {
        if (url == null || url.isEmpty()) {
            showEmpty(getString(R.string.str_curriculum_error));
            return;
        }
        showLoading(true);
        new Thread(() -> {
            String html = Http.get(url);
            List<CurriculumParser.Semester> semesters =
                    (html == null) ? null : CurriculumParser.parse(html);
            main.post(() -> {
                if (destroyed) return;
                showLoading(false);
                if (semesters == null) {
                    showEmpty(getString(R.string.str_curriculum_error));
                } else {
                    showCurriculum(semesters);
                }
            });
        }).start();
    }

    private void showCurriculum(List<CurriculumParser.Semester> semesters) {
        rows.clear();
        int courseCount = 0;
        double totalCredits = 0;
        for (CurriculumParser.Semester s : semesters) {
            rows.add(CurriculumAdapter.Row.header(s.title));
            for (CurriculumCourse c : s.courses) {
                rows.add(CurriculumAdapter.Row.course(c));
                courseCount++;
                totalCredits += parseCredits(c.getCredits());
            }
        }
        adapter.notifyDataSetChanged();

        if (rows.isEmpty()) {
            showEmpty(getString(R.string.str_curriculum_empty));
        } else {
            tvEmpty.setVisibility(View.GONE);
            tvSummary.setText(getString(R.string.str_curriculum_summary,
                    courseCount, CurriculumAdapter.formatCredits(String.valueOf(totalCredits))));
        }
    }

    private void showLoading(boolean loading) {
        pbResult.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) tvEmpty.setVisibility(View.GONE);
    }

    private void showEmpty(String message) {
        tvSummary.setText("");
        tvEmpty.setText(message);
        tvEmpty.setVisibility(View.VISIBLE);
    }

    private static double parseCredits(String raw) {
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        main.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
