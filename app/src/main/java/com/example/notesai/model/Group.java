package com.example.notesai.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a top-level group (e.g., Classes, Work, Projects)
 */
public class Group {
    private String id;
    private String name;
    private String description;
    private int iconResId; // Resource ID for icon
    private List<Category> categories;
    private boolean isCustom; // Whether user created this group

    public Group(String id, String name, String description, int iconResId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconResId = iconResId;
        this.categories = new ArrayList<>();
        this.isCustom = false;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public void addCategory(Category category) {
        this.categories.add(category);
    }

    public boolean isCustom() {
        return isCustom;
    }

    public void setCustom(boolean custom) {
        isCustom = custom;
    }
}

