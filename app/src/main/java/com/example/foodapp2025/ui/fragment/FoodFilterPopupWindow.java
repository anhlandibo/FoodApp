package com.example.foodapp2025.ui.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.widget.ActionBarOverlayLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.example.foodapp2025.R;
import com.example.foodapp2025.data.model.CategoryModel;
import com.example.foodapp2025.viewmodel.CategoryViewModel;
import com.example.foodapp2025.viewmodel.FoodViewModel;

import java.util.ArrayList;

public class FoodFilterPopupWindow extends PopupWindow {
    private View contentView;
    private Spinner spinnerCategory;
    private SeekBar seekMinPrice;
    private TextView minPriceTxt;
    private CheckBox checkPopular;
    private Button applyBtn;

    private CategoryViewModel categoryViewModel;
    private FoodViewModel foodViewModel;
    private FoodFilterBottomSheet.OnFilterApplyListener listener;

    public FoodFilterPopupWindow(Context context, FoodFilterBottomSheet.OnFilterApplyListener listener) {
        super(LayoutInflater.from(context).inflate(R.layout.popup_food_filter, null),
                ActionBarOverlayLayout.LayoutParams.MATCH_PARENT, ActionBarOverlayLayout.LayoutParams.WRAP_CONTENT);

        this.listener = listener;

        contentView = getContentView();
        spinnerCategory = contentView.findViewById(R.id.spinnerCategory);
        seekMinPrice = contentView.findViewById(R.id.seekMinPrice);
        minPriceTxt = contentView.findViewById(R.id.minPriceTxt);
        checkPopular = contentView.findViewById(R.id.checkPopular);
        applyBtn = contentView.findViewById(R.id.applyBtn);

        categoryViewModel = new ViewModelProvider((FragmentActivity) context).get(CategoryViewModel.class);
        foodViewModel = new ViewModelProvider((FragmentActivity) context).get(FoodViewModel.class);

        setupViews();
    }

    private void setupViews() {
        // Lấy danh mục từ ViewModel và hiển thị lên Spinner
        categoryViewModel.getCategories().observe((LifecycleOwner) contentView.getContext(), categories -> {
            if (categories != null && !categories.isEmpty()) {
                ArrayList<String> categoryNames = new ArrayList<>();
                for (CategoryModel category : categories) {
                    categoryNames.add(category.getName());
                }
                ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(contentView.getContext(),
                        android.R.layout.simple_spinner_item, categoryNames);
                categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(categoryAdapter);
            }
        });

        // Lấy giá trị minPrice và maxPrice từ ViewModel
        foodViewModel.getMinPriceFood().observe((LifecycleOwner) contentView.getContext(), minPrice -> {
            seekMinPrice.setProgress(Math.toIntExact(minPrice.getPrice()));
        });

        foodViewModel.getMaxPriceFood().observe((LifecycleOwner) contentView.getContext(), maxPrice -> {
            seekMinPrice.setMax(Math.toIntExact(maxPrice.getPrice()));
        });

        // Cập nhật giá trị minPrice khi người dùng thay đổi SeekBar
        seekMinPrice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                minPriceTxt.setText(progress + " VND");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Áp dụng bộ lọc khi bấm nút
        applyBtn.setOnClickListener(v -> {
            String selectedCategory = spinnerCategory.getSelectedItem().toString();
            int minPrice = seekMinPrice.getProgress();
            boolean isPopularOnly = checkPopular.isChecked();

            if (listener != null) {
                listener.onFilterApplied(selectedCategory, minPrice, isPopularOnly);
            }

            dismiss();  // Đóng PopupWindow sau khi áp dụng filter
        });
    }

    public interface OnFilterApplyListener {
        void onFilterApplied(String category, int minPrice, boolean isPopular);
    }

}
