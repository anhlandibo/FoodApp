package com.example.foodapp2025.ui.activity;
import android.content.Intent;
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
    private static final String BACKEND_URL = "http://10.0.2.2:8080/";
    private final OkHttpClient httpClient = new OkHttpClient();
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private PaymentSheet paymentSheet;
    private String clientSecret;
    private double totalAmount;
    private String orderId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        if (getIntent().getExtras() != null){
            totalAmount = getIntent().getDoubleExtra("totalAmount", 0.0);
            orderId = getIntent().getStringExtra("orderId");
            userId = getIntent().getStringExtra("userId");
        } else {
            Toast.makeText(this, "Error: Payment information not received.", Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        if (totalAmount <= 0 || orderId == null || userId == null) {
            Toast.makeText(this, "Error: Invalid payment information.", Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        PaymentConfiguration.init(this, "pk_test_51RTkupCxcqExvemRVizQ83Ufcad3APCrcTODqH8oSUhkHpy4aSjZWJJuHlStgFlzhiAqz8MEsqsj9rrynRtlBT4300fH5EbzR8");

        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);
        startCheckout();
    }

    private void startCheckout() {

        Map<String, Object> payMap = new HashMap<>();
        payMap.put("amount", (long) totalAmount);
        payMap.put("currency", "vnd");
        payMap.put("orderId", orderId);
        payMap.put("userId", userId);

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
                    Toast.makeText(PaymentActivity.this, "Netword error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("PaymentActivity", "Error when calling backend", e);
                    setResult(RESULT_CANCELED);
                    finish();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d("PaymentActivity", "Received PaymentIntent response: " + responseData);

                    Map<String, String> responseMap = gson.fromJson(responseData, Map.class);
                    clientSecret = responseMap.get("clientSecret");
                    String customerId = responseMap.get("customerId");
                    String ephemeralKey = responseMap.get("ephemeralKey");

                    if (clientSecret != null && customerId != null && ephemeralKey != null) {
                        Log.d("PaymentActivity", "Received clientSecret: " + clientSecret + ", CustomerId: " + customerId + ", EphemeralKey: " + ephemeralKey);
                        runOnUiThread(() -> {
                            PaymentSheet.Configuration configuration = new PaymentSheet.Configuration(
                                    "FoodApp",
                                    new PaymentSheet.CustomerConfiguration(
                                            customerId,
                                            ephemeralKey
                                    )
                            );
                            paymentSheet.presentWithPaymentIntent(clientSecret, configuration);
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(PaymentActivity.this, "Failed to retrieve complete information from backend (client secret, customer ID, or ephemeral key).", Toast.LENGTH_LONG).show();
                            setResultAndFinish(RESULT_CANCELED);
                        });
                    }
                } else {
                    final String errorBody = response.body() != null ? response.body().string() : "No error body";
                    runOnUiThread(() -> {
                        Toast.makeText(PaymentActivity.this, "Error from backend: " + response.code() + " - " + errorBody, Toast.LENGTH_LONG).show();
                        Log.e("PaymentActivity", "Error from backend: " + response.code() + " - " + errorBody);
                        setResultAndFinish(RESULT_CANCELED);
                    });
                }
            }
        });
    }

    private void onPaymentSheetResult(@NonNull final PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            // Thanh toán thành công
            Toast.makeText(this, "Payment successful!", Toast.LENGTH_SHORT).show();
            Log.d("PaymentActivity", "Payment completed.");
            setResultAndFinish(RESULT_OK);
        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            // Người dùng hủy thanh toán
            Toast.makeText(this, "Payment canceled.", Toast.LENGTH_SHORT).show();
            Log.d("PaymentActivity", "Payment canceled.");
            setResultAndFinish(RESULT_CANCELED);
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            // Thanh toán thất bại
            Toast.makeText(this, "Payment failed: " + ((PaymentSheetResult.Failed) paymentSheetResult).getError().getMessage(), Toast.LENGTH_LONG).show();
            Log.e("PaymentActivity", "Payment failed", ((PaymentSheetResult.Failed) paymentSheetResult).getError());
            setResultAndFinish(RESULT_CANCELED);
        }
        finish();
    }
    private void setResultAndFinish(int resultCode) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("orderId", orderId);
        setResult(resultCode, resultIntent);
        finish();
    }
}