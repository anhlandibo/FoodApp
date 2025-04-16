package com.example.foodapp2025.ui.fragment;

import android.app.DatePickerDialog;
import android.net.wifi.hotspot2.pps.Credential;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Process;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.example.foodapp2025.R;
import com.example.foodapp2025.databinding.FragmentProfileBinding;
import com.example.foodapp2025.viewmodel.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userViewModel = new UserViewModel();

        init(view);
        loadUserInformation(userId);
    }

    // Load user information
    public void loadUserInformation(String userId) {
        userViewModel.getUserInformation(userId).observe(getViewLifecycleOwner(), user -> {
            binding.fullName.setText(user.getName());
            binding.emailText.setText(user.getEmail());
            binding.phoneNumber.setText(user.getPhoneNumber());
            binding.address.setText(user.getAddress());
            binding.dateOfBirth.setText(user.getDateOfBirth());

            ArrayAdapter<String> adapter = (ArrayAdapter<String>) binding.gender.getAdapter();
            int position = adapter.getPosition(user.getGender());
            if (position >= 0) {
                binding.gender.setSelection(position);
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

        initEditButton(view);
    }

    public void initEditButton(View view) {
        Button editBtn = view.findViewById(R.id.editBtn);
        editBtn.setOnClickListener(v -> {
            handleEditBtn(view);
        });
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

}