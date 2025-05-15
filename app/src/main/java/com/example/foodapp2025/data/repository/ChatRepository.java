package com.example.foodapp2025.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.foodapp2025.data.model.MessageChatModel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration messageListener;

    public void sendMessage(String userId, MessageChatModel message) {
        String restaurantId = "restaurant";

        // 1. Thêm message mới
        db.collection("userChats")
                .document(userId)
                .collection("chats")
                .document(restaurantId)
                .collection("messages")
                .add(message);

        // 2. Cập nhật tin nhắn cuối cùng
        Map<String, Object> userMeta = new HashMap<>();
        userMeta.put("lastMessage", Collections.singletonMap("text", message.getText()));
        userMeta.put("date", FieldValue.serverTimestamp());

        db.collection("userChats")
                .document(userId)
                .collection("chats")
                .document(restaurantId)
                .set(userMeta, SetOptions.merge());

        // 3. Cập nhật hộp thư nhà hàng
        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    String name = doc.getString("name");
                    String photo = doc.getString("photoURL");
                    if (name == null) name = "";
                    if (photo == null) photo = "";

                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("uid", userId);
                    userInfo.put("displayName", name);
                    userInfo.put("photoURL", photo);
                    userInfo.put("lastMessage", Collections.singletonMap("text", message.getText()));
                    userInfo.put("date", FieldValue.serverTimestamp());

                    db.collection("restaurantInbox")
                            .document(userId)
                            .set(userInfo, SetOptions.merge());
                });
    }


    public void loadMessagesForUser(String userId, MutableLiveData<List<MessageChatModel>> liveData) {
        String restaurantId = "restaurant";

        db.collection("userChats")
                .document(userId)
                .collection("chats")
                .document(restaurantId)
                .collection("messages")
                .orderBy("time")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    List<MessageChatModel> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots) {
                        list.add(doc.toObject(MessageChatModel.class));
                    }
                    liveData.setValue(list);
                });
    }


    public void getUserChatList(String userId, MutableLiveData<List<String>> chatIdsLiveData) {
        db.collection("userChats")
                .document(userId)
                .collection("chats")
                .get()
                .addOnSuccessListener(qs -> {
                    List<String> ids = new ArrayList<>();
                    for (DocumentSnapshot d : qs) ids.add(d.getId());
                    chatIdsLiveData.setValue(ids);
                });
    }
}
