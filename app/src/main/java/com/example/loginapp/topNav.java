package com.example.loginapp;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class topNav extends Fragment {

    public topNav() {
        // Required empty public constructor
    }

    private ImageView calendar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_top_nav, container, false);
        androidx.appcompat.widget.SearchView searchView = view.findViewById(R.id.searchView);

        searchView.setIconifiedByDefault(false);

        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        if (searchIcon != null) {
            searchIcon.setImageResource(R.drawable.search); // make sure this matches your drawable file name
        }


        calendar = view.findViewById(R.id.calendar);
        calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow(v);

            }
        });

        return view;
    }

    private void popupWindow(View anchorView){
        View popupView = LayoutInflater.from(requireContext()).inflate(R.layout.calendar_popup, null);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setElevation(10);
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, -450);
    }
}
