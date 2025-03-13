package com.example.loginapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;


public class Home extends AppCompatActivity {

    private ImageView loc;
    private ImageView user;

    // Set of Trials for Image Carousel
//    private ViewFlipper carousel;
//    private int[] images = {R.drawable.image1, R.drawable.image2, R.drawable.image3};

    private ViewPager2 viewPager;
    private LinearLayout dotLayout;
    private int[] images = {R.drawable.image1, R.drawable.image2, R.drawable.image3};
    private ImageAdapter imageAdapter;
    private Handler handler;
    private Runnable runnable;
    private int currentPage = 0;
    private ImageView dots[];

    private boolean isColorDark(int color) {
        // Calculate luminance based on RGB components
        double darkness = 1 - (0.299 * Color.red(color) +
                0.587 * Color.green(color) +
                0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5; // Return true if the color is dark
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        // To identify background color
        int backgroundColor = ContextCompat.getColor(this, R.color.white); // Replace with your actual color resource

        if (isColorDark(backgroundColor)) {
            // Light icons for dark background
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        } else {
            // Dark icons for light background
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // Image Carousel
//        carousel = findViewById(R.id.image_carousel);
//
//        for(int image : images){
//            ImageView imageView = new ImageView(this);
//            imageView.setImageResource(image);
//            carousel.addView(imageView);
//        }
//        carousel.setFlipInterval(2000);
//        carousel.setAutoStart(true);
//        carousel.startFlipping();



        // Image Carousel
        viewPager = findViewById(R.id.viewPager);
        dotLayout = findViewById(R.id.dotLayout);

        viewPager.setAdapter(new ImageAdapter(this, images));

        dots = new ImageView[images.length];
        for(int i = 0; i < images.length; i++){
            dots[i] = new ImageView(this);
            dots[i].setImageResource(R.drawable.dot_active);
            dots[i].setPadding(8, 0, 8, 0);
            dotLayout.addView(dots[i]);
        }

        updateDots(0);

        // Change dot indicators on swipe
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
                currentPage = position;
            }
        });

        // Start auto-slide
        startAutoSlide();

        // Direct to the Location Window
        loc = findViewById(R.id.location);
        loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Home.this, Market_Location.class));
            }
        });

        // User Profile
        user = findViewById(R.id.user_profile);

        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Home.this, Profile.class));
            }
        });

        ImageView cal = findViewById(R.id.calendar);

        cal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow(v);
            }
        });
    }

    // Method for popupWindow
    private void popupWindow(View anchorView){
        View popupView = LayoutInflater.from(this).inflate(R.layout.calendar_popup, null);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setElevation(10);
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, -450);
    }


    private void updateDots(int position) {
        for (int i = 0; i < dots.length; i++) {
            dots[i].setImageResource(i == position ? R.drawable.dot_active : R.drawable.dot_inactive);
        }
    }

    private void startAutoSlide() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (currentPage == images.length - 1) {
                    currentPage = 0; // Reset to first page
                } else {
                    currentPage++;
                }
                viewPager.setCurrentItem(currentPage, true);
                handler.postDelayed(this, 4000); // Change image every 3 seconds
            }
        };
        handler.postDelayed(runnable, 3000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable); // Stop auto-slide when activity is destroyed
    }
}