package edu.northeastern.timecapsule;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

import edu.northeastern.timecapsule.auth.LoginActivity;
import edu.northeastern.timecapsule.ui.create.CreateStepOneActivity;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_CAPSULE_ID = "extra_capsule_id";

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Session guard: redirect to login if not authenticated
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            goToLogin();
            return;
        }

        syncFcmToken(currentUser);
        handleNotificationIntent(getIntent());

        // TODO: B will replace this with the capsule list fragment

        Button btnCreateCapsule = findViewById(R.id.btnCreateCapsule);

        btnCreateCapsule.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CreateStepOneActivity.class));
        });
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
            return;
        }

        // Placeholder for teammate B's detail page navigation.
    }

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
