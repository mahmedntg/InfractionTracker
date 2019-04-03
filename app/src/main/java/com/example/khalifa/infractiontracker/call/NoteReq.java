package com.example.khalifa.infractiontracker.call;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class NoteReq implements Serializable {

    @SerializedName("to")
    @Expose
    private String to;
    @SerializedName("priority")
    @Expose
    private String priority;
    @SerializedName("data")
    @Expose
    private Note notification;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Note getNotification() {
        return notification;
    }

    public void setNotification(Note notification) {
        this.notification = notification;
    }


}