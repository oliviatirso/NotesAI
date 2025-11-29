package com.example.notesai;

// --- NEW IMPORTS ---
import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
// --- END NEW IMPORTS ---

import android.app.AlertDialog;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;

import com.example.notesai.data.GroupDataManager;
import com.example.notesai.model.Category;
import com.example.notesai.model.Group;
import com.example.notesai.model.Note;

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
    
    // FAB and Data Manager
    private FloatingActionButton fabAdd;
    private GroupDataManager dataManager;
    
    // Launcher for PDF picker
    private ActivityResultLauncher<String[]> pdfPickerLauncher;
    // Launchers for CSV export/import
    private ActivityResultLauncher<String> exportCsvLauncher;
    private ActivityResultLauncher<String[]> importCsvLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d("MainActivity", "onCreate started");
        
        try {
            Log.d("MainActivity", "Initializing ActivityResultLaunchers...");

            pdfPickerLauncher = registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    uri -> {
                        if (uri != null) {
                            String fileName = getFileNameFromUri(uri);
                            Toast.makeText(MainActivity.this, "Selected file: " + fileName, Toast.LENGTH_LONG).show();
                            // TODO: Pass this 'uri' to your summarization logic
                        } else {
                            Toast.makeText(MainActivity.this, "No file selected", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

            exportCsvLauncher = registerForActivityResult(
                    new ActivityResultContracts.CreateDocument("text/csv"),
                    uri -> {
                        if (uri != null) {
                            exportNotesToCsv(uri);
                        }
                    }
            );

            importCsvLauncher = registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    uri -> {
                        if (uri != null) {
                            importNotesFromCsv(uri);
                        }
                    }
            );

            Log.d("MainActivity", "ActivityResultLaunchers initialized");
            
            Log.d("MainActivity", "Loading layout...");
            setContentView(R.layout.activity_main);
            Log.d("MainActivity", "Layout loaded successfully");

            // Initialize original views
            menuButton = findViewById(R.id.menu_button);
            uploadButton = findViewById(R.id.upload_button);
            classNotesCard = findViewById(R.id.class_notes_card);

            // Initialize Navigation Drawer views
            drawerLayout = findViewById(R.id.drawer_layout);
            navigationView = findViewById(R.id.nav_view);
            
            Log.d("MainActivity", "Initializing views...");
            // Initialize FAB and Data Manager
            fabAdd = findViewById(R.id.fab_add);
            
            // Initialize data manager safely
            Log.d("MainActivity", "Initializing GroupDataManager...");
            dataManager = GroupDataManager.getInstance();
            Log.d("MainActivity", "GroupDataManager initialized successfully");

            // --- Set up Navigation Drawer ---
            if (navigationView != null) {
                navigationView.setNavigationItemSelectedListener(this);
            }
            
            // --- Set up FAB ---
            if (fabAdd != null) {
                fabAdd.setOnClickListener(v -> showCreateOptionsDialog());
            }

            // --- Set up original click listeners ---

            // Update menuButton to open the drawer
            if (menuButton != null && drawerLayout != null) {
                menuButton.setOnClickListener(v -> {
                    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    } else {
                        drawerLayout.openDrawer(GravityCompat.START);
                    }
                });
            }

            // --- UPDATED ---
            // Update uploadButton to launch the file picker
            if (uploadButton != null && pdfPickerLauncher != null) {
                uploadButton.setOnClickListener(v -> {
                    // Define the MIME types for PDF, DOCX, and PPTX
                    String[] mimeTypes = {
                            "application/pdf",
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
                            "application/vnd.openxmlformats-officedocument.presentationml.presentation" // .pptx
                    };

                    // Launch the file picker
                    pdfPickerLauncher.launch(mimeTypes);
                });
            }
            // --- END UPDATED ---

            if (classNotesCard != null) {
                classNotesCard.setOnClickListener(v -> {
                    // Navigate to Groups screen
                    Intent intent = new Intent(MainActivity.this, GroupsActivity.class);
                    startActivity(intent);
                });
            }
        
        } catch (Exception e) {
            e.printStackTrace();
            // Log full stack trace
            Log.e("MainActivity", "Error in onCreate", e);
            
            // Show detailed error
            String errorMsg = "Error: " + e.getClass().getSimpleName() + "\n" + e.getMessage();
            if (e.getCause() != null) {
                errorMsg += "\nCause: " + e.getCause().getMessage();
            }
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            
            // Try to show a dialog with error details for debugging
            new AlertDialog.Builder(this)
                .setTitle("App Error")
                .setMessage("Error: " + e.getClass().getSimpleName() + "\n\n" + e.getMessage() + "\n\nCheck Logcat for details.")
                .setPositiveButton("OK", null)
                .show();
        }
    }

    // Handle clicks for items in the navigation drawer
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_export) {
            if (exportCsvLauncher != null) {
                exportCsvLauncher.launch(getString(R.string.export_file_name));
            }
        } else if (id == R.id.nav_import) {
            if (importCsvLauncher != null) {
                importCsvLauncher.launch(new String[]{"text/*", "application/csv", "text/comma-separated-values"});
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // Handle the phone's back button
    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // --- NEW HELPER METHOD ---
    // This helper method gets the file name from the Content URI
    @SuppressLint("Range")
    private String getFileNameFromUri(Uri uri) {
        String fileName = "Unknown";
        // Use a Cursor to query the file's metadata
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                // Get the display name column
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }
    
    // --- CREATE OPTIONS DIALOG ---
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
    
    // --- CREATE GROUP DIALOG ---
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
            
            // Create new group
            String groupId = "group_" + UUID.randomUUID().toString();
            Group newGroup = new Group(groupId, name, description, android.R.drawable.ic_menu_agenda);
            newGroup.setCustom(true);
            dataManager.addGroup(newGroup);
            
            Toast.makeText(this, getString(R.string.group_created), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            
            // Refresh groups screen if it's open, or navigate to it
            // For now, just show success message
        });
        
        dialog.show();
    }
    
    // --- ADD TO GROUP DIALOG ---
    private void showAddToGroupDialog() {
        List<Group> groups = dataManager.getAllGroups();
        
        if (groups.isEmpty()) {
            Toast.makeText(this, "Please create a group first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create dialog with group selection
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
    
    // --- ADD TO GROUP OPTIONS (Category or Note) ---
    private void showAddToGroupOptionsDialog(Group group) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.add_to_group) + ": " + group.getName());
        
        String[] options = {
            getString(R.string.create_category_in_group),
            getString(R.string.create_note)
        };
        
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Create Category
                showCreateCategoryDialog(group);
            } else {
                showSelectCategoryForNoteDialog(group, false);
            }
        });
        
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
        builder.show();
    }
    
    // --- CREATE CATEGORY DIALOG ---
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
    
    // --- SELECT CATEGORY FOR NOTE DIALOG ---
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
    
    // --- CREATE NOTE DIALOG ---
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

    private void importNotesFromCsv(@NonNull Uri uri) {
        try {
            FileUtil.importNotesFromCsv(this, uri, dataManager);
            Toast.makeText(this, R.string.import_success, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("MainActivity", "CSV import failed", e);
            Toast.makeText(this, getString(R.string.import_failed) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void openNoteEditor(String categoryId) {
        Intent intent = new Intent(this, NoteDetailActivity.class);
        intent.putExtra(NoteDetailActivity.EXTRA_CATEGORY_ID, categoryId);
        startActivity(intent);
    }
}