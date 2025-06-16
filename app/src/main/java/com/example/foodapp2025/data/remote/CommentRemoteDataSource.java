// CommentRemoteDataSource.java
package com.example.foodapp2025.data.remote;

import androidx.annotation.OptIn;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.media3.common.util.UnstableApi;

import com.example.foodapp2025.data.model.CommentModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import com.google.type.DateTime;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentRemoteDataSource {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration commentsListenerRegistration;
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

    @OptIn(markerClass = UnstableApi.class)
    public LiveData<Float> getAverageRatingLiveData(String foodId) {
        MutableLiveData<List<CommentModel>> commentsInternalLiveData = new MutableLiveData<>();

        // If there's an existing listener for this foodId or a general one, remove it first
        if (commentsListenerRegistration != null) {
            commentsListenerRegistration.remove();
        }

        commentsListenerRegistration = getCommentsQuery(foodId) // Assuming getCommentsQuery is in this class or accessible
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        androidx.media3.common.util.Log.e("ViewModel", "Error listening to comments", error);
                        commentsInternalLiveData.setValue(new ArrayList<>()); // Post empty list on error
                        return; // Return early on error
                    }
                    if (value != null) {
                        List<CommentModel> list = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            CommentModel comment = doc.toObject(CommentModel.class);
                            if (comment != null && "approved".equals(comment.getModerationStatus())) {
                                list.add(comment);
                            }
                        }
                        commentsInternalLiveData.setValue(list);
                    } else {
                        // It's also good to handle the case where value is null but error is also null,
                        // though less common for snapshot listeners.
                        commentsInternalLiveData.setValue(new ArrayList<>());
                    }
                });

        return Transformations.map(commentsInternalLiveData, comments -> {
            // ... your existing transformation logic ...
            if (comments == null || comments.isEmpty()) {
                return 0.0f;
            }
            float totalRating = 0;
            int validCommentsCount = 0;
            for (CommentModel cmt : comments) {
                if (cmt != null) {
                    totalRating += cmt.getRating();
                    validCommentsCount++;
                }
            }
            if (validCommentsCount == 0) {
                return 0.0f;
            }
            return totalRating / validCommentsCount;
        });
    }


//    @Override
//    protected void onCleared() {
//        super.onCleared();
//        if (commentsListenerRegistration != null) {
//            commentsListenerRegistration.remove(); // IMPORTANT: Clean up listener
//        }
//    }
}
