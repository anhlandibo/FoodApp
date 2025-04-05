package com.example.foodapp2025.ui.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.foodapp2025.R;
import com.example.foodapp2025.data.model.FoodModel;
import com.example.foodapp2025.databinding.FragmentCategoryDetailBinding;
import com.example.foodapp2025.ui.adapter.FoodAdapter;
import com.example.foodapp2025.viewmodel.FoodViewModel;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CategoryDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategoryDetailFragment extends Fragment {
    private FragmentCategoryDetailBinding binding;
    private FoodViewModel foodViewModel;
    private FoodAdapter foodAdapter;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CategoryDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CategoryDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CategoryDetailFragment newInstance(String param1, String param2) {
        CategoryDetailFragment fragment = new CategoryDetailFragment();
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
        binding = FragmentCategoryDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private void initToolbar(View view, String categoryName) {
        MaterialToolbar toolbar = view.findViewById(R.id.categoryDetailToolbar);

        toolbar.setTitle(categoryName);

        toolbar.setNavigationOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.popBackStack(); // return home fragment
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        //Lay args tu Bundle arguments
        String categoryName = null;
        int minPrice = 0;
        boolean isPopular = false;


        if (getArguments() != null) {
            categoryName = getArguments().getString("categoryName");
            minPrice = getArguments().getInt("minPrice"); // default = 0
            isPopular = getArguments().getBoolean("isPopular");
        } else {
            isPopular = false;
            minPrice = 0;
        }

        if (categoryName == null || categoryName.isEmpty()) {
            Toast.makeText(getContext(), "Category not found", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.e("IsPopular", "ispopular: "+ isPopular);

        // Back button
        initToolbar(view, categoryName);


        //Cai dat RecyclerView voi FoodAdapter va GridLayoutManager
        foodAdapter = new FoodAdapter();
        binding.foodRecyclerView.setAdapter(foodAdapter);
        binding.foodRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));


        //Khoi tao ViewModel va quan sat LiveData tu repository
        foodViewModel = new ViewModelProvider(this).get(FoodViewModel.class);
        int finalMinPrice = minPrice;
        boolean finalIsPopular = isPopular;
        foodViewModel.getMenuItems(categoryName).observe(getViewLifecycleOwner(), foodList -> {
            if (foodList != null && !foodList.isEmpty()) {
                ArrayList<FoodModel> filteredList = new ArrayList<>();
                for (FoodModel food : foodList) {
                    Log.d("FoodItem", food.getName() + ": " + food.getPrice() + " - Popular: " + food.getIsPopular() + " min price: " + finalMinPrice);
                    boolean matchesFilter = food.getPrice() >= finalMinPrice;
                    if (finalIsPopular) {
                        matchesFilter = matchesFilter && food.getIsPopular();
                    }
                    if (matchesFilter) {
                        filteredList.add(food);
                    }
                }
                foodAdapter.setFoodList(filteredList);

                if (filteredList.isEmpty()) {
                    Toast.makeText(getContext(), "No food items match your filter", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "No food item found", Toast.LENGTH_SHORT).show();
            }
        });


    }
}