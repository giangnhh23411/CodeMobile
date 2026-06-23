package com.huonggiang.models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DataWareHouse {
    public static ArrayList<Category> getCategories() {
        ArrayList<Category> categories = new ArrayList<>();
        Category c1 = new Category("c1", "Mì các loại", "Mì chống đói");
        Category c2 = new Category("c2", "Rau củ quả", "Rau củ quả tươi sạch");
        Category c3 = new Category("c3", "Nước ngọt", "Nước uống có ga");
        Category c4 = new Category("c4", "Trái cây", "Trái cây nhập khẩu");
        categories.add(c1);
        categories.add(c2);
        categories.add(c3);
        categories.add(c4);
        return categories;
    }

    public static ArrayList<Product> getProducts() {
        ArrayList<Product> products = new ArrayList<>();
        ArrayList<Category> categories = getCategories();

        // c1: Mì các loại (giá VND/gói)
        products.add(new Product("p1", "Mì Omachi chay",           100,  6500, 0,    0.05, categories.get(0).getCategoryId()));
        products.add(new Product("p2", "Mì Hảo Hảo tôm chua cay", 200,  3500, 0,    0.05, categories.get(0).getCategoryId()));
        products.add(new Product("p3", "Mì Kokomi 90g",            150,  3000, 0,    0.05, categories.get(0).getCategoryId()));
        products.add(new Product("p4", "Mì Cung Đình sườn hầm",    80,   5500, 0.05, 0.05, categories.get(0).getCategoryId()));

        // c2: Rau củ quả (giá VND/kg)
        products.add(new Product("p5", "Bắp cải Đà Lạt",   50,  15000, 0,   0,    categories.get(1).getCategoryId()));
        products.add(new Product("p6", "Cà rốt tươi sạch", 60,  25000, 0,   0,    categories.get(1).getCategoryId()));
        products.add(new Product("p7", "Khoai tây Đà Lạt", 70,  30000, 0.1, 0,    categories.get(1).getCategoryId()));
        products.add(new Product("p8", "Hành tây trắng",   120, 20000, 0,   0,    categories.get(1).getCategoryId()));

        // c3: Nước ngọt (giá VND/lon)
        products.add(new Product("p9",  "Coca Cola 330ml", 300, 12000, 0,    0.1, categories.get(2).getCategoryId()));
        products.add(new Product("p10", "Pepsi 330ml",     250, 12000, 0,    0.1, categories.get(2).getCategoryId()));
        products.add(new Product("p11", "7Up 330ml",       200, 11000, 0.05, 0.1, categories.get(2).getCategoryId()));
        products.add(new Product("p12", "Mirinda Cam",     180, 11000, 0,    0.1, categories.get(2).getCategoryId()));

        // c4: Trái cây nhập khẩu (giá VND/kg)
        products.add(new Product("p13", "Táo Envy Mỹ",        40,  180000, 0.1,  0, categories.get(3).getCategoryId()));
        products.add(new Product("p14", "Nho mẫu đơn Nhật",   20,  650000, 0.05, 0, categories.get(3).getCategoryId()));
        products.add(new Product("p15", "Cam Úc nhập khẩu",   100,  95000, 0,    0, categories.get(3).getCategoryId()));
        products.add(new Product("p16", "Dâu tây Đà Lạt",     35,  180000, 0,    0, categories.get(3).getCategoryId()));

        return products;
    }

    public static ArrayList<Employee> getEmployees() {
        ArrayList<Employee> employees = new ArrayList<>();
        employees.add(new Employee("e1", "Nguyễn Văn A", "0987654321", "Hà Nội"));
        employees.add(new Employee("e2", "Trần Thị B", "0912345678", "Hồ Chí Minh"));
        employees.add(new Employee("e3", "Lê Văn C", "0901234567", "Đà Nẵng"));
        employees.add(new Employee("e4", "Phạm Thị D", "0934567890", "Cần Thơ"));
        employees.add(new Employee("e5", "Hoàng Văn E", "0978901234", "Hải Phòng"));
        employees.add(new Employee("e6", "Vũ Thị F", "0967890123", "Nha Trang"));
        employees.add(new Employee("e7", "Phan Văn G", "0956789012", "Huế"));
        employees.add(new Employee("e8", "Đặng Thị H", "0945678901", "Vinh"));
        employees.add(new Employee("e9", "Bùi Văn I", "0923456789", "Nam Định"));
        employees.add(new Employee("e10", "Lý Thị J", "0912345670", "Long An"));
        employees.add(new Employee("e11", "Trương Văn K", "0987654320", "Bắc Ninh"));
        employees.add(new Employee("e12", "Đỗ Thị L", "0912345679", "Thái Bình"));
        employees.add(new Employee("e13", "Ngô Văn M", "0909876543", "Quảng Ninh"));
        employees.add(new Employee("e14", "Dương Thị N", "0933445566", "Đồng Nai"));
        employees.add(new Employee("e15", "Tạ Văn O", "0977889900", "An Giang"));
        return employees;
    }

    public static ArrayList<Customer> getCustomers() {
        ArrayList<Customer> customers = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        cal.set(2000, 5, 20);
        customers.add(new Customer("cust1", "Nguyễn Thị Tèo", "0981234567", "teo@gmail.com", cal.getTime(), "Hà Nội"));
        cal.set(1995, 8, 15);
        customers.add(new Customer("cust2", "Trần Văn Tí", "0912345678", "ti@gmail.com", cal.getTime(), "Hồ Chí Minh"));
        cal.set(1988, 2, 10);
        customers.add(new Customer("cust3", "Lê Thị Lan", "0901234567", "lan@gmail.com", cal.getTime(), "Đà Nẵng"));
        cal.set(1992, 11, 25);
        customers.add(new Customer("cust4", "Phạm Văn Hùng", "0934567890", "hung@gmail.com", cal.getTime(), "Cần Thơ"));
        cal.set(1997, 0, 5);
        customers.add(new Customer("cust5", "Hoàng Thị Mai", "0978901234", "mai@gmail.com", cal.getTime(), "Hải Phòng"));
        cal.set(2001, 3, 30);
        customers.add(new Customer("cust6", "Vũ Văn Nam", "0967890123", "nam@gmail.com", cal.getTime(), "Nha Trang"));
        cal.set(1985, 6, 12);
        customers.add(new Customer("cust7", "Phan Thị Hoa", "0956789012", "hoa@gmail.com", cal.getTime(), "Huế"));
        cal.set(1990, 9, 18);
        customers.add(new Customer("cust8", "Đặng Văn Bình", "0945678901", "binh@gmail.com", cal.getTime(), "Vinh"));
        cal.set(1993, 4, 22);
        customers.add(new Customer("cust9", "Bùi Thị Cúc", "0923456789", "cuc@gmail.com", cal.getTime(), "Nam Định"));
        cal.set(1999, 1, 14);
        customers.add(new Customer("cust10", "Lý Văn Dũng", "0912345670", "dung@gmail.com", cal.getTime(), "Long An"));
        cal.set(1987, 7, 28);
        customers.add(new Customer("cust11", "Trương Thị Huệ", "0987654320", "hue@gmail.com", cal.getTime(), "Bắc Ninh"));
        cal.set(1996, 10, 3);
        customers.add(new Customer("cust12", "Đỗ Văn Quân", "0912345679", "quan@gmail.com", cal.getTime(), "Thái Bình"));
        cal.set(1994, 5, 8);
        customers.add(new Customer("cust13", "Ngô Thị Tuyết", "0909876543", "tuyet@gmail.com", cal.getTime(), "Quảng Ninh"));
        cal.set(1989, 3, 17);
        customers.add(new Customer("cust14", "Dương Văn Sơn", "0933445566", "son@gmail.com", cal.getTime(), "Đồng Nai"));
        cal.set(2002, 0, 1);
        customers.add(new Customer("cust15", "Tạ Thị Ngọc", "0977889900", "ngoc@gmail.com", cal.getTime(), "An Giang"));

        return customers;
    }

    // ── Order filter helpers ──────────────────────────────────────────────────

    /**
     * Returns orders whose date falls within [from, to] (inclusive) and whose
     * status matches {@code status}. Pass {@code null} for status to skip the
     * status filter and return all statuses.
     */
    public static ArrayList<Order> filterOrders(List<Order> orders,
                                                Date from, Date to,
                                                OrderStatus status) {
        ArrayList<Order> result = new ArrayList<>();
        for (Order order : orders) {
            Date d = order.getOrderDate();
            if (d.before(from) || d.after(to)) continue;
            if (status != null && status != order.getStatus()) continue;
            result.add(order);
        }
        return result;
    }

    /**
     * Sums all line totals for the given orderId.
     * lineTotal = quantity × price × (1 − coupon) × (1 + vat)
     */
    public static double computeOrderTotal(List<OrderDetail> details, String orderId) {
        double total = 0;
        for (OrderDetail det : details) {
            if (det.getOrderId().equals(orderId)) {
                double base = det.getQuantity() * det.getPrice() * (1.0 - det.getCoupon());
                total += base * (1.0 + det.getVAT());
            }
        }
        return total;
    }
}
