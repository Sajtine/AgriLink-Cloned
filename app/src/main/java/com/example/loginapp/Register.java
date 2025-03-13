package com.example.loginapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class Register extends AppCompatActivity{

    private EditText reg_username, reg_email, reg_password, check_password;
    MyDatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);


        reg_username = findViewById(R.id.username);
        reg_email = findViewById(R.id.email);
        reg_password = findViewById(R.id.password);
        check_password = findViewById(R.id.confirmPassword);


        Button register = findViewById(R.id.submit);
        TextView userLogin = findViewById(R.id.user_login);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserInfo();
            }
        });


        userLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this,MainActivity.class));
            }
        });
    }


    // Register User
    public void getUserInfo(){

        databaseHelper = new MyDatabaseHelper(this);

        String username = reg_username.getText().toString();
        String password = reg_password.getText().toString();
        String email = reg_email.getText().toString();
        String checkPass = check_password.getText().toString();

        if(username.isEmpty() || password.isEmpty() || email.isEmpty() || checkPass.isEmpty()){
            Toast.makeText(Register.this, "Please fill up the needed data!", Toast.LENGTH_SHORT).show();
        }else if(!password.equals(checkPass)) {
            Toast.makeText(Register.this, "Password doesn't match. Please re-enter password.", Toast.LENGTH_SHORT).show();
            check_password.setText("");
        }else{
            boolean isRegistered = databaseHelper.registerUser(username, email, password);
            if(isRegistered){
                Toast.makeText(Register.this, "Register Successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Register.this, MainActivity.class));
                finish();
            }else{
                Toast.makeText(Register.this, "User already exist!", Toast.LENGTH_SHORT).show();
            }
        }


    }
}