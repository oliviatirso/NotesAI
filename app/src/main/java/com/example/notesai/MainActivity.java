package com.example.notesai;

// --- NEW IMPORTS ---
import android.annotation.SuppressLint;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.app.AlertDialog;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ImageButton menuButton;
    private View uploadButton;
    private View classNotesCard;
    private AISummarizer aiSummarizer;

    // Views for Navigation Drawer
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private final ActivityResultLauncher<String[]> openDocumentLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    String fileName = getFileNameFromUri(uri);
                    Toast.makeText(MainActivity.this, "Processing " + fileName + "...", Toast.LENGTH_SHORT).show();

                    // 1. Read file content
                    // NOTE: For PDFs/DOCX, you need specific parsing logic here.
                    // This simple reader works best for text files for now.
                    String fileContent = FileUtil.readTextFromUri(this, uri);

                    // 2. Send to AI for summarization
                    aiSummarizer.summarizeText(fileContent, new AISummarizer.SummaryCallback() {
                        @Override
                        public void onSuccess(String summary) {
                            // 3. Update UI on the main thread
                            runOnUiThread(() -> {
                                showSummaryDialog(summary);
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                            });
                        }
                    });

                } else {
                    Toast.makeText(MainActivity.this, "No file selected", Toast.LENGTH_SHORT).show();
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize original views
        menuButton = findViewById(R.id.menu_button);
        uploadButton = findViewById(R.id.upload_button);
        classNotesCard = findViewById(R.id.class_notes_card);
        aiSummarizer = new AISummarizer();

        // Initialize Navigation Drawer views
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // --- Set up Navigation Drawer ---
        navigationView.setNavigationItemSelectedListener(this);

        // --- Set up original click listeners ---

        // Update menuButton to open the drawer
        menuButton.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END);
            } else {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        // --- UPDATED ---
        // Update uploadButton to launch the file picker
        uploadButton.setOnClickListener(v -> {
            // Define the MIME types for PDF, DOCX, and PPTX
            String[] mimeTypes = {
                    "application/pdf",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation" // .pptx
            };

            // Launch the file picker
            openDocumentLauncher.launch(mimeTypes);
        });
        // --- END UPDATED ---

        classNotesCard.setOnClickListener(v -> {
            // TODO: Implement class notes detail view
            Toast.makeText(MainActivity.this, "Class Notes clicked", Toast.LENGTH_SHORT).show();
        });
    }

    private void showSummaryDialog(String summary) {
        new AlertDialog.Builder(this)
                .setTitle("AI Summary")
                .setMessage(summary)
                .setPositiveButton("OK", null)
                .show();
    }

    // Handle clicks for items in the navigation drawer
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_about) {
            Toast.makeText(this, "About clicked", Toast.LENGTH_SHORT).show();
        }

        drawerLayout.closeDrawer(GravityCompat.END);
        return true;
    }

    // Handle the phone's back button
    public void onBackPressedDispatcher() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            super.getOnBackPressedDispatcher();
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
}