package com.example.loginappclone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Register extends AppCompatActivity{

    private EditText reg_username, reg_email, reg_password, check_password;
    MyDatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        databaseHelper = new MyDatabaseHelper(this);

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

        String username = reg_username.getText().toString();
        String password = reg_password.getText().toString();
        String email = reg_email.getText().toString();
        String checkPass = check_password.getText().toString();

        Spinner userType= findViewById(R.id.userTypeSpinner); // move here!
        String selectedRole = userType.getSelectedItem().toString();

        if(username.isEmpty() || password.isEmpty() || email.isEmpty() || checkPass.isEmpty() || selectedRole.equals("Select user type")){
            Toast.makeText(Register.this, "Please fill up all fields including user type!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!password.equals(checkPass)) {
            Toast.makeText(Register.this, "Password doesn't match. Please re-enter password.", Toast.LENGTH_SHORT).show();
            check_password.setText("");
            return;
        }


        // Check email in Firebase before registration
        databaseHelper.registerUser(username, email, password, selectedRole, Register.this);


    }

}