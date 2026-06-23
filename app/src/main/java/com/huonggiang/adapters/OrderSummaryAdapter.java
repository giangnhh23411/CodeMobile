package com.huonggiang.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.huonggiang.k23411teapp.R;
import com.huonggiang.models.OrderStatus;
import com.huonggiang.utils.CurrencyUtils;
import com.huonggiang.utils.StatusUtils;

import java.util.ArrayList;
import java.util.Locale;

public class OrderSummaryAdapter extends ArrayAdapter<OrderSummaryAdapter.OrderSummaryItem> {

    // ── Data model ────────────────────────────────────────────────────────────

    public static class OrderSummaryItem {
        public final String      orderId;
        public final String      orderDate;
        public final double      total;
        public final OrderStatus status;

        public OrderSummaryItem(String orderId, String orderDate,
                                double total, OrderStatus status) {
            this.orderId   = orderId;
            this.orderDate = orderDate;
            this.total     = total;
            this.status    = status;
        }
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    static class ViewHolder {
        TextView tvOrderId;
        TextView tvOrderDate;
        TextView tvAmount;
        TextView tvStatus;
    }

    // ── Constructor ───────────────────────────────────────────────────────────

    public OrderSummaryAdapter(Context context, ArrayList<OrderSummaryItem> items) {
        super(context, R.layout.item_order_row, items);
    }

    // ── getView ───────────────────────────────────────────────────────────────

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_order_row, parent, false);
            holder = new ViewHolder();
            holder.tvOrderId   = convertView.findViewById(R.id.tvItemOrderId);
            holder.tvOrderDate = convertView.findViewById(R.id.tvItemOrderDate);
            holder.tvAmount    = convertView.findViewById(R.id.tvItemAmount);
            holder.tvStatus    = convertView.findViewById(R.id.tvItemStatus);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        OrderSummaryItem item = getItem(position);
        if (item == null) return convertView;

        holder.tvOrderId.setText(item.orderId.toUpperCase(Locale.getDefault()));
        holder.tvOrderDate.setText(item.orderDate);
        holder.tvAmount.setText(getContext().getString(
                R.string.str_amount_format, CurrencyUtils.formatNumber(item.total)));

        StatusUtils.applyBadge(holder.tvStatus, item.status, getContext());

        return convertView;
    }
}
