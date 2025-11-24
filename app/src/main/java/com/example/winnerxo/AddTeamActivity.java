package com.example.winnerxo;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class AddTeamActivity extends AppCompatActivity {

    EditText etTeamId, etUrl;
    Button btnAddTeam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_team);

        etTeamId = findViewById(R.id.etTeamId);
        etUrl = findViewById(R.id.etUrl);
        btnAddTeam = findViewById(R.id.btnAddTeam);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        btnAddTeam.setOnClickListener(v -> {
            String teamId = etTeamId.getText().toString().trim();
            String url = etUrl.getText().toString().trim();

            if (teamId.isEmpty() || url.isEmpty()) {
                Toast.makeText(AddTeamActivity.this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Team team = new Team(teamId, url);

            db.collection("teams") // <- כאן שינינו ל-"teams"
                    .add(team)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(AddTeamActivity.this, "Team added successfully", Toast.LENGTH_SHORT).show();
                        etTeamId.setText("");
                        etUrl.setText("");
                    })
                    .addOnFailureListener(exc -> {
                        Toast.makeText(AddTeamActivity.this, "Failed to add team", Toast.LENGTH_LONG).show();
                        Log.e("AddTeamActivity", "Error adding team", exc);
                    });
        });
    }
}
