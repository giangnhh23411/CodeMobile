package DALS;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.huonggiang.models.Category;

import java.util.ArrayList;

public class CategoryDAO {

    public static final String DATABASE_NAME = "K23411TEsales.sqlite";
    public static final String TABLE_NAME    = "Category";

    private static SQLiteDatabase open(Context context) {
        String dbPath = context.getDatabasePath(DATABASE_NAME).getPath();
        return SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    /** READ – lấy toàn bộ category. */
    public static ArrayList<Category> getCategories(Context context) {
        ArrayList<Category> categories = new ArrayList<>();
        SQLiteDatabase database = open(context);
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        while (cursor.moveToNext()) {
            String categoryId   = cursor.getString(0);
            String categoryName = cursor.getString(1);
            String description  = cursor.getString(2);
            categories.add(new Category(categoryId, categoryName, description));
        }
        cursor.close();
        database.close();
        return categories;
    }

    /** Kiểm tra một CategoryId đã tồn tại hay chưa. */
    public static boolean isCategoryExists(Context context, String categoryId) {
        SQLiteDatabase database = open(context);
        Cursor cursor = database.rawQuery(
                "SELECT 1 FROM " + TABLE_NAME + " WHERE CategoryId = ?",
                new String[]{categoryId});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        database.close();
        return exists;
    }

    /** CREATE – thêm category mới. Trả về row id (> 0 nếu thành công). */
    public static long saveNewCategory(Context context, Category category) {
        SQLiteDatabase database = open(context);
        ContentValues values = new ContentValues();
        values.put("CategoryId",   category.getCategoryId());
        values.put("CategoryName", category.getCategoryName());
        values.put("Description",  category.getDescription());
        long result = database.insert(TABLE_NAME, null, values);
        database.close();
        return result;
    }

    /** UPDATE – cập nhật category theo CategoryId. Trả về số dòng bị ảnh hưởng. */
    public static int updateCategory(Context context, Category category) {
        SQLiteDatabase database = open(context);
        ContentValues values = new ContentValues();
        values.put("CategoryName", category.getCategoryName());
        values.put("Description",  category.getDescription());
        int rows = database.update(TABLE_NAME, values,
                "CategoryId = ?", new String[]{category.getCategoryId()});
        database.close();
        return rows;
    }

    /** DELETE – xóa category theo CategoryId. Trả về số dòng bị xóa. */
    public static int deleteCategory(Context context, String categoryId) {
        SQLiteDatabase database = open(context);
        int rows = database.delete(TABLE_NAME,
                "CategoryId = ?", new String[]{categoryId});
        database.close();
        return rows;
    }
}
