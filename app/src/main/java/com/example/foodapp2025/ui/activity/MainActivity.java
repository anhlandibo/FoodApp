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
import android.view.MotionEvent;
import android.view.View;
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
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.foodapp2025.R;
import com.example.foodapp2025.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final int POST_NOTIFICATIONS_REQUEST_CODE = 101;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
        }

        requestNotificationPermission();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            updateFcmTokenToFirestore();
        }

        setupMessageBubble();

        if (navController != null) {
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getId() == R.id.ChatFragment) {
                    setBottomNavigationVisibility(false);
                } else {
                    setBottomNavigationVisibility(true);
                }
            });
        }

        handleNotificationIntent(getIntent());
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
                Toast.makeText(this, "Đã cho phép quyền thông báo.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Ứng dụng sẽ không thể hiển thị thông báo.", Toast.LENGTH_LONG).show();
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
                        if (newY > screenHeight - v.getHeight()) newY = screenHeight - v.getHeight();
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
}
