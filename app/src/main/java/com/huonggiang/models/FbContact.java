package com.huonggiang.models;

import java.io.Serializable;

/**
 * Liên hệ dùng cho màn hình học Firebase (node "contacts" gồm name/email/phone).
 *
 * {@code syncStatus} cho biết trạng thái đồng bộ giữa Local DB và Firebase:
 *  - {@link #SYNCED}         : đã khớp với Firebase
 *  - {@link #PENDING_UPSERT} : thêm/sửa khi offline, chờ đẩy lên
 *  - {@link #PENDING_DELETE} : xoá khi offline, chờ xoá trên Firebase
 */
public class FbContact implements Serializable {

    public static final String SYNCED = "SYNCED";
    public static final String PENDING_UPSERT = "PENDING_UPSERT";
    public static final String PENDING_DELETE = "PENDING_DELETE";

    private final String id;   // key trên Firebase: contacts/{id}
    private final String name;
    private final String email;
    private final String phone;
    private final String syncStatus;

    public FbContact(String id, String name, String email, String phone, String syncStatus) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.syncStatus = syncStatus;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getSyncStatus() {
        return syncStatus;
    }
}
