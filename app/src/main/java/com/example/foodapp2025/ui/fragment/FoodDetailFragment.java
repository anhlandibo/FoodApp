package com.example.foodapp2025.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context; // Import Context
import android.content.Intent;
import android.content.SharedPreferences; // Import SharedPreferences
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.foodapp2025.R;
import com.example.foodapp2025.data.model.CartModel;
import com.example.foodapp2025.data.model.CommentModel;
import com.example.foodapp2025.data.model.FoodModel;
import com.example.foodapp2025.databinding.FragmentFoodDetailBinding;
import com.example.foodapp2025.ui.activity.CommentActivity;
import com.example.foodapp2025.ui.activity.MainActivity;
import com.example.foodapp2025.viewmodel.CartViewModel;
import com.example.foodapp2025.viewmodel.CommentViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashSet; // Import HashSet
import java.util.List;
import java.util.Objects;
import java.util.Set; // Import Set

public class FoodDetailFragment extends Fragment {
    private FragmentFoodDetailBinding binding;
    private CartViewModel cartViewModel;
    private Long quantity = 1L;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean isFavourite = false;
    private CommentViewModel commentViewModel = new CommentViewModel();

    private static final String PREFS_NAME = "FoodAppPrefs";
    private static final String FAV_PREFS_KEY_PREFIX = "user_favorites_";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFoodDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setBottomNavigationVisibility(false);
        }

        FoodModel selectedFood = (FoodModel) getArguments().getSerializable("food");

        if (selectedFood == null) {
            Toast.makeText(getContext(), "Food not found", Toast.LENGTH_SHORT).show();
            NavController navController = Navigation.findNavController(view);
            navController.popBackStack();
            return;
        }

        checkFavouriteStatusLocal(selectedFood.getId());
        checkFavouriteStatusFirestore(selectedFood.getId());


        binding.commentBtn.setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), CommentActivity.class);
            if (selectedFood.getId() != null && !selectedFood.getId().isEmpty()) {
                i.putExtra("FOOD_ID", selectedFood.getId());
                startActivity(i);
            } else {
                Toast.makeText(getContext(), "No food found.", Toast.LENGTH_SHORT).show();
            }
        });

        // 2 decimal digits
        LiveData<Float> avgRatingLiveData = commentViewModel.getAverageRatingLiveData(selectedFood.getId());

        avgRatingLiveData.observe(getViewLifecycleOwner(), avgRating -> {
            if (avgRating != null) {
                // Update your UI with the avgRating
                float roundedAvgRating = Math.round(avgRating * 100) / 100f;
                binding.rateTxt.setText(roundedAvgRating + " Rating");
                binding.ratingBar.setRating(roundedAvgRating);
            }
        });

        Glide.with(this).load(selectedFood.getImageUrl()).into(binding.imageView7);
        binding.titleTxt.setText(selectedFood.getName());
        binding.priceTxt.setText(selectedFood.getPrice() + " $");
        binding.descriptionTxt.setText(selectedFood.getDescription());
