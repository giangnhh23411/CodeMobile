package DALS;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.huonggiang.models.FbContact;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Local DB cho màn hình học Firebase, dùng file SQLite có sẵn trong assets:
 * {@code contact.sqlite} (bảng {@code contact}).
 *
 * File được copy từ assets sang thư mục databases ở lần dùng đầu, sau đó mở READWRITE
 * để CRUD và lưu cột {@code sync_status} (trạng thái đồng bộ với Firebase — xem
 * {@link FbContact}). Đây là "bộ đệm offline" cho phép app vẫn chạy khi mất mạng.
 */
public class FbContactDAO {

    public static final String DB_NAME = "contact.sqlite";
    private static final String TABLE  = "contact";

    private final Context context;

    public FbContactDAO(Context context) {
        this.context = context.getApplicationContext();
        ensureDatabase();
    }

    /** Copy contact.sqlite từ assets sang thư mục databases nếu chưa tồn tại. */
    private void ensureDatabase() {
        File dbFile = context.getDatabasePath(DB_NAME);
        if (dbFile.exists()) return;

        File dir = dbFile.getParentFile();
        if (dir != null && !dir.exists()) dir.mkdirs();
        try (InputStream in = context.getAssets().open(DB_NAME);
             OutputStream out = new FileOutputStream(dbFile)) {
            byte[] buffer = new byte[4096];
            int n;
            while ((n = in.read(buffer)) > 0) out.write(buffer, 0, n);
        } catch (Exception ignored) {
            // Không có asset → open() sẽ tạo bảng rỗng.
        }
    }

    private SQLiteDatabase open() {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(
                context.getDatabasePath(DB_NAME).getPath(), null,
                SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.CREATE_IF_NECESSARY);
        // An toàn nếu file rỗng / lần đầu chưa có bảng
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE + " ("
                + "id TEXT PRIMARY KEY, name TEXT, email TEXT, phone TEXT, sync_status TEXT)");
        return db;
    }

    /** Thêm mới hoặc ghi đè theo id. */
    public void upsert(FbContact c) {
        ContentValues v = new ContentValues();
        v.put("id", c.getId());
        v.put("name", c.getName());
        v.put("email", c.getEmail());
        v.put("phone", c.getPhone());
        v.put("sync_status", c.getSyncStatus());
        SQLiteDatabase db = open();
        db.insertWithOnConflict(TABLE, null, v, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void updateStatus(String id, String status) {
        ContentValues v = new ContentValues();
        v.put("sync_status", status);
        SQLiteDatabase db = open();
        db.update(TABLE, v, "id = ?", new String[]{id});
        db.close();
    }

    public void delete(String id) {
        SQLiteDatabase db = open();
        db.delete(TABLE, "id = ?", new String[]{id});
        db.close();
    }

    /** Trạng thái đồng bộ của một id, hoặc null nếu chưa có trong local. */
    public String getStatus(String id) {
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery(
                "SELECT sync_status FROM " + TABLE + " WHERE id = ?", new String[]{id});
        String status = cursor.moveToFirst() ? cursor.getString(0) : null;
        cursor.close();
        db.close();
        return status;
    }

    /** Các liên hệ để hiển thị (bỏ những bản đang chờ xoá). */
    public List<FbContact> getVisible() {
        return query("sync_status != ?", new String[]{FbContact.PENDING_DELETE},
                "name COLLATE NOCASE");
    }

    /** Các liên hệ theo một trạng thái cụ thể (dùng khi đồng bộ). */
    public List<FbContact> getByStatus(String status) {
        return query("sync_status = ?", new String[]{status}, null);
    }

    private List<FbContact> query(String where, String[] args, String orderBy) {
        List<FbContact> list = new ArrayList<>();
        String sql = "SELECT id, name, email, phone, sync_status FROM " + TABLE;
        if (where != null) sql += " WHERE " + where;
        if (orderBy != null) sql += " ORDER BY " + orderBy;
        SQLiteDatabase db = open();
        Cursor cursor = db.rawQuery(sql, args);
        while (cursor.moveToNext()) {
            list.add(new FbContact(cursor.getString(0), cursor.getString(1),
                    cursor.getString(2), cursor.getString(3), cursor.getString(4)));
        }
        cursor.close();
        db.close();
        return list;
    }
}
