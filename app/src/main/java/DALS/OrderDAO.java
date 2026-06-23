package DALS;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.huonggiang.models.Order;
import com.huonggiang.models.OrderDetail;
import com.huonggiang.models.OrderStatus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Đọc đơn hàng & chi tiết đơn hàng từ SQLite (assets), thay cho dữ liệu mock.
 * DB được copy từ assets sang thư mục databases khi đăng nhập (xem LoginActivity).
 */
public class OrderDAO {

    public static final String DATABASE_NAME = "K23411TEsales.sqlite";

    // Cột OrderDate lưu dạng "2025-01-15 08:30:00"
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /** Lấy toàn bộ đơn hàng. */
    public static ArrayList<Order> getAllOrders(Context context) {
        ArrayList<Order> orders = new ArrayList<>();
        SQLiteDatabase database = open(context);
        // "Order" là từ khoá SQL nên phải để trong dấu nháy kép
        Cursor cursor = database.rawQuery("SELECT * FROM \"Order\"", null);
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);

        while (cursor.moveToNext()) {
            String orderId    = cursor.getString(cursor.getColumnIndexOrThrow("OrderId"));
            String customerId = cursor.getString(cursor.getColumnIndexOrThrow("CustomerId"));
            String employeeId = cursor.getString(cursor.getColumnIndexOrThrow("EmployeeId"));
            String dateStr    = cursor.getString(cursor.getColumnIndexOrThrow("OrderDate"));
            String statusStr  = cursor.getString(cursor.getColumnIndexOrThrow("Status"));
            orders.add(new Order(orderId, customerId, employeeId,
                    parseDate(sdf, dateStr), parseStatus(statusStr)));
        }
        cursor.close();
        database.close();
        return orders;
    }

    /** Lấy toàn bộ chi tiết đơn hàng. */
    public static ArrayList<OrderDetail> getAllOrderDetails(Context context) {
        ArrayList<OrderDetail> details = new ArrayList<>();
        SQLiteDatabase database = open(context);
        Cursor cursor = database.rawQuery("SELECT * FROM OrderDetail", null);

        while (cursor.moveToNext()) {
            details.add(new OrderDetail(
                    cursor.getString(cursor.getColumnIndexOrThrow("OrderDetailId")),
                    cursor.getString(cursor.getColumnIndexOrThrow("OrderId")),
                    cursor.getString(cursor.getColumnIndexOrThrow("ProductId")),
                    cursor.getInt   (cursor.getColumnIndexOrThrow("Quantity")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("Price")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("Coupon")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("VAT"))));
        }
        cursor.close();
        database.close();
        return details;
    }

    private static SQLiteDatabase open(Context context) {
        String dbPath = context.getDatabasePath(DATABASE_NAME).getPath();
        return SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
    }

    private static Date parseDate(SimpleDateFormat sdf, String raw) {
        if (raw == null) return new Date(0);
        try {
            return sdf.parse(raw);
        } catch (ParseException e) {
            return new Date(0);
        }
    }

    private static OrderStatus parseStatus(String raw) {
        if (raw == null) return OrderStatus.COMPLETED;
        try {
            return OrderStatus.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return OrderStatus.COMPLETED;
        }
    }
}
