package com.huonggiang.k23411teapp;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.huonggiang.models.Product;

import java.util.ArrayList;
import java.util.Random;

/**
 * Demo đa luồng với đối tượng: mô phỏng tải sản phẩm ở luồng nền, vừa cập nhật
 * ProgressBar + phần trăm, vừa đổ dần dữ liệu vào ListView.
 *
 * Tính năng:
 *  - Khi mở màn hình: tự động giả lập tải 100 sản phẩm (có dữ liệu thật: tên, số
 *    lượng, giá, khuyến mãi, VAT).
 *  - Bấm "Download" để tải lại theo số lượng nhập trong ô.
 *  - Nhấn giữ một sản phẩm: đổi sang màu tím rồi xóa khỏi danh sách.
 *
 * Nguyên tắc đa luồng: việc "tải" chạy ở luồng nền; mọi thao tác cập nhật View
 * đều được đẩy về main thread qua {@link Handler} gắn với main looper.
 */
public class MultiThreadingObjectActivity extends AppCompatActivity {

    /** Số sản phẩm tự giả lập khi vừa mở màn hình. */
    private static final int DEFAULT_PRODUCTS = 100;
    /** Giới hạn an toàn số sản phẩm tải trong một lượt. */
    private static final int MAX_PRODUCTS = 1000;
    /** Thời gian giả lập tải mỗi sản phẩm (ms). */
    private static final long DOWNLOAD_DELAY_MS = 20;
    /** Màu tím tô khi nhấn giữ để xóa. */
    private static final int PURPLE = Color.parseColor("#9C27B0");

    /** Kho tên sản phẩm để giả lập dữ liệu. */
    private static final String[] PRODUCT_NAMES = {
            "Trái tắc túi 200g", "Mì Omachi chay", "Bắp cải Đà Lạt", "Coca Cola 330ml",
            "Táo Envy Mỹ", "Nho mẫu đơn Nhật", "Cà rốt tươi sạch", "Pepsi 330ml",
            "Khoai tây Đà Lạt", "Dâu tây Đà Lạt", "Hành tây trắng", "Cam Úc nhập khẩu",
            "Mì Hảo Hảo tôm chua cay", "7Up 330ml", "Sữa tươi Vinamilk 1L", "Trứng gà ta vỉ 10"
    };
    /** Các mức khuyến mãi / VAT có thể có. */
    private static final double[] RATES = {0, 0.05, 0.08, 0.1};

    private EditText edtNumberOfProduct;
    private Button btnDownload;
    private TextView txtPercent;
    private ProgressBar progressBarPercent;
    private ListView lvProduct;

    private ArrayList<Product> products;
    private ArrayAdapter<Product> adapterProduct;

