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
import com.huonggiang.models.Major;

import java.util.List;

/**
 * Hiển thị danh sách ngành: tên ngành + dòng phụ (bậc · hệ · khoa).
 */
public class MajorAdapter extends ArrayAdapter<Major> {

    private final Activity context;
    private final int resource;

    public MajorAdapter(@NonNull Context context, int resource, List<Major> items) {
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
            holder.tvMajorName = convertView.findViewById(R.id.tvMajorName);
            holder.tvMajorInfo = convertView.findViewById(R.id.tvMajorInfo);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Major m = getItem(position);
        if (m == null) return convertView;

        holder.tvMajorName.setText(m.getName());
        holder.tvMajorInfo.setText(buildSubtitle(m));
        return convertView;
    }

    /** Danh sách đã lọc còn Đại học - Chính quy, nên phụ đề chỉ cần mã ngành + khoa. */
    private String buildSubtitle(Major m) {
        StringBuilder sb = new StringBuilder();
        if (m.getCode() != null && !m.getCode().isEmpty()) {
            sb.append("Mã ngành: ").append(m.getCode());
        }
        if (m.getDepartmentId() != null && !m.getDepartmentId().isEmpty()) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append("Khoa ").append(m.getDepartmentId());
        }
        return sb.toString();
    }

    static class ViewHolder {
        TextView tvMajorName;
        TextView tvMajorInfo;
    }
}
