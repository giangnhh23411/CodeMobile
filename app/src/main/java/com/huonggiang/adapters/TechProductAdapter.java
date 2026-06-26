package com.huonggiang.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.huonggiang.k23411teapp.R;
import com.huonggiang.models.TechShopData;
import com.huonggiang.utils.CurrencyUtils;
import com.huonggiang.utils.ImageLoader;

import java.util.List;

/**
 * Hiển thị sản phẩm TechShop: ảnh, tên, giá, tồn kho + nút "Thêm" vào giỏ.
 * Ảnh tải bất đồng bộ qua {@link ImageLoader}.
 */
public class TechProductAdapter extends ArrayAdapter<TechShopData.Product> {

    public interface OnAdd {
        void onAdd(TechShopData.Product product);
    }

    private final Activity context;
    private final int resource;
    private final OnAdd onAdd;

    public TechProductAdapter(@NonNull Activity context, int resource,
                              List<TechShopData.Product> items, OnAdd onAdd) {
        super(context, resource, items);
        this.context = context;
        this.resource = resource;
        this.onAdd = onAdd;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = context.getLayoutInflater().inflate(resource, parent, false);
            holder = new ViewHolder();
            holder.ivImage = convertView.findViewById(R.id.ivTechProduct);
            holder.tvName = convertView.findViewById(R.id.tvTechName);
            holder.tvPrice = convertView.findViewById(R.id.tvTechPrice);
            holder.tvStock = convertView.findViewById(R.id.tvTechStock);
            holder.btnAdd = convertView.findViewById(R.id.btnTechAdd);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TechShopData.Product p = getItem(position);
        if (p == null) return convertView;

        holder.tvName.setText(p.name);
        holder.tvPrice.setText(CurrencyUtils.formatVnd(p.price));
        holder.tvStock.setText(context.getString(R.string.str_ts_stock, p.stock));
        ImageLoader.load(holder.ivImage, p.imageUrl);

        boolean available = p.active && p.stock > 0;
        holder.btnAdd.setEnabled(available);
        holder.btnAdd.setOnClickListener(v -> {
            if (onAdd != null) onAdd.onAdd(p);
        });

        return convertView;
    }

    static class ViewHolder {
        ImageView ivImage;
        TextView tvName;
        TextView tvPrice;
        TextView tvStock;
        Button btnAdd;
    }
}
