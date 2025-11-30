package com.example.notesai;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.notesai.data.GroupDataManager;
import com.example.notesai.model.Category;
import com.example.notesai.model.Note;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Date;

public class NoteDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY_ID = "extra_category_id";
    public static final String EXTRA_NOTE_ID = "extra_note_id";

    private GroupDataManager dataManager;
    private TextInputEditText titleInput;
    private TextInputEditText contentInput;
    private Button summarizeButton;
    private String categoryId;
    private String noteId;
    private Note existingNote;
    private AISummarizer aiSummarizer;

    // Flag to track if this note belongs to the "Summaries" category
    private boolean isSummaryNote = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        dataManager = GroupDataManager.getInstance();
        aiSummarizer = new AISummarizer();

        titleInput = findViewById(R.id.input_note_title);
        contentInput = findViewById(R.id.input_note_content);
        Button deleteButton = findViewById(R.id.btn_delete_note);
        Button saveButton = findViewById(R.id.btn_save_note);
        summarizeButton = findViewById(R.id.btn_summarize_note);

        categoryId = getIntent().getStringExtra(EXTRA_CATEGORY_ID);
        noteId = getIntent().getStringExtra(EXTRA_NOTE_ID);

        if (categoryId == null) {
            Toast.makeText(this, "Missing category context", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Check if this note is in the "Summaries" category
        Category category = dataManager.getCategoryById(categoryId);
        if (category != null && "Summaries".equalsIgnoreCase(category.getName())) {
            isSummaryNote = true;
        }

        if (!TextUtils.isEmpty(noteId)) {
            existingNote = dataManager.getNoteById(noteId);
        }

        if (existingNote != null) {
            if (!TextUtils.isEmpty(existingNote.getTitle())) {
                titleInput.setText(existingNote.getTitle());
            }
            if (!TextUtils.isEmpty(existingNote.getContent())) {
                contentInput.setText(existingNote.getContent());
            }
            deleteButton.setVisibility(View.VISIBLE);

            // Show summarize button only for normal notes with content
            if (!TextUtils.isEmpty(existingNote.getContent()) && !isSummaryNote) {
                summarizeButton.setVisibility(View.VISIBLE);
            } else {
                summarizeButton.setVisibility(View.GONE);
            }
        } else {
            deleteButton.setVisibility(View.GONE);
            summarizeButton.setVisibility(View.GONE);
        }

        // Show summarize button when content is entered (for new notes), BUT NOT for summaries
        contentInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                updateSummarizeButtonVisibility();
            }
        });

        // Also update when text changes
        contentInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSummarizeButtonVisibility();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        deleteButton.setOnClickListener(v -> {
            if (existingNote != null) {
                dataManager.deleteNote(existingNote.getId());
                Toast.makeText(this, R.string.notes_deleted, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });

        saveButton.setOnClickListener(v -> saveNote());

        summarizeButton.setOnClickListener(v -> summarizeNote());
    }

    private void updateSummarizeButtonVisibility() {
        // If this is a summary note, NEVER show the summarize button
        if (isSummaryNote) {
            summarizeButton.setVisibility(View.GONE);
            return;
        }

        String content = contentInput.getText() != null ? contentInput.getText().toString().trim() : "";
        if (!TextUtils.isEmpty(content)) {
            summarizeButton.setVisibility(View.VISIBLE);
        } else {
            summarizeButton.setVisibility(View.GONE);
        }
    }

    private void summarizeNote() {
        String content = contentInput.getText() != null ? contentInput.getText().toString().trim() : "";
        String title = titleInput.getText() != null ? titleInput.getText().toString().trim() : "Untitled";

        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, R.string.notes_empty_message, Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, R.string.summarizing, Toast.LENGTH_SHORT).show();
        summarizeButton.setEnabled(false);

        aiSummarizer.summarizeText(content, new AISummarizer.SummaryCallback() {
            @Override
            public void onSuccess(String summary) {
                runOnUiThread(() -> {
                    summarizeButton.setEnabled(true);
                    saveSummaryToFolder(title, summary);
                    new AlertDialog.Builder(NoteDetailActivity.this)
                            .setTitle("AI Summary Created")
                            .setMessage("Your study guide has been saved to the 'Summaries' folder.\n\nPreview:\n" + summary)
                            .setPositiveButton("OK", null)
                            .show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    summarizeButton.setEnabled(true);
                    Toast.makeText(NoteDetailActivity.this, "Summary failed: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void saveSummaryToFolder(String originalTitle, String summaryContent) {
        Category currentCategory = dataManager.getCategoryById(categoryId);
        if (currentCategory == null) {
            Toast.makeText(this, "Error: Could not find current category context", Toast.LENGTH_SHORT).show();
            return;
        }

        String groupId = currentCategory.getGroupId();

        // Check if "Summaries" category exists in this group
        Category summariesCategory = dataManager.findCategoryByName(groupId, "Summaries");

        // If not, create it
        if (summariesCategory == null) {
            summariesCategory = dataManager.createCategory(groupId, "Summaries", "AI Generated Study Guides");
        }

        // Create the new note in the Summaries category
        String summaryTitle = "Study Guide: " + originalTitle;
        dataManager.createNote(summariesCategory.getId(), summaryTitle, summaryContent);

        Toast.makeText(this, "Study Guide saved to 'Summaries'", Toast.LENGTH_SHORT).show();
    }

    private void saveNote() {
        String title = titleInput.getText() != null ? titleInput.getText().toString().trim() : "";
        String content = contentInput.getText() != null ? contentInput.getText().toString().trim() : "";

        if (TextUtils.isEmpty(title)) {
            titleInput.setError(getString(R.string.please_enter_note_title));
            return;
        }
        if (categoryId == null) {
            Toast.makeText(this, "Category not found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (existingNote != null) {
            existingNote.setTitle(title);
            existingNote.setContent(content);
            existingNote.setUpdatedAt(new Date());
            dataManager.updateNote(existingNote);
            Toast.makeText(this, R.string.notes_updated, Toast.LENGTH_SHORT).show();
        } else {
            dataManager.createNote(categoryId, title, content);
            Toast.makeText(this, R.string.note_created, Toast.LENGTH_SHORT).show();
        }

        setResult(RESULT_OK);
        finish();
    }
}