package com.huonggiang.utils;

import com.huonggiang.models.CurriculumCourse;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse chương trình đào tạo (curriculum) từ trang chi tiết ngành của MYUEL.
 *
 * Trang MYUEL render mỗi học kỳ thành một bảng GridView ({@code grvHocphan}); mỗi học
 * phần là một dòng gồm các span: mã ({@code lblCurriculumID}), tên
 * ({@code lblCurriculumName}), tín chỉ ({@code lblCredits}), loại
 * ({@code lblCurriculumTypeName}). Tiêu đề học kỳ là chuỗi "Học kỳ N" nằm ngay trước bảng.
 *
 * Parser quét HTML theo đúng thứ tự xuất hiện: gặp "Học kỳ N" thì mở một học kỳ mới,
 * gặp một dòng học phần thì thêm vào học kỳ đang mở.
 */
public final class CurriculumParser {

    // Hoặc tiêu đề học kỳ "Học kỳ N", hoặc một dòng học phần (4 span theo thứ tự cố định).
    private static final Pattern TOKEN = Pattern.compile(
            "(Học kỳ\\s*\\d+)"
                    + "|lblCurriculumID[^>]*>([^<]*)</span>.*?"
                    + "lblCurriculumName[^>]*>([^<]*)</span>.*?"
                    + "lblCredits[^>]*>([^<]*)</span>.*?"
                    + "lblCurriculumTypeName[^>]*>([^<]*)</span>",
            Pattern.DOTALL);

    private CurriculumParser() {
    }

    /** Một học kỳ kèm danh sách học phần của nó. */
    public static class Semester {
        public final String title;
        public final List<CurriculumCourse> courses = new ArrayList<>();

        Semester(String title) {
            this.title = title;
        }
    }

    /** Tách HTML thành danh sách học kỳ; học kỳ rỗng (không có học phần) bị loại. */
    public static List<Semester> parse(String html) {
        List<Semester> semesters = new ArrayList<>();
        if (html == null) return semesters;

        Semester current = null;
        Matcher matcher = TOKEN.matcher(html);
        while (matcher.find()) {
            String header = matcher.group(1);
            if (header != null) {
                current = new Semester(clean(header));
                semesters.add(current);
            } else if (current != null) { // bỏ qua dòng học phần xuất hiện trước học kỳ đầu tiên
                current.courses.add(new CurriculumCourse(
                        clean(matcher.group(2)),
                        clean(matcher.group(3)),
                        clean(matcher.group(4)),
                        clean(matcher.group(5))));
            }
        }

        List<Semester> result = new ArrayList<>();
        for (Semester s : semesters) {
            if (!s.courses.isEmpty()) result.add(s);
        }
        return result;
    }

    /** Giải mã entity HTML cơ bản + gộp khoảng trắng. */
    private static String clean(String s) {
        if (s == null) return "";
        return s.replace("&amp;", "&")
                .replace("&nbsp;", " ")
                .replace("&#39;", "'")
                .replace("&quot;", "\"")
                .trim()
                .replaceAll("\\s+", " ");
    }
}
