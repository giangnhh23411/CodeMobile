package com.huonggiang.models;

import androidx.annotation.NonNull;
import java.io.Serializable;

public class Employee implements Serializable {
    private String id;
    private String name;
    private String phone;
    private String birthPlace;

    public Employee() {
    }

    public Employee(String id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    public Employee(String id, String name, String phone, String birthPlace) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.birthPlace = birthPlace;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBirthPlace() {
        return birthPlace;
    }

    public void setBirthPlace(String birthPlace) {
        this.birthPlace = birthPlace;
    }

    @NonNull
    @Override
    public String toString() {
        return id + " - " + name + " (" + birthPlace + ")";
    }
}
