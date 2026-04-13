package edu.northeastern.timecapsule.ui.account;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import edu.northeastern.timecapsule.R;
import edu.northeastern.timecapsule.databinding.ActivityMyAccountBinding;

public class MyAccountActivity extends AppCompatActivity {

    private ActivityMyAccountBinding binding;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        binding.btnBack.setOnClickListener(v -> finish());
        binding.etEmail.setText(user.getEmail());

        loadNickname(user.getUid());

        binding.btnSave.setOnClickListener(v -> {
            String nickname = binding.etNickname.getText() != null
                    ? binding.etNickname.getText().toString().trim()
                    : "";

            if (!validateNickname(nickname)) return;

            binding.btnSave.setEnabled(false);

            Map<String, Object> updates = new HashMap<>();
            updates.put("displayName", nickname);

            firestore.collection("users")
                    .document(user.getUid())
                    .set(updates, SetOptions.merge())
                    .addOnSuccessListener(unused -> {
                        binding.btnSave.setEnabled(true);
                        Toast.makeText(this, "Nickname updated", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        binding.btnSave.setEnabled(true);
                        Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void loadNickname(String uid) {
        firestore.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("displayName");
                        if (name != null) {
                            binding.etNickname.setText(name);
                        }
                    }
                });
    }

    private boolean validateNickname(String nickname) {
        if (nickname.isEmpty()) {
            binding.tilNickname.setError("Nickname cannot be empty");
            return false;
        }
        if (nickname.length() > 15) {
            binding.tilNickname.setError("Nickname must be 15 characters or fewer");
            return false;
        }
        if (!nickname.matches("[a-zA-Z0-9 ]+")) {
            binding.tilNickname.setError("No special characters allowed");
            return false;
        }
        binding.tilNickname.setError(null);
        return true;
    }
}
