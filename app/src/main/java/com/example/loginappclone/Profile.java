package com.example.loginappclone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profile extends AppCompatActivity {

    private TextView log_out, username, farmersLocation;
    private DatabaseReference databaseRef;
    private String currentUserUID;

    private boolean isColorDark(int color){
        double darkness = 1 - (0.299 * Color.red(color) +
                0.587 * Color.green(color) +
                0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        int backgroundColor = ContextCompat.getColor(this, R.color.white);

        if(isColorDark(backgroundColor)){
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }else{
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }

        // Navigation
        ImageView home = findViewById(R.id.home);
        home.setOnClickListener(v -> startActivity(new Intent(Profile.this, Home.class)));

        ImageView location = findViewById(R.id.location);
        location.setOnClickListener(v -> startActivity(new Intent(Profile.this, Market_Location.class)));

        ImageView user_profile = findViewById(R.id.user_profile);
        user_profile.setOnClickListener(v -> { /* already on profile */ });

        username = findViewById(R.id.username);
        farmersLocation = findViewById(R.id.farmersLocation);


        // Get current user UID
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);

        currentUserUID = sharedPreferences.getString("uid", null);
        databaseRef = FirebaseDatabase.getInstance().getReference("users").child("farmers");

        if(currentUserUID != null){
            getUserDetails(currentUserUID);
        }

        // logout
        log_out = findViewById(R.id.logout);
        log_out.setOnClickListener(v -> logout());

        TextView edit_profile = findViewById(R.id.editProfile);
        edit_profile.setOnClickListener(v -> startActivity(new Intent(Profile.this, Farmers_Details.class)));

        // Requests button
        MaterialButton checkRequestsButton = findViewById(R.id.checkRequestsButton);
        checkRequestsButton.setOnClickListener(v -> {
            Intent intent = new Intent(Profile.this, Requests.class);
            startActivity(intent);
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        ImageView profile = findViewById(R.id.user_profile);
        profile.setImageResource(R.drawable.profile_active);
    }

    // Fetch user info from Firebase
    private void getUserDetails(String uid){
        databaseRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String name = snapshot.child("username").getValue(String.class);
                    String location = snapshot.child("address").getValue(String.class);

                    username.setText(name != null ? name : "N/A");
                    farmersLocation.setText(location != null ? location : "No location set");
                } else {
                    Toast.makeText(Profile.this, "User data not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(Profile.this, "Failed to load user details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Logout method
    public void logout(){
        FirebaseAuth.getInstance().signOut(); // logout Firebase session too

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(Profile.this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
        finishAffinity();
        startActivity(new Intent(Profile.this, MainActivity.class));
    }
}
