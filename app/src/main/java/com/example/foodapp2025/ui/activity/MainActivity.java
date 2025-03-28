package com.example.foodapp2025.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import com.example.foodapp2025.data.model.BannerModel;
import com.example.foodapp2025.databinding.ActivityMainBinding;
import com.example.foodapp2025.ui.adapter.SliderAdapter;
import com.example.foodapp2025.viewmodel.BannerViewModel;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private BannerViewModel bannerViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        bannerViewModel = new BannerViewModel();
        initBanner();

    }

    private void initBanner() {
        binding.progressBarSlider.setVisibility(View.VISIBLE);
        bannerViewModel.loadBanner().observeForever(bannerModels -> {
            if (bannerModels != null && !bannerModels.isEmpty()){
                banners(bannerModels);
                binding.progressBarSlider.setVisibility(View.GONE);
            }
        });

        //bannerViewModel.loadBanner();
    }

    private void autoImageSlide() {
        final long DELAY_MS = 3000;
        final long PERIOD_MS = 3000;
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int currentItem = binding.viewPagerSlider.getCurrentItem();
                int itemCount = binding.viewPagerSlider.getAdapter().getItemCount();

                if (currentItem < itemCount - 1) {
                    binding.viewPagerSlider.setCurrentItem(currentItem + 1, true);
                } else {
                    binding.viewPagerSlider.setCurrentItem(0, true); // Quay lại slide đầu tiên
                }

                handler.postDelayed(this, PERIOD_MS);            }
        };
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(runnable);
            }
        }, DELAY_MS, PERIOD_MS);
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


}