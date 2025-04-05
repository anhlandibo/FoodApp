package com.example.foodapp2025.ui.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.example.foodapp2025.R;
import com.example.foodapp2025.data.model.BannerModel;
import com.example.foodapp2025.databinding.FragmentHomeBinding;
import com.example.foodapp2025.ui.activity.SplashActivity;
import com.example.foodapp2025.ui.adapter.CategoryAdapter;
import com.example.foodapp2025.ui.adapter.FoodAdapter;
import com.example.foodapp2025.ui.adapter.PopularFoodAdapter;
import com.example.foodapp2025.ui.adapter.SliderAdapter;
import com.example.foodapp2025.viewmodel.BannerViewModel;
import com.example.foodapp2025.viewmodel.CategoryViewModel;
import com.example.foodapp2025.viewmodel.FoodViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private BannerViewModel bannerViewModel;
    private CategoryViewModel categoryViewModel;
    private CategoryAdapter categoryAdapter;

    private FoodViewModel foodViewModel;
    private PopularFoodAdapter popularFoodAdapter;
    private Handler handler = new Handler();
    private Runnable runnable;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bannerViewModel = new ViewModelProvider(this).get(BannerViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        foodViewModel = new ViewModelProvider(this).get(FoodViewModel.class);
        initBanner();
        initCategory();
        initPopular();

        //logout
        binding.logoutBtn.setOnClickListener(v -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null) {
                auth.signOut();
                GoogleSignIn.getClient(requireContext(), GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
                startActivity(new Intent(requireActivity(), SplashActivity.class));
                requireActivity().finish();
            }
        });

        //search
        binding.searchEdt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                String keyword = binding.searchEdt.getText().toString().trim();
                if (!keyword.isEmpty()) {
                    goToSearchResultFragment(keyword, view);
                }
                binding.searchEdt.setText("");
                return true;
            }
            return false;
        });

        //filter
//        binding.filterBtn.setOnClickListener(v -> {
//            FoodFilterBottomSheet bottomSheet = new FoodFilterBottomSheet();
//
//            bottomSheet.setOnFilterApplyListener(new FoodFilterBottomSheet.OnFilterApplyListener() {
//                @Override
//                public void onFilterApplied(String category, int minPrice, boolean isPopular) {
//                    Bundle bundle = new Bundle();
//                    bundle.putString("categoryName", category);
//                    bundle.putInt("minPrice", minPrice);
//                    bundle.putBoolean("isPopular", isPopular);
//                    NavController navController = NavHostFragment.findNavController(HomeFragment.this);
//                    navController.navigate(R.id.categoryDetailFragment, bundle);
//                }
//            });
//            bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
//        });
        binding.filterBtn.setOnClickListener(v -> {
            FoodFilterPopupWindow popupWindow = new FoodFilterPopupWindow(requireContext(), (category, minPrice, isPopular) -> {
                // Xử lý khi áp dụng bộ lọc
                Bundle bundle = new Bundle();
                bundle.putString("categoryName", category);
                bundle.putInt("minPrice", minPrice);
                bundle.putBoolean("isPopular", isPopular);
                NavController navController = NavHostFragment.findNavController(HomeFragment.this);
                navController.navigate(R.id.categoryDetailFragment, bundle);
            });

            // Đặt nền màu trắng cho PopupWindow
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));

            // Cho phép popup đóng khi bấm ngoài khu vực
            popupWindow.setOutsideTouchable(true);
            popupWindow.setFocusable(true);
            // Hiển thị PopupWindow tại vị trí của nút filter
            popupWindow.showAsDropDown(binding.filterBtn, 0, 0);
        });

    }

    private void goToSearchResultFragment(String keyword, View v) {
        Bundle bundle = new Bundle();
        bundle.putString("search_keyword", keyword);

        NavController navController = Navigation.findNavController(v);
        navController.navigate(R.id.searchResultFragment, bundle);
    }

    private void initPopular() {
        binding.popularProgressbar.setVisibility(View.VISIBLE);
        foodViewModel.getPopularFood().observe(getViewLifecycleOwner(), popularFoodModels -> {
            if (popularFoodModels != null && !popularFoodModels.isEmpty()) {
                popularFoodAdapter = new PopularFoodAdapter(popularFoodModels);
                binding.popularFoodView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                binding.popularFoodView.setAdapter(popularFoodAdapter);
                binding.popularProgressbar.setVisibility(View.GONE);
            }
        });
    }

    private void initCategory() {
        binding.categoryProgressbar.setVisibility(View.VISIBLE);
        categoryViewModel.getCategories().observe(getViewLifecycleOwner(), categoryModels -> {
            if (categoryModels != null && !categoryModels.isEmpty()) {
                categoryAdapter = new CategoryAdapter(categoryModels);
                binding.categoryRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
                binding.categoryRecyclerView.setAdapter(categoryAdapter);
                binding.categoryProgressbar.setVisibility(View.GONE);
            }
        });
    }

    private void initBanner() {
        binding.progressBarSlider.setVisibility(View.VISIBLE);
        bannerViewModel.loadBanner().observe(getViewLifecycleOwner(), bannerModels -> {
            if (bannerModels != null && !bannerModels.isEmpty()) {
                banners(bannerModels);
                binding.progressBarSlider.setVisibility(View.GONE);
                autoImageSlide();
            }
        });
    }

    private void autoImageSlide() {
        final long SLIDE_DELAY = 3000; // 3 giây chuyển ảnh

        if (runnable != null) {
            handler.removeCallbacks(runnable); // Xóa runnable cũ nếu có
        }

        runnable = new Runnable() {
            @Override
            public void run() {
                if (binding != null && binding.viewPagerSlider.getAdapter() != null) {
                    int currentItem = binding.viewPagerSlider.getCurrentItem();
                    int itemCount = binding.viewPagerSlider.getAdapter().getItemCount();

                    if (itemCount > 0) {
                        int nextItem = (currentItem + 1) % itemCount; // Chuyển ảnh và quay lại đầu khi hết
                        binding.viewPagerSlider.setCurrentItem(nextItem, true);
                    }
                }
                handler.postDelayed(this, SLIDE_DELAY);
            }
        };

        handler.postDelayed(runnable, SLIDE_DELAY);
    }

    private void banners(ArrayList<BannerModel> bannerModels) {
        binding.viewPagerSlider.setAdapter(new SliderAdapter(bannerModels, binding.viewPagerSlider));
        binding.viewPagerSlider.setClipToPadding(false);
        binding.viewPagerSlider.setClipChildren(false);
        binding.viewPagerSlider.setOffscreenPageLimit(3);
        binding.viewPagerSlider.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        binding.viewPagerSlider.setPageTransformer(compositePageTransformer);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (runnable != null) {
            handler.removeCallbacks(runnable);
        }
        binding = null;
    }

}