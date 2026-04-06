package edu.northeastern.timecapsule;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.chip.ChipGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
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
    private final Handler countdownHandler = new Handler(Looper.getMainLooper());
    private final Runnable countdownRunnable = new Runnable() {
        @Override
        public void run() {
            adapter.notifyDataSetChanged();
            countdownHandler.postDelayed(this, 30_000);
        }
    };
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                // No-op for now. If denied, the app simply won't display notifications.
            });

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

        requestNotificationPermissionIfNeeded();
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

        ChipGroup chipGroupFilter = findViewById(R.id.chipGroupFilter);
        chipGroupFilter.check(R.id.chipAll);
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipAll) viewModel.filterByStatus("all");
            else if (id == R.id.chipLocked) viewModel.filterByStatus("locked");
            else if (id == R.id.chipUnlocked) viewModel.filterByStatus("unlocked");
        });

        btnCreateCapsule.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CreateStepOneActivity.class));
        });

        handleNotificationIntent(getIntent());

        countdownHandler.postDelayed(countdownRunnable, 30_000);
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

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        countdownHandler.removeCallbacks(countdownRunnable);
    }

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}