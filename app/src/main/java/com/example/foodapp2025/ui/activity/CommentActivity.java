package com.example.foodapp2025.ui.activity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp2025.R;
import com.example.foodapp2025.data.model.CommentModel;
import com.example.foodapp2025.data.model.UserModel;
import com.example.foodapp2025.ui.adapter.CommentAdapter;
import com.example.foodapp2025.viewmodel.CommentViewModel;
import com.example.foodapp2025.viewmodel.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.auth.User;

import java.util.List;

public class CommentActivity extends AppCompatActivity {

    private RecyclerView rvComments;
    private EditText etComment;
    private RatingBar ratingBar;
    private Button btnSend;
    private ImageView backBtn;
    private CommentViewModel viewModel;
    private UserViewModel userViewModel;
    private String userName;
    private String userId;
    private String foodId; // lấy từ intent gửi qua
    private CommentAdapter commentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        rvComments = findViewById(R.id.rvComments);
        etComment = findViewById(R.id.etComment);
        ratingBar = findViewById(R.id.ratingBar);
        btnSend = findViewById(R.id.btnSend);
        backBtn = findViewById(R.id.backBtn);

        viewModel = new ViewModelProvider(this).get(CommentViewModel.class);
        userViewModel = new UserViewModel();

        foodId = getIntent().getStringExtra("FOOD_ID");

        setupRecyclerView();
        observeComments();

        btnSend.setOnClickListener(v -> handleSendComment());
        backBtn.setOnClickListener(v -> finish());

    }

    private void setupRecyclerView() {
        commentAdapter = new CommentAdapter();
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(commentAdapter);
    }

    private void observeComments() {
        viewModel.getComments(foodId).observe(this, comments -> {
            commentAdapter.setData(comments);
        });

        commentAdapter.setOnDeleteClickListener(new CommentAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(String userId, CommentModel commentToDelete, int position) {
                if (userId != null && userId.equals(commentToDelete.getUserId())) {
                    handleDeleteComment(userId);
                } else {
                    Toast.makeText(CommentActivity.this, "Bạn không có quyền xóa comment này.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void handleSendComment() {
        String commentText = etComment.getText().toString().trim();
        float stars = ratingBar.getRating();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // <-- LƯU Ý QUAN TRỌNG: getUserInformation là async.
        // Bạn cần đảm bảo userId và userName đã có trước khi gọi postOrUpdateComment.
        // Cách tốt nhất là chờ userModel.getName() và getUid() hoàn tất.
        // Hoặc truyền user.getUid() trực tiếp nếu bạn muốn xử lý userId trong Repository.
        // Hiện tại, code của bạn có thể gọi postOrUpdateComment với userId và userName là null
        // nếu observe không kịp.
        if (user != null) {
            userViewModel.getUserInformation(user.getUid()).observe(this, userModel -> {
                if (userModel != null) {
                    userName = userModel.getName();
                    userId = userModel.getUid();

                    if (commentText.isEmpty() || stars == 0) {
                        Toast.makeText(this, "Describe your experience", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // <-- SỬA Ở ĐÂY: Xử lý post/update comment sau khi có userId/userName
                    // Loại bỏ observe(this, existingComment -> {}) lồng nhau vì nó không cần thiết cho logic post mới
                    CommentModel comment = new CommentModel();
                    comment.setText(commentText);
                    comment.setRating(stars);
                    comment.setTimestamp(System.currentTimeMillis());
                    comment.setUserId(userId);
                    comment.setUserName(userName);
                    // ModerationStatus sẽ được đặt trong Repository

                    viewModel.postOrUpdateComment(foodId, comment).observe(this, success -> {
                        if (success) {
                            Toast.makeText(this, "Comment sent", Toast.LENGTH_SHORT).show();
                            etComment.setText("");
                            ratingBar.setRating(0);
                            // Sau khi comment thành công, làm mới danh sách bình luận
                            viewModel.getComments(foodId); // Gọi lại để refresh UI
                        } else {
                            // <-- THÊM Ở ĐÂY: Thông báo rõ ràng hơn khi bị chặn bởi kiểm duyệt
                            Toast.makeText(this, "Failed to save comment. Your comment may contain inappropriate words.", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Toast.makeText(this, "Failed to get user info.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleDeleteComment(String userId) {
        viewModel.deleteComment(foodId, userId).observe(this, success -> {
            if (success) {
                Toast.makeText(this, "Comment deleted.", Toast.LENGTH_SHORT).show();
                viewModel.getComments(foodId); // Refresh comments after deletion
            } else {
                Toast.makeText(this, "Failed to delete comment.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}