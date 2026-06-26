package com.huonggiang.utils;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.huonggiang.models.TechShopData;

/**
 * Đọc toàn bộ dữ liệu TechShop từ Firebase Realtime Database (1 lần đọc cả cây gốc)
 * rồi trả về qua {@link Callback}. Dữ liệu nhỏ nên đọc gọn một lần, tính toán tại app.
 */
public final class TechShopRepository {

    // Realtime Database theo vùng asia-southeast1 (phải dùng URL đầy đủ).
    public static final String DATABASE_URL =
            "https://codemobile-79754-default-rtdb.asia-southeast1.firebasedatabase.app";

    public interface Callback {
        void onLoaded(TechShopData data);
        void onError(String message);
    }

    private TechShopRepository() {
    }

    /** Tải toàn bộ dữ liệu; callback chạy trên main thread nên cập nhật View trực tiếp được. */
    public static void loadAll(Callback callback) {
        try {
            FirebaseDatabase.getInstance(DATABASE_URL).getReference()
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot root) {
                            callback.onLoaded(parse(root));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            callback.onError(error.getMessage());
                        }
                    });
        } catch (Exception e) {
            // Firebase chưa cấu hình được → báo lỗi thay vì crash
            callback.onError(String.valueOf(e.getMessage()));
        }
    }

    private static TechShopData parse(DataSnapshot root) {
        TechShopData data = new TechShopData();
        for (DataSnapshot s : root.child("categories").getChildren()) {
            data.categories.add(new TechShopData.Category(
                    s.getKey(), str(s, "categoryName"), str(s, "description")));
        }
        for (DataSnapshot s : root.child("products").getChildren()) {
            data.products.add(new TechShopData.Product(
                    s.getKey(), str(s, "categoryId"), str(s, "productName"),
                    dbl(s, "price"), (int) lng(s, "stock"), str(s, "imageUrl"), bool(s, "isActive")));
        }
        for (DataSnapshot s : root.child("customers").getChildren()) {
            data.customers.add(new TechShopData.Customer(
                    s.getKey(), str(s, "fullName"), str(s, "email"), str(s, "phone"), str(s, "address")));
        }
        for (DataSnapshot s : root.child("orders").getChildren()) {
            data.orders.add(new TechShopData.Order(
                    s.getKey(), str(s, "customerId"), str(s, "employeeId"),
                    str(s, "orderDate"), str(s, "status"), dbl(s, "totalAmount")));
        }
        for (DataSnapshot s : root.child("orderDetails").getChildren()) {
            data.orderDetails.add(new TechShopData.OrderDetail(
                    str(s, "orderId"), str(s, "productId"), (int) lng(s, "quantity"), dbl(s, "unitPrice")));
        }
        return data;
    }

    private static String str(DataSnapshot s, String key) {
        Object v = s.child(key).getValue();
        return v == null ? "" : v.toString();
    }

    private static double dbl(DataSnapshot s, String key) {
        Object v = s.child(key).getValue();
        if (v instanceof Number) return ((Number) v).doubleValue();
        try {
            return Double.parseDouble(String.valueOf(v));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static long lng(DataSnapshot s, String key) {
        Object v = s.child(key).getValue();
        if (v instanceof Number) return ((Number) v).longValue();
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static boolean bool(DataSnapshot s, String key) {
        Object v = s.child(key).getValue();
        return v instanceof Boolean ? (Boolean) v : Boolean.parseBoolean(String.valueOf(v));
    }
}
