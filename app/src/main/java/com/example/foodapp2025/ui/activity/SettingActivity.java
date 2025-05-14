package com.example.foodapp2025.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.Navigation;

import com.example.foodapp2025.R;
import com.example.foodapp2025.databinding.ActivitySettingBinding;
import com.example.foodapp2025.viewmodel.UserViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

public class SettingActivity extends AppCompatActivity {
    private ActivitySettingBinding binding;
    private UserViewModel userVM;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userVM = new UserViewModel();

        init();
    }

    public void init() {
        binding.backBtn.setOnClickListener(v -> finish());
//        binding.notificationToggle.setOnClickListener(v -> handleNotificationToggle());
        binding.changePasswordBtn.setOnClickListener(v -> handleChangePasswordCommend());
        binding.signOutBtn.setOnClickListener(v -> handleSignOutCommend());
    }

    public void handleChangePasswordCommend() {
        userVM.handleResetPassword(this);
    }

    public void handleNotificationToggle() {

    }

    public void handleSignOutCommend() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            auth.signOut();
            GoogleSignIn.getClient(this , GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
            startActivity(new Intent(this, SplashActivity.class));
            this.finish();
        }
    }
}