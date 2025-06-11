package com.example.foodapp2025.ui.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp2025.R;
import com.example.foodapp2025.data.model.VoucherModel;
import com.example.foodapp2025.ui.adapter.VoucherAdapter;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class VoucherSelectionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VoucherAdapter adapter;
    private List<VoucherModel> voucherList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voucherselection);

        recyclerView = findViewById(R.id.recyclerVouchers);
        adapter = new VoucherAdapter(voucherList, this::onVoucherSelected);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Set up gift code button listener
        findViewById(R.id.btnEnterGiftCode).setOnClickListener(v -> {
            showGiftCodeDialog();
        });

        // Set up back button listener
        findViewById(R.id.backArrowVoucherSelection).setOnClickListener(v -> {
            finish();
        });

        loadVouchersFromFirestore();
        Log.d("VoucherActivity", "Loaded voucher size: " + voucherList.size());
    }

    private void loadVouchersFromFirestore() {
        FirebaseFirestore.getInstance().collection("vouchers")
                .whereEqualTo("isActive", true)
                .whereEqualTo("isPrivate", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    voucherList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        VoucherModel voucher = doc.toObject(VoucherModel.class);
                        if (voucher != null && !voucher.isExpired()) {
                            voucherList.add(voucher);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void onVoucherSelected(VoucherModel voucher) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("selectedVoucher", voucher);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void showGiftCodeDialog() {
        // Inflate the custom dialog layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_gift_code_entry, null);

        // Get references to dialog elements
        TextInputEditText etGiftCode = dialogView.findViewById(R.id.etGiftCode);

        // Create the dialog
        AlertDialog giftCodeDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Set up button listeners
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> {
            giftCodeDialog.dismiss();
        });

        dialogView.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            String enteredCode = etGiftCode.getText().toString().trim().toUpperCase();

            if (enteredCode.isEmpty()) {
                Toast.makeText(this, "Please enter a gift code", Toast.LENGTH_SHORT).show();
                return;
            }

            // Search for the voucher by code
            searchVoucherByCode(enteredCode, giftCodeDialog);
        });

        giftCodeDialog.show();
    }

    private void searchVoucherByCode(String code, AlertDialog dialog) {
        // Show loading
        Toast.makeText(this, "Searching for gift code...", Toast.LENGTH_SHORT).show();

        // Query Firestore for voucher with matching code
        FirebaseFirestore.getInstance()
                .collection("vouchers")
                .whereEqualTo("code", code)
                .whereEqualTo("isActive", true)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Voucher found!
                        VoucherModel foundVoucher = queryDocumentSnapshots.getDocuments().get(0)
                                .toObject(VoucherModel.class);

                        if (foundVoucher != null && !foundVoucher.isExpired()) {
                            // Apply the voucher using existing logic
                            onVoucherSelected(foundVoucher);
                            dialog.dismiss();
                        } else {
                            Toast.makeText(this, "Gift code expired or invalid", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // No voucher found with this code
                        Toast.makeText(this, "Gift code not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error searching gift code: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}