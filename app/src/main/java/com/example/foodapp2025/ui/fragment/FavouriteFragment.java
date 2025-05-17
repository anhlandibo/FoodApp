package com.example.foodapp2025.ui.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable; // Import này cần thiết cho TextWatcher
import android.text.TextWatcher; // Import này cần thiết cho TextWatcher
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.foodapp2025.R; // Đảm bảo import file R để dùng string resource
import com.example.foodapp2025.data.model.FoodModel;
import com.example.foodapp2025.databinding.FragmentFavouriteBinding;
import com.example.foodapp2025.ui.adapter.FavouriteFoodAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale; // Import này cần thiết cho toLowerCase

public class FavouriteFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    FragmentFavouriteBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private FavouriteFoodAdapter favFoodAdapter;
    private List<FoodModel> favFoodListAllLoaded;

    private List<FoodModel> favFoodListFiltered;

    private static final int PAGE_SIZE = 15;
    private DocumentSnapshot lastDocument;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    private ListenerRegistration favListener;
    private String currentSearchQuery = "";


    public static FavouriteFragment newInstance(String param1, String param2) {
        FavouriteFragment fragment = new FavouriteFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentFavouriteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        favFoodListAllLoaded = new ArrayList<>();
        favFoodListFiltered = new ArrayList<>();

        favFoodAdapter = new FavouriteFoodAdapter(getContext(), new ArrayList<>());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.recyclerViewFavouriteFoods.setLayoutManager(layoutManager);
        binding.recyclerViewFavouriteFoods.setAdapter(favFoodAdapter);

        // Add scroll listener for infinite scrolling
        binding.recyclerViewFavouriteFoods.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Only load more if scrolling down and not already loading or reached last page
                if (dy > 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    // Check if near the end of the list
                    // You might adjust the threshold (e.g., 5) based on PAGE_SIZE
                    if (!isLoading && !isLastPage && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5) {
                        loadFavouriteFoodPage();
                    }
                }
            }
        });

        loadFavouriteFoodPage();

        binding.editTextSearchFavorites.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString(); // Update the search query variable
                filterList(currentSearchQuery); // Apply filter on currently loaded data
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadFavouriteFoodPage(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null){
            favFoodListAllLoaded.clear();
            favFoodListFiltered.clear();
            favFoodAdapter.setData(favFoodListFiltered);
            showEmptyState(true, "Vui lòng đăng nhập để xem danh sách yêu thích.");
            Toast.makeText(getContext(), "Bạn cần đăng nhập để xem mục yêu thích.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isLoading || isLastPage) {
            // Don't load if already loading or no more pages
            return;
        }

        isLoading = true;
        // Show a loading indicator if you have one

        String userId = currentUser.getUid();
        CollectionReference ref = db.collection("users")
                .document(userId)
                .collection("favourites");

        Query query = ref.orderBy("name") // ORDER BY name (or a timestamp/ID for better stability)
                .limit(PAGE_SIZE); // Limit to page size

        if (lastDocument != null) {
            // If loading subsequent pages, start after the last document
            query = query.startAfter(lastDocument);
        }

        query.get() // Use get() for one-time fetch
                .addOnCompleteListener(task -> {
                    isLoading = false;
                    // Hide loading indicator

                    if (task.isSuccessful()) {
                        List<FoodModel> newItems = new ArrayList<>();
                        DocumentSnapshot lastDoc = null;

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            FoodModel food = doc.toObject(FoodModel.class);
                            newItems.add(food);
                            lastDoc = doc; // Keep track of the last document in this batch
                        }

                        if (!newItems.isEmpty()) {
                            // Add new items to the master list
                            favFoodListAllLoaded.addAll(newItems);

                            // Update last document for the next page query
                            lastDocument = lastDoc;

                            // Check if this was the last page (fewer items than page size)
                            if (newItems.size() < PAGE_SIZE) {
                                isLastPage = true;
                            }
                        } else {
                            // No new items means we've reached the end
                            isLastPage = true;
                        }

                        // Re-apply the current filter to include the new items
                        filterList(currentSearchQuery);

                    } else {
                        Log.e("FavouriteFragment", "Error fetching favourite foods: ", task.getException());
                        Toast.makeText(getContext(), "Lỗi khi tải danh sách yêu thích.", Toast.LENGTH_SHORT).show();
                        // Handle error state, perhaps show a retry button or message
                        showEmptyState(true, "Lỗi khi tải danh sách yêu thích."); // Show error on empty state if nothing loaded
                    }
                });
    }

    // Phương thức lọc danh sách dựa trên truy vấn tìm kiếm
    private void filterList(String query) {
        favFoodListFiltered.clear();
        String lowerCaseQuery = query.toLowerCase(Locale.getDefault()).trim();

        if (lowerCaseQuery.isEmpty()) {
            // If query is empty, show all loaded items
            favFoodListFiltered.addAll(favFoodListAllLoaded);
        } else {
            // Filter from the list of all loaded items
            for (FoodModel food : favFoodListAllLoaded) {
                if (food.getName() != null && food.getName().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) {
                    favFoodListFiltered.add(food);
                }
            }
        }

        favFoodAdapter.setData(favFoodListFiltered); // Update adapter with filtered list

        // Update empty state based on the filtered list
        if (favFoodListFiltered.isEmpty()) {
            if (favFoodListAllLoaded.isEmpty()) {
                // No items loaded at all (initial load or error)
                // The message should be set by loadFavouriteFoodPage or error handling
                showEmptyState(true, binding.textViewEmptyFavourites.getText().toString().isEmpty() ?
                        getString(R.string.no_favourite_items_message) : // Default if message not set
                        binding.textViewEmptyFavourites.getText().toString());
            } else {
                // Items are loaded, but none match the search query
                showEmptyState(true, "Không tìm thấy món ăn nào khớp với tìm kiếm của bạn.");
            }
        } else {
            showEmptyState(false, ""); // Hide empty state if filtered list is not empty
        }
    }

    private void showEmptyState(boolean show, String message) {
        if (show) {
            binding.recyclerViewFavouriteFoods.setVisibility(View.GONE);
            binding.emptyStateContainer.setVisibility(View.VISIBLE);
            binding.textViewEmptyFavourites.setText(message);
        } else {
            binding.recyclerViewFavouriteFoods.setVisibility(View.VISIBLE);
            binding.emptyStateContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (favListener != null) {
            favListener.remove();
        }
        binding = null;
    }


}