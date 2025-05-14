package com.example.foodapp2025.ui.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;


import com.example.foodapp2025.R;
import com.example.foodapp2025.data.model.BannerModel;
import com.example.foodapp2025.databinding.ActivityMainBinding;
import com.example.foodapp2025.ui.adapter.SliderAdapter;
import com.example.foodapp2025.ui.fragment.CartFragment;
import com.example.foodapp2025.ui.fragment.HistoryFragment;
import com.example.foodapp2025.ui.fragment.HomeFragment;
import com.example.foodapp2025.ui.fragment.ProfileFragment;
import com.example.foodapp2025.viewmodel.BannerViewModel;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private static final int POST_NOTIFICATIONS_REQUEST_CODE = 101; // Chọn một số duy nhất


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null){
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
        }
        requestNotificationPermission();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            updateFcmTokenToFirestore();
        }
    }
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

    public void setBottomNavigationVisibility(boolean isVisible) {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        if (isVisible) {
            bottomNavigationView.setVisibility(View.VISIBLE);
        } else {
            bottomNavigationView.setVisibility(View.GONE);
        }
    }
}