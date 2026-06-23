package com.huonggiang.models;

import java.io.Serializable;

/**
 * Một ngành đào tạo (Đại học - Chính quy) lấy từ trang MYUEL.
 * {@code url} là link đầy đủ tới trang chi tiết của ngành; {@code code} là mã ngành (OlogyID),
 * dùng để phân biệt các ngành trùng tên.
 */
public class Major implements Serializable {
    private final String name;
    private final String url;
    private final String code;         // OlogyID - mã ngành
    private final String departmentId; // Khoa

    public Major(String name, String url, String code, String departmentId) {
        this.name = name;
        this.url = url;
        this.code = code;
        this.departmentId = departmentId;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getCode() {
        return code;
    }

    public String getDepartmentId() {
        return departmentId;
    }
}
