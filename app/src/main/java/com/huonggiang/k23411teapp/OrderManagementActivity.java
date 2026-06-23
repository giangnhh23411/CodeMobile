package com.huonggiang.k23411teapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.huonggiang.adapters.OrderSummaryAdapter;
import com.huonggiang.models.DataWareHouse;
import com.huonggiang.models.Order;
import com.huonggiang.models.OrderDetail;
import com.huonggiang.models.OrderStatus;
import com.huonggiang.utils.CurrencyUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import DALS.OrderDAO;

public class OrderManagementActivity extends AppCompatActivity {

    private static final String TAG = "OrderManagementActivity";

    TextView tvFromDate, tvToDate, tvEmpty, tvTotalLabel, tvTotalAmount;
    ImageButton ibCalendarFrom, ibCalendarTo;
    MaterialButton btnClearFilter, btnFilter;
    ListView lvOrders;

    final Calendar calFrom = Calendar.getInstance();
    final Calendar calTo = Calendar.getInstance();

    ArrayList<Order> allOrders;
    ArrayList<OrderDetail> allOrderDetails;

    /** null = show all statuses */
    OrderStatus selectedStatus = null;

    OrderSummaryAdapter adapter;
    final ArrayList<OrderSummaryAdapter.OrderSummaryItem> displayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_management);

        View mainView = findViewById(R.id.mainOrderManagement);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        setDefaultDates();

        allOrders = OrderDAO.getAllOrders(this);
        allOrderDetails = OrderDAO.getAllOrderDetails(this);

        addViews();
        addEvents();
        applyFilter();
    }

    private void setDefaultDates() {
        calFrom.set(2026, Calendar.JANUARY, 1, 0, 0, 0);
        calFrom.set(Calendar.MILLISECOND, 0);
        calTo.set(2026, Calendar.JANUARY, 31, 23, 59, 59);
        calTo.set(Calendar.MILLISECOND, 999);
    }

    private void setAllOrdersDates() {
        if (allOrders.isEmpty()) return;
        Date min = allOrders.get(0).getOrderDate();
        Date max = allOrders.get(0).getOrderDate();
        for (Order o : allOrders) {
            if (o.getOrderDate().before(min)) min = o.getOrderDate();
            if (o.getOrderDate().after(max)) max = o.getOrderDate();
        }
        calFrom.setTime(min);
        calFrom.set(Calendar.HOUR_OF_DAY, 0);
        calFrom.set(Calendar.MINUTE, 0);
        calFrom.set(Calendar.SECOND, 0);
        calFrom.set(Calendar.MILLISECOND, 0);
        calTo.setTime(max);
        calTo.set(Calendar.HOUR_OF_DAY, 23);
        calTo.set(Calendar.MINUTE, 59);
        calTo.set(Calendar.SECOND, 59);
        calTo.set(Calendar.MILLISECOND, 999);
    }

    private void addViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvFromDate = findViewById(R.id.tvFromDate);
        tvToDate = findViewById(R.id.tvToDate);
        ibCalendarFrom = findViewById(R.id.ibCalendarFrom);
        ibCalendarTo = findViewById(R.id.ibCalendarTo);
        btnClearFilter = findViewById(R.id.btnClearFilter);
        btnFilter = findViewById(R.id.btnFilter);
        lvOrders = findViewById(R.id.lvOrders);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvTotalLabel = findViewById(R.id.tvTotalLabel);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);

        updateDateLabels();

        adapter = new OrderSummaryAdapter(this, displayList);
        lvOrders.setAdapter(adapter);
    }

    private void addEvents() {
        ibCalendarFrom.setOnClickListener(v -> showDatePicker(true));
        ibCalendarTo.setOnClickListener(v -> showDatePicker(false));
        btnFilter.setOnClickListener(v -> applyFilter());

        lvOrders.setOnItemClickListener((parent, view, position, id) -> {
            OrderSummaryAdapter.OrderSummaryItem item = displayList.get(position);
            handleOrderClick(item);
        });

        btnClearFilter.setOnClickListener(v -> {
            setAllOrdersDates();
            updateDateLabels();
            applyFilter();
        });
    }

    private void showDatePicker(boolean isFrom) {
        Calendar cal = isFrom ? calFrom : calTo;
        new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    if (isFrom) {
                        calFrom.set(year, month, dayOfMonth, 0, 0, 0);
                        calFrom.set(Calendar.MILLISECOND, 0);
                    } else {
                        calTo.set(year, month, dayOfMonth, 23, 59, 59);
                        calTo.set(Calendar.MILLISECOND, 999);
                    }
                    updateDateLabels();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void updateDateLabels() {
        SimpleDateFormat sdf = new SimpleDateFormat(
                getString(R.string.str_date_format_display), Locale.getDefault());
        tvFromDate.setText(sdf.format(calFrom.getTime()));
        tvToDate.setText(sdf.format(calTo.getTime()));
    }

    private void applyFilter() {
        displayList.clear();
        SimpleDateFormat sdf = new SimpleDateFormat(
                getString(R.string.str_date_format_row), Locale.getDefault());

        for (Order order : DataWareHouse.filterOrders(
                allOrders, calFrom.getTime(), calTo.getTime(), selectedStatus)) {
            double total = DataWareHouse.computeOrderTotal(allOrderDetails, order.getOrderId());
            displayList.add(new OrderSummaryAdapter.OrderSummaryItem(
                    order.getOrderId(),
                    sdf.format(order.getOrderDate()),
                    total,
                    order.getStatus()
            ));
        }

        adapter.notifyDataSetChanged();
        lvOrders.setVisibility(displayList.isEmpty() ? View.GONE : View.VISIBLE);
        tvEmpty.setVisibility(displayList.isEmpty() ? View.VISIBLE : View.GONE);
        updateSummaryBar();
    }

    private void updateSummaryBar() {
        if (displayList.isEmpty()) {
            tvTotalLabel.setText(R.string.str_total_summary_empty);
            tvTotalAmount.setText("");
            return;
        }
        double grand = 0;
        for (OrderSummaryAdapter.OrderSummaryItem item : displayList) grand += item.total;
        tvTotalLabel.setText(getString(R.string.str_total_summary, displayList.size()));
        tvTotalAmount.setText(getString(R.string.str_amount_format, CurrencyUtils.formatNumber(grand)));
    }

    private double computeTotal(String orderId) {
        double total = 0;
        for (OrderDetail det : allOrderDetails) {
            if (det.getOrderId().equals(orderId)) {
                double base = det.getQuantity() * det.getPrice() * (1.0 - det.getCoupon());
                total += base * (1.0 + det.getVAT());
            }
        }
        return total;
    }

    private void handleOrderClick(OrderSummaryAdapter.OrderSummaryItem item) {
        Log.d(TAG, "Viewing details for order: " + item.orderId);

        // Navigate to the dedicated Order Detail screen
        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra(OrderDetailActivity.EXTRA_ORDER_ID, item.orderId);
        startActivity(intent);
    }

    // ── Options menu ─────────────────────────────────────────────────────────

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.order_menu_status, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.mnu_order_status_all) {
            selectedStatus = null;
        } else if (id == R.id.mnu_order_status_completed) {
            selectedStatus = OrderStatus.COMPLETED;
        } else if (id == R.id.mnu_order_status_not_payment) {
            selectedStatus = OrderStatus.NOT_PAYMENT;
        } else if (id == R.id.mnu_order_status_on_logistic) {
            selectedStatus = OrderStatus.ON_LOGISTIC;
        } else if (id == R.id.mnu_order_status_complain) {
            selectedStatus = OrderStatus.COMPLAIN;
        } else {
            return super.onOptionsItemSelected(item);
        }
        applyFilter();
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
