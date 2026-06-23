package com.huonggiang.models;

public class SMS {
    private String address;   // số điện thoại
    private long   date;      // thời điểm (epoch millis)
    private String body;      // nội dung
    private int    type;      // 1 = inbox (đến), 2 = sent (đi)

    public SMS() {
    }

    public SMS(String address, long date, String body, int type) {
        this.address = address;
        this.date = date;
        this.body = body;
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
