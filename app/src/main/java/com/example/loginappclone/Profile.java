package com.example.loginappclone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;


public class Profile extends AppCompatActivity {

    private TextView log_out, username, farmersLocation;
    MyDatabaseHelper databaseHelper;

    private boolean isColorDark(int color){
        double darkness = 1 - (0.299 * Color.red(color) +
                0.587 * Color.green(color) +
                0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5; // Return true if the color is dark
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

        // Navigation to the home
        ImageView home = findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Profile.this, Home.class));
            }
        });

        // Navigation to the market
        ImageView location = findViewById(R.id.location);
        location.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View  v) {
                startActivity(new Intent(Profile.this, Market_Location.class));
            }       
        });

        // Navigation to the profile
        ImageView user_profile = findViewById(R.id.user_profile);
        user_profile.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
            }
        });

        databaseHelper = new MyDatabaseHelper(this);

        // Retrieve shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("email", null);

        username = findViewById(R.id.username);
        farmersLocation = findViewById(R.id.farmersLocation);

        if(userEmail != null){
            getUserDetails(userEmail);
        }

        // logout
        log_out = findViewById(R.id.logout);
        log_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        TextView edit_profile = findViewById(R.id.editProfile);
        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Profile.this, Farmers_Details.class));
            }
        });

        // Button Request
        // In your Activity or Fragment:
        MaterialButton checkRequestsButton = findViewById(R.id.checkRequestsButton);

        checkRequestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement your logic here, for example:
                // Open a new activity or show a fragment where the farmer can view their requests
                Intent intent = new Intent(Profile.this, Requests.class);
                startActivity(intent);
            }
        });


        // Fragment
//        addFragment();
    }


    @Override
    public void onResume(){
        super.onResume();

        ImageView profile = findViewById(R.id.user_profile);
        profile.setImageResource(R.drawable.profile_active);
    }

    // Method to get user info
    public void getUserDetails(String email){
        Cursor cursor = databaseHelper.getUserDetails(email);

        if(cursor.moveToFirst()){
            String name = cursor.getString(cursor.getColumnIndexOrThrow("username"));
            String location = cursor.getString(cursor.getColumnIndexOrThrow("address"));

            username.setText(name);
            farmersLocation.setText(location);
        }

    }

    // Logout method
    public void logout(){
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // removes all session data like email and isLoggedIn
        editor.apply();

        Toast.makeText(Profile.this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
        finishAffinity();
        startActivity(new Intent(Profile.this, MainActivity.class));
    }

}