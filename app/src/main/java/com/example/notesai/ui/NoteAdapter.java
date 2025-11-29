package com.example.notesai.ui;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesai.R;
import com.example.notesai.model.Note;

import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    public interface NoteActionListener {
        void onNoteClicked(Note note);
    }

    private final NoteActionListener listener;
    private final List<Note> notes = new ArrayList<>();

    public NoteAdapter(NoteActionListener listener) {
        this.listener = listener;
    }

    public void setNotes(List<Note> newNotes) {
        notes.clear();
        if (newNotes != null) {
            notes.addAll(newNotes);
        }
        notifyDataSetChanged();
    }

    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < 0 || toPosition < 0 || fromPosition >= notes.size() || toPosition >= notes.size()) {
            return;
        }
        Note note = notes.remove(fromPosition);
        notes.add(toPosition, note);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void removeAt(int position) {
        if (position < 0 || position >= notes.size()) return;
        notes.remove(position);
        notifyItemRemoved(position);
    }

    public Note getNoteAt(int position) {
        if (position < 0 || position >= notes.size()) return null;
        return notes.get(position);
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.bind(note, listener);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleView;
        private final TextView previewView;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.note_title);
            previewView = itemView.findViewById(R.id.note_preview);
        }

        void bind(Note note, NoteActionListener listener) {
            titleView.setText(!TextUtils.isEmpty(note.getTitle()) ? note.getTitle() : itemView.getContext().getString(R.string.note_title));
            String content = note.getContent();
            if (TextUtils.isEmpty(content)) {
                previewView.setVisibility(View.GONE);
            } else {
                previewView.setVisibility(View.VISIBLE);
                previewView.setText(content.length() > 140 ? content.substring(0, 140) + "…" : content);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNoteClicked(note);
                }
            });
        }
    }
}

