package com.fifaclient.core;

public enum Category {
    HUD("HUD"),
    VISUAL("Visual"),
    MOVEMENT("Movement"),
    CLIENT("Client");

    public final String label;

    Category(String label) {
        this.label = label;
    }
}
