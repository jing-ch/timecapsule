package edu.northeastern.timecapsule.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import edu.northeastern.timecapsule.MainActivity;
import edu.northeastern.timecapsule.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        viewModel.currentUser.observe(this, user -> {
            if (user != null) goToMain();
        });

        viewModel.errorMessage.observe(this, msg ->
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show());

        binding.btnRegister.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString();
            String confirm = binding.etConfirmPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirm)) {
                binding.tilConfirmPassword.setError("Passwords do not match");
                return;
            }

            binding.tilConfirmPassword.setError(null);
            viewModel.register(email, password);
        });

        binding.tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finishAffinity(); // clear both RegisterActivity and LoginActivity from back stack
    }
}
