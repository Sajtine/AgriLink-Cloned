package com.example.loginappclone;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChangePassword extends AppCompatActivity {

    private EditText newPassword, confirmPassword;
    private TextView backToLogin;
    private Button updateButton;
    private DatabaseReference rootRef;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);

        rootRef = FirebaseDatabase.getInstance().getReference("users");

        phone = getIntent().getStringExtra("phone");


        if (phone != null) {
            // If the number starts with "0", replace it with "+63"
            Log.d("OTP_DEBUG", "Phone Number: " + phone);

            if (phone.startsWith("0")) {
                phone = "+63" + phone.substring(1);
            }

            else if (!phone.startsWith("+63")) {
                phone = "+63" + phone;
            }
        }

        newPassword = findViewById(R.id.new_password);
        confirmPassword = findViewById(R.id.confirm_password);
        backToLogin = findViewById(R.id.back_to_login);
        updateButton = findViewById(R.id.update_button);

        // Go back to login
        backToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ChangePassword.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Handle Update Password
        updateButton.setOnClickListener(v -> {
            String newPass = newPassword.getText().toString().trim();
            String confirmPass = confirmPassword.getText().toString().trim();

            if (newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Please fill in both fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                return;
            }

            updatePasswordInNode("farmers", newPass);
        });

        // Password Toggle
        setupPasswordToggle(newPassword);
        setupPasswordToggle(confirmPassword);
    }

    private void updatePasswordInNode(String node, String newPass) {
        rootRef.child(node).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean found = false;

                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String dbPhone = userSnap.child("phone_number").getValue(String.class);

                    if (dbPhone != null && dbPhone.equals(phone)) {
                        userSnap.getRef().child("password").setValue(newPass);
                        found = true;
                        Toast.makeText(ChangePassword.this, "Password updated successfully!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(ChangePassword.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                }

                if (!found) {
                    // Try vendors if not found in farmers
                    if (node.equals("farmers")) {
                        updatePasswordInNode("vendors", newPass);
                    } else {
                        Toast.makeText(ChangePassword.this, "Phone number not found!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ChangePassword.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupPasswordToggle(EditText editText) {
        final boolean[] isPasswordVisible = {false};

        editText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (editText.getRight()
                        - editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width()
                        - editText.getPaddingRight())) {

                    v.performClick(); // accessibility support
                    togglePasswordVisibility(editText, isPasswordVisible);
                    return true;
                }
            }
            return false;
        });
    }

    private void togglePasswordVisibility(EditText editText, boolean[] isPasswordVisible) {
        Drawable[] drawables = editText.getCompoundDrawables();
        Drawable currentLeft = drawables[0];

        if (!isPasswordVisible[0]) {
            // Show password
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            editText.setCompoundDrawablesWithIntrinsicBounds(
                    currentLeft,
                    null,
                    ContextCompat.getDrawable(this, R.drawable.eye),
                    null);
        } else {
            // Hide password
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            editText.setCompoundDrawablesWithIntrinsicBounds(
                    currentLeft,
                    null,
                    ContextCompat.getDrawable(this, R.drawable.closed_eye),
                    null);
        }

        isPasswordVisible[0] = !isPasswordVisible[0];
        editText.setSelection(editText.getText().length());
    }

}
