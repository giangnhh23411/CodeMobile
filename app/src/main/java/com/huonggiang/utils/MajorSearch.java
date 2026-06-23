package com.huonggiang.utils;

import com.huonggiang.models.Major;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Thuật toán tìm kiếm ngành tối ưu cho tập dữ liệu nhỏ–vừa (vài trăm ngành).
 *
 * Ý tưởng:
 *  1) Chuẩn hoá tiếng Việt: bỏ dấu, đổi đ→d, thường hoá, gộp khoảng trắng
 *     → tìm không phân biệt hoa/thường và không phụ thuộc dấu.
 *  2) Chấm điểm theo mức độ khớp giảm dần:
 *       khớp chính xác > bắt đầu bằng > chứa cả chuỗi > chứa đủ mọi từ khoá
 *       > fuzzy (khoảng cách Levenshtein) để chịu được lỗi gõ.
 *  3) Sắp xếp theo điểm giảm dần, trả về danh sách đã xếp hạng.
 *
 * Độ phức tạp O(N·L) cho mỗi truy vấn (N = số ngành, L = độ dài tên) — tức thì
 * với dữ liệu MYUEL.
 */
public final class MajorSearch {

    private MajorSearch() {
    }

    /** Bỏ dấu tiếng Việt + thường hoá + gộp khoảng trắng. */
    public static String normalize(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        n = n.replace('đ', 'd').replace('Đ', 'D');
        return n.toLowerCase().trim().replaceAll("\\s+", " ");
    }

    /**
     * Trả về danh sách ngành khớp với {@code query}, đã xếp hạng theo độ liên quan.
     * Truy vấn rỗng → trả về toàn bộ danh sách gốc.
     */
    public static List<Major> search(List<Major> all, String query) {
        if (all == null) return new ArrayList<>();
        String q = normalize(query);
        if (q.isEmpty()) return new ArrayList<>(all);

        String[] qTokens = q.split(" ");
        List<Scored> scored = new ArrayList<>();
        for (Major m : all) {
            int s = score(normalize(m.getName()), q, qTokens);
            if (s > 0) scored.add(new Scored(m, s));
        }
        // Điểm cao lên trước; cùng điểm thì tên ngắn hơn (sát hơn) lên trước
        Collections.sort(scored, (a, b) -> {
            if (b.score != a.score) return b.score - a.score;
            return a.major.getName().length() - b.major.getName().length();
        });

        List<Major> result = new ArrayList<>(scored.size());
        for (Scored s : scored) result.add(s.major);
        return result;
    }

    private static int score(String name, String q, String[] qTokens) {
        if (name.equals(q)) return 1000;
        if (name.startsWith(q)) return 800;
        if (name.contains(q)) return 600;

        // Chứa đủ mọi từ khoá (không cần liền nhau)
        boolean allTokens = true;
        for (String t : qTokens) {
            if (!t.isEmpty() && !name.contains(t)) {
                allTokens = false;
                break;
            }
        }
        if (allTokens) return 400;

        // Fuzzy toàn chuỗi: chịu lỗi gõ tỉ lệ ~1/3 độ dài
        int dist = levenshtein(q, name);
        int tolerance = Math.max(1, q.length() / 3);
        if (dist <= tolerance) return 300 - dist;

        // Fuzzy theo từng từ: bắt các từ gõ sai 1 ký tự
        int best = 0;
        for (String nt : name.split(" ")) {
            for (String t : qTokens) {
                if (t.length() < 3) continue;
                int d = levenshtein(t, nt);
                if (d <= 1) best = Math.max(best, 150 - d);
            }
        }
        return best;
    }

    /** Khoảng cách Levenshtein (quy hoạch động, dùng 2 hàng). */
    private static int levenshtein(String a, String b) {
        int n = a.length();
        int m = b.length();
        if (n == 0) return m;
        if (m == 0) return n;

        int[] prev = new int[m + 1];
        int[] curr = new int[m + 1];
        for (int j = 0; j <= m; j++) prev[j] = j;

        for (int i = 1; i <= n; i++) {
            curr[0] = i;
            char ca = a.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                int cost = (ca == b.charAt(j - 1)) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] tmp = prev;
            prev = curr;
            curr = tmp;
        }
        return prev[m];
    }

    private static class Scored {
        final Major major;
        final int score;

        Scored(Major major, int score) {
            this.major = major;
            this.score = score;
        }
    }
}
