package edu.northeastern.timecapsule.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import edu.northeastern.timecapsule.MainActivity;
import edu.northeastern.timecapsule.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel viewModel;
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // If already logged in, check nickname and route
        if (viewModel.getCurrentUser() != null) {
            checkAndRoute(viewModel.getCurrentUser());
            return;
        }

        viewModel.currentUser.observe(this, user -> {
            if (user != null) checkAndRoute(user);
        });

        viewModel.errorMessage.observe(this, msg ->
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show());

        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.login(email, password);
        });

        binding.tvGoToRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void checkAndRoute(FirebaseUser user) {
        firestore.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    String displayName = doc.exists() ? doc.getString("displayName") : null;
                    boolean needsNickname = displayName == null
                            || displayName.trim().isEmpty()
                            || (user.getEmail() != null && displayName.equalsIgnoreCase(user.getEmail()));
                    if (needsNickname) {
                        startActivity(new Intent(this, NicknameSetupActivity.class));
                    } else {
                        startActivity(new Intent(this, MainActivity.class));
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                });
    }
}
