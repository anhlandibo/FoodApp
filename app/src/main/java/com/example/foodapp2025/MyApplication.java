package com.example.foodapp2025;
import android.app.Application;
import okhttp3.OkHttpClient;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class MyApplication extends Application {
    public void onCreate() {
        super.onCreate(); // LUÔN LUÔN GỌI dòng này đầu tiên

        String cloudName = getString(R.string.cloudinary_cloud_name);

        if (cloudName != null && !cloudName.isEmpty()) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", cloudName);

            MediaManager.init(this, config);
        }
    }
}
