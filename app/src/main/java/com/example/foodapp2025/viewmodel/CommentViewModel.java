// CommentViewModel.java
package com.example.foodapp2025.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodapp2025.data.model.CommentModel;
import com.example.foodapp2025.data.repository.CommentRepository;

import java.util.List;

public class CommentViewModel extends ViewModel {

    private final CommentRepository repository = new CommentRepository();

    // Lấy toàn bộ comment theo foodId
    public LiveData<List<CommentModel>> getComments(String foodId) {
        return repository.getComments(foodId);
    }

    // Lấy comment của người dùng hiện tại
    public LiveData<CommentModel> getUserComment(String foodId) {
        return repository.getUserComment(foodId);
    }

    // Thêm hoặc cập nhật comment
    public LiveData<Boolean> postOrUpdateComment(String foodId, CommentModel comment) {
        return repository.postOrUpdateComment(foodId, comment);
    }

    // Xóa comment
    // CommentViewModel.java
    // CommentViewModel.java
    public LiveData<Boolean> deleteComment(String foodId, String userId) {
        return repository.deleteComment(foodId, userId);
    }
}
