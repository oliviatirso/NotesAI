package com.example.notesai;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesai.data.GroupDataManager;
import com.example.notesai.model.Group;
import com.example.notesai.ui.GroupAdapter;

import java.util.List;

public class GroupsActivity extends AppCompatActivity {

    private ImageButton backButton;
    private RecyclerView groupsRecyclerView;
    private GroupAdapter groupAdapter;
    private GroupDataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        // Initialize views
        backButton = findViewById(R.id.back_button);
        groupsRecyclerView = findViewById(R.id.groups_recycler_view);
        TextView screenTitle = findViewById(R.id.screen_title);

        // Initialize data manager
        dataManager = GroupDataManager.getInstance();

        // Set up back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Set up RecyclerView
        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Get groups and set up adapter
        List<Group> groups = dataManager.getAllGroups();
        groupAdapter = new GroupAdapter(groups, new GroupAdapter.OnGroupClickListener() {
            @Override
            public void onGroupClick(Group group) {
                // Navigate to group detail
                Intent intent = new Intent(GroupsActivity.this, GroupDetailActivity.class);
                intent.putExtra("group_id", group.getId());
                intent.putExtra("group_name", group.getName());
                startActivity(intent);
            }
        });
        
        groupsRecyclerView.setAdapter(groupAdapter);
    }
}

