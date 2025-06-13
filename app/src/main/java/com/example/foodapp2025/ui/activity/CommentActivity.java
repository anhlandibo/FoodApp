// CommentActivity.java
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
import androidx.lifecycle.LiveData;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.util.List;

public class CommentActivity extends AppCompatActivity {

    private RecyclerView rvComments;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
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
        backBtn.setOnClickListener(v -> {
            finish();
        });

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

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(this, "User not logged in. Please log in to comment.", Toast.LENGTH_SHORT).show();
            return; // Exit if no user is logged in
        }

        // Ensure comment text and rating are provided before fetching user info (minor optimization)
        if (commentText.isEmpty() || stars == 0) {
            Toast.makeText(this, "Please describe your experience and provide a rating.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Observe user information
        userViewModel.getUserInformation(firebaseUser.getUid()).observe(this, userModel -> {
            if (userModel != null && userModel.getName() != null && userModel.getUid() != null) {
                String currentUserName = userModel.getName();
                String currentUserId = userModel.getUid();

                CommentModel comment = new CommentModel();
                comment.setText(commentText);
                comment.setRating(stars);
                comment.setTimestamp(System.currentTimeMillis());
                comment.setUserId(currentUserId); // Use the fetched userId
                comment.setUserName(currentUserName); // Use the fetched userName
                // ModerationStatus will be set in Repository

                // Call postOrUpdateComment only ONCE
                viewModel.postOrUpdateComment(foodId, comment).observe(this, success -> {
                    if (Boolean.TRUE.equals(success)) { // Check for Boolean.TRUE to avoid NPE if LiveData emits null
                        Toast.makeText(this, "Comment sent successfully.", Toast.LENGTH_SHORT).show();
                        etComment.setText("");
                        ratingBar.setRating(0);

//
//                        avgRatingLiveData.observe(this, avgRating -> {
//                            db.collection("foods")
//                                    .document(foodId)
//                                            .update("star", avgRating);
//                                });
                        viewModel.getComments(foodId); // Refresh UI
                    } else {
                        Toast.makeText(this, "Failed to save comment. It may contain inappropriate words or a network error occurred.", Toast.LENGTH_LONG).show();
                    }
                });

            } else {
                // This 'else' means userModel was null or incomplete after the observe
                Toast.makeText(this, "Failed to retrieve user details. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });

        LiveData<Float> avgRatingLiveData = viewModel.getAverageRatingLiveData(foodId);

        avgRatingLiveData.observe(this, avgRating -> {
            db.collection("food")
                    .document(foodId)
                    .update("star", avgRating);
        });
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
