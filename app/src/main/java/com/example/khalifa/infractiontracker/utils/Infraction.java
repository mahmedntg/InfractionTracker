package com.example.khalifa.infractiontracker.utils;

import java.io.Serializable;

/**
 * Created by mhamedsayed on 3/15/2019.
 */

public class Infraction implements Serializable {
    private String name, solution, image, description, category, key, userId, status, adminComment,userid_category;
    private double latitude, longitude;

    public Infraction() {
    }

    public Infraction(String userId, String name, String solution, String image, String category, String description, double latitude, double longitude) {
        this.userId = userId;
        this.name = name;
        this.solution = solution;
        this.image = image;
        this.category = category;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdminComment() {
        return adminComment;
    }

    public void setAdminComment(String adminComment) {
        this.adminComment = adminComment;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUserid_category() {
        return userid_category;
    }

    public void setUserid_category(String userid_category) {
        this.userid_category = userid_category;
    }
}
