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
import android.util.Log;
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
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Market_Location extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap gmap;
    private TextView market;
    private TextView city;
    private TextView vendor;
    private double userLat;
    private double userLong;
    private final List<Marker> markerList = new ArrayList<>();
    private ProgressBar progressBar;

    private static final int LOCATION_PERMISSION_CODE = 101;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private MyDatabaseHelper myDatabaseHelper;

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
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }

        //Initialize needed widgets and data
        progressBar = findViewById(R.id.progressBar);
        myDatabaseHelper = new MyDatabaseHelper(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        ImageView home = findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Market_Location.this, Home.class);
                startActivity(intent);
            }
        });

        ImageView user = findViewById(R.id.user_profile);
        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Market_Location.this, Profile.class));
            }
        });

        // Add fragment
        addFragment();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Ask for permission as soon as the user lands here
//        checkLocationPermission();
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

    // check location permission
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

    // get current location
    private void getLocation() {
        // Check if location permissions are granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Check network connection
        boolean isConnected = isNetworkConnected();

        if (isConnected) {
            // Online: Get location using the FusedLocationProviderClient
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                updateLocation(location);
                            } else {
                                // If no last known location, try to fetch the current location
                                fetchLocationWithGPS();
                            }
                        }
                    });
        } else {
            // Offline: Try to get the location using GPS
            fetchLocationWithGPS();
        }
    }

    // Helper method to check if the device has network connectivity
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    // Method to fetch location using GPS (offline)
    private void fetchLocationWithGPS() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                
                Toast.makeText(Market_Location.this, "Lat: " + lat + "\nLng: " + lng, Toast.LENGTH_LONG).show();

                updateLocation(location); // Your method to handle the location
            }

            @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override public void onProviderEnabled(String provider) {}
            @Override public void onProviderDisabled(String provider) {}    
        }, null);
    }


    // Method to update the location on the map and process geocoding
    private void updateLocation(Location location) {
        userLat = location.getLatitude();
        userLong = location.getLongitude();
        LatLng userLocation = new LatLng(userLat, userLong);
        gmap.clear();
        gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16));

        // Show full human-readable address
        Geocoder geocoder = new Geocoder(Market_Location.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(userLat, userLong, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String municipality = address.getLocality();

                // Proceed with Market Location
                getMarket(municipality);

            } else {
                Toast.makeText(Market_Location.this, "Address not found!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(Market_Location.this, "Geocoder error!", Toast.LENGTH_SHORT).show();
        }
    }




    // set location on the map
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gmap = googleMap;

        // Optional: Show blue location dot if permission granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            gmap.setMyLocationEnabled(true);
        }

    }

    // request permission to the user
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

    // retrieve the markets base on the user location
    public void getMarket(String municipality) {
        if (municipality == null || municipality.isEmpty()) {
            Toast.makeText(this, "Municipality not found!", Toast.LENGTH_SHORT).show();
            return; // Exit if municipality is null or empty
        }

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);

        // Remove all existing markers from the map
        if (gmap != null) {
            for (Marker marker : markerList) {
                marker.remove(); // Remove each marker from the map
            }
            markerList.clear(); // Clear the list of markers
        }

        // Use Handler to simulate a delay or to post the task on the main thread
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Get data from database using cursor
                Cursor cursor = myDatabaseHelper.getMarketDetails(municipality);

                // Hide the progress bar on the main UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });

                // Reference to the GridLayout
                GridLayout gridLayout = findViewById(R.id.gridLayout);
                gridLayout.removeAllViews();

                // Set the number of columns for GridLayout
                gridLayout.setColumnCount(2);  // 2 columns in GridLayout

                if (cursor != null && cursor.moveToFirst()) {
                    // Loop through the cursor to fetch rows
                    do {
                        // Get data from cursor for each column
                        String marketName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                        String vendorName = cursor.getString(cursor.getColumnIndexOrThrow("vendor"));
                        String barangay = cursor.getString(cursor.getColumnIndexOrThrow("barangay"));
                        String phone_number = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
                        String vendor_id = cursor.getString(cursor.getColumnIndexOrThrow("vendor_id"));

                        // Get coordinates
                        double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude"));
                        double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"));

                        final double finalLat = latitude;
                        final double finalLong = longitude;

                        if (gmap != null) {
                            LatLng marketLocation = new LatLng(finalLat, finalLong);
                            Marker marketMarker = gmap.addMarker(new MarkerOptions()
                                    .position(marketLocation)
                                    .title(marketName)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.market_logo)));

                            // Add marker to the list
                            markerList.add(marketMarker);
                        }

                        CardView cardView = new CardView(Market_Location.this);
                        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                        params.width = 0;
                        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
                        params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED);
                        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);

                        // Margin around card
                        int margin = (int) getResources().getDimension(R.dimen.card_margin);
                        params.setMargins(margin, margin, margin, margin);
                        cardView.setLayoutParams(params);

                        // Card styling
                        cardView.setCardBackgroundColor(ContextCompat.getColor(Market_Location.this, R.color.shade1_green));
                        cardView.setRadius(24f); // Softer rounded corners
                        cardView.setCardElevation(12f); // More shadow for depth
                        cardView.setUseCompatPadding(true); // Ensure shadow padding

                        // Inner layout
                        LinearLayout linearLayout = new LinearLayout(Market_Location.this);
                        linearLayout.setOrientation(LinearLayout.VERTICAL);
                        linearLayout.setPadding(36, 36, 36, 36);
                        linearLayout.setGravity(Gravity.START); // Align content to left

                        // Vendor Name (Highlight)
                        TextView vendorTextView = new TextView(Market_Location.this);
                        vendorTextView.setText("👤 " + vendorName);  // Optional: Emoji for simple icon
                        vendorTextView.setTextColor(ContextCompat.getColor(Market_Location.this, R.color.white));
                        vendorTextView.setTextSize(20);
                        vendorTextView.setTypeface(null, Typeface.BOLD);
                        vendorTextView.setPadding(0, 0, 0, 12);

                        // Market Name
                        TextView marketTextView = new TextView(Market_Location.this);
                        marketTextView.setText("Market: " + marketName);
                        marketTextView.setTextColor(ContextCompat.getColor(Market_Location.this, R.color.white));
                        marketTextView.setTextSize(15);
                        marketTextView.setPadding(0, 0, 0, 8);

                        // Barangay
                        TextView barangayTextView = new TextView(Market_Location.this);
                        barangayTextView.setText("Brgy: " + barangay);
                        barangayTextView.setTextColor(ContextCompat.getColor(Market_Location.this, R.color.white));
                        barangayTextView.setTextSize(15);
                        barangayTextView.setPadding(0, 0, 0, 8);

                        // Distance
                        Location userLocation = new Location("");
                        userLocation.setLatitude(userLat);
                        userLocation.setLongitude(userLong);

                        Location marketLocation = new Location("");
                        marketLocation.setLatitude(finalLat);
                        marketLocation.setLongitude(finalLong);

                        float distance = userLocation.distanceTo(marketLocation);
                        float distanceInKm = distance / 1000;

                        TextView distanceTextView = new TextView(Market_Location.this);
                        distanceTextView.setText("Distance: " + String.format("%.2f", distanceInKm) + " km");
                        distanceTextView.setTextColor(ContextCompat.getColor(Market_Location.this, R.color.white));
                        distanceTextView.setTextSize(15);
                        distanceTextView.setPadding(0, 0, 0, 16);

                        // Button (keep original styling)
                        Button detailsButton = new Button(Market_Location.this);
                        detailsButton.setTextSize(12);
                        detailsButton.setTextColor(Color.WHITE);
                        detailsButton.setText("Details");

                        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        btnParams.topMargin = 16;
                        detailsButton.setLayoutParams(btnParams);

                        // Intent
                        detailsButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Market_Location.this, Market_Details.class);
                                intent.putExtra("marketName", marketName);
                                intent.putExtra("vendorName", vendorName);
                                intent.putExtra("vendor_id", vendor_id);
                                intent.putExtra("barangay", barangay);
                                intent.putExtra("phone", phone_number);
                                intent.putExtra("latitude", finalLat);
                                intent.putExtra("longitude", finalLong);
                                startActivity(intent);
                            }
                        });

                        // Add views
                        linearLayout.addView(vendorTextView);
                        linearLayout.addView(marketTextView);
                        linearLayout.addView(barangayTextView);
                        linearLayout.addView(distanceTextView);
                        linearLayout.addView(detailsButton);

                        cardView.addView(linearLayout);
                        gridLayout.addView(cardView);




                        // ✅ Set click listener to focus map on this market and add marker
                        cardView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (gmap != null) {
                                    LatLng marketLocation = new LatLng(finalLat, finalLong);
                                    Marker newMarker = gmap.addMarker(new MarkerOptions()
                                            .position(marketLocation)
                                            .title(marketName)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.market_logo)));
                                    gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(marketLocation, 16));

                                    if (newMarker != null) {
                                        newMarker.showInfoWindow();
                                    }
                                }
                            }
                        });

                    } while (cursor.moveToNext());
                } else {
                    // Use runOnUiThread to show Toast if no markets found
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Market_Location.this, "No markets found for " + municipality, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                // Close cursor if necessary
                if (cursor != null) {
                    cursor.close();
                }
            }
        }, 2500); // Simulated delay of 2.5 seconds
    }
}
