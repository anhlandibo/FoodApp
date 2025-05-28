package com.example.foodapp2025.ui.activity;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodapp2025.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SelectLocationActivity extends AppCompatActivity {
    MapView mapView;
    Marker marker;
    FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Geocoder geocoder;
    AutoCompleteTextView locationSearch;
    private Handler handler = new Handler();
    private static final long DEBOUNCE_DELAY = 300; // delay 300ms


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        setContentView(R.layout.activity_select_location);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this);
        mapView = findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        IMapController mapController = mapView.getController();
        mapController.setZoom(15.0);

        GeoPoint defaultPoint = new GeoPoint(10.762622, 106.660172); // Hồ Con Rùa
        mapController.setCenter(defaultPoint);

        // Gán listener khi user bấm lên bản đồ
        mapView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                Projection projection = mapView.getProjection();
                GeoPoint geoPoint = (GeoPoint) projection.fromPixels((int) event.getX(), (int) event.getY());

                double lat = geoPoint.getLatitude();
                double lon = geoPoint.getLongitude();

                // Thêm hoặc cập nhật marker
                if (marker == null) {
                    marker = new Marker(mapView);
                    mapView.getOverlays().add(marker);
                }
                marker.setPosition(geoPoint);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marker.setTitle("Địa điểm giao hàng");
                mapView.invalidate();


            }
            return false;
        });

        //Xác nhận vị trí
        Button btn = findViewById(R.id.btnConfirmLocation);
        btn.setOnClickListener(v -> {
            if (marker != null) {
                GeoPoint selectedPoint = marker.getPosition();
                showConfirmDialog(selectedPoint); // Hiển thị hộp thoại xác nhận
            }
        });

        // Lấy vị trí hiện tại khi nhấn nút "Lấy vị trí hiện tại"
        Button btnGetCurrentLocation = findViewById(R.id.btnGetCurrentLocation);
        btnGetCurrentLocation.setOnClickListener(v -> getCurrentLocation());

        //Tìm kiếm địa điểm
        locationSearch = findViewById(R.id.locationSearch);

        locationSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> searchLocationSuggestions(editable.toString()), DEBOUNCE_DELAY);
            }
        });

        Button searchBtn = findViewById(R.id.btnSearchLocation);
        searchBtn.setOnClickListener(v -> {
            // Khi nhấn xác nhận, di chuyển đến địa điểm đã chọn
            String locationName = locationSearch.getText().toString();
            searchLocation(locationName);
        });

    }
    // Hiển thị hộp thoại xác nhận trước khi chuyển về màn hình chính
    private void showConfirmDialog(GeoPoint selectedPoint) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Location")
                .setMessage("Are you sure you want to select this location?")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    // Nếu người dùng xác nhận, chuyển vị trí và quay lại màn hình chính
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("lat", selectedPoint.getLatitude());
                    resultIntent.putExtra("lon", selectedPoint.getLongitude());
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())  // Người dùng hủy bỏ, không làm gì cả
                .show();
    }

    // Tìm kiếm gợi ý địa điểm khi người dùng nhập
    private void searchLocationSuggestions(String query) {
        if (query.length() < 3) {
            return; // Chỉ tìm kiếm khi người dùng nhập ít nhất 3 ký tự
        }
        try {
            List<Address> addresses = geocoder.getFromLocationName(query, 5);  // Tìm tối đa 5 địa điểm
            if (addresses != null && !addresses.isEmpty()) {
                // Lọc danh sách địa điểm và hiển thị gợi ý
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
                for (Address address : addresses) {
                    adapter.add(address.getAddressLine(0));  // Lấy địa chỉ
                }
                locationSearch.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error fetching location suggestions", Toast.LENGTH_SHORT).show();
        }
    }

    // Tìm vị trí của địa điểm khi người dùng xác nhận
    private void searchLocation(String locationName) {
        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                double latitude = address.getLatitude();
                double longitude = address.getLongitude();

                // Di chuyển bản đồ đến tọa độ này
                IMapController mapController = mapView.getController();
                mapController.setZoom(15);
                mapController.setCenter(new org.osmdroid.util.GeoPoint(latitude, longitude));

                // Thêm marker vào vị trí tìm được
//                org.osmdroid.views.overlay.Marker marker = new org.osmdroid.views.overlay.Marker(mapView);
                if (marker == null) {
                    marker = new Marker(mapView);
                    mapView.getOverlays().add(marker);
                }
                marker.setPosition(new org.osmdroid.util.GeoPoint(latitude, longitude));
                marker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM);
                marker.setTitle("Location: " + address.getLocality());
                mapView.invalidate();

                // Nếu location là null thì lấy adminArea hoặc countryName
                String locationTitle = address.getLocality();
                if (locationTitle == null){
                    locationTitle = address.getAdminArea();
                    if (locationTitle == null){
                        locationTitle = address.getCountryName();
                    }
                }

                Toast.makeText(this, "Location found: " + locationTitle, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error searching location", Toast.LENGTH_SHORT).show();
        }
    }

    private void getCurrentLocation() {
        // Kiểm tra quyền
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Yêu cầu quyền nếu chưa được cấp
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double lat = location.getLatitude();
                            double lon = location.getLongitude();

                            GeoPoint geoPoint = new GeoPoint(lat, lon);
                            IMapController mapController = mapView.getController();
                            mapController.setCenter(geoPoint);

                            if (marker == null) {
                                marker = new Marker(mapView);
                                mapView.getOverlays().add(marker);
                            }
                            marker.setPosition(geoPoint);
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            marker.setTitle("Vị trí hiện tại");
                            mapView.invalidate();
                        } else {
                            Toast.makeText(SelectLocationActivity.this, "Không tìm được vị trí. Thử lại sau!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }


    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Ứng dụng cần quyền truy cập vị trí để hoạt động!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}