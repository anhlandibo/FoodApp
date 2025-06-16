package com.example.foodapp2025.ui.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.foodapp2025.R;
import com.example.foodapp2025.databinding.FragmentFavouriteBinding;
import com.example.foodapp2025.ui.adapter.FavouriteFoodAdapter;
import com.example.foodapp2025.viewmodel.FavouriteFoodViewModel;

import java.util.ArrayList;

public class FavouriteFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    FragmentFavouriteBinding binding;
    private FavouriteFoodViewModel viewModel;
    private FavouriteFoodAdapter favFoodAdapter;

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
        binding = FragmentFavouriteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(FavouriteFoodViewModel.class);

        favFoodAdapter = new FavouriteFoodAdapter(getContext(), new ArrayList<>());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.recyclerViewFavouriteFoods.setLayoutManager(layoutManager);
        binding.recyclerViewFavouriteFoods.setAdapter(favFoodAdapter);

        binding.recyclerViewFavouriteFoods.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if (Boolean.FALSE.equals(viewModel.isLoading.getValue()) && !viewModel.isLastPage &&
                            (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5) {
                        Log.d("FavouriteFragment", "Detected end of list, requesting load more from ViewModel (might be redundant).");
                        viewModel.loadMore();
                    }
                }
            }
        });



        viewModel.favFoodListFiltered.observe(getViewLifecycleOwner(), favoriteFoods -> {
            Log.d("FavouriteFragment", "Observed filtered foods update. Count: " + favoriteFoods.size());
            favFoodAdapter.setData(favoriteFoods);

            // Cập nhật trạng thái rỗng dựa trên danh sách hiện tại
            updateEmptyState(favoriteFoods.isEmpty());
        });

        // Quan sát trạng thái loading từ ViewModel
        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            Log.d("FavouriteFragment", "Observed loading state: " + isLoading);
            if (isLoading) {
                binding.progressBar2.setVisibility(View.VISIBLE);
                if (favFoodAdapter.getItemCount() == 0) {
                    binding.textViewEmptyFavourites.setVisibility(View.GONE);
                }
            } else {
                binding.progressBar2.setVisibility(View.GONE);
                updateEmptyState(favFoodAdapter.getItemCount() == 0);
            }
        });

        // Quan sát trạng thái đăng nhập để hiển thị message phù hợp
        viewModel.isUserLoggedIn.observe(getViewLifecycleOwner(), isLoggedIn -> {
            Log.d("FavouriteFragment", "Observed user logged in state: " + isLoggedIn);
            if (!isLoggedIn) {
                favFoodAdapter.setData(new ArrayList<>()); // Xóa dữ liệu cũ
                binding.editTextSearchFavorites.setText(""); // Xóa search query
                updateEmptyState(true, "Please login to see your favorite food.");
                binding.progressBar2.setVisibility(View.GONE); // Ẩn loading
            } else {
                // Khi người dùng đăng nhập, ViewModel sẽ tự động gắn listener và tải/cập nhật dữ liệu.
                binding.editTextSearchFavorites.setText(""); // Reset search box
                // Không cần tự gọi tải ở đây, ViewModel sẽ tự xử lý qua listener
                if (favFoodAdapter.getItemCount() == 0 && Boolean.FALSE.equals(viewModel.isLoading.getValue())) {
                    binding.progressBar2.setVisibility(View.VISIBLE);
                    updateEmptyState(false, "");
                }
            }
        });

        // Cập nhật TextWatcher để gọi ViewModel khi search text thay đổi
        binding.editTextSearchFavorites.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            binding.recyclerViewFavouriteFoods.setVisibility(View.GONE);
            binding.emptyStateContainer.setVisibility(View.VISIBLE);
            if (Boolean.TRUE.equals(viewModel.isLoading.getValue())) {
                binding.textViewEmptyFavourites.setVisibility(View.GONE);
            } else if (!viewModel.currentSearchQuery.isEmpty()) {
                binding.textViewEmptyFavourites.setVisibility(View.VISIBLE);
                binding.textViewEmptyFavourites.setText("Cannot find your food.");
            } else if (Boolean.TRUE.equals(viewModel.isUserLoggedIn.getValue())) {
                binding.textViewEmptyFavourites.setVisibility(View.VISIBLE);
                binding.textViewEmptyFavourites.setText(getString(R.string.no_favourite_items_message));
            } else {
                binding.textViewEmptyFavourites.setVisibility(View.VISIBLE);
                binding.textViewEmptyFavourites.setText("Please login to see your favorite food.");
            }

        } else {
            binding.recyclerViewFavouriteFoods.setVisibility(View.VISIBLE);
            binding.emptyStateContainer.setVisibility(View.GONE);
            binding.textViewEmptyFavourites.setVisibility(View.GONE);
            binding.textViewEmptyFavourites.setText("");
        }
    }

    private void updateEmptyState(boolean isEmpty, String message) {
        if (isEmpty) {
            binding.recyclerViewFavouriteFoods.setVisibility(View.GONE);
            binding.emptyStateContainer.setVisibility(View.VISIBLE);
            binding.textViewEmptyFavourites.setVisibility(View.VISIBLE);
            binding.textViewEmptyFavourites.setText(message);
        } else {
            binding.recyclerViewFavouriteFoods.setVisibility(View.VISIBLE);
            binding.emptyStateContainer.setVisibility(View.GONE);
            binding.textViewEmptyFavourites.setVisibility(View.GONE);
            binding.textViewEmptyFavourites.setText("");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}