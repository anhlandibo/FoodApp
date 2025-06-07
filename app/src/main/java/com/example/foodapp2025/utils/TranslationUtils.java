package com.example.foodapp2025.utils;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.io.IOException;
import java.lang.ref.WeakReference; // Thêm import này
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TranslationUtils {

    public interface TranslationCallback {
        void onTranslated(String translatedText);
    }

    static OkHttpClient client = new OkHttpClient.Builder()
            .callTimeout(10, TimeUnit.SECONDS)
            .build();

    // Để tối ưu hóa, tạo một Gson instance duy nhất
    private static final Gson gson = new Gson(); // Khởi tạo một lần

    public static void translate(Context context, String inputText, String sourceLang, String targetLang, TranslationCallback callback) {
        // Sử dụng WeakReference để tránh rò rỉ bộ nhớ
        WeakReference<Context> contextRef = new WeakReference<>(context);
        String json = gson.toJson(new TranslationRequest(inputText, sourceLang, targetLang));
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url("https://libretranslate.de/translate")
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                Context c = contextRef.get(); // Lấy Context từ WeakReference
                if (c instanceof Activity && c != null) { // Đảm bảo Activity còn tồn tại
                    ((Activity) c).runOnUiThread(() -> callback.onTranslated("Translation failed"));
                }
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                Context c = contextRef.get(); // Lấy Context từ WeakReference
                if (c instanceof Activity && c != null) { // Đảm bảo Activity còn tồn tại
                    if (response.isSuccessful()) {
                        String responseBodyString = response.body() != null ? response.body().string() : "";
                        TranslationResponse translated = gson.fromJson(responseBodyString, TranslationResponse.class);
                        ((Activity) c).runOnUiThread(() -> callback.onTranslated(translated.translatedText));
                    } else {
                        // Tối ưu hóa: lấy body lỗi nếu có
                        String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                        ((Activity) c).runOnUiThread(() -> callback.onTranslated("Error: " + response.code() + " - " + errorBody));
                    }
                }
            }
        });
    }

    static class TranslationRequest {
        String q;
        String source;
        String target;

        public TranslationRequest(String q, String source, String target) {
            this.q = q;
            this.source = source;
            this.target = target;
        }
    }

    static class TranslationResponse {
        String translatedText;
    }
}