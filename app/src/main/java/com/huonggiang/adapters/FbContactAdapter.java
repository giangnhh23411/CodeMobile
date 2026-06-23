package com.huonggiang.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.huonggiang.k23411teapp.R;
import com.huonggiang.models.FbContact;

import java.util.List;

/**
 * Hiển thị liên hệ + huy hiệu trạng thái đồng bộ (xanh = đã đồng bộ, cam = chờ đồng bộ).
 */
public class FbContactAdapter extends ArrayAdapter<FbContact> {

    private final Activity context;
    private final int resource;

    public FbContactAdapter(@NonNull Activity context, int resource, List<FbContact> items) {
        super(context, resource, items);
        this.context = context;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = context.getLayoutInflater().inflate(resource, parent, false);
            holder = new ViewHolder();
            holder.tvName  = convertView.findViewById(R.id.tvFbName);
            holder.tvInfo  = convertView.findViewById(R.id.tvFbInfo);
            holder.tvBadge = convertView.findViewById(R.id.tvFbBadge);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        FbContact c = getItem(position);
        if (c == null) return convertView;

        holder.tvName.setText(c.getName());
        holder.tvInfo.setText(c.getPhone() + "  ·  " + c.getEmail());

        if (FbContact.SYNCED.equals(c.getSyncStatus())) {
            holder.tvBadge.setText(R.string.str_fb_badge_synced);
            holder.tvBadge.setBackgroundColor(Color.parseColor("#C8E6C9"));
            holder.tvBadge.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            holder.tvBadge.setText(R.string.str_fb_badge_pending);
            holder.tvBadge.setBackgroundColor(Color.parseColor("#FFE0B2"));
            holder.tvBadge.setTextColor(Color.parseColor("#E65100"));
        }
        return convertView;
    }

    static class ViewHolder {
        TextView tvName;
        TextView tvInfo;
        TextView tvBadge;
    }
}
