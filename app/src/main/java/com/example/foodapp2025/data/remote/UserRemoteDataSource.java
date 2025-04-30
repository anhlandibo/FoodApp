package com.example.foodapp2025.data.remote;

import android.media.MediaPlayer;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodapp2025.data.model.UserModel;
import com.example.foodapp2025.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.auth.User;

import java.util.HashMap;
import java.util.Map;

public class UserRemoteDataSource {
    private final CollectionReference userRef;

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
    public void updateUserLocation(String uid, double lat, double lon) {
        GeoPoint geoPoint = new GeoPoint(lat, lon);
        userRef.document(uid).update("location", geoPoint)
                .addOnSuccessListener(aVoid -> {

                })
                .addOnFailureListener(e -> {

                });
    }

    // Lấy userId của người dùng hiện tại
    public String getUserID(){
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
    public boolean isValidPhoneNumber(String phone) {
        return phone.matches("^\\d{10}$");
    }
}