package com.example.notesai.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a subcategory within a Group (e.g., "CPS 2231" under "Classes")
 */
public class Category {
    private String id;
    private String name;
    private String description;
    private String groupId; // Parent group ID
    private List<Note> notes;

    public Category(String id, String name, String description, String groupId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.groupId = groupId;
        this.notes = new ArrayList<>();
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

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    public void addNote(Note note) {
        this.notes.add(note);
    }

    public void addNote(int index, Note note) {
        if (index < 0 || index > notes.size()) {
            this.notes.add(note);
        } else {
            this.notes.add(index, note);
        }
    }

    public void removeNoteById(String noteId) {
        if (noteId == null) return;
        for (int i = 0; i < notes.size(); i++) {
            if (noteId.equals(notes.get(i).getId())) {
                notes.remove(i);
                break;
            }
        }
    }

    public void moveNote(int fromPosition, int toPosition) {
        if (fromPosition < 0 || fromPosition >= notes.size()
                || toPosition < 0 || toPosition >= notes.size()) {
            return;
        }
        Note note = notes.remove(fromPosition);
        notes.add(toPosition, note);
    }

    public int getNoteCount() {
        return notes.size();
    }
}

