// CommentRemoteDataSource.java
package com.example.foodapp2025.data.remote;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.foodapp2025.data.model.CommentModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import com.google.type.DateTime;

import java.sql.Time;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentRemoteDataSource {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String COMMENTS_COLLECTION = "comments";
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    // Post new comment
    public Task<Void> postComment(String foodId, CommentModel comment) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return Tasks.forException(new Exception("User not authenticated."));
        }

        String userId = user.getUid();

        // Tạo truy vấn tìm bình luận với userId
        Query query = db.collection("comments")
                .document(foodId)
                .collection("FoodComments")
                .whereEqualTo("userId", userId);

        // Chuyển đổi dữ liệu từ CommentModel thành Map
        Map<String, Object> commentData = comment.toMap();
        commentData.put("userId", userId);
        commentData.put("timestamp", System.currentTimeMillis());

        return query.get().continueWithTask(task -> {
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                // Nếu đã tồn tại bình luận, cập nhật dữ liệu
                DocumentSnapshot document = task.getResult().getDocuments().get(0); // Lấy bình luận đầu tiên
                return document.getReference().update(
                        "text", comment.getText(),
                        "rating", comment.getRating(),
                        "timestamp", System.currentTimeMillis() // Cập nhật thời gian nếu cần
                );
            } else {
                // Nếu chưa tồn tại bình luận, tạo mới
                String commentId = foodId + "_cmtId_" + System.currentTimeMillis();
                DocumentReference newDocRef = db.collection("comments")
                        .document(foodId)
                        .collection("FoodComments")
                        .document(commentId);
                return newDocRef.set(commentData);
            }
        });
    }

    // Update comment
    public Task<Void> updateComment(String foodId, CommentModel comment) {
        return postComment(foodId, comment); // Vì dùng set(comment) sẽ tự động cập nhật
    }

    // Delete comment
    public Task<Void> deleteComment(String foodId, String userId) {
        return db.collection("comments")
                .document(foodId)
                .collection("FoodComments")
                .whereEqualTo("userId", userId) // Truy vấn document duy nhất
                .get()
                .continueWithTask(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        // Lấy document đầu tiên từ kết quả truy vấn
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        return document.getReference().delete(); // Xóa tài liệu
                    } else {
                        // Nếu không tìm thấy tài liệu, trả về một lỗi
                        throw new Exception("No document found with the specified userId.");
                    }
                });
    }


    // Get all comments for a food
    public Query getCommentsQuery(String foodId) {
        return db.collection(COMMENTS_COLLECTION)
                .document(foodId)
                .collection("FoodComments")
                .orderBy("timestamp", Query.Direction.DESCENDING);
    }

    // Get current user's comment for a food
    public DocumentReference getUserCommentRef(String foodId) {
        String userId = auth.getCurrentUser().getUid();
        return db.collection(COMMENTS_COLLECTION)
                .document(foodId)
                .collection("FoodComments")
                .document(userId);
    }
}
