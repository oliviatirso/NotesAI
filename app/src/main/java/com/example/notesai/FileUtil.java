package com.example.notesai;

import android.content.Context;
import android.net.Uri;

import com.example.notesai.data.GroupDataManager;
import com.example.notesai.model.Category;
import com.example.notesai.model.Group;
import com.example.notesai.model.Note;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import java.util.ArrayList;

public class FileUtil {

    public static String readTextFromUri(Context context, Uri uri) {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error reading file: " + e.getMessage();
        }
        return stringBuilder.toString();
    }

    public static void exportNotesToCsv(Context context, Uri uri, List<Group> groups) throws Exception {
        if (uri == null) return;
        OutputStream outputStream = context.getContentResolver().openOutputStream(uri, "w");
        if (outputStream == null) {
            throw new IllegalStateException("Unable to open output stream");
        }
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            writer.write("Group ID,Group Name,Group Description,Category ID,Category Name,Category Description,Note ID,Note Title,Note Content,Updated At");
            writer.newLine();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            for (Group group : groups) {
                for (Category category : group.getCategories()) {
                    List<Note> notes = category.getNotes();
                    if (notes.isEmpty()) {
                        writer.write(csvRow(group.getId(), group.getName(), group.getDescription(),
                                category.getId(), category.getName(), category.getDescription(),
                                "", "", "", ""));
                        writer.newLine();
                    } else {
                        for (Note note : notes) {
                            Date updatedAt = note.getUpdatedAt();
                            String formattedDate = updatedAt != null ? sdf.format(updatedAt) : "";
                            writer.write(csvRow(group.getId(), group.getName(), group.getDescription(),
                                    category.getId(), category.getName(), category.getDescription(),
                                    note.getId(), note.getTitle(), note.getContent(), formattedDate));
                            writer.newLine();
                        }
                    }
                }
            }
            writer.flush();
        }
    }

    public static void importNotesFromCsv(Context context, Uri uri, GroupDataManager dataManager) throws Exception {
        if (uri == null) return;
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            // Skip header
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                List<String> columns = parseCsvLine(line);
                if (columns.size() < 9) continue;

                String groupName = getValue(columns, 1);
                String groupDescription = getValue(columns, 2);
                String categoryName = getValue(columns, 4);
                String categoryDescription = getValue(columns, 5);
                String noteTitle = getValue(columns, 7);
                String noteContent = getValue(columns, 8);

                Group group = dataManager.findGroupByName(groupName);
                if (group == null) {
                    group = dataManager.createGroup(
                            groupName != null ? groupName : "Imported Group",
                            groupDescription,
                            android.R.drawable.ic_menu_agenda
                    );
                }

                Category category = dataManager.findCategoryByName(group.getId(), categoryName);
                if (category == null) {
                    category = dataManager.createCategory(
                            group.getId(),
                            categoryName != null ? categoryName : "Imported Category",
                            categoryDescription
                    );
                }

                if (noteTitle != null && !noteTitle.trim().isEmpty()) {
                    dataManager.createNote(category.getId(), noteTitle, noteContent);
                }
            }
        }
    }

    private static String csvRow(String... values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            builder.append("\"").append(escapeCsv(values[i])).append("\"");
            if (i < values.length - 1) {
                builder.append(",");
            }
        }
        return builder.toString();
    }

    private static String escapeCsv(String input) {
        if (input == null) return "";
        return input.replace("\"", "\"\"");
    }

    private static List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        if (line == null) return result;
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result;
    }

    private static String getValue(List<String> columns, int index) {
        if (index < 0 || index >= columns.size()) return null;
        String value = columns.get(index);
        if (value == null) return null;
        return value.trim();
    }
}