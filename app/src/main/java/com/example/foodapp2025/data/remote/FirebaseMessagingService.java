package com.example.foodapp2025.data.remote;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent; // Import PendingIntent
import android.content.Context; // Import Context
import android.content.Intent; // Import Intent
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast; // Giữ lại nếu bạn muốn dùng Toast trong một số trường hợp

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.foodapp2025.R;
import com.example.foodapp2025.ui.activity.MainActivity; // Import MainActivity của bạn
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final String ORDER_CHANNEL_ID = "order_channel";
    private static final String VOUCHER_CHANNEL_ID = "voucher_channel";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);

        // Lấy User ID của người dùng hiện tại (nếu đã đăng nhập)
        String userId;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            userId = null;
        }

        if (userId == null) {
            Log.w(TAG, "User not logged in, skipping token update.");
            // Có thể enqueue một Worker để thử cập nhật token sau khi đăng nhập
            return;
        }

        // Lưu token vào Firestore
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .update("fcmToken", token)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Token updated in Firestore for user: " + userId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update token in Firestore for user: " + userId, e);
                });
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Kiểm tra xem thông báo có chứa dữ liệu notification payload không
        String title = "";
        String body = "";
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Notification Title: " + title);
            Log.d(TAG, "Notification Body: " + body);
        }

        // Kiểm tra xem thông báo có chứa data payload không
        // Data payload là nơi bạn gửi các thông tin tùy chỉnh như type, orderId, voucherCode
        String notificationType = remoteMessage.getData().get("type"); // Lấy giá trị của trường 'type'
        String orderId = remoteMessage.getData().get("orderId");
        String voucherCode = remoteMessage.getData().get("voucherCode");

        Log.d(TAG, "Notification Type: " + notificationType);
        Log.d(TAG, "Order ID (if any): " + orderId);
        Log.d(TAG, "Voucher Code (if any): " + voucherCode);


        // --- Hiển thị thông báo (Notification) ---
        // Tạo các kênh thông báo cần thiết
        createNotificationChannels();

        String channelId;
        int notificationId;
        String notificationTitle;
        String notificationBody;
        Intent intent;

        if ("new_voucher".equals(notificationType)) {
            // Đây là thông báo voucher mới
            channelId = VOUCHER_CHANNEL_ID;
            notificationId = (int) System.currentTimeMillis(); // ID duy nhất cho mỗi thông báo voucher
            notificationTitle = title.isEmpty() ? "Voucher mới dành cho bạn!" : title; // Sử dụng title từ FCM hoặc mặc định
            notificationBody = body.isEmpty() ? "Kiểm tra ưu đãi mới nhất!" : body; // Sử dụng body từ FCM hoặc mặc định

            intent = new Intent(this, MainActivity.class);
            // Thêm dữ liệu bổ sung để MainActivity có thể xử lý khi nhấn vào thông báo
            intent.putExtra("notification_type", notificationType);
            if (voucherCode != null) {
                intent.putExtra("voucher_code", voucherCode);
            }
            // Clear activity stack và tạo activity mới
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);


        } else if ("order_completed".equals(notificationType)) { // Giả sử backend gửi type = "order_completed"
            // Đây là thông báo đơn hàng hoàn thành (như API cũ của bạn)
            channelId = ORDER_CHANNEL_ID;
            notificationId = (int) System.currentTimeMillis(); // ID duy nhất
            notificationTitle = title.isEmpty() ? "Đơn hàng của bạn đã hoàn thành!" : title;
            notificationBody = body.isEmpty() ? "Chúng tôi đã làm xong đơn của bạn, vui lòng bấm “Tôi đã nhận” trong app." : body;

            intent = new Intent(this, MainActivity.class);
            intent.putExtra("notification_type", notificationType);
            if (orderId != null) {
                intent.putExtra("order_id", orderId);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        } else {
            // Trường hợp thông báo không có type hoặc type không xác định
            channelId = ORDER_CHANNEL_ID; // Kênh mặc định
            notificationId = (int) System.currentTimeMillis();
            notificationTitle = title.isEmpty() ? "Thông báo mới" : title;
            notificationBody = body.isEmpty() ? "Bạn có một thông báo mới từ ứng dụng." : body;

            intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        // Tạo PendingIntent
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, // Request code
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE); // FLAG_IMMUTABLE là bắt buộc từ Android S (API 31)

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Đảm bảo icon này tồn tại
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true) // Tự động đóng thông báo khi người dùng nhấn vào
                .setContentIntent(pendingIntent); // Đặt PendingIntent

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);

        // Kiểm tra quyền POST_NOTIFICATIONS (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Nếu quyền chưa được cấp, bạn không thể hiển thị thông báo.
                // Log lỗi hoặc hiển thị Toast (chỉ để debug)
                Log.e(TAG, "POST_NOTIFICATIONS permission not granted. Cannot show notification.");
                // Toast.makeText(this, "Permission not granted to show notifications.", Toast.LENGTH_LONG).show();
                return;
            }
        }
        manager.notify(notificationId, builder.build());
    }

    // Tạo kênh thông báo (cần cho Android 8.0 Oreo - API 26 trở lên)
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Kênh cho thông báo đơn hàng
            NotificationChannel orderChannel = new NotificationChannel(
                    ORDER_CHANNEL_ID,
                    "Order Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            orderChannel.setDescription("Thông báo khi đơn hàng được hoàn thành");
            // Kênh cho thông báo voucher
            NotificationChannel voucherChannel = new NotificationChannel(
                    VOUCHER_CHANNEL_ID,
                    "Voucher Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            voucherChannel.setDescription("Thông báo về các voucher và ưu đãi mới");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(orderChannel);
                notificationManager.createNotificationChannel(voucherChannel);
            }
        }
    }
}