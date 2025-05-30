package com.example.foodapp2025.ui.activity;//package com.example.foodapp2025.ui.activity;
//
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//
//import com.example.foodapp2025.R;
//
//public class PaymentActivity extends AppCompatActivity {
//    private EditText cardNumberEditText;
//    private EditText expiryDateEditText;
//    private EditText cvvEditText;
//    private Button payButton;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_payment); // Đảm bảo bạn có layout này
//
//        cardNumberEditText = findViewById(R.id.cardNumberEditText);
//        expiryDateEditText = findViewById(R.id.expiryDateEditText);
//        cvvEditText = findViewById(R.id.cvvEditText);
//        payButton = findViewById(R.id.payButton);
//
//        payButton.setOnClickListener(v -> {
//            // Lấy thông tin thẻ từ EditTexts
//            String cardNumber = cardNumberEditText.getText().toString().trim();
//            String expiryDate = expiryDateEditText.getText().toString().trim();
//            String cvv = cvvEditText.getText().toString().trim();
//
//            if (cardNumber.isEmpty() || expiryDate.isEmpty() || cvv.isEmpty()) {
//                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin thẻ.", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(this, "Đang xử lý thanh toán bằng thẻ...", Toast.LENGTH_LONG).show();
//
//                Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
//                setResult(RESULT_OK);
//                finish();
//            }
//        });
//    }
//}

