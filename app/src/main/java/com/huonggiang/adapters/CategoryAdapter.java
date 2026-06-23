package com.huonggiang.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.huonggiang.k23411teapp.R;
import com.huonggiang.models.Category;

public class CategoryAdapter extends ArrayAdapter<Category> {

    private final Activity context;
    private final int resource;

    public CategoryAdapter(@NonNull Context context, int resource) {
        super(context, resource);
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
            holder.tvCategoryId  = convertView.findViewById(R.id.tvCategoryId);
            holder.tvCateName    = convertView.findViewById(R.id.tvCateName);
            holder.tvDescription = convertView.findViewById(R.id.tvDescription);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Category cate = getItem(position);
        if (cate != null) {
            holder.tvCategoryId.setText(cate.getCategoryId());
            holder.tvCateName.setText(cate.getCategoryName());
            holder.tvDescription.setText(cate.getDescription());
        }
        return convertView;
    }

    static class ViewHolder {
        TextView tvCategoryId;
        TextView tvCateName;
        TextView tvDescription;
    }
}
