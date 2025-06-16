package com.example.foodapp2025.viewmodel;

import static android.nfc.tech.MifareUltralight.PAGE_SIZE;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodapp2025.data.model.FoodModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration; // <-- THÊM IMPORT NÀY
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FavouriteFoodViewModel extends ViewModel {
    private static final String TAG = "FavoriteFoodsViewModel";
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final List<FoodModel> favFoodListAllLoaded = new ArrayList<>(); // Danh sách tất cả items đã load
    private final MutableLiveData<List<FoodModel>> _favFoodListFiltered = new MutableLiveData<>(new ArrayList<>()); // LiveData cho danh sách đã lọc/tìm kiếm
    public final LiveData<List<FoodModel>> favFoodListFiltered = _favFoodListFiltered; // Expose LiveData

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false); // LiveData cho trạng thái loading
    public final LiveData<Boolean> isLoading = _isLoading;

    public boolean isLastPage = false;
    public String currentSearchQuery = "";
    private final MutableLiveData<Boolean> _isUserLoggedIn = new MutableLiveData<>(false);
    public final LiveData<Boolean> isUserLoggedIn = _isUserLoggedIn;

    private ListenerRegistration favFoodsListenerRegistration;

    // Constructor
    public FavouriteFoodViewModel() {
        checkLoginStatus();
    }

    // Kiểm tra trạng thái đăng nhập và cập nhật LiveData
    private void checkLoginStatus() {
        boolean isLoggedIn = mAuth.getCurrentUser() != null;
        if (Boolean.TRUE.equals(_isUserLoggedIn.getValue()) != isLoggedIn) {
            _isUserLoggedIn.setValue(isLoggedIn);
            if (!isLoggedIn) {
                // Nếu user đăng xuất, xóa dữ liệu và reset trạng thái
                favFoodListAllLoaded.clear();
                filterList(""); // Clear filter và cập nhật LiveData
                // lastDocument = null; // Không cần nữa
                isLastPage = false;
                currentSearchQuery = "";
                _isLoading.setValue(false); // Dừng loading
                detachFavouriteFoodsListener();
            } else {
                // Nếu user đăng nhập (hoặc đã đăng nhập), bắt đầu lắng nghe dữ liệu nếu chưa có listener
                if (favFoodsListenerRegistration == null && !_isLoading.getValue()) {
                    attachFavouriteFoodsListener();
                }
            }
        }
    }

    private void attachFavouriteFoodsListener() {
        if (!Boolean.TRUE.equals(_isUserLoggedIn.getValue())) {
            Log.d(TAG, "Not attaching listener: user not logged in.");
            return;
        }
        if (mAuth.getCurrentUser() == null) {
            Log.d(TAG, "Not attaching listener: current user is null.");
            return;
        }

        if (favFoodsListenerRegistration != null) {
            favFoodsListenerRegistration.remove(); // Gỡ bỏ listener cũ nếu tồn tại
            Log.d(TAG, "Existing listener removed.");
        }

        _isLoading.setValue(true); // Bật trạng thái loading

        String userId = mAuth.getCurrentUser().getUid();
        CollectionReference ref = db.collection("users")
                .document(userId)
                .collection("favourites");

        favFoodsListenerRegistration = ref.orderBy("name") // Đảm bảo dùng orderBy để có thứ tự ổn định
                .addSnapshotListener((snapshots, e) -> {
                    _isLoading.setValue(false); // Tắt trạng thái loading
                    if (e != null) {
                        Log.e(TAG, "Listen failed.", e);
                        // TODO: Cập nhật LiveData trạng thái lỗi nếu cần
                        return;
                    }

                    if (snapshots != null) {
                        Log.d(TAG, "Favorite foods snapshot received. Changes: " + snapshots.getDocumentChanges().size());
                        favFoodListAllLoaded.clear();
                        for (DocumentSnapshot doc : snapshots) {
                            FoodModel food = doc.toObject(FoodModel.class);
                            if (food != null) {
                                food.setId(doc.getId());
                                favFoodListAllLoaded.add(food);
                            } else {
                                Log.w(TAG, "Failed to parse FoodModel for doc: " + doc.getId());
                            }
                        }
                        isLastPage = true;

                        filterList(currentSearchQuery);
                    }
                });
        Log.d(TAG, "Favorite foods listener attached.");
    }

    private void detachFavouriteFoodsListener() {
        if (favFoodsListenerRegistration != null) {
            favFoodsListenerRegistration.remove();
            favFoodsListenerRegistration = null;
            Log.d(TAG, "Favorite foods listener detached.");
        }
    }


    public void loadFavouriteFoodPage(){
        Log.w(TAG, "loadFavouriteFoodPage() called. This method is likely redundant when using real-time listener for the entire collection.");
    }

    private void filterList(String query) {
        currentSearchQuery = query;
        List<FoodModel> filteredList = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase(Locale.getDefault()).trim();

        if (lowerCaseQuery.isEmpty()) {
            filteredList.addAll(favFoodListAllLoaded);
        } else {
            for (FoodModel food : favFoodListAllLoaded) {
                if (food.getName() != null && food.getName().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) {
                    filteredList.add(food);
                }
            }
        }
        _favFoodListFiltered.setValue(filteredList);
    }

    public void setSearchQuery(String query) {
        filterList(query);
    }

    public void loadMore() {
        Log.d(TAG, "loadMore() called. With real-time listener for all, this might not be needed.");
    }

    private FirebaseAuth.AuthStateListener authStateListener = firebaseAuth -> {
        checkLoginStatus();
    };

    @Override
    protected void onCleared() {
        super.onCleared();
        mAuth.removeAuthStateListener(authStateListener);
        detachFavouriteFoodsListener();
        Log.d(TAG, "FavouriteFoodViewModel onCleared. Listener detached.");
    }
    { // Initializer block
        mAuth.addAuthStateListener(authStateListener);
    }
}