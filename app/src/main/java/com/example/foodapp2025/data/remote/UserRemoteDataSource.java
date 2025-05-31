package com.example.foodapp2025.data.remote;

import static android.app.Activity.RESULT_OK;
import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodapp2025.R;
import com.example.foodapp2025.data.model.UserModel;
import com.example.foodapp2025.databinding.FragmentProfileBinding;
import com.example.foodapp2025.utils.LocationConverter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class UserRemoteDataSource {
    private final CollectionReference userRef;
    private static final String TAG = "UserViewModel";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private MutableLiveData<String> avatarUploadStatus = new MutableLiveData<>(); // "Success", "Failed", "Loading"
    private MutableLiveData<String> newAvatarUrl = new MutableLiveData<>();

    public UserRemoteDataSource() {
        userRef = FirebaseService.getInstance().getFirestore().collection("users");
    }

    // Lấy thông tin tọa độ người dùng từ Firestore
    public LiveData<UserModel> getUserLocation(String userId) {
        MutableLiveData<UserModel> userLiveData = new MutableLiveData<>();
        userRef.document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                UserModel user = documentSnapshot.toObject(UserModel.class);
                userLiveData.setValue(user);
            } else
                // Handle khi không tìm thấy người dùng
                userLiveData.setValue(null);
        }).addOnFailureListener(e -> {
            userLiveData.setValue(null);
        });
        return userLiveData;
    }

    // Cập nhật thông tin tọa độ người dùng
    public void updateUserLocation(Context context, String uid, double lat, double lon) {
        GeoPoint geoPoint = new GeoPoint(lat, lon);
        userRef.document(uid).update("location", geoPoint)
                .addOnSuccessListener(aVoid -> {
                    LocationConverter.getAddressFromCoordinatesAsync(context, geoPoint, new LocationConverter.AddressResultListener() {
                        @Override
                        public void onAddressReceived(String address) {
                            userRef.document(uid).update("address", address)
                                    .addOnSuccessListener(aVoid -> {
                                    })
                                    .addOnFailureListener(aVoid -> {
                                    });
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {

                });
    }

    // Lấy userId của người dùng hiện tại
    public String getUserID() {
        return FirebaseService.getInstance().getFirebaseAuth().getCurrentUser().getUid();
    }

    // Lấy thông tin của user
    public LiveData<UserModel> getUserInformation(String userId) {
        MutableLiveData<UserModel> userLiveData = new MutableLiveData<>();
        userRef.document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                UserModel user = documentSnapshot.toObject(UserModel.class);
                userLiveData.setValue(user);
            } else
                // Handle khi không tìm thấy người dùng
                userLiveData.setValue(null);
        }).addOnFailureListener(e -> {
            userLiveData.setValue(null);
        });
        return userLiveData;
    }

    public void handleEditBtn(View view, FragmentProfileBinding binding, boolean isEditBtnPressed) {
        if (!isEditBtnPressed) {
            isEditBtnPressed = true;
            binding.fullName.setEnabled(true);
            binding.phoneNumber.setEnabled(true);
            binding.address.setEnabled(true);
            binding.dateOfBirth.setEnabled(true);
            binding.gender.setEnabled(true);
            binding.editBtn.setText("Save");
        } else {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DocumentReference userRef = db.collection("users").document(userId);

            Map<String, Object> updates = new HashMap<>();

            updates.put("name", binding.fullName.getText().toString());
            String phoneNumber = binding.phoneNumber.getText().toString();
            if (!isValidPhoneNumber(phoneNumber)) {
                Toast.makeText(view.getContext(), "Invalid phone number", Toast.LENGTH_SHORT).show();
                return;
            }
            updates.put("phoneNumber", phoneNumber);
            updates.put("address", binding.address.getText().toString());
            updates.put("dateOfBirth", binding.dateOfBirth.getText().toString());
            updates.put("gender", binding.gender.getSelectedItem().toString());

            userRef.update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(view.getContext(), "Updated profile successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(view.getContext(), "Error updating profile", Toast.LENGTH_SHORT).show();
                    });

            binding.fullName.setEnabled(false);
            binding.phoneNumber.setEnabled(false);
            binding.address.setEnabled(false);
            binding.dateOfBirth.setEnabled(false);
            binding.gender.setEnabled(false);
            binding.editBtn.setText("Edit Profile");
        }
    }

    public void handleResetPassword(Context context) {
        // Lấy thông tin người dùng hiện tại
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String email = currentUser.getEmail();

            if (email != null && !email.isEmpty()) {
                // Gửi email đặt lại mật khẩu
                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Reset link sent to your email", Toast.LENGTH_SHORT).show();
                            } else {
                                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Failed to send reset email";
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(context, "No email associated with this account", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "User is not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isValidPhoneNumber(String phone) {
        return phone.matches("^\\d{10}$");
    }

    public LiveData<String> getAvatarUploadStatus() {
        return avatarUploadStatus;
    }

    public LiveData<String> getNewAvatarUrl() {
        return newAvatarUrl;
    }

    public void uploadAvatar(Uri imageUri, Context context) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null || imageUri == null) {
            avatarUploadStatus.postValue("Failed: No user or image selected.");
        }

        Log.d("Image URI", "Image URI: " + imageUri);

        avatarUploadStatus.postValue("Loading");
        String newPublicId = currentUser.getUid() + "_" + System.currentTimeMillis();

        MediaManager.get().upload(imageUri)
                .unsigned("android_do_an_avatars_unsigned")
                .option("public_id", newPublicId)

                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d("CloudinaryUpload", "Upload started: " + requestId);
                        Toast.makeText(context, "Đang tải ảnh đại diện...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        double progress = (double) bytes / totalBytes;
                        Log.d("CloudinaryUpload", "Progress: " + (int)(progress * 100) + "%");
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {

                        String imageUrl = (String) resultData.get("url"); // URL công khai của ảnh đã tải lên!
                        Log.d("CloudinaryUpload", "Upload successful! URL: " + imageUrl);
                        Toast.makeText(context, "Ảnh đại diện đã được cập nhật.", Toast.LENGTH_LONG).show();

                        updateUserAvatarUrl(currentUser.getUid(), imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        // hideProgressDialog(); // Ẩn UI tải lên nếu có
                        Log.e("CloudinaryUpload", "Upload failed: " + error.getDescription());
                        Toast.makeText(context, "Lỗi tải ảnh: " + error.getDescription(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.w("CloudinaryUpload", "Upload rescheduled: " + error.getDescription());
                    }
                })
                .dispatch(); // Bắt đầu quá trình tải lên Cloudinary
    }


    // Phương thức để cập nhật URL ảnh đại diện vào Firestore
    private void updateUserAvatarUrl(String userId, String photoUrl) {
        db.collection("users").document(userId)
                .update("photoUrl", photoUrl) // Cập nhật trường photoUrl
                .addOnSuccessListener(aVoid -> {
                    avatarUploadStatus.postValue("Success");
                    newAvatarUrl.postValue(photoUrl); // Báo hiệu URL mới
                    Log.d(TAG, "Photo URL updated successfully for user: " + userId);
                })
                .addOnFailureListener(e -> {
                    avatarUploadStatus.postValue("Failed: Update Firestore failed.");
                    Log.e(TAG, "Failed to update Photo URL in Firestore", e);
                });
    }


}