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
    public final LiveData<Boolean> isLoading = _isLoading; // Expose LiveData

    private DocumentSnapshot lastDocument = null; // Dùng cho pagination
    public boolean isLastPage = false;
    public String currentSearchQuery = "";
    private final MutableLiveData<Boolean> _isUserLoggedIn = new MutableLiveData<>(false);
    public final LiveData<Boolean> isUserLoggedIn = _isUserLoggedIn;

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
                // Nếu user đăng xuất, xóa dữ liệu và reset trạng thái pagination/search
                favFoodListAllLoaded.clear();
                filterList(""); // Clear filter và cập nhật LiveData
                lastDocument = null;
                isLastPage = false;
                currentSearchQuery = "";
                _isLoading.setValue(false); // Dừng loading
            } else {
                // Nếu user đăng nhập (hoặc đã đăng nhập), bắt đầu tải trang đầu tiên nếu chưa có dữ liệu
                if (favFoodListAllLoaded.isEmpty() && !_isLoading.getValue()) {
                    loadFavouriteFoodPage();
                }
            }
        }
    }
    // Hàm tải trang dữ liệu yêu thích từ Firestore
    public void loadFavouriteFoodPage(){
        if (!Boolean.TRUE.equals(_isUserLoggedIn.getValue())) {
            // Không tải nếu user chưa đăng nhập
            return;
        }

        if (_isLoading.getValue() == Boolean.TRUE || isLastPage) {
            // Không tải nếu đang loading hoặc đã hết trang
            return;
        }

        _isLoading.setValue(true); // Bật trạng thái loading

        String userId = mAuth.getCurrentUser().getUid();
        CollectionReference ref = db.collection("users")
                .document(userId)
                .collection("favourites");

        Query query = ref.orderBy("name") // Đảm bảo dùng orderBy để pagination hoạt động
                .limit(PAGE_SIZE);

        if (lastDocument != null) {
            query = query.startAfter(lastDocument);
        }

        query.get() // Lấy dữ liệu một lần
                .addOnCompleteListener(task -> {
                    _isLoading.setValue(false); // Tắt trạng thái loading

                    if (task.isSuccessful()) {
                        List<FoodModel> newItems = new ArrayList<>();
                        DocumentSnapshot lastDoc = null;

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            FoodModel food = doc.toObject(FoodModel.class);
                            if (food != null) {
                                // Gán Document ID vào FoodModel nếu cần cho các thao tác sau này (xóa)
                                // food.setId(doc.getId()); // Cần setter trong FoodModel
                                newItems.add(food);
                                lastDoc = doc;
                            } else {
                                Log.w(TAG, "Failed to parse FoodModel for doc: " + doc.getId());
                            }
                        }

                        if (!newItems.isEmpty()) {
                            favFoodListAllLoaded.addAll(newItems); // Thêm vào danh sách tổng
                            lastDocument = lastDoc; // Cập nhật last document
                            if (newItems.size() < PAGE_SIZE) {
                                isLastPage = true;
                            }
                        } else {
                            isLastPage = true; // Không có item mới -> hết trang
                        }

                        // Áp dụng lại bộ lọc/tìm kiếm trên danh sách tổng đã cập nhật
                        filterList(currentSearchQuery);

                    } else {
                        Log.e(TAG, "Error fetching favourite foods: ", task.getException());
                        // TODO: Cập nhật LiveData trạng thái lỗi nếu cần
                    }
                });
    }
    // Hàm lọc danh sách đã tải theo search query
    private void filterList(String query) {
        currentSearchQuery = query; // Cập nhật query hiện tại
        List<FoodModel> filteredList = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase(Locale.getDefault()).trim();

        if (lowerCaseQuery.isEmpty()) {
            filteredList.addAll(favFoodListAllLoaded); // Nếu rỗng thì dùng toàn bộ danh sách đã load
        } else {
            for (FoodModel food : favFoodListAllLoaded) {
                if (food.getName() != null && food.getName().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) {
                    filteredList.add(food);
                }
            }
        }
        _favFoodListFiltered.setValue(filteredList); // Cập nhật LiveData danh sách đã lọc
    }

    // Phương thức để Fragment gọi khi search text thay đổi
    public void setSearchQuery(String query) {
        filterList(query);
    }
    public void loadMore() {
        loadFavouriteFoodPage();
    }
    private FirebaseAuth.AuthStateListener authStateListener = firebaseAuth -> {
        checkLoginStatus(); // Gọi lại kiểm tra khi trạng thái auth thay đổi
    };

    @Override
    protected void onCleared() {
        super.onCleared();
        mAuth.removeAuthStateListener(authStateListener);
    }
    { // Initializer block
        mAuth.addAuthStateListener(authStateListener);
    }
}
