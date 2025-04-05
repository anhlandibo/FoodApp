package com.example.foodapp2025.ui.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.foodapp2025.R;
import com.example.foodapp2025.data.model.FoodModel;
import com.example.foodapp2025.databinding.FragmentSearchResultBinding;
import com.example.foodapp2025.ui.adapter.FoodAdapter;
import com.example.foodapp2025.viewmodel.FoodViewModel;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchResultFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchResultFragment extends Fragment {
    private FragmentSearchResultBinding binding;
    private FoodAdapter foodAdapter;
    private FoodViewModel foodViewModel;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SearchResultFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchResultFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchResultFragment newInstance(String param1, String param2) {
        SearchResultFragment fragment = new SearchResultFragment();
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
        binding = FragmentSearchResultBinding.inflate(inflater, container, false);

        //Lay keyword
        String keyword = getArguments() != null ? getArguments().getString("search_keyword") : "";
        setUpRecylerView();
        loadAllFoodAndFilter(keyword);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initToolbar(view);
    }

    private void initToolbar(View view){
        MaterialToolbar toolbar = view.findViewById(R.id.searchFoodToobar);

        toolbar.setNavigationOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.popBackStack(); // return home fragment
        });
    }

    private void loadAllFoodAndFilter(String keyword) {
        foodViewModel = new ViewModelProvider(this).get(FoodViewModel.class);

        foodViewModel.getFoodByKeyword(keyword).observe(getViewLifecycleOwner(), filteredFoodList -> {
            if (filteredFoodList != null && !filteredFoodList.isEmpty()){
                foodAdapter.setFoodList(filteredFoodList);
            }
            else {
                Toast.makeText(getContext(), "No food item found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUpRecylerView() {
        foodAdapter = new FoodAdapter();
        binding.searchFoodRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.searchFoodRecyclerView.setAdapter(foodAdapter);
    }


}