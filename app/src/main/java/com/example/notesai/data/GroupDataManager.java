package com.example.notesai.data;

import android.util.Log;

import com.example.notesai.model.Category;
import com.example.notesai.model.Group;
import com.example.notesai.model.Note;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages groups, categories, and notes data
 * This is a simple in-memory implementation that can be replaced with a database later
 */
public class GroupDataManager {
    private static GroupDataManager instance;
    private Map<String, Group> groups;
    private Map<String, Category> categories;
    private Map<String, Note> notes;

    private GroupDataManager() {
        groups = new HashMap<>();
        categories = new HashMap<>();
        notes = new HashMap<>();
        try {
            initializeDefaultData();
        } catch (Exception e) {
            Log.e("GroupDataManager", "Error initializing default data", e);
            // Continue with empty maps if initialization fails
        }
    }

    public static GroupDataManager getInstance() {
        if (instance == null) {
            instance = new GroupDataManager();
        }
        return instance;
    }

    /**
     * Initialize default groups and categories
     */
    private void initializeDefaultData() {
        try {
            // Create Groups
            Group classesGroup = new Group("group_classes", "Classes", "Organize your class notes", android.R.drawable.ic_menu_agenda);
            Group workGroup = new Group("group_work", "Work", "Work-related notes and tasks", android.R.drawable.ic_menu_edit);
            Group projectsGroup = new Group("group_projects", "Projects", "Project notes and documentation", android.R.drawable.ic_menu_view);

            // Add categories to Classes group
            Category cps2231 = new Category("cat_cps2231", "CPS 2231", "Data Structures and Algorithms", "group_classes");
            Note note1 = new Note("note1", "Lecture 1: Introduction", "Overview of data structures...", "cat_cps2231");
            cps2231.addNote(note1);
            classesGroup.addCategory(cps2231);

            // Add categories to Work group
            Category meetingNotes = new Category("cat_meetings", "Meeting Notes", "Notes from team meetings", "group_work");
            Category tasks = new Category("cat_tasks", "Tasks", "Work tasks and to-dos", "group_work");
            workGroup.addCategory(meetingNotes);
            workGroup.addCategory(tasks);

            // Add categories to Projects group
            Category project1 = new Category("cat_project1", "Project Alpha", "Main project documentation", "group_projects");
            projectsGroup.addCategory(project1);

            // Store groups
            groups.put(classesGroup.getId(), classesGroup);
            groups.put(workGroup.getId(), workGroup);
            groups.put(projectsGroup.getId(), projectsGroup);

            // Store categories
            categories.put(cps2231.getId(), cps2231);
            categories.put(meetingNotes.getId(), meetingNotes);
            categories.put(tasks.getId(), tasks);
            categories.put(project1.getId(), project1);
            
            // Store the note
            notes.put(note1.getId(), note1);
            
            Log.d("GroupDataManager", "Default data initialized successfully");
        } catch (Exception e) {
            Log.e("GroupDataManager", "Error initializing default data", e);
            // Continue with empty data if initialization fails
        }
    }

    // Group methods
    public List<Group> getAllGroups() {
        return new ArrayList<>(groups.values());
    }

    public List<Category> getAllCategories() {
        return new ArrayList<>(categories.values());
    }

    public Group getGroupById(String groupId) {
        return groups.get(groupId);
    }

    public void addGroup(Group group) {
        groups.put(group.getId(), group);
    }

    public void updateGroup(Group group) {
        groups.put(group.getId(), group);
    }

    public void deleteGroup(String groupId) {
        Group group = groups.remove(groupId);
        if (group != null) {
            List<Category> categoriesCopy = new ArrayList<>(group.getCategories());
            for (Category category : categoriesCopy) {
                deleteCategory(category.getId());
            }
        }
    }

    // Category methods
    public List<Category> getCategoriesByGroup(String groupId) {
        List<Category> result = new ArrayList<>();
        for (Category category : categories.values()) {
            if (category.getGroupId().equals(groupId)) {
                result.add(category);
            }
        }
        return result;
    }

