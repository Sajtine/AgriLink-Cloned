package com.example.loginapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText userEmail, userPassword;
    private Button login;
    private TextView next;

    MyDatabaseHelper databaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Transparent Status Bar
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);


        userEmail = findViewById(R.id.email);
        userPassword = findViewById(R.id.password);
        login = findViewById(R.id.submit);
        next = findViewById(R.id.register);

        // Login Button Listener
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validation();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,Register.class));
            }
        });


    }

    public void validation(){

        databaseHelper = new MyDatabaseHelper(this);

        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();

        if(databaseHelper.checkUser(email, password)){

            // Storing username in SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("email", email);
            editor.apply();

            Toast.makeText(MainActivity.this, "Login Successfully!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, Home.class));
            finish();
        }else if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Please enter all fields!", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Username or Password is Incorrect!", Toast.LENGTH_SHORT).show();
        }

    }



}