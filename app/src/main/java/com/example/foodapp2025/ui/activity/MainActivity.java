package com.example.foodapp2025.ui.activity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavInflater;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.foodapp2025.R;
import com.example.foodapp2025.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final int POST_NOTIFICATIONS_REQUEST_CODE = 101;
    private NavController navController;
    private String userRole;
    private static final Set<Integer> NON_CUSTOMER_ALLOWED_DESTINATIONS = new HashSet<>();

    static {
        NON_CUSTOMER_ALLOWED_DESTINATIONS.add(R.id.nav_profile);
        NON_CUSTOMER_ALLOWED_DESTINATIONS.add(R.id.nav_history);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Try to get user_role from intent
        userRole = getIntent().getStringExtra("user_role");

        // If null, try to get it from Firestore via email
        if (userRole == null) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null && currentUser.getEmail() != null) {
                String email = currentUser.getEmail();

                FirebaseFirestore.getInstance().collection("users")
                        .whereEqualTo("email", email)
                        .limit(1)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                                userRole = doc.getString("role");
                                if (userRole == null) userRole = "customer";
                            } else {
                                userRole = "customer";
                            }

                            // Everything continues here after role is fetched
                            Log.d("MainActivity", "Logged in user role: " + userRole);
                            finishOnCreate();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("MainActivity", "Failed to fetch user role", e);
                            userRole = "customer";
                            finishOnCreate();
                        });

                return; // Wait for async role fetch before proceeding
            } else {
                userRole = "customer";
            }
        }

        // If role already known (intent or fallback), continue immediately
        finishOnCreate();
    }

    private void finishOnCreate() {
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            NavGraph navGraph = navController.getNavInflater().inflate(R.navigation.nav_graph);
            Bundle startDestinationArgs = new Bundle();
            startDestinationArgs.putString("user_role", userRole);

            if ("customer".equalsIgnoreCase(userRole)) {
                navGraph.setStartDestination(R.id.nav_home);
            } else {
                navGraph.setStartDestination(R.id.nav_profile);
            }
            navController.setGraph(navGraph, startDestinationArgs);

            configureBottomNavigationMenu(binding.bottomNavigation);
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (arguments != null && arguments.getString("user_role") == null) {
                    arguments.putString("user_role", userRole);
                }

                if (destination.getId() == R.id.ChatFragment) {
                    setBottomNavigationVisibility(false);
                } else {
                    setBottomNavigationVisibility(true);
                }
            });
        }

        requestNotificationPermission();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            updateFcmTokenToFirestore();
        }

        if ("customer".equalsIgnoreCase(userRole)) {
            setupMessageBubble();
        } else {
            ImageView bubble = findViewById(R.id.messageBubble);
            if (bubble != null) {
                bubble.setVisibility(View.GONE);
            }
        }

        handleNotificationIntent(getIntent());
    }
    /**
     * Configures the BottomNavigationView menu items based on the user's role.
     *
     * @param bottomNavigationView The BottomNavigationView instance.
     */
    private void configureBottomNavigationMenu(BottomNavigationView bottomNavigationView) {
        Menu menu = bottomNavigationView.getMenu();

        menu.clear();
        getMenuInflater().inflate(R.menu.bottom_nav_menu, menu); // Inflate the full menu

        if (!"customer".equalsIgnoreCase(userRole)) {
            menu.removeItem(R.id.nav_home);
            menu.removeItem(R.id.nav_favourite);
            menu.removeItem(R.id.nav_cart);
            // nav_history and nav_profile are kept
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus(); // This will correctly get the focused EditText even if it's in a Fragment
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    // Call your KeyboardUtils method here
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNotificationIntent(intent);
    }

    private void updateFcmTokenToFirestore() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.w("FCM", "User not logged in, skipping token update.");
            return;
        }
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "fetchToken failed", task.getException());
                        return;
                    }
                    String token = task.getResult();
                    Log.d("FCM", "Fetched token: " + token);
                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(uid)
                            .update("fcmToken", token)
                            .addOnSuccessListener(aVoid -> Log.d("FCM", "Token updated in Firestore"))
                            .addOnFailureListener(e -> Log.e("FCM", "Error updating token", e));
                });
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.POST_NOTIFICATIONS)) {
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Cần quyền thông báo")
                            .setMessage("Ứng dụng cần quyền gửi thông báo để báo cho bạn khi có đơn hàng hoặc ưu đãi mới. Vui lòng cho phép quyền này.")
                            .setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                                    POST_NOTIFICATIONS_REQUEST_CODE))
                            .create()
                            .show();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS},
                            POST_NOTIFICATIONS_REQUEST_CODE);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == POST_NOTIFICATIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Enable notification.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "App will not display notification.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupMessageBubble() {
        final ImageView bubble = findViewById(R.id.messageBubble);
        bubble.setVisibility(View.VISIBLE);

        final int screenWidth = getResources().getDisplayMetrics().widthPixels;
        final int screenHeight = getResources().getDisplayMetrics().heightPixels;

        bubble.post(() -> {
            bubble.setX(screenWidth - bubble.getWidth() - 40);
            bubble.setY(screenHeight / 2f);
        });

        bubble.setOnTouchListener(new View.OnTouchListener() {
            float dX, dY;
            boolean isDragging = false;
            long downTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        downTime = System.currentTimeMillis();
                        isDragging = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;
                        if (newX < 0) newX = 0;
                        if (newX > screenWidth - v.getWidth()) newX = screenWidth - v.getWidth();
                        if (newY < 0) newY = 0;
                        if (newY > screenHeight - v.getHeight())
                            newY = screenHeight - v.getHeight();
                        v.setX(newX);
                        v.setY(newY);
                        isDragging = true;
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (isDragging) {
                            float snapX = v.getX() < screenWidth / 2 ? 0 : screenWidth - v.getWidth();
                            snapToEdge(v, snapX, v.getY());
                        } else {
                            long clickDuration = System.currentTimeMillis() - downTime;
                            if (clickDuration < 200) {
                                v.performClick();
                            }
                        }
                        return true;
                }
                return false;
            }
        });

        bubble.setOnClickListener(v -> {
            if (navController != null && navController.getCurrentDestination() != null
                    && navController.getCurrentDestination().getId() != R.id.ChatFragment) {
                navController.navigate(R.id.ChatFragment);
                setBottomNavigationVisibility(false);
            }
        });
    }

    private void handleNotificationIntent(Intent intent) {
        if (intent != null && navController != null) {
            String notificationType = intent.getStringExtra("notification_type");
            Log.d("MainActivity", "Handling notification intent with type: " + notificationType);

            if ("order_completed".equals(notificationType)) {
                String orderId = intent.getStringExtra("order_id");
                Toast.makeText(this, "Đơn hàng " + orderId + " đã hoàn thành!", Toast.LENGTH_LONG).show();
                // navController.navigate(R.id.orderDetailsFragment, bundleWithOrderId);
            } else if ("new_voucher".equals(notificationType)) {
                String voucherCode = intent.getStringExtra("voucher_code");
                Toast.makeText(this, "Bạn có voucher mới: " + voucherCode + "!", Toast.LENGTH_LONG).show();
                // navController.navigate(R.id.voucherListFragment);
            }
        }
    }

    private void snapToEdge(View view, float x, float y) {
        ValueAnimator animatorX = ValueAnimator.ofFloat(view.getX(), x);
        animatorX.setDuration(300);
        animatorX.addUpdateListener(animation -> view.setX((float) animation.getAnimatedValue()));
        animatorX.start();

        ValueAnimator animatorY = ValueAnimator.ofFloat(view.getY(), y);
        animatorY.setDuration(300);
        animatorY.addUpdateListener(animation -> view.setY((float) animation.getAnimatedValue()));
        animatorY.start();
    }

    public void setBottomNavigationVisibility(boolean isVisible) {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    // Add this method inside your MainActivity class
    public String getUserRole() {
        // Ensure userRole is never null when returned, provide a default if it somehow isn't set.
        if (userRole == null) {
            Log.w("MainActivity", "getUserRole() called before userRole was fully initialized, defaulting to customer.");
            return "customer";
        }
        return userRole;
    }
}
