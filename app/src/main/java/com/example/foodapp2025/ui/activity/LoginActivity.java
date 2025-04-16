package com.example.foodapp2025.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodapp2025.R;
import com.example.foodapp2025.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private static final int GOOGLE_SIGN_IN_REQUEST_CODE = 123;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set full-screen layout flags
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance();

        configureGoogleSignIn();

        // Email login
        binding.loginBtn.setOnClickListener(v -> handleEmailLogin());

        // Google Sign-In button
        binding.googleBtn.setOnClickListener(v -> signInWithGoogle());

        binding.progressBar.setVisibility(View.GONE);
    }

    private void configureGoogleSignIn() {
        String clientId = getString(R.string.default_web_client_id);
        if (clientId == null || clientId.isEmpty()) {
            Log.e(TAG, "Google client ID is missing or empty");
            Toast.makeText(this, "Configuration Error: Missing Google Client ID", Toast.LENGTH_SHORT).show();
            return;
        }

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientId)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
    }

    private void handleEmailLogin() {
        String email = binding.emailLoginEdt.getText().toString().trim();
        String password = binding.passwordEdt.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Please enter complete information");
            return;
        }

        showLoading(true);
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        navigateToMainActivity();
                    } else {
                        Log.e(TAG, "Email login failed: ", task.getException());
                        showToast("Error: " + task.getException().getMessage());
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGN_IN_REQUEST_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null) {
                firebaseAuthWithGoogle(account.getIdToken());
            }
        } catch (ApiException e) {
            Log.e(TAG, "Google sign-in failed", e);
            showToast("Google Sign-In Error: " + e.getMessage());
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        showLoading(true);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        checkAndCreateUser(user);
                    } else {
                        Log.e(TAG, "Google sign-in with Firebase failed: ", task.getException());
                        showToast("Google Sign-In Failed: " + task.getException().getMessage());
                    }
                });
    }

    private void checkAndCreateUser(FirebaseUser user) {
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        createUserInFirestore(user, db);
                    } else {
                        Log.d(TAG, "User already exists. No new record added.");
                        navigateToMainActivity();
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error checking user existence", e));
    }

    private void createUserInFirestore(FirebaseUser user, FirebaseFirestore db) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", user.getUid());
        userData.put("email", user.getEmail());
        userData.put("name", user.getDisplayName());
        userData.put("photoUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);

        db.collection("users").document(user.getUid()).set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "New user created in Firestore");
                    navigateToMainActivity();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error creating user", e));
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            navigateToMainActivity();
        }
    }

    private void navigateToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.loginBtn.setEnabled(!isLoading);
    }
}