package com.huonggiang.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.huonggiang.k23411teapp.R;
import com.huonggiang.models.BillingRow;
import com.huonggiang.utils.CurrencyUtils;

import java.util.List;

public class BillingDetailAdapter extends ArrayAdapter<BillingRow> {
    private final Activity context;
    private final int resource;

    public BillingDetailAdapter(@NonNull Activity context, int resource,
                                @NonNull List<BillingRow> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(resource, parent, false);
            holder = new ViewHolder();
            holder.txtProductName = convertView.findViewById(R.id.txtProductName);
            holder.txtCategoryName = convertView.findViewById(R.id.txtCategoryName);
            holder.txtQtyPrice = convertView.findViewById(R.id.txtQtyPrice);
            holder.txtCouponVat = convertView.findViewById(R.id.txtCouponVat);
            holder.txtLineTotal = convertView.findViewById(R.id.txtLineTotal);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        BillingRow row = getItem(position);
        if (row != null) {
            holder.txtProductName.setText(row.productName);
            holder.txtCategoryName.setText(row.categoryName);
            holder.txtQtyPrice.setText(
                    row.quantity + " × " + CurrencyUtils.formatVnd(row.unitPrice));

            StringBuilder sb = new StringBuilder();
            if (row.coupon > 0) {
                sb.append("CK ").append(Math.round(row.coupon * 100)).append("%");
            }
            if (row.vat > 0) {
                if (sb.length() > 0) sb.append("  ");
                sb.append("VAT ").append(Math.round(row.vat * 100)).append("%");
            }
            if (sb.length() > 0) {
                holder.txtCouponVat.setText(sb.toString());
                holder.txtCouponVat.setVisibility(View.VISIBLE);
            } else {
                holder.txtCouponVat.setVisibility(View.GONE);
            }

            holder.txtLineTotal.setText(CurrencyUtils.formatVnd(row.lineTotal));
        }

        return convertView;
    }

    static class ViewHolder {
        TextView txtProductName;
        TextView txtCategoryName;
        TextView txtQtyPrice;
        TextView txtCouponVat;
        TextView txtLineTotal;
    }
}
