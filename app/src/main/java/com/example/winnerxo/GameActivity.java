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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    // ===== UI =====
    LinearLayout rowsContainer, columnTeamsLayout;
    TextView tvCurrentPlayer;
    EditText[][] cells = new EditText[3][3];
    Button btnCheckMove, btnReset;

    // ===== Players =====
    int currentPlayerIndex = 0;
    Player[] gamePlayers = new Player[2];

    // ===== Teams =====
    List<Team> allTeams = new ArrayList<>();
    List<Team> columnTeams = new ArrayList<>();
    List<Team> rowTeams = new ArrayList<>();

    FirebaseFirestore db;

    // ===== Game state =====
    int[][] boardState = new int[3][3]; // 0=empty, 1=p1, 2=p2
    boolean gameOver = false;

    // Colors
    private final int PLAYER1_COLOR = 0x55FF0000; // red
    private final int PLAYER2_COLOR = 0x550000FF; // blue

    // Sizes (px)
    private static final int CELL_W = 200;
    private static final int CELL_H = 200;
    private static final int ICON_SIZE = 200;
    private static final int MARGIN = 6;
    private static final int PADDING = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        tvCurrentPlayer = findViewById(R.id.tvCurrentPlayer);
        rowsContainer = findViewById(R.id.rowsContainer);
        columnTeamsLayout = findViewById(R.id.columnTeamsLayout);
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

    // ===================== Firebase =====================
    private void loadTeamsFromFirebase() {
        db.collection("teams").get().addOnSuccessListener(query -> {

            allTeams.clear();
            for (QueryDocumentSnapshot doc : query) {
                Team t = doc.toObject(Team.class);
                allTeams.add(t);
            }

            if (allTeams.size() < 6) {
                Toast.makeText(this, "Not enough teams in Firebase", Toast.LENGTH_SHORT).show();
                return;
            }

            Collections.shuffle(allTeams);
            columnTeams.clear();
            rowTeams.clear();

            for (int i = 0; i < 3; i++) {
                columnTeams.add(allTeams.get(i));
                rowTeams.add(allTeams.get(i + 3));
            }

            setupColumnTeams();
            setupBoard();
            updateTurnText();
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error loading teams", Toast.LENGTH_SHORT).show()
        );
    }

    // ===================== UI build =====================
    private void setupColumnTeams() {
        columnTeamsLayout.removeAllViews();

        for (int col = 0; col < 3; col++) {
            ImageView img = new ImageView(this);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(ICON_SIZE, ICON_SIZE);
            params.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);

            img.setLayoutParams(params);
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);

            loadImageFromUrl(columnTeams.get(col).getUrl(), img, ICON_SIZE, ICON_SIZE);
            columnTeamsLayout.addView(img);
        }
    }

    private void setupBoard() {
        rowsContainer.removeAllViews();

        for (int row = 0; row < 3; row++) {

            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setGravity(Gravity.CENTER_VERTICAL);

            // Cells
            for (int col = 0; col < 3; col++) {
                EditText cell = new EditText(this);

                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(CELL_W, CELL_H);
                params.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);

                cell.setLayoutParams(params);
                cell.setSingleLine(true);
                cell.setPadding(PADDING, PADDING, PADDING, PADDING);
                cell.setBackgroundResource(android.R.drawable.edit_text);

                cells[row][col] = cell;
                rowLayout.addView(cell);
            }

            // Row icon
            ImageView rowImg = new ImageView(this);
            LinearLayout.LayoutParams imgParams =
                    new LinearLayout.LayoutParams(ICON_SIZE, ICON_SIZE);
            imgParams.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);

            rowImg.setLayoutParams(imgParams);
            rowImg.setScaleType(ImageView.ScaleType.CENTER_CROP);

            loadImageFromUrl(rowTeams.get(row).getUrl(), rowImg, ICON_SIZE, ICON_SIZE);
            rowLayout.addView(rowImg);

            rowsContainer.addView(rowLayout);
        }
    }

    // ===================== Images =====================
    private void loadImageFromUrl(String urlString, ImageView imageView, int w, int h) {
        new Thread(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setDoInput(true);
                c.connect();
                InputStream i = c.getInputStream();
                Bitmap b = BitmapFactory.decodeStream(i);
                Bitmap s = Bitmap.createScaledBitmap(b, w, h, true);
                runOnUiThread(() -> imageView.setImageBitmap(s));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ===================== Game logic =====================
    private void checkMove() {

        if (gameOver) return;

        // Find first enabled cell with text
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {

                EditText cell = cells[r][c];
                String name = cell.getText().toString().trim();

                if (!name.isEmpty() && cell.isEnabled()) {

                    int row = r;
                    int col = c;

                    db.collection("players")
                            .whereEqualTo("playerid", name)
                            .get()
                            .addOnSuccessListener(q -> {

                                // Player not found -> don't change turn
                                if (q.isEmpty()) {
                                    Toast.makeText(this, "Player not found!", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                Player p = q.getDocuments().get(0).toObject(Player.class);
                                if (p == null || p.getTeams() == null) {
                                    Toast.makeText(this, "Player data is invalid!", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                boolean okRow = false, okCol = false;

                                // ✅ teams זה עכשיו List<String>
                                for (String teamId : p.getTeams()) {
                                    if (teamId == null) continue;

                                    if (teamId.equals(rowTeams.get(row).getTeamid()))
                                        okRow = true;

                                    if (teamId.equals(columnTeams.get(col).getTeamid()))
                                        okCol = true;
                                }

                                if (okRow && okCol) {

                                    // ✅ Valid move: lock + color
                                    cell.setEnabled(false);
                                    cell.setBackgroundColor(currentPlayerIndex == 0 ? PLAYER1_COLOR : PLAYER2_COLOR);

                                    boardState[row][col] = currentPlayerIndex + 1;

                                    // ✅ Win check
                                    if (checkWin()) {
                                        Toast.makeText(this,
                                                "Winner: " + gamePlayers[currentPlayerIndex].getPlayerid(),
                                                Toast.LENGTH_LONG).show();
                                        gameOver = true;
                                        disableBoard();
                                        return;
                                    }

                                    // ✅ Draw check
                                    if (checkDraw()) {
                                        Toast.makeText(this, "Draw!", Toast.LENGTH_LONG).show();
                                        gameOver = true;
                                        return;
                                    }

                                    // ✅ Next turn after valid move
                                    currentPlayerIndex = (currentPlayerIndex + 1) % 2;
                                    updateTurnText();

                                } else {

                                    // ✅ Player exists but doesn't match teams -> still change turn
                                    Toast.makeText(this,
                                            "Player did not play in required teams!",
                                            Toast.LENGTH_SHORT).show();

                                    currentPlayerIndex = (currentPlayerIndex + 1) % 2;
                                    updateTurnText();
                                }
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error querying Firebase", Toast.LENGTH_SHORT).show()
                            );

                    return;
                }
            }
        }

        Toast.makeText(this,
                "Please enter a player name in an empty cell!",
                Toast.LENGTH_SHORT).show();
    }

    private boolean checkWin() {
        int p = currentPlayerIndex + 1;

        // rows
        for (int r = 0; r < 3; r++) {
            if (boardState[r][0] == p && boardState[r][1] == p && boardState[r][2] == p) return true;
        }

        // cols
        for (int c = 0; c < 3; c++) {
            if (boardState[0][c] == p && boardState[1][c] == p && boardState[2][c] == p) return true;
        }

        // diagonals
        return (boardState[0][0] == p && boardState[1][1] == p && boardState[2][2] == p) ||
                (boardState[0][2] == p && boardState[1][1] == p && boardState[2][0] == p);
    }

    private boolean checkDraw() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (boardState[r][c] == 0) return false;
            }
        }
        return true;
    }

    private void disableBoard() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                cells[r][c].setEnabled(false);
            }
        }
    }

    private void resetBoard() {
        gameOver = false;
        currentPlayerIndex = 0;

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                cells[r][c].setText("");
                cells[r][c].setEnabled(true);
                cells[r][c].setBackgroundResource(android.R.drawable.edit_text);
                boardState[r][c] = 0;
            }
        }

        allTeams.clear();
        columnTeams.clear();
        rowTeams.clear();
        rowsContainer.removeAllViews();
        columnTeamsLayout.removeAllViews();

        loadTeamsFromFirebase();
        updateTurnText();
    }

    private void updateTurnText() {
        tvCurrentPlayer.setText("Turn: " + gamePlayers[currentPlayerIndex].getPlayerid());
    }
}
