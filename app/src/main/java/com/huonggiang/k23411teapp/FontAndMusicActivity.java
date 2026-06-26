package com.huonggiang.k23411teapp;

import android.content.res.AssetFileDescriptor;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Màn hình demo: phát nhạc từ assets/musics và đổi font chữ từ assets/fonts.
 *
 * - Hai nút phát hai bản nhạc (dùng {@link MediaPlayer} + prepareAsync).
 * - ListView liệt kê các font trong assets/fonts (mỗi dòng hiển thị bằng chính
 *   font đó để xem trước); nhấn vào font nào thì áp dụng font đó cho TextView mẫu.
 */
public class FontAndMusicActivity extends AppCompatActivity {

    private static final String LOG_TAG = "FontAndMusic";
    private static final String FONTS_DIR = "fonts";
    private static final String AUDIO_1 = "musics/alexzavesa-calm-elegant-logo-519008.mp3";
    private static final String AUDIO_2 = "musics/alexzavesa-calm-inspiring-technology-logo-short-version-518993.mp3";

    private Button btnPlayAudio1;
    private Button btnPlayAudio2;
    private TextView tvSample;
    private ListView lvFonts;

    private final ArrayList<FontItem> fonts = new ArrayList<>();
    private ArrayAdapter<FontItem> fontAdapter;

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_font_and_music);
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
        loadFonts();
    }

    private void addViews() {
        btnPlayAudio1 = findViewById(R.id.button2);
        btnPlayAudio2 = findViewById(R.id.button3);
        tvSample = findViewById(R.id.textView11);
        lvFonts = findViewById(R.id.lvFonts);

        // Mỗi dòng hiển thị tên font bằng chính font đó (xem trước)
        fontAdapter = new ArrayAdapter<FontItem>(this, android.R.layout.simple_list_item_1, fonts) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View row = super.getView(position, convertView, parent);
                FontItem item = getItem(position);
                TextView tv = row.findViewById(android.R.id.text1);
                if (item != null && tv != null) {
                    tv.setText(item.name);
                    tv.setTypeface(item.typeface);
                }
                return row;
            }
        };
        lvFonts.setAdapter(fontAdapter);
    }

    private void addEvents() {
        btnPlayAudio1.setOnClickListener(v -> playAudio(AUDIO_1));
        btnPlayAudio2.setOnClickListener(v -> playAudio(AUDIO_2));

        // Nhấn một font: áp dụng cho TextView mẫu
        lvFonts.setOnItemClickListener((parent, view, position, id) -> {
            FontItem item = fontAdapter.getItem(position);
            if (item == null) return;
            tvSample.setTypeface(item.typeface);
            Toast.makeText(this, getString(R.string.str_font_selected, item.name), Toast.LENGTH_SHORT).show();
        });
    }

    /** Nạp tất cả font (.ttf/.otf) trong thư mục assets/fonts vào danh sách. */
    private void loadFonts() {
        try {
            String[] files = getAssets().list(FONTS_DIR);
            if (files != null) {
                Arrays.sort(files);
                for (String fileName : files) {
                    String lower = fileName.toLowerCase();
                    if (!lower.endsWith(".ttf") && !lower.endsWith(".otf")) continue;
                    try {
                        Typeface typeface = Typeface.createFromAsset(getAssets(), FONTS_DIR + "/" + fileName);
                        fonts.add(new FontItem(stripExtension(fileName), typeface));
                    } catch (Exception ex) {
                        Log.e(LOG_TAG, "Không tải được font: " + fileName, ex);
                    }
                }
            }
            fontAdapter.notifyDataSetChanged();
        } catch (Exception ex) {
            Log.e(LOG_TAG, "Không liệt kê được thư mục " + FONTS_DIR, ex);
        }
    }

    private static String stripExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }

    // ── Phát nhạc ─────────────────────────────────────────────────────────────

    /**
     * Phát một file nhạc nằm trong thư mục assets.
     *
     * @param audioFile đường dẫn tương đối trong assets, ví dụ "musics/song.mp3"
     */
    private void playAudio(String audioFile) {
        try {
            releasePlayer(); // dừng & giải phóng bài đang phát (nếu có)

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build());

            // File trong assets là dạng nén nên phải nạp qua AssetFileDescriptor (offset + length)
            AssetFileDescriptor afd = getAssets().openFd(audioFile);
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();

            // Chuẩn bị ở luồng nền, xong mới phát; tự giải phóng khi phát xong hoặc lỗi
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                Toast.makeText(this, R.string.str_audio_playing, Toast.LENGTH_SHORT).show();
            });
            mediaPlayer.setOnCompletionListener(mp -> releasePlayer());
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(LOG_TAG, "MediaPlayer error what=" + what + " extra=" + extra);
                Toast.makeText(this, R.string.str_audio_error, Toast.LENGTH_SHORT).show();
                releasePlayer();
                return true;
            });
            mediaPlayer.prepareAsync();
        } catch (Exception ex) {
            Log.e(LOG_TAG, "Lỗi phát nhạc: " + audioFile, ex);
            Toast.makeText(this, R.string.str_audio_error, Toast.LENGTH_SHORT).show();
            releasePlayer();
        }
    }

    /** Dừng và giải phóng MediaPlayer hiện tại (nếu có). */
    private void releasePlayer() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            } catch (IllegalStateException ignored) {
                // player đang ở trạng thái không hợp lệ -> bỏ qua, vẫn release bên dưới
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onStop() {
        releasePlayer(); // dừng nhạc khi màn hình không còn hiển thị
        super.onStop();
    }

    /** Một font: tên hiển thị + Typeface đã nạp. */
    private static class FontItem {
        final String name;
        final Typeface typeface;

        FontItem(String name, Typeface typeface) {
            this.name = name;
            this.typeface = typeface;
        }

        @NonNull
        @Override
        public String toString() {
            return name;
        }
    }
}
