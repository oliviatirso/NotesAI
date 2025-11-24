package com.example.notesai;

import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ClassNotesActivity extends AppCompatActivity {

    private ImageButton backButton;
    private EditText notesInput;
    private Button summarizeButton;
    private TextView summaryOutput;
    private Button saveSummaryButton;
    private ScrollView summaryScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_notes);

        // Initialize views
        backButton = findViewById(R.id.back_button);
        notesInput = findViewById(R.id.notes_input);
        summarizeButton = findViewById(R.id.summarize_button);
        summaryOutput = findViewById(R.id.summary_output);
        saveSummaryButton = findViewById(R.id.save_summary_button);
        summaryScrollView = findViewById(R.id.summary_scroll_view);

        // Set up click listeners
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to previous activity
            }
        });

        summarizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String notes = notesInput.getText().toString().trim();
                
                if (TextUtils.isEmpty(notes)) {
                    Toast.makeText(ClassNotesActivity.this, 
                        getString(R.string.notes_empty_message), 
                        Toast.LENGTH_SHORT).show();
                    return;
                }

                // TODO: Implement actual summarization logic (API call, ML model, etc.)
                summarizeNotes(notes);
            }
        });

        saveSummaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String summary = summaryOutput.getText().toString();
                
                if (TextUtils.isEmpty(summary) || 
                    summary.equals(getString(R.string.summary_output_hint))) {
                    Toast.makeText(ClassNotesActivity.this, 
                        "No summary to save", 
                        Toast.LENGTH_SHORT).show();
                    return;
                }

                // TODO: Implement save functionality (save to file, database, etc.)
                Toast.makeText(ClassNotesActivity.this, 
                    "Summary saved!", 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void summarizeNotes(String notes) {
        // Show loading state
        summarizeButton.setEnabled(false);
        summarizeButton.setText(getString(R.string.summarizing));
        summaryOutput.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        summaryOutput.setText(getString(R.string.summarizing));

        // TODO: Replace this placeholder with actual summarization logic
        // This is a placeholder that simulates summarization
        // In the real implementation, you would:
        // 1. Call an API (OpenAI, Gemini, etc.)
        // 2. Use an on-device ML model
        // 3. Process the notes and generate summary
        
        // Simulate async operation
        final String notesFinal = notes; // Make final for inner class
        notesInput.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Placeholder summary (replace with actual API/model call)
                String placeholderSummary = generatePlaceholderSummary(notesFinal);
                
                // Update UI
                summaryOutput.setTextColor(ContextCompat.getColor(ClassNotesActivity.this, R.color.text_primary));
                summaryOutput.setText(placeholderSummary);
                
                // Show save button
                saveSummaryButton.setVisibility(View.VISIBLE);
                
                // Reset button
                summarizeButton.setEnabled(true);
                summarizeButton.setText(getString(R.string.summarize_button));
                
                // Scroll to top of summary
                summaryScrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        summaryScrollView.fullScroll(View.FOCUS_UP);
                    }
                });
            }
        }, 1000); // Simulate 1 second delay
    }

    private String generatePlaceholderSummary(String notes) {
        // This is a simple placeholder - replace with actual summarization
        if (notes == null || notes.trim().isEmpty()) {
            return "Summary will be generated here. This is a placeholder implementation.";
        }
        
        if (notes.length() <= 200) {
            return notes; // Return original if too short
        }
        
        // Simple placeholder: take first few sentences
        String[] sentences = notes.split("[.!?]+");
        StringBuilder summary = new StringBuilder();
        int maxSentences = Math.min(3, sentences.length);
        
        for (int i = 0; i < maxSentences; i++) {
            if (sentences[i] != null && !sentences[i].trim().isEmpty()) {
                summary.append(sentences[i].trim());
                if (i < maxSentences - 1) {
                    summary.append(". ");
                } else {
                    summary.append(".");
                }
            }
        }
        
        return summary.length() > 0 ? summary.toString() : 
            "Summary will be generated here. This is a placeholder implementation.";
    }
}

