package com.huonggiang.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.huonggiang.k23411teapp.R;
import com.huonggiang.models.Product;
import com.huonggiang.utils.CurrencyUtils;

import java.util.ArrayList;

public class ProductAdapter extends ArrayAdapter<Product> {

    private final Activity context;
    private final int resource;

    public ProductAdapter(@NonNull Context context, int resource, ArrayList<Product> items) {
        super(context, resource, items);
        this.context  = (Activity) context;
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
            holder.tvProductName = convertView.findViewById(R.id.tvProductName);
            holder.tvProductId   = convertView.findViewById(R.id.tvProductId);
            holder.tvPrice       = convertView.findViewById(R.id.tvPrice);
            holder.tvQuantity    = convertView.findViewById(R.id.tvQuantity);
            holder.tvCouponTag   = convertView.findViewById(R.id.tvCouponTag);
            holder.tvVatTag      = convertView.findViewById(R.id.tvVatTag);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Product p = getItem(position);
        if (p == null) return convertView;

        holder.tvProductName.setText(p.getProductName());
        holder.tvProductId.setText(p.getProductId().toUpperCase());
        holder.tvPrice.setText(CurrencyUtils.formatVnd(p.getPrice()));
        holder.tvQuantity.setText("Còn: " + p.getQuantity());

        // Coupon tag
        if (p.getCoupon() > 0) {
            int pct = (int) Math.round(p.getCoupon() * 100);
            holder.tvCouponTag.setText("Giảm " + pct + "%");
            holder.tvCouponTag.setVisibility(View.VISIBLE);
            applyPill(holder.tvCouponTag,
                    ContextCompat.getColor(getContext(), R.color.status_complain_bg),
                    ContextCompat.getColor(getContext(), R.color.status_complain));
        } else {
            holder.tvCouponTag.setVisibility(View.GONE);
        }

        // VAT tag
        if (p.getVAT() > 0) {
            int pct = (int) Math.round(p.getVAT() * 100);
            holder.tvVatTag.setText("VAT " + pct + "%");
            holder.tvVatTag.setVisibility(View.VISIBLE);
            applyPill(holder.tvVatTag,
                    ContextCompat.getColor(getContext(), R.color.status_on_logistic_bg),
                    ContextCompat.getColor(getContext(), R.color.status_on_logistic));
        } else {
            holder.tvVatTag.setVisibility(View.GONE);
        }

        return convertView;
    }

    private void applyPill(TextView tv, int bgColor, int textColor) {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(32f);
        bg.setColor(bgColor);
        tv.setBackground(bg);
        tv.setTextColor(textColor);
    }

    static class ViewHolder {
        TextView tvProductName;
        TextView tvProductId;
        TextView tvPrice;
        TextView tvQuantity;
        TextView tvCouponTag;
        TextView tvVatTag;
    }
}
