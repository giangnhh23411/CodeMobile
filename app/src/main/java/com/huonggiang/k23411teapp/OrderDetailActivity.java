package com.huonggiang.k23411teapp;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.huonggiang.adapters.BillingDetailAdapter;
import com.huonggiang.utils.CurrencyUtils;
import com.huonggiang.utils.StatusUtils;
import com.huonggiang.models.BillingRow;
import com.huonggiang.models.Category;
import com.huonggiang.models.Customer;
import com.huonggiang.models.DataWareHouse;
import com.huonggiang.models.Employee;
import com.huonggiang.models.Order;
import com.huonggiang.models.OrderDetail;
import com.huonggiang.models.Product;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import DALS.OrderDAO;

public class OrderDetailActivity extends AppCompatActivity {

    /** Key used to pass the order ID via Intent extra. */
    public static final String EXTRA_ORDER_ID = "extra_order_id";

    TextView tvDetailOrderId, tvDetailOrderDate, tvDetailStatus,
             tvDetailEmployee, tvDetailCustomer, tvDetailGrandTotal;
    ListView lvDetailItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_detail);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.mainOrderDetail), (v, insets) -> {
                    Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                    return insets;
                });

        // Toolbar with back arrow
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Bind views
        tvDetailOrderId    = findViewById(R.id.tvDetailOrderId);
        tvDetailOrderDate  = findViewById(R.id.tvDetailOrderDate);
        tvDetailStatus     = findViewById(R.id.tvDetailStatus);
        tvDetailEmployee   = findViewById(R.id.tvDetailEmployee);
        tvDetailCustomer   = findViewById(R.id.tvDetailCustomer);
        tvDetailGrandTotal = findViewById(R.id.tvDetailGrandTotal);
        lvDetailItems      = findViewById(R.id.lvDetailItems);

        // Receive orderId from calling Activity
        String orderId = getIntent().getStringExtra(EXTRA_ORDER_ID);
        if (orderId != null) {
            populateDetail(orderId);
        }
    }

    /** Loads all data and populates the screen for the given orderId. */
    private void populateDetail(String orderId) {
        ArrayList<Order>       allOrders       = OrderDAO.getAllOrders(this);
        ArrayList<OrderDetail> allOrderDetails = OrderDAO.getAllOrderDetails(this);
        ArrayList<Product>     allProducts     = DataWareHouse.getProducts();
        ArrayList<Category>    allCategories   = DataWareHouse.getCategories();
        ArrayList<Employee>    allEmployees    = DataWareHouse.getEmployees();
        ArrayList<Customer>    allCustomers    = DataWareHouse.getCustomers();

        // Find the Order header
        Order order = null;
        for (Order o : allOrders) {
            if (o.getOrderId().equals(orderId)) { order = o; break; }
        }
        if (order == null) return;

        // Fill header info
        SimpleDateFormat sdf = new SimpleDateFormat(
                getString(R.string.str_date_format_row), Locale.getDefault());
        tvDetailOrderId.setText(orderId.toUpperCase());
        tvDetailOrderDate.setText(sdf.format(order.getOrderDate()));
        StatusUtils.applyBadge(tvDetailStatus, order.getStatus(), this);

        Employee emp = findEmployee(allEmployees, order.getEmployeeId());
        tvDetailEmployee.setText(emp != null ? emp.getName() : order.getEmployeeId());

        Customer cust = findCustomer(allCustomers, order.getCustomerId());
        tvDetailCustomer.setText(cust != null ? cust.getCustomerName() : order.getCustomerId());

        // Build BillingRow list
        List<BillingRow> rows = new ArrayList<>();
        double grandTotal = 0;
        for (OrderDetail det : allOrderDetails) {
            if (!det.getOrderId().equals(orderId)) continue;
            Product product = findProduct(allProducts, det.getProductId());
            if (product == null) continue;
            Category cat = findCategory(allCategories, product.getCategoryId());
            String catName = cat != null ? cat.getCategoryName() : "";
            BillingRow row = new BillingRow(
                    product.getProductName(), catName,
                    det.getQuantity(), det.getPrice(),
                    det.getCoupon(), det.getVAT());
            rows.add(row);
            grandTotal += row.lineTotal;
        }

        BillingDetailAdapter adapter = new BillingDetailAdapter(
                this, R.layout.item_billing_detail, rows);
        lvDetailItems.setAdapter(adapter);

        // Grand total
        tvDetailGrandTotal.setText(
                getString(R.string.str_amount_format, CurrencyUtils.formatNumber(grandTotal)));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Employee findEmployee(List<Employee> list, String id) {
        for (Employee e : list) if (e.getId().equals(id)) return e;
        return null;
    }

    private Customer findCustomer(List<Customer> list, String id) {
        for (Customer c : list) if (c.getCustomerID().equals(id)) return c;
        return null;
    }

    private Product findProduct(List<Product> list, String id) {
        for (Product p : list) if (p.getProductId().equals(id)) return p;
        return null;
    }

    private Category findCategory(List<Category> list, String id) {
        for (Category c : list) if (c.getCategoryId().equals(id)) return c;
        return null;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
