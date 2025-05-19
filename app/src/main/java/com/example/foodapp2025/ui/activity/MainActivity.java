package com.example.foodapp2025.ui.activity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
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


    private void updateFcmTokenToFirestore() {
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
                            .addOnSuccessListener(aVoid ->
                                    Log.d("FCM", "Token updated in Firestore"))
                            .addOnFailureListener(e ->
                                    Log.e("FCM", "Error updating token", e));
                });
    }

    private void requestNotificationPermission() {
        // Chỉ cần yêu cầu quyền này từ Android 13 (API 33) trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Kiểm tra xem quyền đã được cấp chưa
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Quyền chưa được cấp

                // Nên hiển thị lý do yêu cầu quyền không?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                    // Hiển thị giải thích tại sao cần quyền thông báo cho người dùng
                    // Ví dụ: dùng AlertDialog hoặc Snackbar
                    // Sau khi người dùng đọc giải thích và đồng ý, mới gọi requestPermissions
                    // Đây là một ví dụ đơn giản (nên cải thiện UI/UX ở đây)
                    Log.d("Permission", "Showing notification permission rationale");
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Cần quyền thông báo")
                            .setMessage("Ứng dụng cần quyền gửi thông báo để báo cho bạn khi đơn hàng hoàn thành. Vui lòng cho phép quyền này.")
                            .setPositiveButton("OK", (dialog, which) -> {
                                ActivityCompat.requestPermissions(this,
                                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                                        POST_NOTIFICATIONS_REQUEST_CODE);
                            })
                            // Có thể thêm nút Cancel
                            .create()
                            .show();

                } else {
                    // Không cần hiển thị giải thích, yêu cầu quyền trực tiếp
                    Log.d("Permission", "Requesting notification permission directly");
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS},
                            POST_NOTIFICATIONS_REQUEST_CODE);
                }
            } else {
                // Quyền đã được cấp rồi
                Log.d("Permission", "POST_NOTIFICATIONS permission already granted");
            }
        } else {
            // Dưới Android 13, quyền thông báo được cấp mặc định khi cài app, không cần làm gì
            Log.d("Permission", "No need to request POST_NOTIFICATIONS permission below Android 13");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == POST_NOTIFICATIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền đã được cấp thành công!
                Log.d("Permission", "POST_NOTIFICATIONS permission granted by user");
                Toast.makeText(this, "Đã cho phép quyền thông báo.", Toast.LENGTH_SHORT).show();
                // Bạn có thể làm gì đó sau khi có quyền, ví dụ: đăng ký topic FCM...
            } else {
                // Người dùng từ chối cấp quyền
                Log.d("Permission", "POST_NOTIFICATIONS permission denied by user");
                Toast.makeText(this, "Ứng dụng sẽ không thể hiển thị thông báo đơn hàng.", Toast.LENGTH_LONG).show();
                // Có thể hướng dẫn người dùng vào Cài đặt ứng dụng để cấp quyền thủ công
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1) Grab your NavController exactly once
        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        // 2) Wire up NavigationUI one time
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);

        // 3) Always pop up to the tab’s root BEFORE any navigation
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int destId = item.getItemId();
            // clear any child fragments in that nav-graph
            navController.popBackStack(destId, /* inclusive= */ false);
            // let NavigationUI do the actual navigate & highlighting
            return NavigationUI.onNavDestinationSelected(item, navController);
        });

        // 4) Handle “tap again” to reset to root
        binding.bottomNavigation.setOnItemReselectedListener(item -> {
            navController.popBackStack(item.getItemId(), /* inclusive= */ false);
        });
        requestNotificationPermission();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            updateFcmTokenToFirestore();
        }

        // Initialize the message bubble
        final ImageView messageBubble = findViewById(R.id.messageBubble);

        // Set the initial position of the bubble
        messageBubble.setVisibility(View.VISIBLE);
        messageBubble.setX(0);  // Start from the left edge
        messageBubble.setY(0);  // Start from the top edge

        // Set touch listener for dragging
        messageBubble.setOnTouchListener(new View.OnTouchListener() {
            float dX, dY;
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            boolean isDragging = false;  // Track whether we are currently dragging

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Record the difference between the view's position and the touch position
                        dX = view.getX() - event.getRawX();
                        dY = view.getY() - event.getRawY();
                        isDragging = false;  // Reset dragging state
                        return false; // Let the click event go through

                    case MotionEvent.ACTION_MOVE:
                        // Only handle dragging logic if the user is actually moving the bubble
                        if (!isDragging) {
                            isDragging = true;  // Mark as dragging
                        }

                        // Get new X and Y positions
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;

                        // Prevent the bubble from going off-screen
                        if (newX < 0) newX = 0;
                        if (newX > screenWidth - view.getWidth()) newX = screenWidth - view.getWidth();
                        if (newY < 0) newY = 0;
                        if (newY > screenHeight - view.getHeight()) newY = screenHeight - view.getHeight();

                        // Move the bubble smoothly
                        view.animate().x(newX).y(newY).setDuration(0).start();
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (isDragging) {
                            // Snap to the closest edge after dragging
                            float finalX = event.getRawX() + dX;
                            float finalY = event.getRawY() + dY;

                            // Snap horizontally (left or right)
                            if (finalX < screenWidth / 2) {
                                snapToEdge(view, 0, view.getY());
                            } else {
                                snapToEdge(view, screenWidth - view.getWidth(), view.getY());
                            }

                            // Snap vertically (top or bottom)
                            if (finalY < screenHeight / 2) {
                                snapToEdge(view, view.getX(), 0);
                            } else {
                                snapToEdge(view, view.getX(), screenHeight - view.getHeight());
                            }
                            return true;
                        }
                        // Allow the click event to be triggered if there was no dragging
                        return false;

                    default:
                        return false;
                }
            }
        });


        // Set onClick listener to navigate to the ChatFragment
        messageBubble.setOnClickListener(v -> {
            Log.d("MainActivity", "Message Bubble clicked!");
//            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

            // Prevent duplicate navigation
            if (!(navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.ChatFragment)) {
                navController.navigate(R.id.ChatFragment);
                setBottomNavigationVisibility(false); // Hide bottom navigation
            }
        });

//        // Setup Navigation
//        NavController navController = null;
//        if (navHostFragment != null) {
//            navController = navHostFragment.getNavController();
//            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
//        }

        // Manage Bottom Navigation Visibility on Fragment Change
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.ChatFragment) {
                setBottomNavigationVisibility(false);
            } else {
                setBottomNavigationVisibility(true);
            }
        });
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
