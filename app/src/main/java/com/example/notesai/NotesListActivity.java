package com.example.notesai;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesai.data.GroupDataManager;
import com.example.notesai.model.Category;
import com.example.notesai.model.Note;
import com.example.notesai.ui.NoteAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collections;
import java.util.List;

public class NotesListActivity extends AppCompatActivity implements NoteAdapter.NoteActionListener {

    public static final String EXTRA_CATEGORY_ID = "extra_category_id";
    public static final String EXTRA_CATEGORY_NAME = "extra_category_name";

    private GroupDataManager dataManager;
    private RecyclerView recyclerView;
    private TextView emptyView;
    private NoteAdapter noteAdapter;
    private String categoryId;
    private Category category;

    private final ActivityResultLauncher<Intent> noteEditorLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> refreshNotes());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_list);

        categoryId = getIntent().getStringExtra(EXTRA_CATEGORY_ID);
        String categoryName = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);

        dataManager = GroupDataManager.getInstance();
        category = dataManager.getCategoryById(categoryId);
        if (category == null) {
            Toast.makeText(this, R.string.category_missing, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.notes_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(categoryName != null ? categoryName : getString(R.string.notes_list_title));
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        TextView subtitle = findViewById(R.id.notes_subtitle);
        if (category != null && category.getDescription() != null && !category.getDescription().isEmpty()) {
            subtitle.setText(category.getDescription());
            subtitle.setVisibility(View.VISIBLE);
        } else {
            subtitle.setVisibility(View.GONE);
        }

        recyclerView = findViewById(R.id.notes_recycler_view);
        emptyView = findViewById(R.id.notes_empty_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        noteAdapter = new NoteAdapter(this);
        recyclerView.setAdapter(noteAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT
        ) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                int fromPos = viewHolder.getBindingAdapterPosition();
                int toPos = target.getBindingAdapterPosition();
                if (fromPos == RecyclerView.NO_POSITION || toPos == RecyclerView.NO_POSITION) {
                    return false;
                }
                noteAdapter.onItemMove(fromPos, toPos);
                dataManager.moveNote(categoryId, fromPos, toPos);
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                Note note = noteAdapter.getNoteAt(position);
                if (note != null) {
                    dataManager.deleteNote(note.getId());
                }
                noteAdapter.removeAt(position);
                toggleEmptyState();
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

        FloatingActionButton fab = findViewById(R.id.fab_add_note);
        fab.setOnClickListener(v -> openNoteEditor(null));

        refreshNotes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshNotes();
    }

    private void refreshNotes() {
        if (categoryId == null) return;
        List<Note> notes = dataManager.getNotesByCategory(categoryId);
        noteAdapter.setNotes(notes);
        toggleEmptyState();
    }

    private void toggleEmptyState() {
        if (noteAdapter.getItemCount() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void openNoteEditor(@Nullable Note note) {
        Intent intent = new Intent(this, NoteDetailActivity.class);
        intent.putExtra(NoteDetailActivity.EXTRA_CATEGORY_ID, categoryId);
        if (note != null) {
            intent.putExtra(NoteDetailActivity.EXTRA_NOTE_ID, note.getId());
        }
        noteEditorLauncher.launch(intent);
    }

    @Override
    public void onNoteClicked(Note note) {
        openNoteEditor(note);
    }
}

