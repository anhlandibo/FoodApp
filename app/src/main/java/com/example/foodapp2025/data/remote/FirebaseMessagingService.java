package com.example.foodapp2025.data.remote;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.foodapp2025.R;
import com.example.foodapp2025.ui.activity.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final String ORDER_CHANNEL_ID = "order_channel";
    private static final String VOUCHER_CHANNEL_ID = "voucher_channel";
    private static final String PAYMENT_CHANNEL_ID = "payment_channel";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);

        String userId;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            userId = null;
        }

        if (userId == null) {
            Log.w(TAG, "User not logged in, skipping token update.");
            return;
        }

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

        String notificationType = remoteMessage.getData().get("type");
        String orderId = remoteMessage.getData().get("orderId");
        String voucherCode = remoteMessage.getData().get("voucherCode");

        String customTitle = remoteMessage.getData().get("custom_title");
        String customBody = remoteMessage.getData().get("custom_body");

        Log.d(TAG, "Notification Type: " + notificationType);
        Log.d(TAG, "Order ID (if any): " + orderId);
        Log.d(TAG, "Voucher Code (if any): " + voucherCode);
        Log.d(TAG, "Custom Title (from data): " + customTitle);
        Log.d(TAG, "Custom Body (from data): " + customBody);

        createNotificationChannels();

        String channelId;
        int notificationId;
        String notificationTitle;
        String notificationBody;
        Intent intent;

        if ("new_voucher".equals(notificationType)) {
            channelId = VOUCHER_CHANNEL_ID;
            notificationId = (int) System.currentTimeMillis();
            notificationTitle = (customTitle != null && !customTitle.isEmpty()) ? customTitle : "New voucher available!";
            notificationBody = (customBody != null && !customBody.isEmpty()) ? customBody : "Check out new voucher for you!";

            intent = new Intent(this, MainActivity.class);
            intent.putExtra("notification_type", notificationType);
            if (voucherCode != null) {
                intent.putExtra("voucher_code", voucherCode);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        } else if ("order_completed".equals(notificationType)) {
            channelId = ORDER_CHANNEL_ID;
            notificationId = (int) System.currentTimeMillis();
            notificationTitle = (customTitle != null && !customTitle.isEmpty()) ? customTitle : "Your order is being delivered!";
            notificationBody = (customBody != null && !customBody.isEmpty()) ? customBody : "Please give us your thoughts about our products and service.";

            intent = new Intent(this, MainActivity.class);
            intent.putExtra("notification_type", notificationType);
            if (orderId != null) {
                intent.putExtra("order_id", orderId);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        } else if ("payment_success".equals(notificationType)) {
            channelId = PAYMENT_CHANNEL_ID;
            notificationId = (int) System.currentTimeMillis();
            notificationTitle = (customTitle != null && !customTitle.isEmpty()) ? customTitle : "Payment Successful! 🥳";
            notificationBody = (customBody != null && !customBody.isEmpty()) ? customBody : "Your payment has been successfully processed.";

            intent = new Intent(this, MainActivity.class);
            intent.putExtra("notification_type", notificationType);
            if (orderId != null) {
                intent.putExtra("order_id", orderId);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        } else if ("payment_failure".equals(notificationType)) {
            channelId = PAYMENT_CHANNEL_ID;
            notificationId = (int) System.currentTimeMillis();
            notificationTitle = (customTitle != null && !customTitle.isEmpty()) ? customTitle : "Payment Failed! 😟";
            notificationBody = (customBody != null && !customBody.isEmpty()) ? customBody : "Your payment could not be processed. Please try again.";

            intent = new Intent(this, MainActivity.class);
            intent.putExtra("notification_type", notificationType);
            if (orderId != null) {
                intent.putExtra("order_id", orderId);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        } else {
            channelId = ORDER_CHANNEL_ID;
            notificationId = (int) System.currentTimeMillis();
            notificationTitle = (customTitle != null && !customTitle.isEmpty()) ? customTitle : "New notification"; // Mặc định chung
            notificationBody = (customBody != null && !customBody.isEmpty()) ? customBody : "You got a new message."; // Mặc định chung

            intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "POST_NOTIFICATIONS permission not granted. Cannot show notification.");
                return;
            }
        }
        manager.notify(notificationId, builder.build());
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel orderChannel = new NotificationChannel(
                    ORDER_CHANNEL_ID,
                    "Order Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            orderChannel.setDescription("Notify when the orders are done.");

            NotificationChannel voucherChannel = new NotificationChannel(
                    VOUCHER_CHANNEL_ID,
                    "Voucher Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            voucherChannel.setDescription("Notify about new vouchers.");

            NotificationChannel paymentChannel = new NotificationChannel(
                    PAYMENT_CHANNEL_ID,
                    "Payment Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            paymentChannel.setDescription("Notify about payment status.");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(orderChannel);
                notificationManager.createNotificationChannel(voucherChannel);
                notificationManager.createNotificationChannel(paymentChannel);
            }
        }
    }
}