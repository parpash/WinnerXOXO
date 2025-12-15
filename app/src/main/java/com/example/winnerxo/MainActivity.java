package com.example.winnerxo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText etPlayer1, etPlayer2;
    Button btnStartGame, btnPropose, btnManageDatabase, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etPlayer1 = findViewById(R.id.etPlayer1);
        etPlayer2 = findViewById(R.id.etPlayer2);
        btnStartGame = findViewById(R.id.btnStartGame);
        btnPropose = findViewById(R.id.btnPropose);
        btnLogout = findViewById(R.id.btnLogout);
        btnManageDatabase = findViewById(R.id.btnManageDatabase);

        if (User.currentUser != null && User.currentUser.isOwner()) {
            btnManageDatabase.setVisibility(Button.VISIBLE);
        }

        btnStartGame.setOnClickListener(v -> {
            String player1 = etPlayer1.getText().toString().trim();
            String player2 = etPlayer2.getText().toString().trim();

            if (player1.isEmpty() || player2.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter both player names", Toast.LENGTH_SHORT).show();
                return;
            }


            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            intent.putExtra("PLAYER1_NAME", player1);
            intent.putExtra("PLAYER2_NAME", player2);
            startActivity(intent);
        });


        btnPropose.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this,
                    "Would go to: ProposePlayersActivity",
                    Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> {
            User.currentUser = null;
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        btnManageDatabase.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyScreenActivity.class);
            startActivity(intent);
        });
    }
}
