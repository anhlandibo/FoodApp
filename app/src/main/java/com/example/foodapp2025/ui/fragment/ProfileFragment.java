package com.example.foodapp2025.ui.fragment;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.example.foodapp2025.R;
import com.example.foodapp2025.databinding.FragmentProfileBinding;
import com.example.foodapp2025.viewmodel.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private FragmentProfileBinding binding;
    private UserViewModel userViewModel;
    private boolean isEditBtnPressed = false;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ImageView avatarImageView;
    private LinearLayout changeAvtLayout;
    private Spinner genderSpinner;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

//        genderSpinner = binding.gender;
//
//        String[] gendersList = getResources().getStringArray(R.array.gender_array);
//
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(
//                requireContext(),
//                R.layout.custom_spinner_item,
//                R.id.spinner_item_text,
//                gendersList
//        );
//
//        adapter.setDropDownViewResource(R.layout.custom_spinner_item);
//        genderSpinner.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userViewModel = new UserViewModel();

        // Initialize OkHttpClient and Gson

        init(view);
        loadUserInformation(userId);
        setupAvatarChangeFunctionality();
    }

    // Load user information
    public void loadUserInformation(String userId) {
        userViewModel.getUserInformation(userId).observe(getViewLifecycleOwner(), user -> {
            binding.fullName.setText(user.getName());
            binding.emailText.setText(user.getEmail());
            binding.phoneNumber.setText(user.getPhoneNumber());
            binding.address.setText(user.getAddress());
            binding.dateOfBirth.setText(user.getDateOfBirth());

            String photoUrlString = user.getPhotoUrl();

            ArrayAdapter<String> adapter = (ArrayAdapter<String>) binding.gender.getAdapter();
            int position = adapter.getPosition(user.getGender());
            if (position >= 0) {
                binding.gender.setSelection(position);
            }

            if(photoUrlString != null && !photoUrlString.isEmpty()) {
                Glide.with(binding.profileAvt.getContext()) // Lấy context từ ImageView
                        .load(photoUrlString) // Truyền thẳng chuỗi URL vào đây. Glide tự parse và xử lý null.
                        .error(R.drawable.app_icon) // <-- Ảnh mặc định nếu photoUrlString là null, rỗng, hoặc tải lỗi
                        .into(binding.profileAvt); // Đích đến là ImageView của bạn
            } else {
//                avatarImageView.setImageResource(R.drawable.profile);
            }
        });
    }

    // Init components
    public void init(View view) {
        binding.fullName.setEnabled(false);
        binding.phoneNumber.setEnabled(false);
        binding.address.setEnabled(false);
        binding.gender.setEnabled(false);

        datePickerDropDown();
        binding.dateOfBirth.setEnabled(false);
        // Đặt OnClickListener
        initButton(view);
    }

    public void initButton(View view) {
        Button editBtn = view.findViewById(R.id.editBtn);
        editBtn.setOnClickListener(v -> {
            handleEditBtn(view);
        });

        // Setting btn
        LinearLayout settingLayout = view.findViewById(R.id.settingButton);
        settingLayout.setOnClickListener(v -> {
            handleSettingButton(view);
        });

        // Change Avatar
        changeAvtLayout = view.findViewById(R.id.changeAvtBtn);
        changeAvtLayout.setOnClickListener(v -> checkPermissionsAndPickImage());
    }

    public void datePickerDropDown() {
        binding.dateOfBirth.setInputType(InputType.TYPE_NULL);
        binding.dateOfBirth.setFocusable(false);

        binding.dateOfBirth.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dpDialog = new DatePickerDialog(requireContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                                selectedDay, selectedMonth + 1, selectedYear);

                        binding.dateOfBirth.setText(date);
                    },
                    year, month, day);

            dpDialog.show();
        });
    }
    public void handleEditBtn(View view) {
        if (!isEditBtnPressed) {
            userViewModel.handleEditBtn(view, binding, isEditBtnPressed);
            isEditBtnPressed = true;
        } else {
            userViewModel.handleEditBtn(view, binding, isEditBtnPressed);
            isEditBtnPressed = false;
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            loadUserInformation(userId);
        }
    }

    public void handleSettingButton(View view) {
        NavController navController = Navigation.findNavController(view);
        navController.navigate(R.id.profileToSetting);
    }

    private void setupAvatarChangeFunctionality() {
        // Đăng ký ActivityResultLauncher cho quyền
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openImageChooser();
                    } else {
                        Toast.makeText(requireContext(), "Cần quyền truy cập thư viện ảnh để thay đổi avatar.", Toast.LENGTH_SHORT).show();
                    }
                });

        // Đăng ký ActivityResultLauncher để chọn ảnh
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) { // Sử dụng requireActivity().RESULT_OK
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            userViewModel.uploadAvatar(imageUri, requireContext());
                        }
                    } else {
                        Toast.makeText(requireContext(), "Không có ảnh được chọn.", Toast.LENGTH_SHORT).show();
                    }
                });

        // Quan sát thông tin người dùng và trạng thái upload với getViewLifecycleOwner()
        observeAvatarUploadStatus();
    }


    private void observeAvatarUploadStatus() {
        userViewModel.getAvatarUploadStatus().observe(getViewLifecycleOwner(), status -> {
            if ("Success".equals(status)) {
                Toast.makeText(requireContext(), "Ảnh đại diện đã được cập nhật.", Toast.LENGTH_SHORT).show();
                loadUserInformation(userViewModel.getUserID());
            } else if (status != null && status.startsWith("Failed")) {
                System.out.println(status);
                Toast.makeText(requireContext(), "Cập nhật ảnh đại diện thất bại: " + status, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void checkPermissionsAndPickImage() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) { // Sử dụng requireContext()
            openImageChooser();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }
}
