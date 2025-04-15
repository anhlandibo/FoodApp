package com.example.foodapp2025.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.foodapp2025.R;
import com.example.foodapp2025.data.model.CartModel;
import com.example.foodapp2025.data.model.FoodModel;
import com.example.foodapp2025.databinding.FragmentFoodDetailBinding;
import com.example.foodapp2025.ui.activity.MainActivity;
import com.example.foodapp2025.viewmodel.CartViewModel;

public class FoodDetailFragment extends Fragment {
    private FragmentFoodDetailBinding binding;
    private CartViewModel cartViewModel;
    private int quantity = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFoodDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setBottomNavigationVisibility(false);
        }

        // Get selected food item
        FoodModel selectedFood = (FoodModel) getArguments().getSerializable("food");

        if (selectedFood == null) {
            Toast.makeText(getContext(), "Food not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Load food details
        Glide.with(this).load(selectedFood.getImageUrl()).into(binding.imageView7);
        binding.titleTxt.setText(selectedFood.getName());
        binding.priceTxt.setText(selectedFood.getPrice() + " VND");
        binding.descriptionTxt.setText(selectedFood.getDescription());
        binding.rateTxt.setText(selectedFood.getStar() + " Rating");
        binding.ratingBar.setRating(Float.parseFloat(selectedFood.getStar().toString()));
        binding.totalTxt.setText(quantity * selectedFood.getPrice() + " VND");
        binding.numTxt.setText(String.valueOf(quantity));

        // Handle Add to Cart button click
        binding.addCartBtn.setOnClickListener(v -> {
            CartModel newItem = new CartModel(selectedFood.getImageUrl(), selectedFood.getName(), selectedFood.getPrice(), quantity);
            cartViewModel.addItem(newItem);
            Toast.makeText(getContext(), "Item added to cart!", Toast.LENGTH_SHORT).show();
            cartViewModel.saveCartToFirestore();
        });

        // Handle Increase and Decrease Quantity
        binding.minusBtn.setOnClickListener(v -> {
            quantity++;
            binding.numTxt.setText(String.valueOf(quantity));
            binding.totalTxt.setText(quantity * selectedFood.getPrice() + " VND");
        });

        binding.minusBtn.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                binding.numTxt.setText(String.valueOf(quantity));
                binding.totalTxt.setText(quantity * selectedFood.getPrice() + " VND");
            }
        });

        // Handle Back Button
        binding.backBtn.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.popBackStack();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setBottomNavigationVisibility(true);
        }
        binding = null; // Prevent memory leaks
    }
}