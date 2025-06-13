package com.example.foodapp2025.data.remote;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodapp2025.data.model.MessageChatModel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;              // ← NEW
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;                                      // ← NEW
import java.util.List;
import java.util.Map;                                          // ← NEW

public class ChatRemoteDataSource {
    private static final String TAG = "ChatRemoteDataSource";
    private final FirebaseFirestore db;
    private final CollectionReference chatsCollection;

    public ChatRemoteDataSource() {
        db = FirebaseFirestore.getInstance();
        chatsCollection = db.collection("chats");
    }

    /**
     * Send a message and update userChats subcollections.
     */
    public void sendMessage(String chatId, MessageChatModel message, String senderId, String receiverId) {
        DocumentReference chatRef = chatsCollection.document(chatId);

        // Save message to chats/{chatId}/messages
        chatRef.collection("messages").add(message)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Message sent to chats collection");

                    updateUserChatMetadata(chatId, senderId, receiverId, message);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to send message", e));
    }

    /**
     * Update both users’ userChats/{uid}/chats/{chatId} docs with metadata.
     */
    private void updateUserChatMetadata(String chatId, String senderId, String receiverId, MessageChatModel message) {
        // First: fetch sender info
        db.collection("users").document(senderId).get().addOnSuccessListener(senderSnap -> {
            if (!senderSnap.exists()) return;

            // Then: fetch receiver info
            db.collection("users").document(receiverId).get().addOnSuccessListener(receiverSnap -> {
                if (!receiverSnap.exists()) return;

                Map<String, Object> senderInfo = new HashMap<>();
                senderInfo.put("uid", senderId);
                senderInfo.put("displayName", senderSnap.getString("name"));
                senderInfo.put("photoUrl", senderSnap.getString("photoUrl"));

                Map<String, Object> receiverInfo = new HashMap<>();
                receiverInfo.put("uid", receiverId);
                receiverInfo.put("displayName", receiverSnap.getString("name"));
                receiverInfo.put("photoUrl", receiverSnap.getString("photoUrl"));

                Map<String, Object> senderChat = new HashMap<>();
                senderChat.put("userInfo", receiverInfo);
                senderChat.put("lastMessage", Collections.singletonMap("text", message.getText()));
                senderChat.put("date", FieldValue.serverTimestamp());

                Map<String, Object> receiverChat = new HashMap<>();
                receiverChat.put("userInfo", senderInfo);
                receiverChat.put("lastMessage", Collections.singletonMap("text", message.getText()));
                receiverChat.put("date", FieldValue.serverTimestamp());

                // Save to userChats/{senderId}/chats/{chatId}
                db.collection("userChats").document(senderId)
                        .collection("chats").document(chatId)
                        .set(senderChat);

                // Save to userChats/{receiverId}/chats/{chatId}
                db.collection("userChats").document(receiverId)
                        .collection("chats").document(chatId)
                        .set(receiverChat);
            });
        });
    }

    /**
     * Load the messages from chats/{chatId}/messages
     */
    public LiveData<List<MessageChatModel>> loadMessages(String chatId) {
        MutableLiveData<List<MessageChatModel>> messagesLiveData = new MutableLiveData<>();
        DocumentReference chatRef = chatsCollection.document(chatId);

        chatRef.collection("messages")
                .orderBy("time")
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error loading messages", e);
                        return;
                    }
                    List<MessageChatModel> messages = new ArrayList<>();
                    if (snapshot != null) {
                        for (QueryDocumentSnapshot doc : snapshot) {
                            messages.add(doc.toObject(MessageChatModel.class));
                        }
                        messagesLiveData.setValue(messages);
                    }
                });

        return messagesLiveData;
    }

    /**
     * Load the current user's chat list from userChats/{uid}/chats
     */
    public LiveData<List<String>> getChatList(String userId) {
        MutableLiveData<List<String>> chatListLiveData = new MutableLiveData<>();

        db.collection("userChats")
                .document(userId)
                .collection("chats")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> chatIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        chatIds.add(doc.getId());
                    }
                    chatListLiveData.setValue(chatIds);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load user chat list", e));

        return chatListLiveData;
    }
}
