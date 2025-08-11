package com.example.loginappclone;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


public class MyProfile extends Fragment {

    private TextView user_name;
    private TextView user_email;
    private MyDatabaseHelper databaseHelper;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);

        TextView logout = view.findViewById(R.id.click);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Log out Successfully!", Toast.LENGTH_SHORT).show();

                logout();
            }
        });

        user_name = view.findViewById(R.id.user_name);
        user_email = view.findViewById(R.id.user_email);

        databaseHelper = new MyDatabaseHelper(getActivity());


        // Retrieved SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("email", null);

        if(userEmail != null){
            fetchUserData(userEmail);
        }

        return view;
    }

    public void fetchUserData(String email){
        Cursor cursor = databaseHelper.getUserDetails(email);

        if(cursor.moveToFirst()){
            String name = cursor.getString(cursor.getColumnIndexOrThrow("username"));
            String userEmail = cursor.getString(cursor.getColumnIndexOrThrow("email"));

            user_name.setText(name);
            user_email.setText(userEmail);
        }

        cursor.close();
    }

    // Logout User
    public void logout() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // removes all session data like email and isLoggedIn
        editor.apply();

        requireActivity().finishAffinity(); // closes all activities
        startActivity(new Intent(getActivity(), MainActivity.class)); // opens login screen
    }

}