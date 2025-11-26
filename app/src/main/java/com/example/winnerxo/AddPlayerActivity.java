package com.example.winnerxo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AddPlayerActivity extends AppCompatActivity {

    private EditText etPlayerName, etTeamId;
    private Button btnAddTeam, btnSavePlayer, btnBackMain;
    private LinearLayout llTeamsList;

    private List<Team> playerTeams = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_player);

        etPlayerName = findViewById(R.id.etPlayerName);
        etTeamId = findViewById(R.id.etTeamName); // משתמש להזנת teamid
        btnAddTeam = findViewById(R.id.btnAddTeam);
        btnSavePlayer = findViewById(R.id.btnSavePlayer);
        btnBackMain = findViewById(R.id.btnBackMain);
        llTeamsList = findViewById(R.id.llTeamsList);

        db = FirebaseFirestore.getInstance();

        btnAddTeam.setOnClickListener(v -> addTeamById());
        btnSavePlayer.setOnClickListener(v -> savePlayer());
        btnBackMain.setOnClickListener(v -> {
            Intent intent = new Intent(AddPlayerActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void addTeamById() {
        String teamIdInput = etTeamId.getText().toString().trim();

        if (TextUtils.isEmpty(teamIdInput)) {
            Toast.makeText(this, "Please enter a Team ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // חיפוש הקבוצה לפי teamid ב-Firebase
        db.collection("teams")
                .whereEqualTo("teamid", teamIdInput)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        Team team = doc.toObject(Team.class);

                        playerTeams.add(team);

                        // הצגת הקבוצה ברשימה עם מספר סדר
                        TextView tvTeam = new TextView(this);
                        tvTeam.setText(playerTeams.size() + ". " + team.getTeamid());
                        tvTeam.setPadding(12, 12, 12, 12);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(0, 0, 0, 8);
                        tvTeam.setLayoutParams(params);
                        llTeamsList.addView(tvTeam);

                        etTeamId.setText(""); // מנקה את השדה
                    } else {
                        Toast.makeText(this, "Team not found in database", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking team", Toast.LENGTH_SHORT).show();
                    Log.e("AddPlayer", "Error checking team", e);
                });
    }

    private void savePlayer() {
        String playerName = etPlayerName.getText().toString().trim();

        if (TextUtils.isEmpty(playerName)) {
            Toast.makeText(this, "Please enter player name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (playerTeams.isEmpty()) {
            Toast.makeText(this, "Please add at least one team", Toast.LENGTH_SHORT).show();
            return;
        }

        Player player = new Player(playerName);
        player.setTeams(playerTeams);

        // שמירת השחקן ב-Firebase
        db.collection("players")
                .document(playerName)
                .set(player)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Player saved successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AddPlayerActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving player", Toast.LENGTH_SHORT).show();
                    Log.e("AddPlayer", "Error saving player", e);
                });
    }
}
