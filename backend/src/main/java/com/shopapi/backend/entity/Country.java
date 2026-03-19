package com.shopapi.backend.entity;

public enum Country {
    SK("Slovensko"),
    CZ("Česko");

    private final String displayName;

    Country(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
