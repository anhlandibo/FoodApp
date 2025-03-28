package com.example.foodapp2025.data.remote;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodapp2025.data.model.UserModel;
import com.example.foodapp2025.utils.Result;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthRemoteDataSource {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;

    public  AuthRemoteDataSource(){
        firebaseAuth = FirebaseService.getInstance().getFirebaseAuth();
        firestore = FirebaseFirestore.getInstance();
    }

    //login
    public LiveData<Result<UserModel>> login(String email, String password){
        MutableLiveData<Result<UserModel>> liveData = new MutableLiveData<>();
        liveData.setValue(Result.loading()); //dang tai

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                   if (task.isSuccessful() && task.getResult() != null){
                       FirebaseUser firebaseUser = task.getResult().getUser();
                       if (firebaseUser != null){
                           UserModel userModel = new UserModel(firebaseUser.getEmail(), firebaseUser.getDisplayName(), firebaseUser.getUid());
                           liveData.setValue(Result.success(userModel));
                       }
                       else{
                           liveData.setValue(Result.error("Cannot get the information"));
                       }
                   }
                   else{
                       liveData.setValue(Result.error(task.getException() != null ? task.getException().getMessage() : "Fail to login"));
                   }
                });
        return liveData;
    }

    //register
    public LiveData<Result<UserModel>> register(String email, String password, String name) {
        MutableLiveData<Result<UserModel>> liveData = new MutableLiveData<>();
        liveData.setValue(Result.loading());

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();
                            UserModel user = new UserModel(email, name, uid);

                            //save vao Firestore
                            firestore.collection("users")
                                    .document(uid)
                                    .set(user)
                                    .addOnSuccessListener(s -> liveData.setValue(Result.success(user)))
                                    .addOnFailureListener(e -> liveData.setValue(Result.error("Failed to save to Firestore: "+e.getMessage())));

                        } else {
                            liveData.setValue(Result.error("Cannot get the information"));
                        }
                    } else {
                        liveData.setValue(Result.error(task.getException() != null ? task.getException().getMessage() : "Register unsuccessfully"));
                    }
                });

        return liveData;
    }

    public UserModel getCurrentUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            return new UserModel(firebaseUser.getEmail(), firebaseUser.getDisplayName(), firebaseUser.getUid());
        }
        return null;
    }
    public void logout() {
        firebaseAuth.signOut();
    }

}
