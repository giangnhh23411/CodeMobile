package DALS;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.huonggiang.models.Product;

import java.util.ArrayList;

public class ProductDAO {

    public static final String DATABASE_NAME = "K23411TEsales.sqlite";
    public static final String TABLE_NAME    = "Product";

    /** Lấy tất cả sản phẩm thuộc một category */
    public static ArrayList<Product> getProductsByCategory(Context context, String categoryId) {
        ArrayList<Product> products = new ArrayList<>();
        String dbPath = context.getDatabasePath(DATABASE_NAME).getPath();
        SQLiteDatabase database = SQLiteDatabase.openDatabase(
                dbPath, null, SQLiteDatabase.OPEN_READONLY);

        Cursor cursor = database.rawQuery(
                "SELECT * FROM " + TABLE_NAME + " WHERE CategoryId = ?",
                new String[]{categoryId});

        while (cursor.moveToNext()) {
            products.add(cursorToProduct(cursor));
        }
        cursor.close();
        database.close();
        return products;
    }

    /** Lấy toàn bộ sản phẩm */
    public static ArrayList<Product> getAllProducts(Context context) {
        ArrayList<Product> products = new ArrayList<>();
        String dbPath = context.getDatabasePath(DATABASE_NAME).getPath();
        SQLiteDatabase database = SQLiteDatabase.openDatabase(
                dbPath, null, SQLiteDatabase.OPEN_READONLY);

        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        while (cursor.moveToNext()) {
            products.add(cursorToProduct(cursor));
        }
        cursor.close();
        database.close();
        return products;
    }

    private static Product cursorToProduct(Cursor cursor) {
        String productId   = cursor.getString(cursor.getColumnIndexOrThrow("ProductId"));
        String productName = cursor.getString(cursor.getColumnIndexOrThrow("ProductName"));
        int    quantity    = cursor.getInt   (cursor.getColumnIndexOrThrow("Quantity"));
        double price       = cursor.getDouble(cursor.getColumnIndexOrThrow("Price"));
        double coupon      = cursor.getDouble(cursor.getColumnIndexOrThrow("Coupon"));
        double vat         = cursor.getDouble(cursor.getColumnIndexOrThrow("VAT"));
        String categoryId  = cursor.getString(cursor.getColumnIndexOrThrow("CategoryId"));
        return new Product(productId, productName, quantity, price, coupon, vat, categoryId);
    }
}
