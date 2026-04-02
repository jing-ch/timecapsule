package edu.northeastern.timecapsule.ui.read;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.northeastern.timecapsule.R;
import edu.northeastern.timecapsule.adapter.CapsuleAdapter;
import edu.northeastern.timecapsule.model.Capsule;
import edu.northeastern.timecapsule.viewmodel.CapsuleListViewModel;

public class CapsuleListActivity extends AppCompatActivity {

    private CapsuleListViewModel viewModel;
    private CapsuleAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capsule_list);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewCapsules);
        EditText searchEditText = findViewById(R.id.etSearch);

        viewModel = new ViewModelProvider(this).get(CapsuleListViewModel.class);

        adapter = new CapsuleAdapter(new CapsuleAdapter.OnCapsuleClickListener() {
            @Override
            public void onCapsuleClick(Capsule capsule) {
                Intent intent = new Intent(CapsuleListActivity.this, CapsuleDetailActivity.class);
                intent.putExtra("capsuleId", capsule.getCapsuleId());
                startActivity(intent);
            }

            @Override
            public void onCapsuleLongClick(Capsule capsule) {
                showDeleteDialog(capsule);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        viewModel.loadCapsules();

        viewModel.getCapsules().observe(this, capsules -> {
            viewModel.setOriginalCapsules(capsules);
        });

        viewModel.getFilteredCapsules().observe(this, capsules -> {
            adapter.setCapsules(capsules);
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.filterByTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void showDeleteDialog(Capsule capsule) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Capsule")
                .setMessage("Are you sure you want to delete this capsule?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteCapsule(capsule.getCapsuleId()).observe(this, success -> {
                        if (Boolean.TRUE.equals(success)) {
                            Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}