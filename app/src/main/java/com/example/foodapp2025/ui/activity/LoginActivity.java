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
import com.google.firebase.firestore.DocumentSnapshot;
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
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            fetchUserRoleOrDefault(user.getUid()); // <--- Renamed and modified
                        } else {
                            showToast("Login successful, but user not found.");
                        }
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
                        if (user != null) {
                            // After successful Firebase Auth with Google, ensure user data in Firestore
                            ensureUserDocumentAndFetchRole(user);
                        } else {
                            showToast("Google Sign-In successful, but user not found.");
                        }
                    } else {
                        Log.e(TAG, "Google sign-in with Firebase failed: ", task.getException());
                        showToast("Google Sign-In Failed: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Ensures a user document exists in Firestore and then fetches their role, defaulting if missing.
     *
     * @param user The authenticated FirebaseUser.
     */
    private void ensureUserDocumentAndFetchRole(FirebaseUser user) {
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        // User does not exist in Firestore, create with default role
                        createUserInFirestore(user, db);
                    } else {
                        // User exists, proceed to fetch their role (will default if non-existent)
                        fetchUserRoleOrDefault(userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking user document existence", e);
                    showToast("Error checking user data. Please try again.");
                    showLoading(false);
                });
    }

    /**
     * Creates a new user document in Firestore with a default 'customer' role.
     *
     * @param user The authenticated FirebaseUser.
     * @param db   The FirebaseFirestore instance.
     */
    private void createUserInFirestore(FirebaseUser user, FirebaseFirestore db) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", user.getUid());
        userData.put("email", user.getEmail());
        userData.put("name", user.getDisplayName());
        userData.put("photoUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);
        userData.put("role", "customer"); // Assign default role for new users

        db.collection("users").document(user.getUid()).set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "New user created in Firestore with default role: customer");
                    fetchUserRoleOrDefault(user.getUid()); // Fetch role for the newly created user
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating user document", e);
                    showToast("Error setting up user profile. Please try again.");
                    showLoading(false);
                });
    }

    /**
     * Fetches the user's role from Firestore. If the 'role' field is missing, it defaults to 'customer'.
     * Then decides navigation based on the role.
     *
     * @param userId The UID of the authenticated Firebase user.
     */
    private void fetchUserRoleOrDefault(String userId) {
        showLoading(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    showLoading(false);
                    // We trust that the user document exists based on previous logic (ensureUserDocumentAndFetchRole)
                    // If, for some edge case, it doesn't, we'll log it but proceed to default.
                    String role = "customer"; // Default role

                    if (documentSnapshot.exists()) {
                        String fetchedRole = documentSnapshot.getString("role");
                        if (fetchedRole != null && !fetchedRole.isEmpty()) {
                            role = fetchedRole; // Use the fetched role if it exists and is not empty
                            Log.d(TAG, "User role fetched: " + role);
                        } else {
                            Log.w(TAG, "User document exists, but 'role' field is missing or empty. Defaulting to 'customer'.");
                            // Optionally, you could update the document here to set the role,
                            // but for now, we just use the default for this session.
                        }
                    } else {
                        Log.e(TAG, "User document not found for UID: " + userId + ". This should not happen if ensureUserDocumentAndFetchRole worked correctly. Defaulting to 'customer'.");
                    }

                    // Now, decide navigation based on the determined role
                    decideNavigationBasedOnRole(role);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error fetching user role for UID: " + userId, e);
                    showToast("Error retrieving user data. Please try again.");
                    // In case of error, we can still default to customer or show an error
                    // For now, let's just show an error as we couldn't even retrieve the document.
                });
    }

    /**
     * Decides where to navigate based on the user's role.
     *
     * @param role The determined role of the logged-in user.
     */
    private void decideNavigationBasedOnRole(String role) {
        navigateToMainActivity(role);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            // If already logged in, directly fetch role (which will default if missing) and navigate
            fetchUserRoleOrDefault(user.getUid());
        }
    }

    /**
     * Navigates to MainActivity, optionally passing the user's role.
     *
     * @param role The role of the logged-in user ("customer", "shipper", etc.).
     */
    private void navigateToMainActivity(String role) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("user_role", role); // Pass the role to MainActivity
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.loginBtn.setEnabled(!isLoading);
        binding.emailLoginEdt.setEnabled(!isLoading);
        binding.passwordEdt.setEnabled(!isLoading);
        binding.googleBtn.setEnabled(!isLoading);
    }
}