package com.example.winnerxo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    LinearLayout rowsContainer;
    TextView tvCurrentPlayer;
    EditText[][] cells = new EditText[3][3];
    Button btnCheckMove, btnReset;

    int currentPlayerIndex = 0;
    Player[] gamePlayers = new Player[2];

    List<Team> allTeams = new ArrayList<>();
    List<Team> columnTeams = new ArrayList<>();
    List<Team> rowTeams = new ArrayList<>();

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        tvCurrentPlayer = findViewById(R.id.tvCurrentPlayer);
        rowsContainer = findViewById(R.id.rowsContainer);
        btnCheckMove = findViewById(R.id.btnCheckMove);
        btnReset = findViewById(R.id.btnReset);

        db = FirebaseFirestore.getInstance();

        String player1Name = getIntent().getStringExtra("PLAYER1_NAME");
        String player2Name = getIntent().getStringExtra("PLAYER2_NAME");
        gamePlayers[0] = new Player(player1Name);
        gamePlayers[1] = new Player(player2Name);

        loadTeamsFromFirebase();

        btnCheckMove.setOnClickListener(v -> checkMove());
        btnReset.setOnClickListener(v -> resetBoard());
    }

    private void loadTeamsFromFirebase() {
        db.collection("teams").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                allTeams.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Team t = doc.toObject(Team.class);
                    allTeams.add(t);
                }

                Collections.shuffle(allTeams);
                columnTeams.clear();
                rowTeams.clear();
                for (int i = 0; i < 3; i++) {
                    columnTeams.add(allTeams.get(i));
                    rowTeams.add(allTeams.get(i + 3));
                }

                runOnUiThread(this::setupBoard);
                updateTurnText();
            } else {
                Toast.makeText(this, "Error loading teams", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBoard() {
        rowsContainer.removeAllViews();

        for (int row = 0; row < 3; row++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setGravity(Gravity.CENTER_VERTICAL);

            for (int col = 0; col < 3; col++) {
                LinearLayout colLayout = new LinearLayout(this);
                colLayout.setOrientation(LinearLayout.VERTICAL);
                colLayout.setGravity(Gravity.CENTER_HORIZONTAL);

                // סמל קבוצת העמודה מעל המשבצות (רק בשורה הראשונה)
                if (row == 0) {
                    ImageView colImg = new ImageView(this);
                    LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(200, 200);
                    imgParams.setMargins(8, 8, 8, 8);
                    colImg.setLayoutParams(imgParams);
                    colImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    loadImageFromUrl(columnTeams.get(col).getUrl(), colImg, 200, 200);
                    colLayout.addView(colImg);
                }

                EditText cell = new EditText(this);
                LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(250, 200);
                cellParams.setMargins(8, 8, 8, 8);
                cell.setLayoutParams(cellParams);
                cell.setSingleLine(true);
                cell.setEllipsize(android.text.TextUtils.TruncateAt.END);
                cell.setBackgroundResource(android.R.drawable.edit_text);
                cell.setPadding(8, 8, 8, 8);

                cells[row][col] = cell;
                colLayout.addView(cell);

                rowLayout.addView(colLayout);
            }

            // סמל הקבוצה המתאים לשורה מימין
            ImageView rowImg = new ImageView(this);
            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(200, 200);
            imgParams.setMargins(16, 8, 8, 8);
            rowImg.setLayoutParams(imgParams);
            rowImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
            loadImageFromUrl(rowTeams.get(row).getUrl(), rowImg, 200, 200);
            rowLayout.addView(rowImg);

            rowsContainer.addView(rowLayout);
        }
    }

    private void loadImageFromUrl(String urlString, ImageView imageView, int width, int height) {
        new Thread(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width, height, true);
                runOnUiThread(() -> imageView.setImageBitmap(scaled));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateTurnText() {
        tvCurrentPlayer.setText("Turn: " + gamePlayers[currentPlayerIndex].getPlayerid());
    }

    private void checkMove() {
        Player currentPlayer = gamePlayers[currentPlayerIndex];
        boolean moveMade = false;

        outerLoop:
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                EditText cell = cells[row][col];
                String enteredName = cell.getText().toString().trim();
                if (!enteredName.isEmpty() && cell.isEnabled()) {

                    final int finalRow = row;
                    final int finalCol = col;

                    db.collection("players").whereEqualTo("playerid", enteredName)
                            .get().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    if (task.getResult().isEmpty()) {
                                        Toast.makeText(this, "Player not found!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Player dbPlayer = task.getResult().getDocuments()
                                                .get(0).toObject(Player.class);

                                        Team colTeam = columnTeams.get(finalCol);
                                        Team rowTeam = rowTeams.get(finalRow);
                                        boolean playedCol = false, playedRow = false;

                                        for (Team t : dbPlayer.getTeams()) {
                                            if (t.getTeamid().equals(colTeam.getTeamid())) playedCol = true;
                                            if (t.getTeamid().equals(rowTeam.getTeamid())) playedRow = true;
                                        }

                                        if (playedCol && playedRow) {
                                            cell.setEnabled(false);
                                        } else {
                                            Toast.makeText(this, "Player did not play in required teams!", Toast.LENGTH_SHORT).show();
                                        }

                                        currentPlayerIndex = (currentPlayerIndex + 1) % 2;
                                        updateTurnText();
                                    }
                                } else {
                                    Toast.makeText(this, "Error querying Firebase", Toast.LENGTH_SHORT).show();
                                }
                            });

                    moveMade = true;
                    break outerLoop;
                }
            }
        }

        if (!moveMade) {
            Toast.makeText(this, "Please enter a player name in an empty cell!", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetBoard() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                cells[row][col].setText("");
                cells[row][col].setEnabled(true);
            }
        }
        currentPlayerIndex = 0;
        updateTurnText();
    }
}