    public Category getCategoryById(String categoryId) {
        return categories.get(categoryId);
    }

    public void addCategory(Category category) {
        categories.put(category.getId(), category);
        // Also add to parent group
        Group group = groups.get(category.getGroupId());
        if (group != null) {
            group.addCategory(category);
        }
    }

    public void updateCategory(Category category) {
        categories.put(category.getId(), category);
    }

    public void deleteCategory(String categoryId) {
        Category category = categories.remove(categoryId);
        if (category != null) {
            Group parentGroup = groups.get(category.getGroupId());
            if (parentGroup != null) {
                List<Category> groupCategories = parentGroup.getCategories();
                for (int i = 0; i < groupCategories.size(); i++) {
                    if (categoryId.equals(groupCategories.get(i).getId())) {
                        groupCategories.remove(i);
                        break;
                    }
                }
            }
            for (Note note : new ArrayList<>(category.getNotes())) {
                notes.remove(note.getId());
            }
        }
    }

    // Note methods
    public List<Note> getNotesByCategory(String categoryId) {
        Category category = categories.get(categoryId);
        if (category == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(category.getNotes());
    }

    public Note getNoteById(String noteId) {
        return notes.get(noteId);
    }

    public void addNote(Note note) {
        notes.put(note.getId(), note);
        // Also add to parent category
        Category category = categories.get(note.getCategoryId());
        if (category != null) {
            category.addNote(note);
        }
    }

    public void updateNote(Note note) {
        notes.put(note.getId(), note);
        Category category = categories.get(note.getCategoryId());
        if (category != null) {
            List<Note> noteList = category.getNotes();
            for (int i = 0; i < noteList.size(); i++) {
                if (note.getId().equals(noteList.get(i).getId())) {
                    noteList.set(i, note);
                    return;
                }
            }
            // If not in list yet, add it
            category.addNote(note);
        }
    }

    public void deleteNote(String noteId) {
        Note removed = notes.remove(noteId);
        if (removed != null) {
            Category category = categories.get(removed.getCategoryId());
            if (category != null) {
                category.removeNoteById(noteId);
            }
        }
    }

    public void moveNote(String categoryId, int fromPosition, int toPosition) {
        Category category = categories.get(categoryId);
        if (category != null) {
            category.moveNote(fromPosition, toPosition);
        }
    }

    /**
     * Check if custom groups are enabled (for future feature)
     */
    public boolean isCustomGroupsEnabled() {
        return true; // Can be made configurable later
    }

    public Group findGroupByName(String name) {
        if (name == null) return null;
        for (Group group : groups.values()) {
            if (group.getName() != null && group.getName().equalsIgnoreCase(name.trim())) {
                return group;
            }
        }
        return null;
    }

    public Category findCategoryByName(String groupId, String name) {
        if (groupId == null || name == null) return null;
        for (Category category : getCategoriesByGroup(groupId)) {
            if (category.getName() != null && category.getName().equalsIgnoreCase(name.trim())) {
                return category;
            }
        }
        return null;
    }

    public Group createGroup(String name, String description, int iconResId) {
        String id = "group_" + UUID.randomUUID();
        Group group = new Group(id, name != null ? name : "Untitled Group", description, iconResId);
        group.setCustom(true);
        addGroup(group);
        return group;
    }

    public Category createCategory(String groupId, String name, String description) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId cannot be null");
        }
        String id = "cat_" + UUID.randomUUID();
        Category category = new Category(id, name != null ? name : "Untitled Category", description, groupId);
        addCategory(category);
        return category;
    }

    public Note createNote(String categoryId, String title, String content) {
        if (categoryId == null) {
            throw new IllegalArgumentException("categoryId cannot be null");
        }
        String id = "note_" + UUID.randomUUID();
        Note note = new Note(id, title != null ? title : "", content != null ? content : "", categoryId);
        addNote(note);
        return note;
    }
}

