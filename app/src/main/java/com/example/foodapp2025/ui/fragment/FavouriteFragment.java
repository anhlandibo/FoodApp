package com.example.foodapp2025.ui.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer; // Import Observer
import androidx.lifecycle.ViewModelProvider; // Import ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.foodapp2025.R;
import com.example.foodapp2025.data.model.FoodModel;
import com.example.foodapp2025.databinding.FragmentFavouriteBinding;
import com.example.foodapp2025.ui.adapter.FavouriteFoodAdapter;
import com.example.foodapp2025.viewmodel.FavouriteFoodViewModel; // Import ViewModel

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FavouriteFragment extends Fragment {
    // Giữ lại ARG_PARAMS nếu bạn dùng
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    FragmentFavouriteBinding binding;
    private FavouriteFoodViewModel viewModel; // Khai báo ViewModel
    private FavouriteFoodAdapter favFoodAdapter;

    // Không cần các biến trạng thái này trong Fragment nữa
    // private List<FoodModel> favFoodListAllLoaded;
    // private List<FoodModel> favFoodListFiltered;
    // private static final int PAGE_SIZE = 15; // Constant có thể giữ
    // private DocumentSnapshot lastDocument;
    // private boolean isLoading = false;
    // private boolean isLastPage = false;
    // private ListenerRegistration favListener;

    // Không cần biến này trong Fragment nữa, nó ở ViewModel
    // private String currentSearchQuery = "";


    public static FavouriteFragment newInstance(String param1, String param2) {
        FavouriteFragment fragment = new FavouriteFragment();
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
        binding = FragmentFavouriteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo ViewModel
        // Sử dụng ViewModelProvider(this) nếu ViewModel chỉ dùng cho Fragment này.
        // Sử dụng ViewModelProvider(requireActivity()) hoặc activityViewModels()
        // nếu muốn ViewModel tồn tại cùng Activity và được chia sẻ giữa các Fragment (TABs)
        // Cách dùng requireActivity() hoặc activityViewModels() phù hợp hơn cho trường hợp chia sẻ giữa các tab.
        // Cần thêm dependency 'androidx.fragment:fragment-ktx:1.2.0' để dùng activityViewModels()
        viewModel = new ViewModelProvider(requireActivity()).get(FavouriteFoodViewModel.class);


        favFoodAdapter = new FavouriteFoodAdapter(getContext(), new ArrayList<>());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.recyclerViewFavouriteFoods.setLayoutManager(layoutManager);
        binding.recyclerViewFavouriteFoods.setAdapter(favFoodAdapter);

        // Thêm scroll listener để thông báo ViewModel khi cần load thêm
        binding.recyclerViewFavouriteFoods.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Chỉ load thêm khi cuộn xuống và ViewModel không bận và chưa hết trang
                if (dy > 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    // Kiểm tra nếu gần cuối danh sách (cách 5 item)
                    // Sử dụng trạng thái từ ViewModel
                    if (Boolean.FALSE.equals(viewModel.isLoading.getValue()) && !viewModel.isLastPage &&
                            (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5) {
                        Log.d("FavouriteFragment", "Detected end of list, requesting load more from ViewModel");
                        viewModel.loadMore(); // Gọi ViewModel để load thêm
                    }
                }
            }
        });


        // *** BẮT ĐẦU QUAN SÁT LIVE DATA TỪ VIEWMODEL ***

        // Quan sát danh sách món ăn yêu thích đã lọc từ ViewModel
        viewModel.favFoodListFiltered.observe(getViewLifecycleOwner(), favoriteFoods -> {
            Log.d("FavouriteFragment", "Observed filtered foods update. Count: " + favoriteFoods.size());
            // Cập nhật Adapter với danh sách từ ViewModel
            favFoodAdapter.setData(favoriteFoods); // Đảm bảo Adapter nhận FoodModel

            // Cập nhật trạng thái rỗng dựa trên danh sách hiện tại
            updateEmptyState(favoriteFoods.isEmpty());

        });

        // Quan sát trạng thái loading từ ViewModel
        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            Log.d("FavouriteFragment", "Observed loading state: " + isLoading);
            // Cập nhật UI loading indicator
            if (isLoading) {
                binding.progressBar2.setVisibility(View.VISIBLE);
                // Có thể ẩn empty state message tạm thời khi đang load
                if (favFoodAdapter.getItemCount() == 0) {
                    binding.textViewEmptyFavourites.setVisibility(View.GONE);
                }
            } else {
                binding.progressBar2.setVisibility(View.GONE);
                // Sau khi loading xong, cập nhật lại empty state message
                updateEmptyState(favFoodAdapter.getItemCount() == 0);
            }
        });

        // Quan sát trạng thái đăng nhập để hiển thị message phù hợp
        viewModel.isUserLoggedIn.observe(getViewLifecycleOwner(), isLoggedIn -> {
            Log.d("FavouriteFragment", "Observed user logged in state: " + isLoggedIn);
            if (!isLoggedIn) {
                // Nếu user đăng xuất, hiển thị message yêu cầu đăng nhập
                favFoodAdapter.setData(new ArrayList<>()); // Xóa dữ liệu cũ
                binding.editTextSearchFavorites.setText(""); // Xóa search query
                updateEmptyState(true, "Vui lòng đăng nhập để xem danh sách yêu thích.");
                binding.progressBar2.setVisibility(View.GONE); // Ẩn loading
            } else {
                // Nếu user đăng nhập, ViewModel sẽ tự động load dữ liệu nếu cần.
                // Empty state sẽ được cập nhật khi LiveData favFoodListFiltered thay đổi.
                // Chỉ cần đảm bảo search box được reset và trạng thái empty/loading ban đầu đúng
                binding.editTextSearchFavorites.setText(""); // Reset search box
                if (favFoodAdapter.getItemCount() == 0 && Boolean.FALSE.equals(viewModel.isLoading.getValue())) {
                    // Nếu chưa có dữ liệu và không loading, có thể hiển thị loading ban đầu
                    binding.progressBar2.setVisibility(View.VISIBLE);
                    updateEmptyState(false, ""); // Ẩn message cũ
                }
            }
        });


        // Cập nhật TextWatcher để gọi ViewModel khi search text thay đổi
        binding.editTextSearchFavorites.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Gọi ViewModel để xử lý search/filter
                viewModel.setSearchQuery(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Không gọi loadFavouriteFoodPage() ở đây nữa.
        // ViewModel sẽ tự động load dữ liệu khi cần (khi isUserLoggedIn thay đổi thành true và danh sách rỗng).
    }

    // Hàm cập nhật trạng thái rỗng (chỉ dựa vào số lượng item trong adapter)
    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            binding.recyclerViewFavouriteFoods.setVisibility(View.GONE);
            binding.emptyStateContainer.setVisibility(View.VISIBLE);
            // Text message sẽ phụ thuộc vào trạng thái loading và search query
            if (Boolean.TRUE.equals(viewModel.isLoading.getValue())) {
                binding.textViewEmptyFavourites.setVisibility(View.GONE); // Ẩn text khi loading
            } else if (!viewModel.currentSearchQuery.isEmpty()){
                binding.textViewEmptyFavourites.setVisibility(View.VISIBLE);
                binding.textViewEmptyFavourites.setText("Không tìm thấy món ăn nào khớp với tìm kiếm của bạn.");
            } else if (Boolean.TRUE.equals(viewModel.isUserLoggedIn.getValue())) {
                binding.textViewEmptyFavourites.setVisibility(View.VISIBLE);
                binding.textViewEmptyFavourites.setText(getString(R.string.no_favourite_items_message)); // Khi đăng nhập và không có item
            } else {
                binding.textViewEmptyFavourites.setVisibility(View.VISIBLE);
                binding.textViewEmptyFavourites.setText("Vui lòng đăng nhập để xem danh sách yêu thích."); // Khi chưa đăng nhập
            }

        } else {
            binding.recyclerViewFavouriteFoods.setVisibility(View.VISIBLE);
            binding.emptyStateContainer.setVisibility(View.GONE);
            binding.textViewEmptyFavourites.setVisibility(View.VISIBLE); // Đảm bảo text view hiển thị lại
            binding.textViewEmptyFavourites.setText(""); // Xóa text
        }
    }

    // Helper method nếu bạn muốn set message tùy chỉnh (ít dùng khi dùng ViewModel observe)
    private void updateEmptyState(boolean isEmpty, String message) {
        if (isEmpty) {
            binding.recyclerViewFavouriteFoods.setVisibility(View.GONE);
            binding.emptyStateContainer.setVisibility(View.VISIBLE);
            binding.textViewEmptyFavourites.setVisibility(View.VISIBLE);
            binding.textViewEmptyFavourites.setText(message);
        } else {
            binding.recyclerViewFavouriteFoods.setVisibility(View.VISIBLE);
            binding.emptyStateContainer.setVisibility(View.GONE);
            binding.textViewEmptyFavourites.setVisibility(View.VISIBLE);
            binding.textViewEmptyFavourites.setText("");
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Không cần cleanup listener Firestore ở đây nữa
        binding = null; // Quan trọng để tránh memory leak với View Binding
    }

    // Firebase Auth State Listener có thể được thêm ở Activity chính để đảm bảo nó luôn lắng nghe
    // và cập nhật trạng thái đăng nhập cho ViewModel.
    // Tuy nhiên, thêm listener trong ViewModel như code mẫu cũng hoạt động.
    // Nếu thêm ở Activity, bạn cần một shared ViewModel hoặc cách khác để Fragment access trạng thái đó.
}