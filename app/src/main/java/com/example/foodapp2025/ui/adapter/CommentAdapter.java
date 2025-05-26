// CommentAdapter.java
package com.example.foodapp2025.ui.adapter;

import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp2025.R;
import com.example.foodapp2025.data.model.CommentModel;
import com.example.foodapp2025.ui.activity.CommentActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<CommentModel> commentList;
    private OnDeleteClickListener onDeleteClickListener;
    private FirebaseAuth firebaseAuth;

    public interface OnDeleteClickListener {
        void onDeleteClick(String userId, CommentModel comment, int position);
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    private ImageView delBtn;

    public void setData(List<CommentModel> list) {
        this.commentList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentModel comment = commentList.get(position);

        holder.tvUserName.setText(comment.getUserName());
        holder.tvCommentText.setText(comment.getText());
        holder.ratingBar.setRating(comment.getRating());
        holder.tvCommentTime.setText(formatTime(comment.getTimestamp()));

        holder.deleteBtn.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                // Gọi phương thức onDeleteClick của listener, truyền comment và vị trí
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                onDeleteClickListener.onDeleteClick(userId, comment, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList == null ? 0 : commentList.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvCommentText, tvCommentTime;
        RatingBar ratingBar;
        ImageView deleteBtn;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvCommentText = itemView.findViewById(R.id.tvCommentText);
            tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
            ratingBar = itemView.findViewById(R.id.ratingBarSmall);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }
    }

    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
