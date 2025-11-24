package com.example.winnerxo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText etPhone, etPassword;
    Button btnRegister, btnBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        FirebaseFirestore db = FirebaseFirestore.getInstance();


        btnRegister.setOnClickListener(view -> {
            String phone = etPhone.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }


            db.collection("users")
                    .whereEqualTo("phonenumber", phone)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                // כבר קיים משתמש כזה
                                Toast.makeText(this, "User with this phone already exists!", Toast.LENGTH_SHORT).show();
                            } else {

                                User user = new User(phone, password);

                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put("phonenumber", user.getPhonenumber());
                                userMap.put("password", user.getPassword());
                                userMap.put("isOwner", user.isOwner());

                                db.collection("users")
                                        .add(userMap)
                                        .addOnSuccessListener(documentReference -> {
                                            Toast.makeText(this, "User registered successfully!", Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                            startActivity(intent);
                                            finish();
                                        })
                                        .addOnFailureListener(exc -> {
                                            Toast.makeText(this, "Error registering user.", Toast.LENGTH_SHORT).show();
                                            Log.e("Firestore", "Error adding user", exc);
                                        });
                            }
                        } else {
                            Toast.makeText(this, "Error checking user.", Toast.LENGTH_SHORT).show();
                            Log.e("Firestore", "Error checking existing users", task.getException());
                        }
                    });
        });


        btnBackToLogin.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
