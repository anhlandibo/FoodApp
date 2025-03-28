package com.example.foodapp2025.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.test.runner.permission.RequestPermissionCallable;

import com.example.foodapp2025.data.model.UserModel;
import com.example.foodapp2025.data.remote.AuthRemoteDataSource;
import com.example.foodapp2025.data.repository.AuthRepository;
import com.example.foodapp2025.databinding.ActivityLoginBinding;
import com.example.foodapp2025.utils.Result;
import com.example.foodapp2025.viewmodel.AuthViewModel;
import com.example.foodapp2025.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        authViewModel = new AuthViewModel(new AuthRepository(new AuthRemoteDataSource()));
        setContentView(binding.getRoot());


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        binding.progressBar.setVisibility(View.GONE);


        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.emailLoginEdt.getText().toString().trim();
                String password = binding.passwordEdt.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter complete information", Toast.LENGTH_SHORT).show();
                    return;
                }

                binding.progressBar.setVisibility(View.VISIBLE);
                binding.loginBtn.setEnabled(false);

                authViewModel.login(email, password).observe(LoginActivity.this, result -> {
                    switch (result.getStatus()){
                        case SUCCESS:
                            binding.progressBar.setVisibility(View.GONE);
                            binding.loginBtn.setEnabled(true);
                            UserModel user = result.getData();
                            if (user != null){
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            break;
                        case ERROR:
                            binding.progressBar.setVisibility(View.GONE);
                            binding.loginBtn.setEnabled(true);
                            Toast.makeText(getApplicationContext(), "Error: " + result.getMessage(), Toast.LENGTH_SHORT).show();
                            break;
                    }
                });
            }
        });

    }
}