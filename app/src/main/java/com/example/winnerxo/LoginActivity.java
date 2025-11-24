package com.example.winnerxo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    EditText etPhone, etPassword;
    Button btnLogin, btnRegister;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin.setOnClickListener(view -> {
            String phone = etPhone.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users")
                    .whereEqualTo("phonenumber", phone)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                Toast.makeText(this, "User not found. Please check phone number or register.", Toast.LENGTH_SHORT).show();
                            } else {
                                String dbPassword = task.getResult().getDocuments().get(0).getString("password");
                                Boolean isOwner = task.getResult().getDocuments().get(0).getBoolean("isOwner");

                                if (dbPassword != null && dbPassword.equals(password)) {
                                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                                    User loggedInUser = new User(phone, password);
                                    User.currentUser = loggedInUser;
                                    if (isOwner != null && isOwner) {
                                        Toast.makeText(this, "You are the owner!", Toast.LENGTH_SHORT).show();
                                    }
                                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                                    startActivity(intent);

                                } else {
                                    Toast.makeText(this, "Incorrect password.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Toast.makeText(this, "Error checking user.", Toast.LENGTH_SHORT).show();
                            Log.e("Firestore", "Error checking users", task.getException());
                        }
                    });
        });


        btnRegister.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
