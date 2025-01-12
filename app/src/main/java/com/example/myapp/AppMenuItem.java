package com.example.myapp;

public class AppMenuItem {
    private final String title;
    private final String subtitle;

    public AppMenuItem(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }
}