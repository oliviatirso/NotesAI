package com.example.notesai.model;

import java.util.Date;

/**
 * Represents a note within a category
 */
public class Note {
    private String id;
    private String title;
    private String content;
    private String summary; // AI-generated summary
    private String categoryId; // Parent category ID
    private Date createdAt;
    private Date updatedAt;
    private boolean isPdfNote; // Whether this is from a PDF upload

    public Note(String id, String title, String content, String categoryId) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.categoryId = categoryId;
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.isPdfNote = false;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isPdfNote() {
        return isPdfNote;
    }

    public void setPdfNote(boolean pdfNote) {
        isPdfNote = pdfNote;
    }
}

