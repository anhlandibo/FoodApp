package com.example.foodapp2025.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodapp2025.data.model.CommentModel;
import com.example.foodapp2025.data.remote.CommentRemoteDataSource;
import com.example.foodapp2025.utils.CommentModerateUtils; // <-- THÊM IMPORT NÀY
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class CommentRepository {

    private final CommentRemoteDataSource remoteDataSource;
    private final CommentModerateUtils moderationUtil; // <-- THÊM DÒNG NÀY

    public CommentRepository() {
        this.remoteDataSource = new CommentRemoteDataSource();
        this.moderationUtil = CommentModerateUtils.getInstance(); // <-- THÊM DÒNG NÀY
    }

    // Lấy danh sách comment của 1 món ăn
    public LiveData<List<CommentModel>> getComments(String foodId) {
        MutableLiveData<List<CommentModel>> liveData = new MutableLiveData<>();

        remoteDataSource.getCommentsQuery(foodId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        liveData.setValue(new ArrayList<>());
                        return;
                    }
                    if (value != null) {
                        List<CommentModel> list = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            CommentModel comment = doc.toObject(CommentModel.class);
                            // <-- SỬA Ở ĐÂY: Chỉ hiển thị các comment đã được duyệt
                            if (comment != null && "approved".equals(comment.getModerationStatus())) {
                                list.add(comment);
                            }
                        }
                        liveData.setValue(list);
                    }
                });

        return liveData;
    }

    // Lấy comment hiện tại của user cho món ăn
    public LiveData<CommentModel> getUserComment(String foodId) {
        MutableLiveData<CommentModel> liveData = new MutableLiveData<>();

        remoteDataSource.getUserCommentRef(foodId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        CommentModel comment = documentSnapshot.toObject(CommentModel.class);
                        liveData.setValue(comment);
                    } else {
                        liveData.setValue(null);
                    }
                })
                .addOnFailureListener(e -> liveData.setValue(null));

        return liveData;
    }

    // Post hoặc update comment
    public LiveData<Boolean> postOrUpdateComment(String foodId, CommentModel comment) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        // <-- THÊM Ở ĐÂY: KIỂM DUYỆT BÌNH LUẬN TRƯỚC KHI LƯU
        if (moderationUtil.containsBannedWord(comment.getText())) {
            // Nếu bình luận chứa từ cấm, không cho phép post
            result.setValue(false); // Trả về false để chỉ ra rằng không thành công
            return result;
        }

        // Nếu bình luận không chứa từ cấm, đặt trạng thái moderationStatus là "approved"
        comment.setModerationStatus("approved"); // <-- THÊM DÒNG NÀY: Cần CommentModel có setModerationStatus()

        remoteDataSource.postComment(foodId, comment)
                .addOnSuccessListener(aVoid -> result.setValue(true))
                .addOnFailureListener(e -> result.setValue(false));

        return result;
    }

    // Xóa comment
    public LiveData<Boolean> deleteComment(String foodId, String userId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        remoteDataSource.deleteComment(foodId, userId)
                .addOnSuccessListener(aVoid -> result.setValue(true))
                .addOnFailureListener(e -> result.setValue(false));

        return result;
    }
}