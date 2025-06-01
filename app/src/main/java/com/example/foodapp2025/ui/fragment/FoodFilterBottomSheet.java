package com.example.foodapp2025.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;

import com.example.foodapp2025.R;
import com.example.foodapp2025.data.model.CategoryModel;
import com.example.foodapp2025.databinding.FragmentFoodFilterBottomSheetBinding;
import com.example.foodapp2025.viewmodel.CategoryViewModel;
import com.example.foodapp2025.viewmodel.FoodViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FoodFilterBottomSheet#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FoodFilterBottomSheet extends BottomSheetDialogFragment {

    private FragmentFoodFilterBottomSheetBinding binding;

    private CategoryViewModel categoryViewModel;
    private FoodViewModel foodViewModel;

    private OnFilterApplyListener listener;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FoodFilterBottomSheet() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FoodFilterBottomSheet.
     */
    // TODO: Rename and change types and number of parameters
    public static FoodFilterBottomSheet newInstance(String param1, String param2) {
        FoodFilterBottomSheet fragment = new FoodFilterBottomSheet();
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
        binding = FragmentFoodFilterBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Khoi tao ViewModel
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        foodViewModel = new ViewModelProvider(this).get(FoodViewModel.class);


        //Lay du lieu category
        categoryViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null && !categories.isEmpty()) {
                //Lay danh muc va gan vao spinner
                ArrayList<String> categoryNames = new ArrayList<>();
                for (CategoryModel it : categories) {
                    categoryNames.add(it.getName());
                }

                ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item, categoryNames);
                categoryAdapter.setDropDownViewResource(R.layout.spinner_item);
                binding.spinnerCategory.setAdapter(categoryAdapter);
            }
        });

        //Lay minprice, maxprice
        foodViewModel.getMinPriceFood().observe(getViewLifecycleOwner(), minPrice -> {
            binding.seekMinPrice.setProgress(Math.toIntExact(Math.round(minPrice.getPrice())));
        });

        foodViewModel.getMaxPriceFood().observe(getViewLifecycleOwner(), maxPrice -> {
            binding.seekMinPrice.setMax(Math.toIntExact(Math.round(maxPrice.getPrice())));
            binding.maxPriceTxt.setText(Math.toIntExact(Long.parseLong(Math.round(maxPrice.getPrice()) + " VND")));
        });

        binding.seekMinPrice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                binding.minPriceTxt.setText(progress + " VND");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Apply cac options va chuyen sang man hinh ket qua
        binding.applyBtn.setOnClickListener(v -> {
            boolean isPopularOnly = binding.checkPopular.isChecked();
            String selectedCategory = binding.spinnerCategory.getSelectedItem().toString();
            int minPrice = binding.seekMinPrice.getProgress();

//            Bundle bundle = new Bundle();
//            bundle.putString("category", selectedCategory);
//            bundle.putInt("minPrice", minPrice);
//            bundle.putBoolean("isPopular", isPopularOnly);
//            dismiss();
//            NavController navController = NavHostFragment.findNavController(this);
//            navController.navigate(R.id.categoryDetailFragment, bundle);

            if (listener != null){
                listener.onFilterApplied(selectedCategory, minPrice, isPopularOnly);
            }
            dismiss();
        });
    }
    public void setOnFilterApplyListener(OnFilterApplyListener listener) {
        this.listener = listener;
    }
    public interface OnFilterApplyListener{
        void onFilterApplied(String category, int minPrice, boolean isPopular);
    }
}