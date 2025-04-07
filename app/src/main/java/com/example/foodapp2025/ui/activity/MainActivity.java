package com.example.foodapp2025.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;


import com.example.foodapp2025.R;
import com.example.foodapp2025.data.model.BannerModel;
import com.example.foodapp2025.databinding.ActivityMainBinding;
import com.example.foodapp2025.ui.adapter.SliderAdapter;
import com.example.foodapp2025.ui.fragment.CartFragment;
import com.example.foodapp2025.ui.fragment.FavoriteFragment;
import com.example.foodapp2025.ui.fragment.HomeFragment;
import com.example.foodapp2025.ui.fragment.ProfileFragment;
import com.example.foodapp2025.viewmodel.BannerViewModel;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null){
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
        }


    }
    public void setBottomNavigationVisibility(boolean isVisible) {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        if (isVisible) {
            bottomNavigationView.setVisibility(View.VISIBLE);
        } else {
            bottomNavigationView.setVisibility(View.GONE);
        }
    }



}