package com.huonggiang.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Gói toàn bộ dữ liệu TechShop đọc từ Firebase Realtime Database (node gốc), kèm các
 * model con tương ứng với từng node. Dùng chung cho cả màn Admin và màn Client.
 *
 * Schema Firebase:
 *  - categories/{id}   : categoryName, description
 *  - products/{id}     : categoryId, productName, price, stock, imageUrl, isActive
 *  - customers/{id}    : fullName, email, phone, address
 *  - orders/{id}       : customerId, employeeId, orderDate, status, totalAmount
 *  - orderDetails/{id} : orderId, productId, quantity, unitPrice
 */
public class TechShopData {

    public final List<Category> categories = new ArrayList<>();
    public final List<Product> products = new ArrayList<>();
    public final List<Customer> customers = new ArrayList<>();
    public final List<Order> orders = new ArrayList<>();
    public final List<OrderDetail> orderDetails = new ArrayList<>();

    public static class Category {
        public final String id;
        public final String name;
        public final String description;

        public Category(String id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
    }

    public static class Product {
        public final String id;
        public final String categoryId;
        public final String name;
        public final double price;
        public final int stock;
        public final String imageUrl;
        public final boolean active;

        public Product(String id, String categoryId, String name, double price,
                       int stock, String imageUrl, boolean active) {
            this.id = id;
            this.categoryId = categoryId;
            this.name = name;
            this.price = price;
            this.stock = stock;
            this.imageUrl = imageUrl;
            this.active = active;
        }
    }

    public static class Customer {
        public final String id;
        public final String fullName;
        public final String email;
        public final String phone;
        public final String address;

        public Customer(String id, String fullName, String email, String phone, String address) {
            this.id = id;
            this.fullName = fullName;
            this.email = email;
            this.phone = phone;
            this.address = address;
        }
    }

    public static class Order {
        public final String id;
        public final String customerId;
        public final String employeeId;
        public final String orderDate;
        public final String status;
        public final double totalAmount;

        public Order(String id, String customerId, String employeeId,
                     String orderDate, String status, double totalAmount) {
            this.id = id;
            this.customerId = customerId;
            this.employeeId = employeeId;
            this.orderDate = orderDate;
            this.status = status;
            this.totalAmount = totalAmount;
        }
    }

    public static class OrderDetail {
        public final String orderId;
        public final String productId;
        public final int quantity;
        public final double unitPrice;

        public OrderDetail(String orderId, String productId, int quantity, double unitPrice) {
            this.orderId = orderId;
            this.productId = productId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
    }
}
