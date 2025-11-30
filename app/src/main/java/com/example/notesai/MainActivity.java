package com.example.notesai;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.notesai.data.GroupDataManager;
import com.example.notesai.model.Category;
import com.example.notesai.model.Group;
import com.example.notesai.model.Note;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Views from original code
    private ImageButton menuButton;
    private View uploadButton;
    private View classNotesCard;

    // Views for Navigation Drawer
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    // FAB, Data Manager, and AI
    private FloatingActionButton fabAdd;
    private GroupDataManager dataManager;
    private AISummarizer aiSummarizer;

    // Launchers
    private ActivityResultLauncher<String[]> pdfPickerLauncher;
    private ActivityResultLauncher<String> exportCsvLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("MainActivity", "onCreate started");

        try {
            FileUtil.initPdfBox(getApplicationContext());

            // Initialize Data Manager and AI
            dataManager = GroupDataManager.getInstance();
            aiSummarizer = new AISummarizer();

            // --- Initialize Launchers ---
            pdfPickerLauncher = registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    uri -> {
                        if (uri != null) {
                            processUploadedFile(uri);
                        } else {
                            Toast.makeText(MainActivity.this, "No file selected", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

            // Launcher for the Export function
            exportCsvLauncher = registerForActivityResult(
                    new ActivityResultContracts.CreateDocument("text/csv"),
                    uri -> {
                        if (uri != null) {
                            exportNotesToCsv(uri);
                        }
                    }
            );

            setContentView(R.layout.activity_main);

            // --- Initialize Views ---
            menuButton = findViewById(R.id.menu_button);
            uploadButton = findViewById(R.id.upload_button);
            classNotesCard = findViewById(R.id.class_notes_card);
            drawerLayout = findViewById(R.id.drawer_layout);
            navigationView = findViewById(R.id.nav_view);
            fabAdd = findViewById(R.id.fab_add);

            // --- Set up Navigation Drawer ---
            if (navigationView != null) {
                navigationView.setNavigationItemSelectedListener(this);
            }

            // --- Set up FAB ---
            if (fabAdd != null) {
                fabAdd.setOnClickListener(v -> showCreateOptionsDialog());
            }

            // --- Set up Listeners ---
            if (menuButton != null && drawerLayout != null) {
                menuButton.setOnClickListener(v -> {
                    if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                        drawerLayout.closeDrawer(GravityCompat.END);
                    } else {
                        drawerLayout.openDrawer(GravityCompat.END);
                    }
                });
            }

            // Update uploadButton to launch the file picker
            if (uploadButton != null && pdfPickerLauncher != null) {
                uploadButton.setOnClickListener(v -> {
                    // Define the MIME types for PDF, DOCX, and PPTX
                    String[] mimeTypes = {
                            "application/pdf",
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
                            "text/plain"
                    };
                    pdfPickerLauncher.launch(mimeTypes);
                });
            }

            if (classNotesCard != null) {
                classNotesCard.setOnClickListener(v -> {
                    Intent intent = new Intent(MainActivity.this, GroupsActivity.class);
                    startActivity(intent);
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("MainActivity", "Error in onCreate", e);
            Toast.makeText(this, "Error initializing app: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // --- UPLOAD PROCESSING LOGIC ---

    private void processUploadedFile(Uri uri) {
        String fileName = getFileNameFromUri(uri);
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Reading " + fileName + "...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(() -> {
            // Extract text in background using our FileUtil (supports PDF)
            String extractedText = FileUtil.readTextFromUri(this, uri);

            runOnUiThread(() -> {
                progressDialog.dismiss();
                if (extractedText == null || extractedText.startsWith("Error")) {
                    Toast.makeText(this, "Failed to read file.", Toast.LENGTH_SHORT).show();
                } else {
                    showUploadActionDialog(fileName, extractedText);
                }
            });
        }).start();
    }

    private void showUploadActionDialog(String fileName, String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("File Processed");
        builder.setMessage("Would you like to summarize '" + fileName + "' using AI before saving?");

        builder.setPositiveButton("Summarize & Save", (dialog, which) -> {
            performSummarization(fileName, content);
        });

        builder.setNeutralButton("Just Save", (dialog, which) -> {
            initiateSaveSequence(fileName, content, null);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void performSummarization(String defaultTitle, String content) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Generating Summary...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        aiSummarizer.summarizeText(content, new AISummarizer.SummaryCallback() {
            @Override
            public void onSuccess(String summary) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    // Show summary result to user
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("AI Summary Generated")
                            .setMessage(summary)
                            .setPositiveButton("Save Note", (dialog, which) -> {
                                initiateSaveSequence(defaultTitle, content, summary);
                            })
                            .setNegativeButton("Discard", null)
                            .show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Summary failed: " + error, Toast.LENGTH_LONG).show();
                    // Offer to save without summary
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Error")
                            .setMessage("Could not generate summary. Save note anyway?")
                            .setPositiveButton("Yes", (d, w) -> initiateSaveSequence(defaultTitle, content, null))
                            .setNegativeButton("No", null)
                            .show();
                });
            }
        });
    }

    // Since we are in MainActivity, we need to ask WHERE to save the note
    private void initiateSaveSequence(String defaultTitle, String content, @Nullable String summary) {
        // 1. Ask for Title
        final EditText input = new EditText(this);
        input.setText(defaultTitle);
        input.setSelectAllOnFocus(true);

        new AlertDialog.Builder(this)
                .setTitle("Name Your Note")
                .setView(input)
                .setPositiveButton("Next", (dialog, which) -> {
                    String title = input.getText().toString().trim();
                    if (title.isEmpty()) title = "Untitled Upload";
                    selectGroupForUploadedNote(title, content, summary);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void selectGroupForUploadedNote(String title, String content, String summary) {
        List<Group> groups = dataManager.getAllGroups();
        if (groups.isEmpty()) {
            Toast.makeText(this, "No groups found. Please create a group first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = new String[groups.size()];
        for (int i = 0; i < groups.size(); i++) {
            names[i] = groups.get(i).getName();
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Group")
                .setItems(names, (dialog, which) -> {
                    Group selectedGroup = groups.get(which);
                    selectCategoryForUploadedNote(selectedGroup, title, content, summary);
                })
                .show();
    }

    private void selectCategoryForUploadedNote(Group group, String title, String content, String summary) {
        List<Category> categories = dataManager.getCategoriesByGroup(group.getId());
        if (categories.isEmpty()) {
            Toast.makeText(this, "No categories in this group. Please create one.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = new String[categories.size()];
        for (int i = 0; i < categories.size(); i++) {
            names[i] = categories.get(i).getName();
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Category")
                .setItems(names, (dialog, which) -> {
                    Category selectedCategory = categories.get(which);
                    saveFinalNote(selectedCategory, title, content, summary);
                })
                .show();
    }

    private void saveFinalNote(Category category, String title, String content, String summary) {
        // 1. Save the original note in the selected category (e.g., "Project Alpha")
        String noteId = "note_" + UUID.randomUUID().toString();
        Note newNote = new Note(noteId, title, content, category.getId());
        dataManager.addNote(newNote);

        // 2. If a summary was generated, save it to a "Summaries" folder in the SAME group
        if (summary != null) {
            saveSummaryToFolder(category.getGroupId(), title, summary);
            Toast.makeText(this, "Note saved to '" + category.getName() + "' & Summary saved to 'Summaries'", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Note Saved Successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Helper to find or create the "Summaries" category in the given group and save the summary note there.
     */
    private void saveSummaryToFolder(String groupId, String originalTitle, String summaryContent) {
        // Check if "Summaries" category exists in this group
        Category summariesCategory = dataManager.findCategoryByName(groupId, "Summaries");

        // If not, create it
        if (summariesCategory == null) {
            summariesCategory = dataManager.createCategory(groupId, "Summaries", "AI Generated Study Guides");
        }

        // Create the new note in the Summaries category
        String summaryTitle = "Study Guide: " + originalTitle;
        dataManager.createNote(summariesCategory.getId(), summaryTitle, summaryContent);
    }

    // --- NAVIGATION & HELPERS ---

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_export) {
            if (exportCsvLauncher != null) {
                // Defines the default filename for the export
                exportCsvLauncher.launch(getString(R.string.export_file_name));
            }
        } else if (id == R.id.nav_about) {
            new AlertDialog.Builder(this)
                    .setTitle("About NoteAI")
                    .setMessage(getString(R.string.app_name) + " allows you to organize your class notes and use AI to summarize PDF documents and text.")
                    .setPositiveButton("OK", null)
                    .show();
        }

        drawerLayout.closeDrawer(GravityCompat.END);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressLint("Range")
    private String getFileNameFromUri(Uri uri) {
        String fileName = "Unknown";
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }

    private void showCreateOptionsDialog() {
        try {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_options, null);
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .create();

            View createNoteOption = dialogView.findViewById(R.id.option_create_note);
            View createGroupOption = dialogView.findViewById(R.id.option_create_group);
            View addToGroupOption = dialogView.findViewById(R.id.option_add_to_group);

            if (createNoteOption != null) {
                createNoteOption.setOnClickListener(v -> {
                    dialog.dismiss();
                    showSelectGroupForNoteDialog();
                });
            }

            if (createGroupOption != null) {
                createGroupOption.setOnClickListener(v -> {
                    dialog.dismiss();
                    showCreateGroupDialog();
                });
            }

            if (addToGroupOption != null) {
                addToGroupOption.setOnClickListener(v -> {
                    dialog.dismiss();
                    showAddToGroupDialog();
                });
            }

            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error showing dialog: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showCreateGroupDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_group, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        TextInputEditText nameInput = dialogView.findViewById(R.id.group_name_input);
        TextInputEditText descriptionInput = dialogView.findViewById(R.id.group_description_input);
        android.widget.Button btnCreate = dialogView.findViewById(R.id.btn_create_group);
        android.widget.Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
            String description = descriptionInput.getText() != null ? descriptionInput.getText().toString().trim() : "";

            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, getString(R.string.please_enter_group_name), Toast.LENGTH_SHORT).show();
                return;
            }

            String groupId = "group_" + UUID.randomUUID().toString();
            Group newGroup = new Group(groupId, name, description, android.R.drawable.ic_menu_agenda);
            newGroup.setCustom(true);
            dataManager.addGroup(newGroup);

            Toast.makeText(this, getString(R.string.group_created), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showAddToGroupDialog() {
        List<Group> groups = dataManager.getAllGroups();

        if (groups.isEmpty()) {
            Toast.makeText(this, "Please create a group first", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.select_group));

        String[] groupNames = new String[groups.size()];
        for (int i = 0; i < groups.size(); i++) {
            groupNames[i] = groups.get(i).getName();
        }

        builder.setItems(groupNames, (dialog, which) -> {
            Group selectedGroup = groups.get(which);
            showAddToGroupOptionsDialog(selectedGroup);
        });

        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showSelectGroupForNoteDialog() {
        List<Group> groups = dataManager.getAllGroups();
        if (groups.isEmpty()) {
            Toast.makeText(this, "Please create a group first", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = new String[groups.size()];
        for (int i = 0; i < groups.size(); i++) {
            names[i] = groups.get(i).getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.select_group));
        builder.setItems(names, (dialog, which) -> {
            Group selected = groups.get(which);
            showSelectCategoryForNoteDialog(selected, true);
        });
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.show();
    }

    private void showAddToGroupOptionsDialog(Group group) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.add_to_group) + ": " + group.getName());

        String[] options = {
                getString(R.string.create_category_in_group),
                getString(R.string.create_note)
        };

        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                showCreateCategoryDialog(group);
            } else {
                showSelectCategoryForNoteDialog(group, false);
            }
        });

        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showCreateCategoryDialog(Group group) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.create_category_in_group));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 32);

        EditText nameInput = new EditText(this);
        nameInput.setHint(getString(R.string.category_name_hint));
        layout.addView(nameInput);

        builder.setView(layout);
        builder.setPositiveButton(getString(R.string.create), (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, getString(R.string.please_enter_category_name), Toast.LENGTH_SHORT).show();
                return;
            }

            String categoryId = "cat_" + UUID.randomUUID().toString();
            Category newCategory = new Category(categoryId, name, "", group.getId());
            dataManager.addCategory(newCategory);

            Toast.makeText(this, getString(R.string.category_created), Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.show();
    }

    private void showSelectCategoryForNoteDialog(Group group, boolean launchEditor) {
        List<Category> categories = dataManager.getCategoriesByGroup(group.getId());

        if (categories.isEmpty()) {
            Toast.makeText(this, "Please create a category first", Toast.LENGTH_SHORT).show();
            showCreateCategoryDialog(group);
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.select_category));

        String[] categoryNames = new String[categories.size()];
        for (int i = 0; i < categories.size(); i++) {
            categoryNames[i] = categories.get(i).getName();
        }

        builder.setItems(categoryNames, (dialog, which) -> {
            Category selectedCategory = categories.get(which);
            if (launchEditor) {
                openNoteEditor(selectedCategory.getId());
            } else {
                showCreateNoteDialog(selectedCategory);
            }
        });

        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.show();
    }

    private void showCreateNoteDialog(Category category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.add_note_to_category));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 32);

        EditText titleInput = new EditText(this);
        titleInput.setHint(getString(R.string.note_title_hint));
        layout.addView(titleInput);

        EditText contentInput = new EditText(this);
        contentInput.setHint(getString(R.string.note_content_hint));
        contentInput.setMinLines(3);
        layout.addView(contentInput);

        builder.setView(layout);
        builder.setPositiveButton(getString(R.string.create), (dialog, which) -> {
            String title = titleInput.getText().toString().trim();
            String content = contentInput.getText().toString().trim();

            if (TextUtils.isEmpty(title)) {
                Toast.makeText(this, getString(R.string.please_enter_note_title), Toast.LENGTH_SHORT).show();
                return;
            }

            String noteId = "note_" + UUID.randomUUID().toString();
            Note newNote = new Note(noteId, title, content, category.getId());
            dataManager.addNote(newNote);

            Toast.makeText(this, getString(R.string.note_created), Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.show();
    }

    private void exportNotesToCsv(@NonNull Uri uri) {
        try {
            FileUtil.exportNotesToCsv(this, uri, dataManager.getAllGroups());
            Toast.makeText(this, R.string.export_success, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("MainActivity", "CSV export failed", e);
            Toast.makeText(this, getString(R.string.export_failed) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void openNoteEditor(String categoryId) {
        Intent intent = new Intent(this, NoteDetailActivity.class);
        intent.putExtra(NoteDetailActivity.EXTRA_CATEGORY_ID, categoryId);
        startActivity(intent);
    }
}