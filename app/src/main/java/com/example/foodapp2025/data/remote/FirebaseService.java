package com.example.foodapp2025.data.remote;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseService {
    private static FirebaseService instance;
    private final FirebaseFirestore firestore;

    private final FirebaseAuth firebaseAuth;

    private FirebaseService() {
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public static synchronized FirebaseService getInstance() {
        if (instance == null) {
            instance = new FirebaseService();
        }
        return instance;
    }

    public FirebaseFirestore getFirestore(){
        return firestore;
    }

    public FirebaseAuth getFirebaseAuth(){
        return firebaseAuth;
    }

}