//        binding.rateTxt.setText(selectedFood.getStar() + " Rating");
//        binding.ratingBar.setRating(Float.parseFloat(selectedFood.getStar().toString()));
//        binding.rateTxt.setText(avgStarRating + " Rating");
//        binding.ratingBar.setRating(avgStarRating);
        binding.totalTxt.setText(quantity * selectedFood.getPrice() + " $");
        binding.numTxt.setText(String.valueOf(quantity));

        binding.addCartBtn.setOnClickListener(v -> {
            CartModel newItem = new CartModel(selectedFood.getImageUrl(), selectedFood.getName(), selectedFood.getPrice(), quantity);
            cartViewModel.addItem(newItem);
            Toast.makeText(getContext(), "Added to cart!", Toast.LENGTH_SHORT).show();
            cartViewModel.saveCartToFirestore();
        });

        binding.plusBtn.setOnClickListener(v -> {
            quantity++;
            binding.numTxt.setText(String.valueOf(quantity));
            binding.totalTxt.setText(quantity * selectedFood.getPrice() + " $");
        });

        binding.minusBtn.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                binding.numTxt.setText(String.valueOf(quantity));
                binding.totalTxt.setText(quantity * selectedFood.getPrice() + " $");
            } else {
                Toast.makeText(getContext(), "Must order at least 1", Toast.LENGTH_SHORT).show();
            }
        });


        binding.backBtn.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.popBackStack();
        });

        binding.favBtn.setOnClickListener(v -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(getContext(), "Login to see your favourite.", Toast.LENGTH_SHORT).show();
                return;
            }
            String userId = currentUser.getUid();
            String foodId = selectedFood.getId();

            if (foodId == null || foodId.isEmpty()) {
                Toast.makeText(getContext(), "ID is invalid.", Toast.LENGTH_SHORT).show();
                return;
            }

            DocumentReference favoriteFoodRef = db.collection("users")
                    .document(userId)
                    .collection("favourites")
                    .document(foodId);

            binding.favBtn.setEnabled(false);

            if (isFavourite) {
                favoriteFoodRef.delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Deleted from favourite!", Toast.LENGTH_SHORT).show();
                            binding.favBtn.setImageResource(R.drawable.favorite_white);
                            isFavourite = false;
                            removeFavouriteIdLocal(userId, foodId);
                            binding.favBtn.setEnabled(true);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Error while removing: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            binding.favBtn.setEnabled(true);
                        });
            } else {
                favoriteFoodRef.set(selectedFood)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Added to favourite!", Toast.LENGTH_SHORT).show();
                            binding.favBtn.setImageResource(R.drawable.fav_filled);
                            isFavourite = true;
                            addFavouriteIdLocal(userId, foodId);
                            binding.favBtn.setEnabled(true);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Error while adding: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            binding.favBtn.setEnabled(true);
                        });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setBottomNavigationVisibility(true);
        }
        binding = null;
    }

    private void checkFavouriteStatusLocal(String foodId) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        binding.favBtn.setImageResource(R.drawable.favorite_white);
        isFavourite = false;

        if (currentUser != null && foodId != null && !foodId.isEmpty()) {
            String userId = currentUser.getUid();
            Set<String> favouriteIds = getFavouriteIdsLocal(userId);

            if (favouriteIds.contains(foodId)) {
                binding.favBtn.setImageResource(R.drawable.fav_filled);
                isFavourite = true;
            } else {
                binding.favBtn.setImageResource(R.drawable.favorite_white);
                isFavourite = false;
            }
        } else {
            binding.favBtn.setEnabled(true);
        }
    }

    private void checkFavouriteStatusFirestore(String foodId) {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null && foodId != null && !foodId.isEmpty()) {
            String userId = currentUser.getUid();
            DocumentReference favoriteFoodRef = db.collection("users").document(userId)
                    .collection("favourites")
                    .document(foodId);

            favoriteFoodRef.get().addOnCompleteListener(task -> {
                binding.favBtn.setEnabled(true);

                if (task.isSuccessful()) {
                    boolean existsInFirestore = task.getResult().exists();

                    if (existsInFirestore != isFavourite) {
                        if (existsInFirestore) {
                            binding.favBtn.setImageResource(R.drawable.fav_filled);
                            isFavourite = true;
                            addFavouriteIdLocal(userId, foodId);
                        } else {
                            binding.favBtn.setImageResource(R.drawable.favorite_white);
                            isFavourite = false;
                            removeFavouriteIdLocal(userId, foodId);
                        }
                    }
                } else {
                    Log.e("FoodDetail", "Error checking favourite status from Firestore", task.getException());
                }
            });
        } else {
            binding.favBtn.setEnabled(true);
        }
    }

    // SharedPreferences
    private SharedPreferences getPrefs(String userId) {
        return requireActivity().getSharedPreferences(FAV_PREFS_KEY_PREFIX + userId, Context.MODE_PRIVATE);
    }

    private Set<String> getFavouriteIdsLocal(String userId) {
        SharedPreferences prefs = getPrefs(userId);
        return prefs.getStringSet("foodIds", new HashSet<>()) ;
    }

    private void addFavouriteIdLocal(String userId, String foodId) {
        SharedPreferences prefs = getPrefs(userId);
        Set<String> favouriteIds = new HashSet<>(getFavouriteIdsLocal(userId));
        if (favouriteIds.add(foodId)) {
            prefs.edit().putStringSet("foodIds", favouriteIds).apply();
        }
    }

    private void removeFavouriteIdLocal(String userId, String foodId) {
        SharedPreferences prefs = getPrefs(userId);
        Set<String> favouriteIds = new HashSet<>(getFavouriteIdsLocal(userId));
        if (favouriteIds.remove(foodId)) {
            prefs.edit().putStringSet("foodIds", favouriteIds).apply();
        }
    }

//    public LiveData<Float> getAverageRatingLiveData(String foodId) {
//        LiveData<List<CommentModel>> commentsLiveData = commentViewModel.getComments(foodId);
//
//        // Using Transformations.map
//        return Transformations.map(commentsLiveData, comments -> {
//            if (comments == null || comments.isEmpty()) {
//                return 0.0f; // Default to 0 if no comments or list is null
//            }
//            float totalRating = 0;
//            int validCommentsCount = 0;
//            for (CommentModel cmt : comments) {
//                if (cmt != null) { // Good practice to check individual items too
//                    totalRating += cmt.getRating();
//                    validCommentsCount++;
//                }
//            }
//            if (validCommentsCount == 0) {
//                return 0.0f; // Avoid division by zero if all comments were null (unlikely but safe)
//            }
//            return totalRating / validCommentsCount;
//        });
//    }

}