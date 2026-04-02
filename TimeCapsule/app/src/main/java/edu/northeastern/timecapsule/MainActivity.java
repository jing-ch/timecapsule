package edu.northeastern.timecapsule;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

import edu.northeastern.timecapsule.adapter.CapsuleAdapter;
import edu.northeastern.timecapsule.auth.LoginActivity;
import edu.northeastern.timecapsule.model.Capsule;
import edu.northeastern.timecapsule.ui.create.CreateStepOneActivity;
import edu.northeastern.timecapsule.ui.read.CapsuleDetailActivity;
import edu.northeastern.timecapsule.viewmodel.CapsuleListViewModel;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_CAPSULE_ID = "extra_capsule_id";

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private CapsuleListViewModel viewModel;
    private CapsuleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            goToLogin();
            return;
        }

        syncFcmToken(currentUser);

        Button btnCreateCapsule = findViewById(R.id.btnCreateCapsule);
        EditText etSearch = findViewById(R.id.etSearch);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewCapsules);

        viewModel = new ViewModelProvider(this).get(CapsuleListViewModel.class);

        adapter = new CapsuleAdapter(new CapsuleAdapter.OnCapsuleClickListener() {
            @Override
            public void onCapsuleClick(Capsule capsule) {
                Intent intent = new Intent(MainActivity.this, CapsuleDetailActivity.class);
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

        if (viewModel.getCapsules() != null) {
            viewModel.getCapsules().observe(this, capsules -> {
                viewModel.setOriginalCapsules(capsules);
            });
        }

        viewModel.getFilteredCapsules().observe(this, capsules -> {
            adapter.setCapsules(capsules);
        });

        viewModel.errorMessage.observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.filterByTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnCreateCapsule.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CreateStepOneActivity.class));
        });

        handleNotificationIntent(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            auth.signOut();
            goToLogin();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNotificationIntent(intent);

        if (intent != null && intent.getBooleanExtra("refresh", false)) {
            recreate();
        }
    }

    private void syncFcmToken(FirebaseUser user) {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    if (token == null || token.isEmpty()) {
                        return;
                    }

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("fcmToken", token);

                    firestore.collection("users")
                            .document(user.getUid())
                            .set(updates, SetOptions.merge());
                });
    }

    private void handleNotificationIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        String capsuleId = intent.getStringExtra(EXTRA_CAPSULE_ID);
        if (capsuleId == null || capsuleId.isEmpty()) {
            capsuleId = intent.getStringExtra("capsuleId");
        }

        if (capsuleId == null || capsuleId.isEmpty()) {
            return;
        }

        Intent detailIntent = new Intent(this, CapsuleDetailActivity.class);
        detailIntent.putExtra("capsuleId", capsuleId);
        startActivity(detailIntent);
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

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}