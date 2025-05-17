// ChatFragment.java
package com.example.foodapp2025.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp2025.R;
import com.example.foodapp2025.data.model.MessageChatModel;
import com.example.foodapp2025.ui.adapter.MessageChatAdapter;
import com.example.foodapp2025.viewmodel.ChatViewModel;
import com.google.firebase.Timestamp;

import java.util.ArrayList;

public class ChatFragment extends Fragment {

    private EditText messageET;
    private ImageView sendBtn, backBtn;
    private RecyclerView recyclerView;
    private MessageChatAdapter adapter;
    private ChatViewModel chatViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatbox, container, false);

        messageET = view.findViewById(R.id.messageET);
        sendBtn   = view.findViewById(R.id.sendBtn);
        backBtn   = view.findViewById(R.id.backBtn);
        recyclerView = view.findViewById(R.id.recycler_view);

        // Setup RecyclerView
        adapter = new MessageChatAdapter(new ArrayList<>(), getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Setup ViewModel + load real-time
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        chatViewModel.getMessages().observe(getViewLifecycleOwner(), msgs -> {
            adapter.setMessages(msgs);
            if (!msgs.isEmpty()) {
                recyclerView.scrollToPosition(msgs.size() - 1);
            }
        });
        chatViewModel.loadMessages();

        // Gửi tin
        sendBtn.setOnClickListener(v -> {
            String text = messageET.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(getContext(), "Message cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }
            // 0 = user, hoặc đổi thành 1 nếu là nhà hàng
            chatViewModel.sendMessage(text, Timestamp.now(), /*senderType=*/0);
            messageET.setText("");
        });

        // Nút back đơn giản
        backBtn.setOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }
}
