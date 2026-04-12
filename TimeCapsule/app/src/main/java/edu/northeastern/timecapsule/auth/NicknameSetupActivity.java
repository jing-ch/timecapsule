package edu.northeastern.timecapsule.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import edu.northeastern.timecapsule.MainActivity;
import edu.northeastern.timecapsule.databinding.ActivityNicknameSetupBinding;

public class NicknameSetupActivity extends AppCompatActivity {

    private ActivityNicknameSetupBinding binding;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNicknameSetupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        binding.etEmail.setText(user.getEmail());

        binding.btnConfirm.setOnClickListener(v -> {
            String nickname = binding.etNickname.getText() != null
                    ? binding.etNickname.getText().toString().trim()
                    : "";

            if (!validateNickname(nickname)) return;

            binding.btnConfirm.setEnabled(false);

            Map<String, Object> data = new HashMap<>();
            data.put("displayName", nickname);
            data.put("email", user.getEmail());

            firestore.collection("users")
                    .document(user.getUid())
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener(unused -> {
                        startActivity(new Intent(this, MainActivity.class));
                        finishAffinity();
                    })
                    .addOnFailureListener(e -> {
                        binding.btnConfirm.setEnabled(true);
                        Toast.makeText(this, "Failed to save nickname: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
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
