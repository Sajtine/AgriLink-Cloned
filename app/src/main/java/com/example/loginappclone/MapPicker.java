package com.example.loginappclone;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapPicker extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap gmap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private ImageButton backButton;
    private Button confirmLocation;
    private TextView selectedCoordinates;
    private LatLng selectedLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker); // Use correct layout file here

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        backButton = findViewById(R.id.btnBack);
        confirmLocation = findViewById(R.id.btnConfirmLocation);
        selectedCoordinates = findViewById(R.id.selectedCoordinates);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        backButton.setOnClickListener(v -> {
            finish();
        });

        confirmLocation.setOnClickListener(v -> {
            if (selectedLoc != null) {
                Intent intent = new Intent();
                intent.putExtra("latitude", selectedLoc.latitude);
                intent.putExtra("longitude", selectedLoc.longitude);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gmap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            showUserLocation();
        }

        gmap.setOnCameraIdleListener(() -> {
            selectedLoc = gmap.getCameraPosition().target;

            double lat = selectedLoc.latitude;
            double lng = selectedLoc.longitude;

            Geocoder geocoder = new Geocoder(MapPicker.this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String streetName = address.getThoroughfare();
                    String barangay = address.getSubLocality();
                    String city = address.getLocality();

                    if (streetName == null) streetName = "Unnamed Street";
                    if (barangay == null) barangay = "Unknown Barangay";
                    if (city == null) city = "Unknown City";

                    String fullAddress = streetName + ", " + barangay + ", " + city;
                    selectedCoordinates.setText(fullAddress);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void showUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        gmap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 18f));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showUserLocation();
        }
    }
}
