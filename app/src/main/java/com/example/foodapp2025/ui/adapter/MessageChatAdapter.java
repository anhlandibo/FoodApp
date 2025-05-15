package com.example.foodapp2025.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp2025.R;
import com.example.foodapp2025.data.model.MessageChatModel;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<MessageChatModel> messageChatModelList;
    private Context context;

    private static final int VIEW_TYPE_MESSAGE_SENT = 0; // Người dùng gửi
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 1; // Nhà hàng gửi

    public MessageChatAdapter(List<MessageChatModel> messageChatModelList, Context context) {
        this.messageChatModelList = messageChatModelList;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        MessageChatModel message = messageChatModelList.get(position);
        // Kiểm tra viewType và phân biệt người gửi
        if (message.getViewType() == VIEW_TYPE_MESSAGE_SENT) {
            return VIEW_TYPE_MESSAGE_SENT; // Gửi bởi người dùng
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED; // Gửi bởi nhà hàng
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(context).inflate(R.layout.viewholder_send, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(context).inflate(R.layout.viewholder_receive, parent, false);
            return new ReceivedMessageHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MessageChatModel message = messageChatModelList.get(position);
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return messageChatModelList.size();
    }

    // Method to update the list of messages
    public void setMessages(List<MessageChatModel> messages) {
        this.messageChatModelList = messages;
        notifyDataSetChanged();  // Notify adapter of changes
    }

    // ViewHolder for sent messages
    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView message;
        TextView time;

        public SentMessageHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.message);
            time = itemView.findViewById(R.id.time);
        }

        void bind(MessageChatModel messageModel) {
            message.setText(messageModel.getText());

            Timestamp timestamp = messageModel.getTime();
            if (timestamp != null) {
                Date date = timestamp.toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                time.setText(sdf.format(date));
            } else {
                time.setText(""); // Fallback
            }
        }
    }

    // ViewHolder for received messages
    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView message;
        TextView time;

        public ReceivedMessageHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.message);
            time = itemView.findViewById(R.id.time);
        }

        void bind(MessageChatModel messageModel) {
            message.setText(messageModel.getText());

            Timestamp timestamp = messageModel.getTime();
            if (timestamp != null) {
                Date date = timestamp.toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                time.setText(sdf.format(date));
            } else {
                time.setText(""); // Fallback
            }
        }
    }
}
