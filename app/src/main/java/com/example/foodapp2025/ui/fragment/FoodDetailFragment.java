package com.example.foodapp2025.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.foodapp2025.R;
import com.example.foodapp2025.data.model.FoodModel;
import com.example.foodapp2025.databinding.FragmentFoodDetailBinding;
import com.example.foodapp2025.ui.activity.MainActivity;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FoodDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FoodDetailFragment extends Fragment {
    private FragmentFoodDetailBinding binding;
    private int quantity = 1;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FoodDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FoodDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FoodDetailFragment newInstance(String param1, String param2) {
        FoodDetailFragment fragment = new FoodDetailFragment();
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
        binding = FragmentFoodDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private void initBackBtn(View view, String foodName){
        ImageView temp = view.findViewById(R.id.backBtn);

        temp.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.popBackStack(); // return category detail fragment
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setBottomNavigationVisibility(false);
        }

        //Lay categoryName tu Bundle arguments
        String foodName = null;
        if (getArguments() != null) {
            foodName = getArguments().getString("foodName");
        }

        if (foodName == null || foodName.isEmpty()) {
            Toast.makeText(getContext(), "Food not found", Toast.LENGTH_SHORT).show();
            return;
        }

        //Lay selectedFood trong list cac food
        FoodModel selectedFood = (FoodModel) getArguments().getSerializable("food");

        if (selectedFood == null){
            Toast.makeText(getContext(), "Food not found", Toast.LENGTH_SHORT).show();
            return;
        }


        Glide.with(FoodDetailFragment.this).load(selectedFood.getImageUrl()).into(binding.imageView7);

        binding.titleTxt.setText(selectedFood.getName());
        binding.priceTxt.setText(selectedFood.getPrice() + " VND");
        binding.descriptionTxt.setText(selectedFood.getDescription());
        binding.rateTxt.setText(selectedFood.getStar() + " Rating");
        binding.ratingBar.setRating(Float.parseFloat(String.valueOf(selectedFood.getStar())));
        binding.totalTxt.setText(quantity * selectedFood.getPrice() + " VND");

        initBackBtn(view, foodName);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setBottomNavigationVisibility(true);
        }
    }
}