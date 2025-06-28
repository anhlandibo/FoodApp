package com.example.foodapp2025.ui.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout; // Import này

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

        viewModel = new ViewModelProvider(requireActivity()).get(FavouriteFoodViewModel.class);

        favFoodAdapter = new FavouriteFoodAdapter(getContext(), new ArrayList<>());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.recyclerViewFavouriteFoods.setLayoutManager(layoutManager);
        binding.recyclerViewFavouriteFoods.setAdapter(favFoodAdapter);

        // Thiết lập SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d("FavouriteFragment", "Swipe-to-refresh triggered.");
            viewModel.forceRefreshFavourites(); // Yêu cầu ViewModel làm mới dữ liệu
        });

        viewModel.favFoodListFiltered.observe(getViewLifecycleOwner(), favoriteFoods -> {
            Log.d("FavouriteFragment", "Observed filtered foods update. Count: " + favoriteFoods.size());
            favFoodAdapter.setData(favoriteFoods);
            // Tắt biểu tượng loading của SwipeRefreshLayout khi dữ liệu đã được tải
            binding.swipeRefreshLayout.setRefreshing(false);
            updateEmptyState(favoriteFoods.isEmpty());
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            Log.d("FavouriteFragment", "Observed loading state: " + isLoading);
            if (isLoading) {
                binding.progressBar2.setVisibility(View.VISIBLE);
                // Nếu đang loading và không có item nào, ẩn thông báo rỗng tạm thời
                if (favFoodAdapter.getItemCount() == 0) {
                    binding.textViewEmptyFavourites.setVisibility(View.GONE);
                }
                // Nếu đang loading, hiển thị biểu tượng loading của SwipeRefreshLayout
                if (!binding.swipeRefreshLayout.isRefreshing()) {
                    binding.swipeRefreshLayout.setRefreshing(true);
                }
            } else {
                binding.progressBar2.setVisibility(View.GONE);
                binding.swipeRefreshLayout.setRefreshing(false); // Tắt biểu tượng refresh
                updateEmptyState(favFoodAdapter.getItemCount() == 0);
            }
        });

        viewModel.isUserLoggedIn.observe(getViewLifecycleOwner(), isLoggedIn -> {
            Log.d("FavouriteFragment", "Observed user logged in state: " + isLoggedIn);
            if (!isLoggedIn) {
                favFoodAdapter.setData(new ArrayList<>());
                binding.editTextSearchFavorites.setText("");
                updateEmptyState(true, "Please login to see your favorite food.");
                binding.progressBar2.setVisibility(View.GONE);
                binding.swipeRefreshLayout.setRefreshing(false); // Đảm bảo tắt refresh khi đăng xuất
            } else {
                binding.editTextSearchFavorites.setText("");
                // Khi người dùng đăng nhập, ViewModel sẽ tự động gắn listener và tải/cập nhật dữ liệu.
                // Do đó, nếu list rỗng và không đang tải, có thể hiển thị loading ban đầu
                // để người dùng biết data đang được fetch.
                if (favFoodAdapter.getItemCount() == 0 && Boolean.FALSE.equals(viewModel.isLoading.getValue())) {
                    // Cố gắng hiển thị loading ngay lập tức nếu chưa có data
                    binding.swipeRefreshLayout.setRefreshing(true);
                    // Có thể tạm thời ẩn empty state để tránh nhấp nháy nếu đang loading
                    binding.textViewEmptyFavourites.setVisibility(View.GONE);
                }
            }
        });

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