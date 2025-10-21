package com.example.loginappclone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Vendor extends AppCompatActivity {

    TextView welcomeText;
    SharedPreferences sharedPreferences;
    private TextView badgeCount;
    private ImageView messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vendor);

        welcomeText = findViewById(R.id.welcomeText);
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);

        // Badge notif id
        badgeCount = findViewById(R.id.badgeCount);

        // Get vendor name
        String username = sharedPreferences.getString("username", "Vendor");

        // Menu
        ImageView profile = findViewById(R.id.profile);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);

        profile.setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.END);
        });

        // Handle back button in drawer menu
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(drawerLayout.isDrawerOpen(GravityCompat.END)){
                    drawerLayout.closeDrawer(GravityCompat.END);
                }else{
                    finish();
                }
            }
        });

        // Menu destination
        NavigationView navigationView = findViewById(R.id.navigation_view);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            // Close the drawer first
            drawerLayout.closeDrawer(GravityCompat.END);

            // Delay navigation slightly so the drawer closes smoothly first
            new android.os.Handler().postDelayed(() -> {
                if (id == R.id.nav_displayedProduct) {
                    startActivity(new Intent(Vendor.this, VendorProducts.class));
                } else if (id == R.id.nav_history) {
                    startActivity(new Intent(Vendor.this, Product_History.class));
                } else if (id == R.id.nav_logout) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();

                    Intent intent = new Intent(Vendor.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 250); // delay in milliseconds (adjust if needed)

            return true;
        });

        // to message destination
        messages = findViewById(R.id.messages);
        messages.setOnClickListener(v -> {
            Intent intent = new Intent(Vendor.this, ChatList.class);
            startActivity(intent);
        });


        // Nav username
        TextView navUsername = navigationView.getHeaderView(0).findViewById(R.id.nav_username);
        navUsername.setText(username);

        welcomeText.setText("Welcome, " + username + "!");

        //CardViews
        CardView cardViewRequest = findViewById(R.id.cardViewRequest);
        CardView cardViewUpdateInfo = findViewById(R.id.cardViewUpdateInfo);
        CardView cardViewAcceptedOffers = findViewById(R.id.cardViewAcceptedOffers);
        CardView cardViewUpdateProduct = findViewById(R.id.cardViewUpdateProduct);


        // View Request
        cardViewRequest.setOnClickListener(v -> {
            Intent intent = new Intent(Vendor.this, View_Offers.class);
            startActivity(intent);
        });

        // Update Info
        cardViewUpdateInfo.setOnClickListener(v -> {
            Intent intent = new Intent(Vendor.this, Vendor_Info.class);
            startActivity(intent);
        });

        // Accepted Offers
        cardViewAcceptedOffers.setOnClickListener(v -> {
            Intent intent = new Intent(Vendor.this, Accepted_Products.class);
            startActivity(intent);
        });

        // Update Product
        cardViewUpdateProduct.setOnClickListener(v ->{
            Intent intent = new Intent(Vendor.this, AddProducts.class);
            startActivity(intent);
        });

        // Call the method for badge notif
        checkUnreadMessages();


    }

    // Notification Badge for unread messages
    private void checkUnreadMessages() {
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String currentUserUID = prefs.getString("uid", "");

        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("chats");

        chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int unreadCount = 0;

                for(DataSnapshot roomSnap : snapshot.getChildren()){
                    for (DataSnapshot msgSnap : roomSnap.getChildren()){
                        Message msg = msgSnap.getValue(Message.class);
                        if (msg != null && msg.receiverId.equals(currentUserUID) && !msg.isRead) {
                            unreadCount++;
                        }
                    }
                }

                if (unreadCount > 0){
                    showBadge(unreadCount);
                }else{
                    hideBadge();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Method to show bagde notif
    private void showBadge(int count){
        badgeCount.setText(String.valueOf(count));
        badgeCount.setVisibility(View.VISIBLE);
    }

    private void hideBadge(){
        badgeCount.setVisibility(View.GONE);
    }

    // Check the unread messages when returned to the page
    @Override
    protected void onResume(){
        super.onResume();
        checkUnreadMessages();
    }
}
