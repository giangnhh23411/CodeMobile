package com.huonggiang.k23411teapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Demo đa luồng: nhập số lượng button cần tạo rồi bấm "Create Button" để tạo
 * đồng thời bằng NHIỀU luồng chạy song song. Mỗi luồng xử lý một phần công việc,
 * tất cả cùng cập nhật một ProgressBar + phần trăm tiến độ chung.
 *
 * Nguyên tắc đa luồng trên Android:
 *  - Việc nặng (xử lý từng "quy trình") chạy ở các luồng nền trong {@link ExecutorService}.
 *  - Mọi thao tác cập nhật View (ProgressBar, txtPercent, addView) bắt buộc phải chạy ở
 *    main thread, nên được đẩy về qua {@link Handler} gắn với main looper.
 *  - Trạng thái giao diện (rendered/total) chỉ được đọc/ghi ở main thread nên không cần
 *    khoá đồng bộ; các luồng nền không chia sẻ biến thay đổi nào với nhau.
 */
public class MultiThreadActivity extends AppCompatActivity {

    /** Số luồng chạy song song để cùng xử lý công việc. */
    private static final int THREAD_COUNT = 4;
    /** Giới hạn an toàn, tránh tạo quá nhiều View gây treo/OOM. */
    private static final int MAX_BUTTONS = 5000;

    private EditText edtNumberButton;
    private TextView txtPercent;
    private ProgressBar progressBarPercent;
    private LinearLayout linearLayoutButton;
    private Button btnCreate;

    /** Handler gắn với main thread để cập nhật giao diện từ luồng nền. */
    private final Handler main = new Handler(Looper.getMainLooper());
    private ExecutorService executor;

    private int total;                 // tổng số button của lượt hiện tại (main thread)
    private int rendered;              // số button đã thêm xong (main thread)
    private volatile boolean running;  // đang có một lượt tạo chạy hay không

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_multi_thread);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        addViews();
        addEvents();
    }

    private void addViews() {
        edtNumberButton = findViewById(R.id.edtNumberButton);
        txtPercent = findViewById(R.id.txtPercent);
        progressBarPercent = findViewById(R.id.progressBarPercent);
        linearLayoutButton = findViewById(R.id.linearLayoutButton);
        btnCreate = findViewById(R.id.button11);
    }

    private void addEvents() {
        if (btnCreate != null) {
            btnCreate.setOnClickListener(v -> startCreateButtons());
        }
    }

    /** Bắt đầu một lượt tạo button bằng nhiều luồng song song. */
    private void startCreateButtons() {
        if (running) {
            Toast.makeText(this, R.string.str_mt_running, Toast.LENGTH_SHORT).show();
            return;
        }

        int n = parseCount();
        if (n <= 0) {
            Toast.makeText(this, R.string.str_mt_invalid_number, Toast.LENGTH_SHORT).show();
            return;
        }
        if (n > MAX_BUTTONS) {
            n = MAX_BUTTONS;
            Toast.makeText(this, getString(R.string.str_mt_capped, MAX_BUTTONS), Toast.LENGTH_SHORT).show();
        }

        // Đặt lại giao diện cho lượt mới
        linearLayoutButton.removeAllViews();
        progressBarPercent.setMax(n);
        progressBarPercent.setProgress(0);
        txtPercent.setText(getString(R.string.str_mt_percent, 0));

        total = n;
        rendered = 0;
        running = true;
        btnCreate.setEnabled(false);

        executor = Executors.newFixedThreadPool(THREAD_COUNT);

        // Chia đều n công việc cho THREAD_COUNT luồng (phần dư rải cho các luồng đầu)
        int base = n / THREAD_COUNT;
        int remainder = n % THREAD_COUNT;
        int next = 0;
        for (int t = 0; t < THREAD_COUNT; t++) {
            int count = base + (t < remainder ? 1 : 0);
            if (count == 0) continue;
            final int start = next;
            final int end = next + count; // xử lý các index trong [start, end)
            next = end;
            executor.execute(() -> worker(start, end));
        }
    }

    /** Chạy ở luồng nền: xử lý các "quy trình" có index trong [start, end). */
    private void worker(int start, int end) {
        for (int i = start; i < end; i++) {
            if (!running) return; // lượt tạo đã bị huỷ (vd: activity bị đóng)

            // Giả lập thời gian xử lý của mỗi quy trình
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            final int index = i;
            main.post(() -> onItemDone(index)); // đẩy việc cập nhật View về main thread
        }
    }

    /** Chạy ở main thread: thêm 1 button đã xử lý xong và cập nhật tiến độ. */
    private void onItemDone(int index) {
        if (!running) return; // bỏ qua nếu lượt tạo đã bị huỷ

        Button b = new Button(this);
        b.setText(getString(R.string.str_mt_button_label, index + 1));
        linearLayoutButton.addView(b);

        rendered++;
        progressBarPercent.setProgress(rendered);
        int percent = (int) (rendered * 100L / total);
        txtPercent.setText(getString(R.string.str_mt_percent, percent));

        if (rendered >= total) {
            finishRun();
            Toast.makeText(this, getString(R.string.str_mt_done, total), Toast.LENGTH_SHORT).show();
        }
    }

    /** Kết thúc lượt tạo: cho phép bấm lại và giải phóng luồng. */
    private void finishRun() {
        running = false;
        if (btnCreate != null) btnCreate.setEnabled(true);
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
    }

    private int parseCount() {
        if (edtNumberButton == null) return 0;
        String s = edtNumberButton.getText().toString().trim();
        if (TextUtils.isEmpty(s)) return 0;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    protected void onDestroy() {
        running = false; // báo cho các luồng nền dừng lại
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        main.removeCallbacksAndMessages(null); // bỏ các cập nhật còn chờ
        super.onDestroy();
    }
}
