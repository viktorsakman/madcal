package com.cs407.madcal;

public class NotificationItem {
    private String message = null;
    private int id = -1;

    public NotificationItem(String sender, String message, int id) {
        this.message = message;
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public int getId() {
        return id;
    }
}
