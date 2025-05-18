// ChatViewModel.java
package com.example.foodapp2025.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodapp2025.data.model.MessageChatModel;
import com.example.foodapp2025.data.repository.ChatRepository;
import com.google.firebase.Timestamp;

import java.util.List;

public class ChatViewModel extends ViewModel {
    private final MutableLiveData<List<MessageChatModel>> messages = new MutableLiveData<>();
    private final ChatRepository repository;

    public ChatViewModel() {
        repository = new ChatRepository();
    }

    public LiveData<List<MessageChatModel>> getMessages() {
        return messages;
    }

    /** User gửi tin: chỉ cần text, time, senderType (0=user, 1=restaurant) */
    public void sendMessage(String text, Timestamp time, int senderType) {
        MessageChatModel message = new MessageChatModel(text, senderType, time);
        repository.sendMessage(getCurrentUserId(), message);
    }

    /** Load message theo user hiện tại */
    public void loadMessages() {
        repository.loadMessagesForUser(getCurrentUserId(), messages);
    }

    private String getCurrentUserId() {
        return com.google.firebase.auth.FirebaseAuth
                .getInstance()
                .getCurrentUser()
                .getUid();
    }
}
