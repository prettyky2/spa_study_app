package com.example.myapp;

public class AppMenuItem {
    private final String title;
    private final int id; // 고유 ID 필드 추가

    public AppMenuItem(String title, int id) {
        this.title = title;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public int getId() {
        return id; // getId 메서드 추가
    }
}