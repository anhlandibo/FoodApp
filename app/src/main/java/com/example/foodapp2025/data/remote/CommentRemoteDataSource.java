// CommentRemoteDataSource.java
package com.example.foodapp2025.data.remote;

import com.example.foodapp2025.data.model.CommentModel;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class CommentRemoteDataSource {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String COMMENTS_COLLECTION = "comments";
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    // Post new comment
    public Task<Void> postComment(String foodId, CommentModel comment) {
        String userId = auth.getCurrentUser().getUid();
        DocumentReference docRef = db.collection(COMMENTS_COLLECTION)
                .document(foodId)
                .collection("FoodComments")
                .document(userId);

        return docRef.set(comment.toMap());
    }

    // Update comment
    public Task<Void> updateComment(String foodId, CommentModel comment) {
        return postComment(foodId, comment); // Vì dùng set(comment) sẽ tự động cập nhật
    }

    // Delete comment
    public Task<Void> deleteComment(String foodId, String userId) {
        DocumentReference commentRef = FirebaseFirestore.getInstance()
                .collection("food")
                .document(foodId)
                .collection("comments")
                .document(userId);  // Xóa comment của userId

        return commentRef.delete();  // Xóa comment
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
