package com.example.loginapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Market_Location extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap gmap;
    private double userLat;
    private double userLong;
    private final List<Marker> markerList = new ArrayList<>();
    private ProgressBar progressBar;
    private Button retryButton;
    GridLayout gridLayout;

    private static final int LOCATION_PERMISSION_CODE = 101;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private MyDatabaseHelper myDatabaseHelper;
    private String productFilter = "All"; // Default filter
    private String currentMunicipality = "";

    private boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) +
                0.587 * Color.green(color) +
                0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.market_location);

        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        int backgroundColor = ContextCompat.getColor(this, R.color.white);
        if (isColorDark(backgroundColor)) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        progressBar = findViewById(R.id.progressBar);
        retryButton = findViewById(R.id.retry_button);
        myDatabaseHelper = new MyDatabaseHelper(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // to filter markets
        gridLayout = findViewById(R.id.gridLayout);
        LinearLayout chipContainer = findViewById(R.id.filter_chip_container);

        String[] productFilters = {"All", "Rice", "Corn", "Banana"};

        // Add chips dynamically
        // Outside the loop, track selected chip
        Button[] selectedChip = {null};

        for (String filter : productFilters) {
            Button chip = new Button(this);
            chip.setText(filter);
            chip.setAllCaps(false);
            chip.setTextSize(13);
            chip.setTextColor(ContextCompat.getColor(this, R.color.black));
            chip.setBackgroundResource(filter.equals("All") ? R.drawable.chip_selected : R.drawable.chip_background);
            chip.setPadding(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    dpToPx(32)
            );
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
            chip.setLayoutParams(params);
            chip.setMinWidth(dpToPx(48));

            // Click listener to update selected background
            chip.setOnClickListener(v -> {
                // Deselect previously selected chip
                if (selectedChip[0] != null) {
                    selectedChip[0].setBackgroundResource(R.drawable.chip_background);
                }

                // Select this chip
                chip.setBackgroundResource(R.drawable.chip_selected);
                selectedChip[0] = chip;

                // Update selected filter
                productFilter = filter;

                // Call market filter again with current municipality and selected filter
                getMarket(currentMunicipality, productFilter);

                // Optional: Show selection
                Toast.makeText(this, "Selected: " + filter, Toast.LENGTH_SHORT).show();
            });


            chipContainer.addView(chip);

            // Set initial selected chip if it's "All"
            if (filter.equals("All")) {
                selectedChip[0] = chip;
            }
        }




        ImageView home = findViewById(R.id.home);
        home.setOnClickListener(v -> startActivity(new Intent(Market_Location.this, Home.class)));

        ImageView user = findViewById(R.id.user_profile);
        user.setOnClickListener(v -> startActivity(new Intent(Market_Location.this, Profile.class)));

        retryButton.setOnClickListener(v -> {
            retryButton.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            checkLocationPermission();
        });

        addFragment();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }



    public void addFragment() {
        Fragment fragment = new topNav();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.top_nav_container, fragment);
        ft.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        ImageView location = findViewById(R.id.location);
        location.setImageResource(R.drawable.location_active);
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE);
        } else {
            getLocation();
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (isNetworkConnected()) {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            updateLocation(location);
                        } else {
                            fetchLocationWithGPS();
                        }
                    });
        } else {
            fetchLocationWithGPS();
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void fetchLocationWithGPS() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();

                if (lat == 0.0 && lng == 0.0) {
                    Toast.makeText(Market_Location.this, "GPS error: Try again.", Toast.LENGTH_SHORT).show();
                    retryButton.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                updateLocation(location);
            }

            @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override public void onProviderEnabled(String provider) {}
            @Override public void onProviderDisabled(String provider) {}
        }, null);
    }

    private void updateLocation(Location location) {
        userLat = location.getLatitude();
        userLong = location.getLongitude();
        LatLng userLocation = new LatLng(userLat, userLong);
        gmap.clear();
        gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16));

        Geocoder geocoder = new Geocoder(Market_Location.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(userLat, userLong, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                currentMunicipality = address.getLocality();
                getMarket(currentMunicipality, productFilter);
            } else {
                Toast.makeText(Market_Location.this, "Address not found!", Toast.LENGTH_SHORT).show();
                retryButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(Market_Location.this, "Geocoder error!", Toast.LENGTH_SHORT).show();
            retryButton.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gmap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            gmap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Location permission is needed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getMarket(String municipality, String productFilter) {
        if (municipality == null || municipality.isEmpty()) {
            Toast.makeText(this, "Municipality not found!", Toast.LENGTH_SHORT).show();
            retryButton.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        retryButton.setVisibility(View.GONE);

        if (gmap != null) {
            for (Marker marker : markerList) {
                marker.remove();
            }
            markerList.clear();
        }

        new Handler().postDelayed(() -> {
            Cursor cursor = myDatabaseHelper.getMarketDetails(municipality, productFilter);
            progressBar.setVisibility(View.GONE);
            GridLayout gridLayout = findViewById(R.id.gridLayout);
            gridLayout.removeAllViews();
            gridLayout.setColumnCount(2); // Two-column layout

            if (cursor != null && cursor.moveToFirst()) {
                int index = 0;
                do {
                    // === Market Data ===
                    String marketName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    String vendorName = cursor.getString(cursor.getColumnIndexOrThrow("vendor"));
                    String barangay = cursor.getString(cursor.getColumnIndexOrThrow("barangay"));
                    String phone_number = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
                    String vendor_id = cursor.getString(cursor.getColumnIndexOrThrow("vendor_id"));
                    double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude"));
                    double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"));

                    // === Marker ===
                    LatLng marketLocation = new LatLng(latitude, longitude);
                    Marker marketMarker = gmap.addMarker(new MarkerOptions()
                            .position(marketLocation)
                            .title(marketName)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.market_logo)));
                    markerList.add(marketMarker);

                    // === CardView ===
                    CardView cardView = new CardView(this);
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = (int) (getResources().getDisplayMetrics().widthPixels / 2.2);
                    params.height = GridLayout.LayoutParams.WRAP_CONTENT;

                    // Margins for two-column spacing
                    int leftRightMargin = dpToPx(4);  // Half of your desired 8dp gap between items
                    int topBottomMargin = dpToPx(8);

                    if (index % 2 == 0) {  // Left column
                        params.setMargins(dpToPx(8), topBottomMargin, leftRightMargin, topBottomMargin);
                    } else {  // Right column
                        params.setMargins(leftRightMargin, topBottomMargin, dpToPx(8), topBottomMargin);
                    }

                    cardView.setLayoutParams(params);

                    cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.shade1_green));
                    cardView.setRadius(24f);
                    cardView.setCardElevation(12f);
                    cardView.setUseCompatPadding(true);

                    // === Content ===
                    LinearLayout linearLayout = new LinearLayout(this);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    linearLayout.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));

                    TextView vendorTextView = createTextView("👤 " + vendorName, 20, true);
                    TextView marketTextView = createTextView("Market: " + marketName, 15, false);
                    TextView barangayTextView = createTextView("Brgy: " + barangay, 15, false);

                    // === Distance ===
                    Location userLoc = new Location("");
                    userLoc.setLatitude(userLat);
                    userLoc.setLongitude(userLong);
                    Location marketLoc = new Location("");
                    marketLoc.setLatitude(latitude);
                    marketLoc.setLongitude(longitude);
                    float distance = userLoc.distanceTo(marketLoc) / 1000;
                    TextView distanceTextView = createTextView("Distance: " + String.format("%.2f", distance) + " km", 15, false);
                    distanceTextView.setPadding(0, 0, 0, dpToPx(12));

                    // === Details Button ===
                    Button detailsButton = new Button(this);
                    detailsButton.setText("DETAILS");
                    detailsButton.setTextSize(12);
                    detailsButton.setTextColor(Color.WHITE);
                    LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            dpToPx(36));
                    btnParams.gravity = Gravity.START;
                    btnParams.topMargin = dpToPx(8);
                    detailsButton.setLayoutParams(btnParams);

                    detailsButton.setOnClickListener(v -> {
                        Intent intent = new Intent(Market_Location.this, Market_Details.class);
                        intent.putExtra("marketName", marketName);
                        intent.putExtra("vendorName", vendorName);
                        intent.putExtra("vendor_id", vendor_id);
                        intent.putExtra("barangay", barangay);
                        intent.putExtra("phone", phone_number);
                        intent.putExtra("latitude", latitude);
                        intent.putExtra("longitude", longitude);
                        startActivity(intent);
                    });

                    // === Add views ===
                    linearLayout.addView(vendorTextView);
                    linearLayout.addView(marketTextView);
                    linearLayout.addView(barangayTextView);
                    linearLayout.addView(distanceTextView);
                    linearLayout.addView(detailsButton);
                    cardView.addView(linearLayout);
                    gridLayout.addView(cardView);

                    // === Card Click Zoom Marker ===
                    cardView.setOnClickListener(v -> {
                        if (gmap != null) {
                            gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(marketLocation, 16));
                            if (marketMarker != null) marketMarker.showInfoWindow();
                        }
                    });

                    index++;
                } while (cursor.moveToNext());
            } else {
                Toast.makeText(Market_Location.this, "No markets found for " + municipality, Toast.LENGTH_SHORT).show();
                retryButton.setVisibility(View.VISIBLE);
            }

            if (cursor != null) cursor.close();
        }, 2500);
    }


    // Helper method to create consistent TextViews
    private TextView createTextView(String text, int textSizeSp, boolean isBold) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp);
        textView.setTypeface(null, isBold ? Typeface.BOLD : Typeface.NORMAL);
        textView.setMaxLines(1);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setPadding(0, 0, 0, dpToPx(8)); // Bottom padding
        return textView;
    }


    // Convert dp to px
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

}
