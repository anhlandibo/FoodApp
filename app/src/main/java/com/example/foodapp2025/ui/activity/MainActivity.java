package com.example.foodapp2025.ui.activity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent; // Import Intent
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
    private NavController navController; // Khai báo navController ở đây

    private void updateFcmTokenToFirestore() {
        // Kiểm tra xem người dùng đã đăng nhập chưa trước khi cố gắng lấy UID
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
                            .addOnSuccessListener(aVoid ->
                                    Log.d("FCM", "Token updated in Firestore"))
                            .addOnFailureListener(e ->
                                    Log.e("FCM", "Error updating token", e));
                });
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                    Log.d("Permission", "Showing notification permission rationale");
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Cần quyền thông báo")
                            .setMessage("Ứng dụng cần quyền gửi thông báo để báo cho bạn khi có đơn hàng hoặc ưu đãi mới. Vui lòng cho phép quyền này.") // Cập nhật nội dung thông báo
                            .setPositiveButton("OK", (dialog, which) -> {
                                ActivityCompat.requestPermissions(this,
                                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                                        POST_NOTIFICATIONS_REQUEST_CODE);
                            })
                            .create()
                            .show();
                } else {
                    Log.d("Permission", "Requesting notification permission directly");
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS},
                            POST_NOTIFICATIONS_REQUEST_CODE);
                }
            } else {
                Log.d("Permission", "POST_NOTIFICATIONS permission already granted");
            }
        } else {
            Log.d("Permission", "No need to request POST_NOTIFICATIONS permission below Android 13");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == POST_NOTIFICATIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permission", "POST_NOTIFICATIONS permission granted by user");
                Toast.makeText(this, "Đã cho phép quyền thông báo.", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("Permission", "POST_NOTIFICATIONS permission denied by user");
                Toast.makeText(this, "Ứng dụng sẽ không thể hiển thị thông báo.", Toast.LENGTH_LONG).show(); // Cập nhật nội dung
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null){
            navController = navHostFragment.getNavController(); // Gán navController
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
        }

        // Yêu cầu quyền thông báo ngay khi activity được tạo
        requestNotificationPermission();

        // Cập nhật FCM token nếu người dùng đã đăng nhập
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
            boolean isDragging = false;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = view.getX() - event.getRawX();
                        dY = view.getY() - event.getRawY();
                        isDragging = false;
                        return false;

                    case MotionEvent.ACTION_MOVE:
                        if (!isDragging) {
                            isDragging = true;
                        }

                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;

                        if (newX < 0) newX = 0;
                        if (newX > screenWidth - view.getWidth()) newX = screenWidth - view.getWidth();
                        if (newY < 0) newY = 0;
                        if (newY > screenHeight - view.getHeight()) newY = screenHeight - view.getHeight();

                        view.animate().x(newX).y(newY).setDuration(0).start();
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (isDragging) {
                            float finalX = event.getRawX() + dX;
                            float finalY = event.getRawY() + dY;

                            if (finalX < screenWidth / 2) {
                                snapToEdge(view, 0, view.getY());
                            } else {
                                snapToEdge(view, screenWidth - view.getWidth(), view.getY());
                            }

                            if (finalY < screenHeight / 2) {
                                snapToEdge(view, view.getX(), 0);
                            } else {
                                snapToEdge(view, view.getX(), screenHeight - view.getHeight());
                            }
                            return true;
                        }
                        return false;

                    default:
                        return false;
                }
            }
        });

        messageBubble.setOnClickListener(v -> {
            Log.d("MainActivity", "Message Bubble clicked!");
            if (navController != null) {
                if (!(navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.ChatFragment)) {
                    navController.navigate(R.id.ChatFragment);
                    setBottomNavigationVisibility(false);
                }
            }
        });

        if (navHostFragment != null) { // Kiểm tra lại navHostFragment đã được khởi tạo
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getId() == R.id.ChatFragment) {
                    setBottomNavigationVisibility(false);
                } else {
                    setBottomNavigationVisibility(true);
                }
            });
        }

        // --- Xử lý Intent khi Activity được khởi chạy từ thông báo ---
        handleNotificationIntent(getIntent());
    }

    // Phương thức này sẽ được gọi khi Activity đã chạy và một Intent mới được gửi đến (ví dụ, khi nhấn vào thông báo thứ hai)
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Cập nhật Intent mới cho Activity
        handleNotificationIntent(intent); // Xử lý Intent mới
    }

    private void handleNotificationIntent(Intent intent) {
        if (intent != null && navController != null) {
            String notificationType = intent.getStringExtra("notification_type");
            Log.d("MainActivity", "Handling notification intent with type: " + notificationType);

            if ("order_completed".equals(notificationType)) {
                String orderId = intent.getStringExtra("order_id");
                // TODO: Điều hướng đến màn hình chi tiết đơn hàng
                Log.d("MainActivity", "Navigating to OrderDetails with ID: " + orderId);
                // Ví dụ: navController.navigate(R.id.orderDetailsFragment, bundleWithOrderId);
                Toast.makeText(this, "Đơn hàng " + orderId + " đã hoàn thành!", Toast.LENGTH_LONG).show();
            } else if ("new_voucher".equals(notificationType)) {
                String voucherCode = intent.getStringExtra("voucher_code");
                // TODO: Điều hướng đến màn hình danh sách voucher hoặc chi tiết voucher
                Log.d("MainActivity", "Navigating to VoucherList/Details with code: " + voucherCode);
                // Ví dụ: navController.navigate(R.id.voucherListFragment, bundleWithVoucherCode);
                Toast.makeText(this, "Bạn có voucher mới: " + voucherCode + "!", Toast.LENGTH_LONG).show();
                // Tắt hiển thị bottom navigation nếu điều hướng sang màn hình không có bottom nav
                // setBottomNavigationVisibility(false);
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
