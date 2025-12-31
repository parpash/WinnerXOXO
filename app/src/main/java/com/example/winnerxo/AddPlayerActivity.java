package com.example.winnerxo;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class AddPlayerActivity extends AppCompatActivity {

    EditText etPlayerName, etTeamName;
    Button btnAddTeam, btnSavePlayer, btnBackMain;
    LinearLayout llTeamsList;

    FirebaseFirestore db;

    // ✅ רשימת הקבוצות שנוספו לשחקן (String teamid)
    List<String> teamsList = new ArrayList<>();

    // ✅ כל ה-teamid שקיימים ב-Firebase (לבדיקה מהירה)
    HashSet<String> validTeamIds = new HashSet<>();

    boolean teamsLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_player); // ✅ השם של ה-XML שלך

        etPlayerName = findViewById(R.id.etPlayerName);
        etTeamName = findViewById(R.id.etTeamName);
        btnAddTeam = findViewById(R.id.btnAddTeam);
        btnSavePlayer = findViewById(R.id.btnSavePlayer);
        btnBackMain = findViewById(R.id.btnBackMain);
        llTeamsList = findViewById(R.id.llTeamsList);

        db = FirebaseFirestore.getInstance();

        // ✅ טוען את כל הקבוצות שקיימות במאגר
        loadValidTeamsFromFirebase();

        btnAddTeam.setOnClickListener(v -> addTeamToList());
        btnSavePlayer.setOnClickListener(v -> savePlayerToFirebase());
        btnBackMain.setOnClickListener(v -> finish());
    }

    // ============================
    // ✅ Load all valid team IDs once
    // ============================
    private void loadValidTeamsFromFirebase() {
        teamsLoaded = false;
        validTeamIds.clear();

        db.collection("teams").get()
                .addOnSuccessListener(query -> {

                    for (QueryDocumentSnapshot doc : query) {
                        Team t = doc.toObject(Team.class);
                        if (t != null && t.getTeamid() != null) {
                            validTeamIds.add(t.getTeamid().trim());
                        }
                    }

                    teamsLoaded = true;

                    if (validTeamIds.isEmpty()) {
                        Toast.makeText(this, "No teams found in Firebase!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Teams loaded: " + validTeamIds.size(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    teamsLoaded = false;
                    Toast.makeText(this, "Error loading teams from Firebase", Toast.LENGTH_SHORT).show();
                });
    }

    // ============================
    // ✅ Add team ONLY if exists in Firebase
    // ============================
    private void addTeamToList() {
        String teamName = etTeamName.getText().toString().trim();

        if (!teamsLoaded) {
            Toast.makeText(this, "Teams are still loading... try again", Toast.LENGTH_SHORT).show();
            return;
        }

        if (teamName.isEmpty()) {
            Toast.makeText(this, "Enter a team name", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ בדיקה שהקבוצה קיימת ב-Firebase
        if (!validTeamIds.contains(teamName)) {
            Toast.makeText(this, "This team does not exist in the database!", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ מניעת כפילויות
        if (teamsList.contains(teamName)) {
            Toast.makeText(this, "This team already exists in the list", Toast.LENGTH_SHORT).show();
            return;
        }

        teamsList.add(teamName);
        etTeamName.setText("");

        refreshTeamsListUI();
    }

    // ============================
    // ✅ Show teams list UI (no XML change)
    // ============================
    private void refreshTeamsListUI() {
        llTeamsList.removeAllViews();

        for (int i = 0; i < teamsList.size(); i++) {
            String team = teamsList.get(i);

            TextView tv = new TextView(this);
            tv.setText((i + 1) + ". " + team);
            tv.setTextSize(18);
            tv.setPadding(12, 10, 12, 10);
            tv.setGravity(Gravity.START);
            tv.setTextColor(0xFF212121);
            tv.setBackgroundColor(0xFFFFFFFF);

            llTeamsList.addView(tv);
        }
    }

    // ============================
    // ✅ Save player to Firestore
    // ============================
    private void savePlayerToFirebase() {
        String playerName = etPlayerName.getText().toString().trim();

        if (playerName.isEmpty()) {
            Toast.makeText(this, "Enter player name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (teamsList.isEmpty()) {
            Toast.makeText(this, "Add at least one team", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> playerData = new HashMap<>();
        playerData.put("playerid", playerName);
        playerData.put("teams", teamsList); // ✅ List<String>

        // ✅ נשמור את השם בתור document id כדי למנוע כפילויות
        db.collection("players")
                .document(playerName)
                .set(playerData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Player saved!", Toast.LENGTH_SHORT).show();
                    clearForm();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error saving player", Toast.LENGTH_SHORT).show()
                );
    }

    private void clearForm() {
        etPlayerName.setText("");
        etTeamName.setText("");
        teamsList.clear();
        llTeamsList.removeAllViews();
    }
}
