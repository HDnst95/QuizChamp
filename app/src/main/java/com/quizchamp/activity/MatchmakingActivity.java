package com.quizchamp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.quizchamp.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MatchmakingActivity extends AppCompatActivity {

    private EditText playerEditText;
    private EditText opponentMatchIdEditText;
    private Button startButton;
    private Button joinButton;
    private Button backButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matchmaking);

        playerEditText = findViewById(R.id.playerEditText);
        Intent intent = getIntent();
        playerEditText.setText(intent.getStringExtra("PLAYER_NAME"));
        opponentMatchIdEditText = findViewById(R.id.opponentMatchIdEditText);
        startButton = findViewById(R.id.startButton);
        joinButton = findViewById(R.id.joinButton);
        backButton = findViewById(R.id.backButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String playerName = playerEditText.getText().toString();
                String matchID = generateMatchID();
                saveMatchID(matchID, playerName); // Pass both matchID and playerName
                startGame(playerName, matchID);
            }
        });

        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String playerName = playerEditText.getText().toString();
                String matchID = opponentMatchIdEditText.getText().toString();
                joinGame(playerName, matchID);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private String generateMatchID() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder matchID = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            matchID.append(characters.charAt(random.nextInt(characters.length())));
        }
        return matchID.toString();
    }

    private void startGame(String playerName, String matchID) {
        Intent intent = new Intent(MatchmakingActivity.this, MultiPlayerActivity.class);
        intent.putExtra("PLAYER_NAME", playerName);
        intent.putExtra("MATCH_ID", matchID);
        intent.putExtra("IS_HOST", true);
        startActivity(intent);
    }

    private void joinGame(String playerName, String matchID) {
        db.collection("matches").document(matchID) // Ensure even number of segments
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Match ID exists, update with joining player
                                db.collection("matches").document(matchID) // Ensure even number of segments
                                        .update("joinedPlayer", playerName)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Start the game
                                                Intent intent = new Intent(MatchmakingActivity.this, MultiPlayerActivity.class);
                                                intent.putExtra("PLAYER_NAME", playerName);
                                                intent.putExtra("MATCH_ID", matchID);
                                                intent.putExtra("IS_HOST", false);
                                                startActivity(intent);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Handle the error
                                            }
                                        });
                            } else {
                                opponentMatchIdEditText.setError("Invalid Match ID");
                            }
                        } else {
                            // Handle the error
                        }
                    }
                });
    }

    private void saveMatchID(String matchID, String playerName) {
        Map<String, Object> matchData = new HashMap<>();
        matchData.put("hostPlayer", playerName);
        matchData.put("joinedPlayer", null);

        db.collection("matches").document(matchID)
                .set(matchData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Match ID saved successfully
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle the error
                    }
                });
    }
}