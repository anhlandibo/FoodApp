package com.example.foodapp2025.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp2025.R;
import com.example.foodapp2025.data.model.VoucherModel;
import com.example.foodapp2025.ui.adapter.VoucherAdapter;
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

        loadVouchersFromFirestore();
        Log.d("VoucherActivity", "Loaded voucher size: " + voucherList.size());

    }

    private void loadVouchersFromFirestore() {
        FirebaseFirestore.getInstance().collection("vouchers")
                .whereEqualTo("isActive", true)
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
        resultIntent.putExtra("selectedVoucher", voucher); // Serializable hoặc Parcelable
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}

