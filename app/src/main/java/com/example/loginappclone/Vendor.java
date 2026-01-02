package com.example.loginappclone;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.pusher.pushnotifications.PushNotificationReceivedListener;
import com.pusher.pushnotifications.PushNotifications;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Vendor extends AppCompatActivity {

    TextView welcomeText;
    SharedPreferences sharedPreferences;
    String currentUserUID;
    private TextView badgeCount;
    private ImageView messages;
    private static final int NOTIFICATION_PERMISSION_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vendor);

        welcomeText = findViewById(R.id.welcomeText);
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);

        // Notification Permission
        askNotificationPermission();

        // Badge notif id
        badgeCount = findViewById(R.id.badgeCount);

        // Get vendor name
        String username = sharedPreferences.getString("username", "Vendor");

        // Menu
        ImageView profile = findViewById(R.id.profile);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);

        String vendorUID = sharedPreferences.getString("uid", "");
//        if (!vendorUID.isEmpty()) {
//            try {
//                // Subscribe device to the vendor interest
//                PushNotifications.addDeviceInterest("vendor_" + vendorUID);
//                Log.d("PushNotifications", "Successfully added device interest: vendor_" + vendorUID);
//
//                // Log current device interests
//                Set<String> interestSet = PushNotifications.getDeviceInterests();
//                Log.d("PushNotifications", "Current device interests: " + interestSet);
//
//                // --- Send test notification ---
//                sendTestNotification(
//                        "vendor_" + vendorUID,
//                        "Test Notification",
//                        "Hello! This notification appears in foreground."
//                );
//
//            } catch (Exception e) {
//                Log.e("PushNotifications", "Failed to add device interest", e);
//            }
//        }


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

                    // Remove all device interests
                    PushNotifications.clearDeviceInterests();

                    // Optionally, delete the FCM token
                    FirebaseMessaging.getInstance().deleteToken()
                            .addOnCompleteListener(task -> {
                                Log.d("Logout", "FCM token deleted, no more notifications will be received");
                            });

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

//        FirebaseMessaging.getInstance().getToken()
//                .addOnSuccessListener(token -> {
//                    String vendorUID = sharedPreferences.getString("uid", "");
//                    if (!vendorUID.isEmpty()) {
//                        FirebaseDatabase.getInstance()
//                                .getReference("users")
//                                .child("vendors")
//                                .child(vendorUID)
//                                .child("fcmToken")
//                                .setValue(token);
//                    }
//                });

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

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                        this,
                        "Notifications enabled",
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                Toast.makeText(
                        this,
                        "Notifications disabled. You may miss offer alerts.",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }


    // Notification Badge for unread messages
    private void checkUnreadMessages() {
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUserUID = sharedPreferences.getString("uid", "");

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

        // Check foreground notification
        PushNotifications.setOnMessageReceivedListenerForVisibleActivity(
                this,
                new PushNotificationReceivedListener() {
                    @Override
                    public void onMessageReceived(RemoteMessage remoteMessage) {

                        Log.d("VENDOR_FOREGROUND", "🔥 Vendor received foreground push");

                        if (remoteMessage.getData() != null) {
                            Log.d("VENDOR_FOREGROUND", "DATA: " + remoteMessage.getData());

                            String title = remoteMessage.getData().get("title");
                            String body = remoteMessage.getData().get("body");

//                            runOnUiThread(() -> {
//                                Toast.makeText(
//                                        Vendor.this,
//                                        "FG PUSH: " + title + " - " + body,
//                                        Toast.LENGTH_LONG
//                                ).show();
//                            });
                        } else {
                            Log.d("VENDOR_FOREGROUND", "❌ No data payload");
                        }
                    }
                }
        );
    }

    // Permission Notification
    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE
                );
            }
        }
    }


    // Test
//    private void sendTestNotification(String interest, String title, String body) {
//        String INSTANCE_ID = "82e5c130-03f5-40a0-9494-22f7bc17bc27";
//        String SECRET_KEY = "ACC2F781E0170E544CB222730E491FEFA38C499FD863DD756EF58699578E27CF";
//
//        String url = "https://" + INSTANCE_ID + ".pushnotifications.pusher.com/publish_api/v1/instances/"
//                + INSTANCE_ID + "/publishes/interests";
//
//        JSONObject jsonBody = new JSONObject();
//        try {
//            JSONArray interestsArray = new JSONArray();
//            interestsArray.put(interest);
//            jsonBody.put("interests", interestsArray);
//
//            JSONObject fcm = new JSONObject();
//
//            JSONObject data = new JSONObject();
//            data.put("title", title);
//            data.put("body", body);
//            data.put("type", "test_foreground");
//
//            fcm.put("data", data);
//
//            jsonBody.put("fcm", fcm);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//            return;
//        }
//
//        JsonObjectRequest request = new JsonObjectRequest(
//                Request.Method.POST,
//                url,
//                jsonBody,
//                response -> {
//                    Log.d("PushTest", "Notification sent successfully: " + response.toString());
//                },
//                error -> {
//                    Log.e("PushTest", "Error sending notification", error);
//                }
//        ) {
//            @Override
//            public java.util.Map<String, String> getHeaders() {
//                java.util.Map<String, String> headers = new java.util.HashMap<>();
//                headers.put("Content-Type", "application/json");
//                headers.put("Authorization", "Bearer " + SECRET_KEY);
//                return headers;
//            }
//        };
//
//        RequestQueue queue = Volley.newRequestQueue(this);
//        queue.add(request);
//    }

}
