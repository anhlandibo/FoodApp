// CommentRepository.java
package com.example.foodapp2025.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodapp2025.data.model.CommentModel;
import com.example.foodapp2025.data.remote.CommentRemoteDataSource;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class CommentRepository {

    private final CommentRemoteDataSource remoteDataSource;

    public CommentRepository() {
        this.remoteDataSource = new CommentRemoteDataSource();
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
                            if (comment != null) {
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

        remoteDataSource.postComment(foodId, comment)
                .addOnSuccessListener(aVoid -> result.setValue(true))
                .addOnFailureListener(e -> result.setValue(false));

        return result;
    }

    // Xóa comment
    // CommentRepository.java
    public LiveData<Boolean> deleteComment(String foodId, String userId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        remoteDataSource.deleteComment(foodId, userId)
                .addOnSuccessListener(aVoid -> result.setValue(true))
                .addOnFailureListener(e -> result.setValue(false));

        return result;
    }

}
