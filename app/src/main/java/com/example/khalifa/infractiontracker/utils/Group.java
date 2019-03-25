package com.example.khalifa.infractiontracker.utils;

import java.io.Serializable;

/**
 * Created by mhamedsayed on 3/23/2019.
 */

public class Group implements Serializable {
    private String name, description, userId, image, key;
    private int totalUsers;

    public Group() {
    }

    public Group(String name, String description, String userId, String image) {
        this.name = name;
        this.description = description;
        this.userId = userId;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }
}
