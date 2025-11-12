package com.example.notesai;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private ImageButton menuButton;
    private View uploadButton;
    private View classNotesCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        menuButton = findViewById(R.id.menu_button);
        uploadButton = findViewById(R.id.upload_button);
        classNotesCard = findViewById(R.id.class_notes_card);

        // Set up click listeners
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement menu drawer/navigation
                Toast.makeText(MainActivity.this, "Menu clicked", Toast.LENGTH_SHORT).show();
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement PDF upload functionality
                Toast.makeText(MainActivity.this, "Upload PDF clicked", Toast.LENGTH_SHORT).show();
            }
        });

        classNotesCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement class notes detail view
                Toast.makeText(MainActivity.this, "Class Notes clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }
}