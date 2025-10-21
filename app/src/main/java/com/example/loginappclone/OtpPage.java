package com.example.loginappclone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class OtpPage extends AppCompatActivity {

    private EditText otpDigit1, otpDigit2, otpDigit3, otpDigit4, otpDigit5, otpDigit6;
    private Button verifyButton;
    private TextView resendOtp;

    private String verificationId, phone;
    private FirebaseAuth auth;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_page);


        auth = FirebaseAuth.getInstance();

        // SharedPreferences
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);


        verificationId = getIntent().getStringExtra("verificationId");
        phone = getIntent().getStringExtra("phoneNumber");

        Log.d("OTP_DEBUG", "Verification ID: " + verificationId);
        Log.d("OTP_DEBUG", "Phone Number: " + phone);


        otpDigit1 = findViewById(R.id.otp_digit_1);
        otpDigit2 = findViewById(R.id.otp_digit_2);
        otpDigit3 = findViewById(R.id.otp_digit_3);
        otpDigit4 = findViewById(R.id.otp_digit_4);
        otpDigit5 = findViewById(R.id.otp_digit_5);
        otpDigit6 = findViewById(R.id.otp_digit_6);
        verifyButton = findViewById(R.id.verify_button);
        resendOtp = findViewById(R.id.resend_otp);

        // Setup OTP EditText focus switching
        setupOtpInputs();

        verifyButton.setOnClickListener(v -> verifyOtp());

        resendOtp.setOnClickListener(v -> {
            Toast.makeText(this, "Please go back and request a new OTP.", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupOtpInputs() {
        moveToNext(otpDigit1, otpDigit2);
        moveToNext(otpDigit2, otpDigit3);
        moveToNext(otpDigit3, otpDigit4);
        moveToNext(otpDigit4, otpDigit5);
        moveToNext(otpDigit5, otpDigit6);
    }

    private void moveToNext(EditText current, EditText next) {
        current.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1 && next != null) {
                    next.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        current.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && current.getText().toString().isEmpty()) {
                if (current != otpDigit1) {

                    if (next != null) next.requestFocus();
                }
            }
            return false;
        });
    }

    private void verifyOtp() {
        // Combine 6 digits
        String code = otpDigit1.getText().toString().trim() +
                otpDigit2.getText().toString().trim() +
                otpDigit3.getText().toString().trim() +
                otpDigit4.getText().toString().trim() +
                otpDigit5.getText().toString().trim() +
                otpDigit6.getText().toString().trim();

        if (TextUtils.isEmpty(code) || code.length() < 6) {
            Toast.makeText(this, "Enter valid 6-digit OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        if (verificationId == null) {
            Toast.makeText(this, "Verification ID missing. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }


        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // OTP success — move to ChangePassword
                        Toast.makeText(this, "OTP Verified!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(OtpPage.this, ChangePassword.class);
                        intent.putExtra("phone", phone);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Invalid or expired OTP!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
