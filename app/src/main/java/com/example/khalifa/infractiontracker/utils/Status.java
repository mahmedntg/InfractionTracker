package com.example.khalifa.infractiontracker.utils;

import java.io.Serializable;

/**
 * Created by mhamedsayed on 3/22/2019.
 */

public enum Status implements Serializable {
    PENDING("قيد الانتظار"), APPROVED("تمت الموافقة"), REJECTED("تم الرفض");
    private String value;

    Status(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
