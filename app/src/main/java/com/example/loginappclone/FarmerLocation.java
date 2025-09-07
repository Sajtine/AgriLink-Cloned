package com.example.loginappclone;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FarmerLocation extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private TextView txtFarmerName, txtFarmerAddress;

    private LatLng farmerLatLng;
    private LatLng vendorLatLng;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private static final int LOCATION_PERMISSION_REQUEST = 101;

    private Polyline polyline; // current route line
    private List<LatLng> routePoints = new ArrayList<>(); // full route points
    private static final float OFF_ROUTE_THRESHOLD = 20f; // meters
    private final String orsApiKey = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjE4ODEzNWU0ODc0ZDQ0MmE5MzhmY2UwNWMxMDY3ZDc4IiwiaCI6Im11cm11cjY0In0=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_location);

        txtFarmerName = findViewById(R.id.txtFarmerName);
        txtFarmerAddress = findViewById(R.id.txtFarmerAddress);

        ImageButton button = findViewById(R.id.btnBack);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FarmerLocation.this, View_Offers.class);
                startActivity(intent);
                finish();
            }
        });

        // Get farmer details from intent
        Intent intent = getIntent();
        String farmerName = intent.getStringExtra("farmerName");
        String farmerAddress = intent.getStringExtra("farmerAddress");
        double farmerLat = intent.getDoubleExtra("latitude", 0);
        double farmerLng = intent.getDoubleExtra("longitude", 0);

        txtFarmerName.setText(farmerName);
        txtFarmerAddress.setText(farmerAddress);
        farmerLatLng = new LatLng(farmerLat, farmerLng);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapContainer);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "Map not found.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return;
        }

        map.setMyLocationEnabled(true); // ✅ Vendor blue dot

        // Add farmer marker (fixed)
        map.addMarker(new MarkerOptions()
                .position(farmerLatLng)
                .title(txtFarmerName.getText().toString()));

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(farmerLatLng, 14f));

        fetchRouteOnce(); // fetch initial route
        startLocationUpdates();
    }

    private void fetchRouteOnce() {
        // Use last known location as starting point
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng start = new LatLng(location.getLatitude(), location.getLongitude());
                getRoute(start, farmerLatLng);
            }
        });
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null || routePoints.isEmpty()) return;

                vendorLatLng = new LatLng(
                        locationResult.getLastLocation().getLatitude(),
                        locationResult.getLastLocation().getLongitude()
                );

                // Move camera smoothly
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(vendorLatLng, 15));

                // Update polyline / check off-route
                updatePolylineAsVendorMoves();
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void getRoute(LatLng origin, LatLng dest) {
        String url = "https://api.openrouteservice.org/v2/directions/driving-car?"
                + "api_key=" + orsApiKey
                + "&start=" + origin.longitude + "," + origin.latitude
                + "&end=" + dest.longitude + "," + dest.latitude;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray features = response.getJSONArray("features");
                        if (features.length() == 0) {
                            Toast.makeText(this, "No route found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        JSONObject geometry = features.getJSONObject(0).getJSONObject("geometry");
                        JSONArray coordinates = geometry.getJSONArray("coordinates");

                        routePoints.clear();
                        for (int i = 0; i < coordinates.length(); i++) {
                            JSONArray coord = coordinates.getJSONArray(i);
                            double lng = coord.getDouble(0);
                            double lat = coord.getDouble(1);
                            routePoints.add(new LatLng(lat, lng));
                        }

                        // Draw initial full route
                        if (polyline != null) polyline.remove();
                        polyline = map.addPolyline(new PolylineOptions()
                                .width(10)
                                .color(getResources().getColor(R.color.shade2_blue))
                                .addAll(routePoints));

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Request failed", Toast.LENGTH_SHORT).show();
                });

        queue.add(request);
    }

    private void updatePolylineAsVendorMoves() {
        if (routePoints.isEmpty() || polyline == null) return;

        // Find closest point index
        int closestIndex = 0;
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < routePoints.size(); i++) {
            LatLng point = routePoints.get(i);
            double dist = distanceBetweenMeters(vendorLatLng, point);
            if (dist < minDistance) {
                minDistance = dist;
                closestIndex = i;
            }
        }

        // Off-route check
        if (minDistance > OFF_ROUTE_THRESHOLD) {
            getRoute(vendorLatLng, farmerLatLng); // fetch new route
            return;
        }

        // Shorten polyline if vendor on route
        if (closestIndex > 0) {
            routePoints = routePoints.subList(closestIndex, routePoints.size());
            polyline.setPoints(routePoints);
        }
    }

    private double distanceBetweenMeters(LatLng a, LatLng b) {
        float[] results = new float[1];
        android.location.Location.distanceBetween(
                a.latitude, a.longitude,
                b.latitude, b.longitude,
                results
        );
        return results[0];
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.mapContainer);
                if (mapFragment != null) {
                    mapFragment.getMapAsync(this);
                }
            } else {
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}
