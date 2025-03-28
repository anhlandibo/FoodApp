package com.example.foodapp2025.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodapp2025.data.remote.AuthRemoteDataSource;
import com.example.foodapp2025.data.repository.AuthRepository;
import com.example.foodapp2025.databinding.ActivitySignupBinding;
import com.example.foodapp2025.viewmodel.AuthViewModel;
import com.example.foodapp2025.viewmodel.RegisterViewModel;

public class SignupActivity extends AppCompatActivity {
    private ActivitySignupBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        authViewModel = new AuthViewModel(new AuthRepository(new AuthRemoteDataSource()));
        setContentView(binding.getRoot());

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = binding.nameEdt.getText().toString().trim();
                String email = binding.emailEdt.getText().toString().trim();
                String password = binding.passwordEdt.getText().toString().trim();
                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter complete information", Toast.LENGTH_SHORT).show();
                    return;
                }
                authViewModel.register(email, password, name).observe(SignupActivity.this, result -> {
                    switch (result.getStatus()){
                        case SUCCESS:
                            Toast.makeText(getApplicationContext(), "Sign up successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                            finish();
                            break;
                        case ERROR:
                            Toast.makeText(getApplicationContext(), "Error: " + result.getMessage(), Toast.LENGTH_SHORT).show();
                            break;
                    }
                });

            }


        });

    }
}