package com.example.winnerxo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyScreenActivity extends AppCompatActivity {

    Button btnBackHome, btnAddPlayer, btnAddTeam, btnCheckPlayer;
    EditText etCheckPlayer;

    FirebaseFirestore db;
    CollectionReference playersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_screen);

        btnBackHome = findViewById(R.id.btnBackHome);
        btnAddPlayer = findViewById(R.id.btnAddPlayer);
        btnAddTeam = findViewById(R.id.btnAddTeam);
        btnCheckPlayer = findViewById(R.id.btnCheckPlayer);
        etCheckPlayer = findViewById(R.id.etCheckPlayer);

        db = FirebaseFirestore.getInstance();
        playersRef = db.collection("players");

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(MyScreenActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        btnAddPlayer.setOnClickListener(v -> {
            Intent intent = new Intent(MyScreenActivity.this, AddPlayerActivity.class);
            startActivity(intent);
        });

        btnAddTeam.setOnClickListener(v -> {
            Intent intent = new Intent(MyScreenActivity.this, AddTeamActivity.class);
            startActivity(intent);
        });

        btnCheckPlayer.setOnClickListener(v -> {
            String playerName = etCheckPlayer.getText().toString().trim();
            if (playerName.isEmpty()) {
                Toast.makeText(MyScreenActivity.this, "Enter a player name", Toast.LENGTH_SHORT).show();
                return;
            }

            playersRef.get()
                    .addOnSuccessListener(snapshots -> {
                        boolean found = false;
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            String name = doc.getString("name");
                            if (playerName.equalsIgnoreCase(name)) {
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            Toast.makeText(MyScreenActivity.this, "Player exists in the system", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MyScreenActivity.this, "Player not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(exc -> {
                        Toast.makeText(MyScreenActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                        Log.e("MyScreenActivity", "Error checking player", exc);
                    });
        });
    }
}
