package com.example.foodapp2025.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodapp2025.data.repository.AuthRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterViewModel extends ViewModel {
//    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
//    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
//
//    private MutableLiveData<String> registerStatus = new MutableLiveData<>();
//    public LiveData<String> getRegisterStatus(){
//        return registerStatus;
//    }
//
//    public void registerUser(String name, String email, String password){
//        if (name.isEmpty() || email.isEmpty() || password.length() < 6){
//            registerStatus.setValue("Vui long nhap day du thong tin va mat khau it nhat 6 ki tu");
//            return;
//        }
//
//        firebaseAuth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()){
//                        FirebaseUser user = firebaseAuth.getCurrentUser();
//                        if (user != null){
//                            String uid = user.getUid();
//                            Map<String, Object> userData = new HashMap<>();
//                            userData.put("name", name);
//                            userData.put("email", email);
//                            userData.put("uid", uid);
//
//                            firestore.collection("users").document(uid).set(userData)
//                                    .addOnSuccessListener(Void -> registerStatus.setValue("Dang ky thanh cong"))
//                                    .addOnFailureListener(e -> registerStatus.setValue("That bai"));
//
//                        }
//                    }
//                    else{
//                        registerStatus.setValue("Loi: " + task.getException().getMessage());
//
//                    }
//                });
//    }

}