//package com.example.foodapp2025.ui.activity;
//
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.foodapp2025.R;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.stripe.android.PaymentConfiguration;
//import com.stripe.android.payments.paymentlauncher.PaymentLauncher;
//import com.stripe.android.payments.paymentlauncher.PaymentResult;
//import com.stripe.android.paymentsheet.PaymentSheet; // Thêm import này
//import com.stripe.android.paymentsheet.PaymentSheetResult;
//import com.stripe.android.paymentsheet.PaymentSheetResultCallback; // Thêm import này
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.MediaType;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okhttp3.Response;
//
//public class PaymentActivity extends AppCompatActivity {
//    private EditText cardNumberEditText;
//    private EditText expiryDateEditText;
//    private EditText cvvEditText;
//    private Button payButton;
//
//    private static final String BACKEND_URL = "http://10.0.2.2:4000/";
//    private final OkHttpClient httpClient = new OkHttpClient();
//    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
//
//    private PaymentLauncher paymentLauncher;
//    private PaymentSheet paymentSheet; // Thêm biến PaymentSheet
//
//    // Để lưu clientSecret nhận được từ backend
//    private String clientSecret;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_payment);
//
//        PaymentConfiguration.init(this, "pk_test_51RTkupCxcqExvemRVizQ83Ufcad3APCrcTODqH8oSUhkHpy4aSjZWJJuHlStgFlzhiAqz8MEsqsj9rrynRtlBT4300fH5EbzR8");
//
//        // Khởi tạo PaymentSheet thay vì PaymentLauncher cho luồng Payment Sheet
//        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);
//
//        // Ánh xạ các views
//        cardNumberEditText = findViewById(R.id.cardNumberEditText);
//        expiryDateEditText = findViewById(R.id.expiryDateEditText);
//        cvvEditText = findViewById(R.id.cvvEditText);
//        payButton = findViewById(R.id.payButton);
//
//        payButton.setOnClickListener(v -> {
//            String cardNumber = cardNumberEditText.getText().toString().trim();
//            String expiryDate = expiryDateEditText.getText().toString().trim();
//            String cvv = cvvEditText.getText().toString().trim();
//
//            if (cardNumber.isEmpty() || expiryDate.isEmpty() || cvv.isEmpty()) {
//                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin thẻ.", Toast.LENGTH_SHORT).show();
//            } else {
//                startCheckout();
//            }
//        });
//    }
//
//    private void startCheckout() {
//
//        double amount = 1000000.0;
//        String currency = "vnd";
//        String tempOrderId = "order_" + System.currentTimeMillis();
//        String tempUserId = "z6MODvzTiMTgKQFBX7gD3vj7Csu1";
//
//
//        Map<String, Object> payMap = new HashMap<>();
//        payMap.put("amount", (long) amount);
//        payMap.put("currency", currency);
//        payMap.put("orderId", tempOrderId);
//        payMap.put("userId", tempUserId);
//
//        String json = gson.toJson(payMap);
//
//        Log.d("PaymentActivity", "Requesting PaymentIntent with data: " + json);
//
//        RequestBody requestBody = RequestBody.create(
//                json,
//                MediaType.get("application/json; charset=utf-8")
//        );
//
//        Request request = new Request.Builder()
//                .url(BACKEND_URL + "create-payment-intent")
//                .post(requestBody)
//                .build();
//
//        httpClient.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                runOnUiThread(() -> {
//                    Toast.makeText(PaymentActivity.this, "Lỗi mạng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    Log.e("PaymentActivity", "Lỗi khi gọi backend", e);
//                });
//            }
//
//            @Override
//            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                if (response.isSuccessful()) {
//                    String responseData = response.body().string();
//                    Log.d("PaymentActivity", "Phản hồi PaymentIntent: " + responseData);
//
//                    Map<String, String> responseMap = gson.fromJson(responseData, Map.class);
//                    clientSecret = responseMap.get("clientSecret"); // Lưu clientSecret
//                    String customerId = responseMap.get("customerId");
//                    String ephemeralKey = responseMap.get("ephemeralKey");
//
//                    if (clientSecret != null && customerId != null && ephemeralKey != null) {
//                        Log.d("PaymentActivity", "Đã nhận clientSecret: " + clientSecret);
//                        runOnUiThread(() -> {
//                            PaymentSheet.Configuration configuration = new PaymentSheet.Configuration(
//                                    "FoodApp",
//                                    new PaymentSheet.CustomerConfiguration(
//                                            customerId,
//                                            ephemeralKey
//                                    )
//                            );
//                            paymentSheet.presentWithPaymentIntent(clientSecret, configuration);
//                        });
//                    } else {
//                        runOnUiThread(() -> Toast.makeText(PaymentActivity.this, "Không lấy được client secret từ backend.", Toast.LENGTH_SHORT).show());
//                    }
//                } else {
//                    final String errorBody = response.body() != null ? response.body().string() : "Không có thân lỗi";
//                    runOnUiThread(() -> {
//                        Toast.makeText(PaymentActivity.this, "Lỗi backend: " + response.code() + " - " + errorBody, Toast.LENGTH_LONG).show();
//                        Log.e("PaymentActivity", "Lỗi backend: " + response.code() + " - " + errorBody);
//                    });
//                }
//            }
//        });
//    }
//
//    // Callback mới cho PaymentSheet
//    private void onPaymentSheetResult(@NonNull PaymentSheetResult paymentSheetResult) {
//        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
//            Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
//            setResult(RESULT_OK);
//            finish();
//        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
//            Toast.makeText(this, "Thanh toán đã bị hủy.", Toast.LENGTH_SHORT).show();
//            setResult(RESULT_CANCELED);
//        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
//            PaymentSheetResult.Failed finalResult = (PaymentSheetResult.Failed) paymentSheetResult;
//            Toast.makeText(this, "Thanh toán thất bại: " + finalResult.getError().getMessage(), Toast.LENGTH_LONG).show();
//            Log.e("PaymentActivity", "Thanh toán thất bại", finalResult.getError());
//            setResult(RESULT_CANCELED);
//        }
//    }
//}

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodapp2025.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PaymentActivity extends AppCompatActivity {

    private static final String BACKEND_URL = "http://10.0.2.2:4000/"; // Thay bằng URL của bạn
    private final OkHttpClient httpClient = new OkHttpClient();
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private PaymentSheet paymentSheet;

    // Để lưu clientSecret nhận được từ backend
    private String clientSecret;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment); // Tạo một layout mới đơn giản, ví dụ chỉ có ProgressBar

        PaymentConfiguration.init(this, "pk_test_51RTkupCxcqExvemRVizQ83Ufcad3APCrcTODqH8oSUhkHpy4aSjZWJJuHlStgFlzhiAqz8MEsqsj9rrynRtlBT4300fH5EbzR8");

        // Khởi tạo PaymentSheet
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);
        startCheckout();
    }

    private void startCheckout() {
        double amount = 1000000.0; // Lấy từ giỏ hàng thực tế
        String currency = "vnd";
        String tempOrderId = "order_" + System.currentTimeMillis();
        String tempUserId = "user_test_123"; // QUAN TRỌNG: Thay bằng ID người dùng thực tế của bạn (ví dụ: Firebase UID)

        Map<String, Object> payMap = new HashMap<>();
        payMap.put("amount", (long) amount);
        payMap.put("currency", currency);
        payMap.put("orderId", tempOrderId);
        payMap.put("userId", tempUserId);

        String json = gson.toJson(payMap);

        Log.d("PaymentActivity", "Requesting PaymentIntent with data: " + json);

        RequestBody requestBody = RequestBody.create(
                json,
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(BACKEND_URL + "create-payment-intent")
                .post(requestBody)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(PaymentActivity.this, "Lỗi mạng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("PaymentActivity", "Lỗi khi gọi backend", e);
                    // Đóng Activity nếu không thể tiếp tục
                    finish();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d("PaymentActivity", "Phản hồi PaymentIntent: " + responseData);

                    Map<String, String> responseMap = gson.fromJson(responseData, Map.class);
                    clientSecret = responseMap.get("clientSecret");
                    String customerId = responseMap.get("customerId");
                    String ephemeralKey = responseMap.get("ephemeralKey");

                    if (clientSecret != null && customerId != null && ephemeralKey != null) {
                        Log.d("PaymentActivity", "Đã nhận clientSecret: " + clientSecret + ", CustomerId: " + customerId + ", EphemeralKey: " + ephemeralKey);
                        runOnUiThread(() -> {
                            PaymentSheet.Configuration configuration = new PaymentSheet.Configuration(
                                    "FoodApp", // Tên hiển thị trên Payment Sheet
                                    new PaymentSheet.CustomerConfiguration(
                                            customerId,
                                            ephemeralKey
                                    )
                            );
                            paymentSheet.presentWithPaymentIntent(clientSecret, configuration);
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(PaymentActivity.this, "Không lấy được đủ thông tin từ backend (client secret, customer ID, hoặc ephemeral key).", Toast.LENGTH_LONG).show();
                            finish(); // Đóng Activity nếu thiếu thông tin
                        });
                    }
                } else {
                    final String errorBody = response.body() != null ? response.body().string() : "Không có thân lỗi";
                    runOnUiThread(() -> {
                        Toast.makeText(PaymentActivity.this, "Lỗi backend: " + response.code() + " - " + errorBody, Toast.LENGTH_LONG).show();
                        Log.e("PaymentActivity", "Lỗi backend: " + response.code() + " - " + errorBody);
                        finish(); // Đóng Activity nếu có lỗi
                    });
                }
            }
        });
    }

    private void onPaymentSheetResult(@NonNull final PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            // Thanh toán thành công
            Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
            Log.d("PaymentActivity", "Payment completed.");
            // Bạn có thể gửi kết quả về màn hình trước đó hoặc chuyển sang màn hình xác nhận đơn hàng
            setResult(RESULT_OK);
        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            // Người dùng hủy thanh toán
            Toast.makeText(this, "Thanh toán đã bị hủy.", Toast.LENGTH_SHORT).show();
            Log.d("PaymentActivity", "Payment canceled.");
            setResult(RESULT_CANCELED);
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            // Thanh toán thất bại
            Toast.makeText(this, "Thanh toán thất bại: " + ((PaymentSheetResult.Failed) paymentSheetResult).getError().getMessage(), Toast.LENGTH_LONG).show();
            Log.e("PaymentActivity", "Payment failed", ((PaymentSheetResult.Failed) paymentSheetResult).getError());
            setResult(RESULT_CANCELED);
        }
        finish();
    }
}