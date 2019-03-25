package com.example.khalifa.infractiontracker.utils;

import java.io.Serializable;

/**
 * Created by mhamedsayed on 3/15/2019.
 */

public class User implements Serializable {

    private String name, email, address, password, phone;

    public User() {
    }

    public User(String email) {
        this.email = email;
    }

    public User(String name, String email, String phone, String address) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return getEmail() != null ? getEmail().equals(user.getEmail()) : user.getEmail() == null;
    }

    @Override
    public int hashCode() {
        return getEmail() != null ? getEmail().hashCode() : 0;
    }
}
