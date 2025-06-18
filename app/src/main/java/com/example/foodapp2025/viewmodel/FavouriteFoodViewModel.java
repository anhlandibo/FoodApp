package com.example.foodapp2025.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodapp2025.data.model.FoodModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class FavouriteFoodViewModel extends ViewModel {
    private static final String TAG = "FavoriteFoodsViewModel";
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final List<FoodModel> favFoodListAllLoaded = new ArrayList<>();
    private final MutableLiveData<List<FoodModel>> _favFoodListFiltered = new MutableLiveData<>(new ArrayList<>());
    public final LiveData<List<FoodModel>> favFoodListFiltered = _favFoodListFiltered;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;

    // isLastPage không còn cần thiết vì chúng ta luôn tải tất cả
    // public boolean isLastPage = false;
    public String currentSearchQuery = "";
    private final MutableLiveData<Boolean> _isUserLoggedIn = new MutableLiveData<>(false);
    public final LiveData<Boolean> isUserLoggedIn = _isUserLoggedIn;

    private ListenerRegistration favFoodsListenerRegistration;
    private FirebaseAuth.AuthStateListener authStateListener;

    // Constructor
    public FavouriteFoodViewModel() {
        authStateListener = firebaseAuth -> checkLoginStatus();
        mAuth.addAuthStateListener(authStateListener);
        checkLoginStatus();
    }

    // Kiểm tra trạng thái đăng nhập và cập nhật LiveData
    private void checkLoginStatus() {
        boolean isLoggedIn = mAuth.getCurrentUser() != null;
        if (Boolean.TRUE.equals(_isUserLoggedIn.getValue()) != isLoggedIn) {
            _isUserLoggedIn.setValue(isLoggedIn);
            if (!isLoggedIn) {
                Log.d(TAG, "User logged out. Clearing data.");
                favFoodListAllLoaded.clear();
                filterList("");
                currentSearchQuery = "";
                _isLoading.setValue(false);
                detachFavouriteFoodsListener();
            } else {
                Log.d(TAG, "User logged in. Attaching listener if not already attached.");
                if (favFoodsListenerRegistration == null) {
                    attachFavouriteFoodsListener();
                }
            }
        }
    }

    private void attachFavouriteFoodsListener() {
        if (!Boolean.TRUE.equals(_isUserLoggedIn.getValue()) || mAuth.getCurrentUser() == null) {
            Log.d(TAG, "Not attaching listener: user not logged in or current user is null.");
            _isLoading.setValue(false); // Đảm bảo tắt loading nếu không thể attach
            return;
        }

        detachFavouriteFoodsListener(); // Luôn gỡ bỏ listener cũ trước khi gắn cái mới

        _isLoading.setValue(true); // Bật trạng thái loading

        String userId = mAuth.getCurrentUser().getUid();
        CollectionReference favRef = db.collection("users")
                .document(userId)
                .collection("favourites");

        favFoodsListenerRegistration = favRef
                .orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener((favSnapshots, e) -> {
                    // Tắt trạng thái loading ban đầu. Loading sẽ được bật lại nếu cần fetch từ food chính
                    _isLoading.setValue(false);
                    if (e != null) {
                        Log.e(TAG, "Listen failed for favourites.", e);
                        return;
                    }

                    if (favSnapshots != null) {
                        Log.d(TAG, "Favorite foods snapshot received. Changes: " + favSnapshots.getDocumentChanges().size());

                        List<FoodModel> currentFavsFromUser = new ArrayList<>();
                        List<String> foodIdsToFetch = new ArrayList<>();

                        for (DocumentSnapshot doc : favSnapshots) {
                            FoodModel food = doc.toObject(FoodModel.class);
                            if (food != null) {
                                food.setId(doc.getId());
                                currentFavsFromUser.add(food);
                                foodIdsToFetch.add(food.getId());
                            } else {
                                Log.w(TAG, "Failed to parse FoodModel for favourite doc: " + doc.getId());
                            }
                        }

                        if (foodIdsToFetch.isEmpty()) {
                            Log.d(TAG, "No favourite foods found for this user.");
                            favFoodListAllLoaded.clear();
                            filterList(currentSearchQuery);
                            _isLoading.setValue(false);
                            return;
                        }

                        List<Task<DocumentSnapshot>> fetchFoodTasks = new ArrayList<>();
                        for (String foodId : foodIdsToFetch) {
                            fetchFoodTasks.add(db.collection("food").document(foodId).get());
                        }

                        // Bật loading trong khi chờ fetch food details
                        _isLoading.setValue(true);

                        Tasks.whenAllSuccess(fetchFoodTasks)
                                .addOnSuccessListener(objects -> {
                                    favFoodListAllLoaded.clear();
                                    WriteBatch batch = db.batch();
                                    boolean changesMade = false;

                                    for (Object obj : objects) {
                                        if (obj instanceof DocumentSnapshot) {
                                            DocumentSnapshot foodDocFromMain = (DocumentSnapshot) obj;
                                            if (foodDocFromMain.exists()) {
                                                FoodModel latestFoodData = foodDocFromMain.toObject(FoodModel.class);
                                                if (latestFoodData != null) {
                                                    latestFoodData.setId(foodDocFromMain.getId());

                                                    FoodModel favFoodFromUser = null;
                                                    for (FoodModel fav : currentFavsFromUser) {
                                                        if (fav.getId().equals(latestFoodData.getId())) {
                                                            favFoodFromUser = fav;
                                                            break;
                                                        }
                                                    }

                                                    if (favFoodFromUser != null && !areFoodModelsEqual(favFoodFromUser, latestFoodData)) {
                                                        Log.d(TAG, "Data mismatch for " + latestFoodData.getName() + " (ID: " + latestFoodData.getId() + "), preparing to update favourite.");
                                                        batch.set(favRef.document(latestFoodData.getId()), latestFoodData);
                                                        changesMade = true;
                                                    }
                                                    favFoodListAllLoaded.add(latestFoodData);
                                                } else {
                                                    Log.w(TAG, "Failed to parse FoodModel from main 'food' collection for ID: " + foodDocFromMain.getId());
                                                }
                                            } else {
                                                Log.d(TAG, "Food document not found in main 'food' collection for ID: " + foodDocFromMain.getId() + ". It might have been deleted. Removing from favourites.");
                                                batch.delete(favRef.document(foodDocFromMain.getId()));
                                                changesMade = true;
                                            }
                                        }
                                    }

                                    if (changesMade) {
                                        Log.d(TAG, "Committing batch updates to user's favourites.");
                                        batch.commit()
                                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Batch write successful: Favourites updated on Firebase."))
                                                .addOnFailureListener(e1 -> Log.e(TAG, "Batch write failed: Favourites not updated on Firebase.", e1));
                                    } else {
                                        Log.d(TAG, "No changes detected, no batch write needed.");
                                    }

                                    filterList(currentSearchQuery);
                                    _isLoading.setValue(false);
                                    Log.d(TAG, "Favourite foods data synced and filtered.");
                                })
                                .addOnFailureListener(exception -> {
                                    Log.e(TAG, "Error fetching food details from main collection: " + exception.getMessage(), exception);
                                    _isLoading.setValue(false);
                                    favFoodListAllLoaded.clear();
                                    favFoodListAllLoaded.addAll(currentFavsFromUser);
                                    filterList(currentSearchQuery);
                                });
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

    // Phương thức công khai để kích hoạt refresh thủ công
    public void forceRefreshFavourites() {
        Log.d(TAG, "forceRefreshFavourites() called. Detaching and re-attaching listener.");
        if (Boolean.TRUE.equals(_isUserLoggedIn.getValue())) {
            detachFavouriteFoodsListener(); // Gỡ bỏ listener cũ
            attachFavouriteFoodsListener(); // Gắn lại listener để kích hoạt tải và đồng bộ hóa
        } else {
            Log.d(TAG, "Cannot force refresh: user not logged in.");
            _isLoading.setValue(false); // Đảm bảo trạng thái loading được reset
        }
    }

    // Helper method to compare two FoodModel objects for differences
    private boolean areFoodModelsEqual(FoodModel f1, FoodModel f2) {
        if (f1 == f2) return true;
        if (f1 == null || f2 == null) return false;

        boolean areEqual = Objects.equals(f1.getId(), f2.getId()) &&
                Objects.equals(f1.getName(), f2.getName()) &&
                Objects.equals(f1.getPrice(), f2.getPrice()) &&
                Objects.equals(f1.getDescription(), f2.getDescription()) &&
                Objects.equals(f1.getImageUrl(), f2.getImageUrl()) &&
                Objects.equals(f1.getCategoryName(), f2.getCategoryName()) &&
                Objects.equals(f1.getIsPopular(), f2.getIsPopular()) &&
                Objects.equals(f1.getIsDeleted(), f2.getIsDeleted()) &&
                Objects.equals(f1.getStar(), f2.getStar()) &&
                Objects.equals(f1.getTime(), f2.getTime());

        if (!areEqual) {
            Log.d(TAG, "Comparison mismatch for food ID: " + f1.getId());
            if (!Objects.equals(f1.getName(), f2.getName())) Log.d(TAG, "  Name mismatch: '" + f1.getName() + "' vs '" + f2.getName() + "'");
            if (!Objects.equals(f1.getPrice(), f2.getPrice())) Log.d(TAG, "  Price mismatch: " + f1.getPrice() + " vs " + f2.getPrice());
            // ... (thêm log cho các trường khác nếu cần debug sâu hơn)
        }
        return areEqual;
    }


    public void loadFavouriteFoodPage(){
        Log.w(TAG, "loadFavouriteFoodPage() called. This method is now redundant as all data is loaded and synced via listener.");
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
        Log.d(TAG, "loadMore() called. This method is now redundant as all data is loaded and synced via listener.");
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
            Log.d(TAG, "Auth state listener removed.");
        }
        detachFavouriteFoodsListener();
        Log.d(TAG, "FavouriteFoodViewModel onCleared. Listener detached.");
    }
}