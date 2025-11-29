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
import com.example.notesai.model.Category;
import com.example.notesai.model.Group;
import com.example.notesai.ui.CategoryAdapter;

import java.util.List;

public class GroupDetailActivity extends AppCompatActivity {

    private ImageButton backButton;
    private RecyclerView categoriesRecyclerView;
    private CategoryAdapter categoryAdapter;
    private GroupDataManager dataManager;
    private String groupId;
    private String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        // Get group info from intent
        groupId = getIntent().getStringExtra("group_id");
        groupName = getIntent().getStringExtra("group_name");

        // Initialize views
        backButton = findViewById(R.id.back_button);
        categoriesRecyclerView = findViewById(R.id.categories_recycler_view);
        TextView groupTitle = findViewById(R.id.group_title);

        // Set group title
        if (groupName != null) {
            groupTitle.setText(groupName);
        }

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
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get categories for this group
        if (groupId != null) {
            List<Category> categories = dataManager.getCategoriesByGroup(groupId);
            categoryAdapter = new CategoryAdapter(categories, new CategoryAdapter.OnCategoryClickListener() {
                @Override
                public void onCategoryClick(Category category) {
                    Intent intent = new Intent(GroupDetailActivity.this, NotesListActivity.class);
                    intent.putExtra(NotesListActivity.EXTRA_CATEGORY_ID, category.getId());
                    intent.putExtra(NotesListActivity.EXTRA_CATEGORY_NAME, category.getName());
                    startActivity(intent);
                }
            });

            categoriesRecyclerView.setAdapter(categoryAdapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (groupId != null && categoryAdapter != null) {
            List<Category> categories = dataManager.getCategoriesByGroup(groupId);
            categoryAdapter.setCategories(categories);
        }
    }
}

