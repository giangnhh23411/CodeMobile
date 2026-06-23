package com.huonggiang.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.huonggiang.k23411teapp.R;
import com.huonggiang.models.RaoVatItem;
import com.huonggiang.utils.ImageLoader;

import java.util.ArrayList;

/**
 * Adapter hiển thị danh sách tin rao vặt: ảnh, tiêu đề, giá, khu vực.
 * Ảnh được tải bất đồng bộ qua {@link ImageLoader}.
 */
public class RaoVatAdapter extends ArrayAdapter<RaoVatItem> {

    private final Activity context;
    private final int resource;

    public RaoVatAdapter(@NonNull Context context, int resource, ArrayList<RaoVatItem> items) {
        super(context, resource, items);
        this.context = (Activity) context;
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
            holder.ivThumb = convertView.findViewById(R.id.ivThumb);
            holder.tvTitle = convertView.findViewById(R.id.tvTitle);
            holder.tvPrice = convertView.findViewById(R.id.tvPrice);
            holder.tvLocation = convertView.findViewById(R.id.tvLocation);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        RaoVatItem item = getItem(position);
        if (item == null) return convertView;

        holder.tvTitle.setText(item.getTitle());
        holder.tvPrice.setText(item.getPrice());
        holder.tvLocation.setText(context.getString(R.string.str_rv_location, item.getLocation()));
        ImageLoader.load(holder.ivThumb, item.getThumb());

        return convertView;
    }

    static class ViewHolder {
        ImageView ivThumb;
        TextView tvTitle;
        TextView tvPrice;
        TextView tvLocation;
    }
}
