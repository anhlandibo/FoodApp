package com.example.foodapp2025.ui.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.foodapp2025.R;
import com.example.foodapp2025.data.model.BannerModel;
import com.example.foodapp2025.databinding.FragmentHomeBinding;
import com.example.foodapp2025.ui.activity.SplashActivity;
import com.example.foodapp2025.ui.adapter.CategoryAdapter;
import com.example.foodapp2025.ui.adapter.PopularFoodAdapter;
import com.example.foodapp2025.ui.adapter.SliderAdapter;
import com.example.foodapp2025.viewmodel.BannerViewModel;
import com.example.foodapp2025.viewmodel.CategoryViewModel;
import com.example.foodapp2025.viewmodel.FoodViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

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

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }


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

            //Đặt animation
            popupWindow.setAnimationStyle(android.R.style.Animation_Toast);

            //Đặt chiều rộng, chiều cao
            popupWindow.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
            popupWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);

            // Hiển thị PopupWindow tại vị trí của nút filter
            popupWindow.showAsDropDown(binding.filterBtn, 0, 0);
        });

        AtomicBoolean isListening = new AtomicBoolean(false);
        binding.voiceBtn.setOnClickListener(v -> {
//            // Kiểm tra quyền ghi âm
//            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//                // Nếu chưa có quyền, yêu cầu quyền
//                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, 1);
//            } else {
//                // Nếu đã có quyền, khởi tạo SpeechRecognizer
//                initializeSpeechRecognizer(view);
//                // Bắt đầu nhận diện giọng nói
//                speechRecognizer.startListening(speechRecognizerIntent);
//            }
            if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                // Nếu chưa có quyền, yêu cầu quyền
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            else {
                if (!isListening.get()){
                    isListening.set(true);
                    initializeSpeechRecognizer(view);
                    speechRecognizer.startListening(speechRecognizerIntent);
                    Toast.makeText(getContext(), "Is listening...", Toast.LENGTH_SHORT).show();
                }
                else{
                    isListening.set(false);
                    if (speechRecognizer != null){
                        speechRecognizer.stopListening();
                        speechRecognizer.destroy();
                    }
                    Toast.makeText(getContext(), "Stop listening", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void initializeSpeechRecognizer(View view) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getContext());
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN");

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {}
            @Override
            public void onBeginningOfSpeech() {}
            @Override
            public void onRmsChanged(float rmsdB) {}
            @Override
            public void onBufferReceived(byte[] buffer) {}
            @Override
            public void onEndOfSpeech() {}
            @Override
            public void onError(int error) {
                Toast.makeText(getContext(), "Speech recognition error: " + error, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> recognizedWords = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (recognizedWords != null && !recognizedWords.isEmpty()) {
                    String spokenText = recognizedWords.get(0); // Lấy kết quả đầu tiên từ danh sách nhận diện
                    binding.searchEdt.setText(spokenText); // Đặt kết quả vào ô tìm kiếm
                }
            }
            @Override
            public void onPartialResults(Bundle partialResults) {}
            @Override
            public void onEvent(int eventType, Bundle params) {}
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

//        final long DELAY_MS = 3000;
//        final long PERIOD_MS = 3000;
//        int n = binding.viewPagerSlider.getAdapter().getItemCount();
//        final  Handler handler1 = new Handler();
//        final Runnable runnable1 = new Runnable() {
//            @Override
//            public void run() {
//                if (binding.viewPagerSlider.getCurrentItem() == n-1){
//                    binding.viewPagerSlider.setCurrentItem(0);
//                }
//                else{
//                    binding.viewPagerSlider.setCurrentItem(binding.viewPagerSlider.getCurrentItem()+1, true);
//                }
//            }
//        };
//
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                handler1.post(runnable1);
//            }
//        }, DELAY_MS, PERIOD_MS);

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


        new TabLayoutMediator(binding.tabLayout, binding.viewPagerSlider,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {

                    }
                }).attach();

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