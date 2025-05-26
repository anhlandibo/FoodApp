package com.example.foodapp2025.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log; // Để in log

import com.google.firebase.firestore.GeoPoint;

import java.util.List;
import java.util.Locale;

public class LocationConverter {

    private static final String TAG = "LocationConverter";

    public interface AddressResultListener {
        void onAddressReceived(String address);
        void onError(String errorMessage);
    }

    public static void getAddressFromCoordinatesAsync(Context context, GeoPoint geoPoint, AddressResultListener listener) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());

            geocoder.getFromLocation(geoPoint.getLatitude(), geoPoint.getLongitude(), 1, new Geocoder.GeocodeListener() {
                @Override
                public void onGeocode(List<Address> addresses) {
                    if (addresses != null && !addresses.isEmpty()) {
                        Address returnedAddress = addresses.get(0);
                        StringBuilder strReturnedAddress = new StringBuilder("");
                        for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                            strReturnedAddress.append(returnedAddress.getAddressLine(i));
                        }
                        listener.onAddressReceived(strReturnedAddress.toString());
                    } else {
                        listener.onAddressReceived("Không tìm thấy địa chỉ.");
                        Log.w(TAG, "Geocoder.onGeocode: Không tìm thấy địa chỉ cho tọa độ này.");
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    listener.onError("Lỗi Geocoder: " + errorMessage);
                    Log.e(TAG, "Geocoder.onError: " + errorMessage);
                }
            });
        } else {
            Log.w(TAG, "Sử dụng Geocoder đồng bộ trên API < 33. Cân nhắc dùng AsyncTask/ExecutorService.");
            String address = getAddressFromCoordinatesSync(context, geoPoint.getLatitude(), geoPoint.getLongitude());
            listener.onAddressReceived(address);
        }
    }

    // Phương thức đồng bộ (chỉ để tham khảo hoặc dùng cho API < 33 nếu bạn đã xử lý luồng)
    private static String getAddressFromCoordinatesSync(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String addressText = "Không tìm thấy địa chỉ";

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                addressText = strReturnedAddress.toString();
            }
        } catch (Exception e) { // Bắt Exception chung để bao gồm IOException
            Log.e(TAG, "Lỗi khi lấy địa chỉ đồng bộ: " + e.getMessage(), e);
            addressText = "Lỗi khi lấy địa chỉ: " + e.getMessage();
        }
        return addressText;
    }
}