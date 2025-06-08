// Đặt trong package của ứng dụng của bạn, ví dụ: com.example.foodapp2025.utils
package com.example.foodapp2025.utils;

import android.util.Log;

import com.example.foodapp2025.R; // Đảm bảo R.xml.remote_config_defaults là đúng
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;
import com.google.firebase.firestore.FirebaseFirestore;


public class CommentModerateUtils { // <-- TÊN LỚP MỚI

    private static final String TAG = "CommentModerateUtil"; // TAG có thể đổi theo tên lớp mới
    private static CommentModerateUtils instance; // <-- CẬP NHẬT: Kiểu của instance
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private List<String> bannedWords = new ArrayList<>();
    private final Gson gson = new Gson();

    private CommentModerateUtils() { // <-- CẬP NHẬT: Constructor phải khớp với tên lớp
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(TimeUnit.HOURS.toSeconds(1))
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Remote Config defaults set successfully.");
                        fetchBannedWords();
                    } else {
                        Log.e(TAG, "Failed to set Remote Config defaults.");
                        fetchBannedWords();
                    }
                });
    }

    public static synchronized CommentModerateUtils getInstance() { // <-- CẬP NHẬT: Kiểu trả về và khởi tạo
        if (instance == null) {
            instance = new CommentModerateUtils(); // <-- CẬP NHẬT: Khởi tạo với tên lớp mới
        }
        return instance;
    }

    public void fetchBannedWords() {
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean updated = task.getResult();
                        Log.d(TAG, "Config params updated: " + updated);
                        String bannedWordsJson = mFirebaseRemoteConfig.getString("banned_words_list");
                        Type type = new TypeToken<List<String>>() {}.getType();
                        List<String> fetchedList = gson.fromJson(bannedWordsJson, type);

                        if (fetchedList != null) {
                            bannedWords = fetchedList;
                            Log.d(TAG, "Banned words loaded: " + bannedWords.toString());
                        } else {
                            bannedWords = new ArrayList<>();
                            Log.w(TAG, "Banned words list is null after fetching, using empty list.");
                        }
                    } else {
                        Log.e(TAG, "Failed to fetch config for banned words.", task.getException());
                    }
                });
    }

    public boolean containsBannedWord(String comment) {
        if (comment == null || comment.trim().isEmpty()) {
            return false;
        }

        String lowerCaseComment = comment.toLowerCase();

        for (String bannedWord : bannedWords) {
            String regexBannedWord = bannedWord.toLowerCase()
                    .replaceAll("[*._\\-]", ".*")
                    .replaceAll("\\s+", "\\s*");

            Pattern pattern = Pattern.compile("\\b" + regexBannedWord + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(lowerCaseComment);

            if (matcher.find()) {
                Log.d(TAG, "Banned word found: '" + bannedWord + "' in comment: '" + comment + "'");
                return true;
            }
        }
        return false;
    }

    // Phương thức này chỉ là một ví dụ về cách bạn sẽ submit comment lên Firestore.
    // Logic submit thực tế nên nằm ở lớp Repository của bạn.
    public Task<DocumentReference> submitCommentToFirestore(String userId, String commentText, FirebaseFirestore firestore) {
        if (containsBannedWord(commentText)) {
            Log.w(TAG, "Comment blocked by client-side moderation: " + commentText);
            return null;
        }

        Map<String, Object> commentData = new HashMap<>();
        commentData.put("userId", userId);
        commentData.put("text", commentText);
        commentData.put("timestamp", System.currentTimeMillis());
        commentData.put("moderationStatus", "approved");

        return firestore.collection("comments").add(commentData)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "Comment added with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.e(TAG, "Error adding comment", e));
    }
}