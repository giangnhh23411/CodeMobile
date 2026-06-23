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
import com.huonggiang.models.Contact;

import java.util.List;

public class ContactAdapter extends ArrayAdapter<Contact> {

    private final Activity context;
    private final int resource;

    public ContactAdapter(@NonNull Activity context, int resource, @NonNull List<Contact> objects) {
        super(context, resource, objects);
        this.context  = context;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);
            holder = new ViewHolder();
            holder.tvContactAvatar = convertView.findViewById(R.id.tvContactAvatar);
            holder.tvContactName   = convertView.findViewById(R.id.tvContactName);
            holder.tvContactPhone  = convertView.findViewById(R.id.tvContactPhone);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Contact contact = getItem(position);
        if (contact != null) {
            holder.tvContactName.setText(contact.getName());
            holder.tvContactPhone.setText(contact.getPhone());

            String name = contact.getName();
            String letter = (name != null && !name.isEmpty())
                    ? name.substring(0, 1).toUpperCase()
                    : "#";
            holder.tvContactAvatar.setText(letter);
        }
        return convertView;
    }

    static class ViewHolder {
        TextView tvContactAvatar;
        TextView tvContactName;
        TextView tvContactPhone;
    }
}
