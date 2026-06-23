package com.huonggiang.models;

/**
 * DTO hiển thị một dòng chi tiết hóa đơn trong ListView.
 * lineTotal = quantity × unitPrice × (1 − coupon) × (1 + vat)
 */
public class BillingRow {
    public String productName;
    public String categoryName;
    public int quantity;
    public double unitPrice;
    public double coupon;   // 0.0 → 1.0 (e.g., 0.05 = 5%)
    public double vat;      // 0.0 → 1.0 (e.g., 0.08 = 8%)
    public double lineTotal;

    public BillingRow(String productName, String categoryName,
                      int quantity, double unitPrice, double coupon, double vat) {
        this.productName = productName;
        this.categoryName = categoryName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.coupon = coupon;
        this.vat = vat;
        this.lineTotal = quantity * unitPrice * (1.0 - coupon) * (1.0 + vat);
    }
}
