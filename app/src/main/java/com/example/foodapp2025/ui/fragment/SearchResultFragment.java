package com.example.foodapp2025.ui.fragment;

import static androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread;

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
import com.example.foodapp2025.databinding.FragmentSearchResultBinding;
import com.example.foodapp2025.ui.activity.PaymentActivity;
import com.example.foodapp2025.ui.adapter.FoodAdapter;
import com.example.foodapp2025.viewmodel.FoodViewModel;
import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchResultFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchResultFragment extends Fragment {
    private FragmentSearchResultBinding binding;
    private FoodAdapter foodAdapter;
    private FoodViewModel foodViewModel;
    private final OkHttpClient httpClient = new OkHttpClient();


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

    private void initToolbar(View view) {
        MaterialToolbar toolbar = view.findViewById(R.id.searchFoodToobar);

        toolbar.setNavigationOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.popBackStack(); // return home fragment
        });
    }

    private void loadAllFoodAndFilter(String keyword) {
        searchWithNLP(keyword);
//        // Nếu keyword có emotional context, dùng NLP
//        if (hasEmotionalKeywords(keyword)) {
//            searchWithNLP(keyword);
//        } else {
//            // Dùng search thông thường
//            foodViewModel = new ViewModelProvider(this).get(FoodViewModel.class);
//            foodViewModel.getFoodByKeyword(keyword).observe(getViewLifecycleOwner(), filteredFoodList -> {
//                if (filteredFoodList != null && !filteredFoodList.isEmpty()){
//                    foodAdapter.setFoodList(filteredFoodList);
//                } else {
//                    Toast.makeText(getContext(), "No food item found", Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
    }

    private void setUpRecylerView() {
        foodAdapter = new FoodAdapter();
        binding.searchFoodRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.searchFoodRecyclerView.setAdapter(foodAdapter);
    }

    private void searchWithNLP(String keyword) {
        // Tạo JSON request body
        String json = "{"
                + "\"query\":\"" + keyword + "\""
                + "}";

        RequestBody requestBody = RequestBody.create(
                json,
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url("http://10.0.231.175:5000/search") // Using server URL later
                .post(requestBody)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Chạy trên UI thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(),
                                "Network error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        Log.e("SearchResultFragment", "NLP API Error", e);
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                String responseBody = response.body().string();

               if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            // Parse JSON response
                            parseNLPResponse(responseBody);
                        } else {
                            Toast.makeText(getContext(),
                                    "Server error: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    // Method để parse JSON response từ Python API
    private void parseNLPResponse(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray resultArray = jsonObject.getJSONArray("result");

            ArrayList<FoodModel> nlpResults = new ArrayList<>();

            for (int i = 0; i < resultArray.length(); i++) {
                JSONObject dishJson = resultArray.getJSONObject(i);

                // Convert JSON sang FoodModel
                FoodModel food = new FoodModel();
                String id = dishJson.getString("id");

                if (!id.equals("unknown")) {
                    food.setId(id);
                    food.setName(dishJson.getString("name"));
                    food.setDescription(dishJson.getString("description"));
                    food.setImageUrl(dishJson.getString("imageUrl"));
                    food.setPrice(dishJson.getDouble("price"));
                    food.setStar(dishJson.getDouble("star"));
                    food.setTime(dishJson.getString("time"));
                    food.setCategoryName(dishJson.getString("categoryName"));
                }

                nlpResults.add(food);
            }

            // Cập nhật RecyclerView
            if (!nlpResults.isEmpty()) {
                foodAdapter.setFoodList(nlpResults);
//                Toast.makeText(getContext(),
//                        "Found " + nlpResults.size() + " smart recommendations!",
//                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "No recommendations found", Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            Log.e("SearchResultFragment", "JSON Parse Error", e);
            Toast.makeText(getContext(), "Response parse error", Toast.LENGTH_SHORT).show();
        }
    }


    private boolean hasEmotionalKeywords(String keyword) {
        String[] emotionalWords = {
                "stressed", "tired", "sad", "happy", "jumpy", "restless",
                "energetic", "comfort", "celebration", "anxious"
        };

        String lowerKeyword = keyword.toLowerCase();
        for (String word : emotionalWords) {
            if (lowerKeyword.contains(word)) {
                return true;
            }
        }
        return false;
    }
}