package com.huonggiang.models;

import java.io.Serializable;

/**
 * Một học phần trong chương trình đào tạo.
 */
public class CurriculumCourse implements Serializable {
    private final String code;    // Mã học phần
    private final String name;    // Tên học phần
    private final String credits; // Số tín chỉ (vd "3.00")
    private final String type;    // Loại học phần (Bắt buộc / Tự chọn)

    public CurriculumCourse(String code, String name, String credits, String type) {
        this.code = code;
        this.name = name;
        this.credits = credits;
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getCredits() {
        return credits;
    }

    public String getType() {
        return type;
    }
}