    /** Handler gắn với main thread để cập nhật giao diện từ luồng nền. */
    private final Handler main = new Handler(Looper.getMainLooper());
    private Thread worker;
    private volatile boolean running; // đang tải hay không

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_multi_threading_object);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        addViews();
        addEvents();

        // Tự giả lập 100 sản phẩm ngay khi mở màn hình
        edtNumberOfProduct.setText(String.valueOf(DEFAULT_PRODUCTS));
        processDownloadProductLongTime();
    }

    private void addViews() {
        edtNumberOfProduct = findViewById(R.id.edtNumberOfProduct);
        btnDownload = findViewById(R.id.btnDownload);
        txtPercent = findViewById(R.id.txtPercent);
        progressBarPercent = findViewById(R.id.progressBarPercent);
        lvProduct = findViewById(R.id.lvProduct);

        products = new ArrayList<>();
        // Mỗi dòng hiển thị đầy đủ thông tin sản phẩm
        adapterProduct = new ArrayAdapter<Product>(this, android.R.layout.simple_list_item_1, products) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View row = super.getView(position, convertView, parent);
                row.setBackgroundColor(Color.TRANSPARENT); // reset, tránh dính màu tím khi tái sử dụng row
                Product p = getItem(position);
                TextView tv = row.findViewById(android.R.id.text1);
                if (p != null && tv != null) {
                    String priceStr = String.format("%,dđ", (long) p.getPrice());
                    tv.setText(getString(R.string.str_mt_product_full,
                            p.getProductId(), p.getProductName(), p.getQuantity(), priceStr,
                            (int) Math.round(p.getCoupon() * 100), (int) Math.round(p.getVAT() * 100)));
                }
                return row;
            }
        };
        lvProduct.setAdapter(adapterProduct);
    }

    private void addEvents() {
        if (btnDownload != null) {
            btnDownload.setOnClickListener(v -> processDownloadProductLongTime());
        }
        // Nhấn giữ một sản phẩm: tô tím rồi xóa
        lvProduct.setOnItemLongClickListener((parent, view, position, id) -> {
            Product p = adapterProduct.getItem(position);
            view.setBackgroundColor(PURPLE);
            main.postDelayed(() -> {
                if (p != null && products.remove(p)) {
                    adapterProduct.notifyDataSetChanged();
                    Toast.makeText(this, getString(R.string.str_mt_deleted, p.getProductName()),
                            Toast.LENGTH_SHORT).show();
                }
            }, 250);
            return true; // đã xử lý, không gọi tiếp click thường
        });
    }

    /** Bắt đầu tải sản phẩm ở luồng nền. */
    private void processDownloadProductLongTime() {
        if (running) {
            Toast.makeText(this, R.string.str_mt_running, Toast.LENGTH_SHORT).show();
            return;
        }

        int n = parseCount();
        if (n <= 0) {
            Toast.makeText(this, R.string.str_mt_invalid_number, Toast.LENGTH_SHORT).show();
            return;
        }
        if (n > MAX_PRODUCTS) {
            n = MAX_PRODUCTS;
            Toast.makeText(this, getString(R.string.str_mt_capped, MAX_PRODUCTS), Toast.LENGTH_SHORT).show();
        }

        // Đặt lại giao diện cho lượt tải mới
        products.clear();
        adapterProduct.notifyDataSetChanged();
        progressBarPercent.setMax(n);
        progressBarPercent.setProgress(0);
        txtPercent.setText(getString(R.string.str_mt_percent, 0));

        running = true;
        btnDownload.setEnabled(false);

        final int total = n;
        worker = new Thread(() -> {
            Random rnd = new Random();
            for (int i = 1; i <= total; i++) {
                if (!running) return; // bị huỷ (vd: activity bị đóng)

                // Giả lập thời gian tải một sản phẩm
                try {
                    Thread.sleep(DOWNLOAD_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                // Giả lập dữ liệu thật cho một sản phẩm
                Product p = new Product(
                        "p" + i,
                        PRODUCT_NAMES[(i - 1) % PRODUCT_NAMES.length],
                        10 + rnd.nextInt(191),              // số lượng: 10..200
                        (5 + rnd.nextInt(296)) * 1000.0,    // giá: 5.000..300.000
                        RATES[rnd.nextInt(RATES.length)],   // khuyến mãi
                        RATES[rnd.nextInt(RATES.length)],   // VAT
                        "c" + (1 + rnd.nextInt(4)));        // mã loại: c1..c4
                final int done = i;
                main.post(() -> onProductDownloaded(p, done, total)); // cập nhật UI ở main thread
            }
        });
        worker.start();
    }

    /** Chạy ở main thread: thêm sản phẩm vừa tải và cập nhật tiến độ. */
    private void onProductDownloaded(Product p, int done, int total) {
        if (!running) return; // bỏ qua nếu lượt tải đã bị huỷ

        products.add(p);
        adapterProduct.notifyDataSetChanged();

        progressBarPercent.setProgress(done);
        int percent = (int) (done * 100L / total);
        txtPercent.setText(getString(R.string.str_mt_percent, percent));

        if (done >= total) {
            running = false;
            btnDownload.setEnabled(true);
            Toast.makeText(this, getString(R.string.str_mt_download_done, total), Toast.LENGTH_SHORT).show();
        }
    }

    private int parseCount() {
        if (edtNumberOfProduct == null) return 0;
        String s = edtNumberOfProduct.getText().toString().trim();
        if (TextUtils.isEmpty(s)) return 0;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    protected void onDestroy() {
        running = false; // báo cho luồng nền dừng lại
        if (worker != null) {
            worker.interrupt();
            worker = null;
        }
        main.removeCallbacksAndMessages(null); // bỏ các cập nhật còn chờ
        super.onDestroy();
    }
}
