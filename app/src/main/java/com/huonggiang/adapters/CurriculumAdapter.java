package com.huonggiang.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.huonggiang.k23411teapp.R;
import com.huonggiang.models.CurriculumCourse;

import java.util.List;

/**
 * Danh sách chương trình đào tạo phân nhóm theo học kỳ:
 *  - Dòng tiêu đề học kỳ (header)
 *  - Các dòng học phần
 */
public class CurriculumAdapter extends BaseAdapter {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_COURSE = 1;

    /** Một dòng trong danh sách: hoặc là tiêu đề học kỳ, hoặc là một học phần. */
    public static class Row {
        final boolean header;
        final String title;            // dùng khi là header
        final CurriculumCourse course; // dùng khi là học phần

        private Row(boolean header, String title, CurriculumCourse course) {
            this.header = header;
            this.title = title;
            this.course = course;
        }

        public static Row header(String title) {
            return new Row(true, title, null);
        }

        public static Row course(CurriculumCourse course) {
            return new Row(false, null, course);
        }
    }

    private final LayoutInflater inflater;
    private final List<Row> rows;

    public CurriculumAdapter(Activity context, List<Row> rows) {
        this.inflater = context.getLayoutInflater();
        this.rows = rows;
    }

    @Override
    public int getCount() {
        return rows.size();
    }

    @Override
    public Row getItem(int position) {
        return rows.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).header ? TYPE_HEADER : TYPE_COURSE;
    }

    @Override
    public boolean isEnabled(int position) {
        return false; // không cho bấm vào dòng
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Row row = rows.get(position);
        if (row.header) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_curriculum_header, parent, false);
            }
            ((TextView) convertView.findViewById(R.id.tvSemester)).setText(row.title);
            return convertView;
        }

        CourseHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_curriculum_course, parent, false);
            holder = new CourseHolder();
            holder.tvName    = convertView.findViewById(R.id.tvCourseName);
            holder.tvCode    = convertView.findViewById(R.id.tvCourseCode);
            holder.tvCredits = convertView.findViewById(R.id.tvCourseCredits);
            holder.tvType    = convertView.findViewById(R.id.tvCourseType);
            convertView.setTag(holder);
        } else {
            holder = (CourseHolder) convertView.getTag();
        }

        CurriculumCourse c = row.course;
        holder.tvName.setText(c.getName());
        holder.tvCode.setText(c.getCode());
        holder.tvCredits.setText(formatCredits(c.getCredits()) + " TC");
        holder.tvType.setText(c.getType());
        return convertView;
    }

    static class CourseHolder {
        TextView tvName;
        TextView tvCode;
        TextView tvCredits;
        TextView tvType;
    }

    /** "3.00" -> "3", "1.50" -> "1.5". */
    public static String formatCredits(String raw) {
        if (raw == null) return "";
        try {
            double d = Double.parseDouble(raw);
            if (d == Math.floor(d)) return String.valueOf((int) d);
            return String.valueOf(d);
        } catch (NumberFormatException e) {
            return raw;
        }
    }
}
