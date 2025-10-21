package com.example.loginappclone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText forgotNumber;
    private Button resetButton;
    private FirebaseAuth mAuth;
    private TextView login;

    private String verificationId; // store OTP session id

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        SharedPreferences sharedPreferences = getSharedPreferences("UserRefs", MODE_PRIVATE);

        forgotNumber = findViewById(R.id.forgot_number);
        resetButton = findViewById(R.id.reset_button);
        mAuth = FirebaseAuth.getInstance();

        resetButton.setOnClickListener(v -> {
            String phone = forgotNumber.getText().toString().trim();

            if (TextUtils.isEmpty(phone)) {
                Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            if (phone.startsWith("0")) {
                phone = "+63" + phone.substring(1); // 09123456789 -> +639123456789
            }

            sharedPreferences.edit().putString("phoneNumber", phone).apply();

            sendOTP(phone);
        });

        // Back to Login
        login = findViewById(R.id.back_to_login);
        login.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

    }

    private void sendOTP(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                                // Auto verification OR instant validation
                                Toast.makeText(ForgotPasswordActivity.this, "Verification complete!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(ForgotPasswordActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                Log.e("OTP_DEBUG", "Verification failed", e);
                            }

                            @Override
                            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                super.onCodeSent(s, token);
                                verificationId = s; // Save for later use
                                Toast.makeText(ForgotPasswordActivity.this, "OTP sent to " + phoneNumber, Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(ForgotPasswordActivity.this, OtpPage.class);
                                intent.putExtra("verificationId", verificationId);
                                intent.putExtra("phoneNumber", phoneNumber);
                                startActivity(intent);
                            }
                        })
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}
