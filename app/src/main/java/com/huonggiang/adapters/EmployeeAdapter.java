package com.huonggiang.adapters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.huonggiang.k23411teapp.R;
import com.huonggiang.models.Employee;

public class EmployeeAdapter extends ArrayAdapter<Employee> {
    Activity context;
    int resource;

    public EmployeeAdapter(@NonNull Activity context, int resource) {
        super(context, resource);
        this.context = context;
        this.resource = resource;
    }

    public EmployeeAdapter(@NonNull Activity context, int resource, @NonNull java.util.List<Employee> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
    }

    private int selectedPosition = -1;

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = this.context.getLayoutInflater();
            convertView = inflater.inflate(this.resource, parent, false);
            holder = new ViewHolder();
            holder.txtID = convertView.findViewById(R.id.txtID);
            holder.txtName = convertView.findViewById(R.id.txtName);
            holder.txtPhone = convertView.findViewById(R.id.txtPhone);
            holder.imgCall = convertView.findViewById(R.id.imgCall);
            holder.imgSms = convertView.findViewById(R.id.imgSms);
            holder.rootLayout = (ViewGroup) convertView; // Giả sử item root là một ViewGroup
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Employee emp = getItem(position);
        if (emp != null) {
            holder.txtID.setText(emp.getId());
            holder.txtName.setText(emp.getName());
            holder.txtPhone.setText(emp.getPhone());

            // Đổi trạng thái activated để selector hoạt động
            convertView.setActivated(position == selectedPosition);

            // Xử lý sự kiện Gọi điện
            if (holder.imgCall != null) {
                holder.imgCall.setOnClickListener(view -> {
                    String phone = emp.getPhone();
                    if (phone != null && !phone.isEmpty()) {
                        Intent intentCall = new Intent(Intent.ACTION_DIAL);
                        intentCall.setData(Uri.parse("tel:" + phone));
                        context.startActivity(intentCall);
                    } else {
                        Toast.makeText(context, R.string.str_phone_empty, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // Xử lý sự kiện Nhắn tin
            if (holder.imgSms != null) {
                holder.imgSms.setOnClickListener(view -> {
                    String phone = emp.getPhone();
                    if (phone != null && !phone.isEmpty()) {
                        Intent intentSms = new Intent(Intent.ACTION_SENDTO);
                        intentSms.setData(Uri.parse("sms:" + phone));
                        context.startActivity(intentSms);
                    } else {
                        Toast.makeText(context, R.string.str_phone_empty, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        return convertView;
    }

    static class ViewHolder {
        TextView txtID;
        TextView txtName;
        TextView txtPhone;
        ImageView imgCall;
        ImageView imgSms;
        ViewGroup rootLayout;
    }
}
