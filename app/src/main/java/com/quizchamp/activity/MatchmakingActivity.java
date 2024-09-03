package com.quizchamp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quizchamp.R;
import com.quizchamp.UUID.MatchNameGenerator;

public class MatchmakingActivity extends AppCompatActivity {

    private static final String TAG = "MatchmakingActivity";
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String playerId;
    private String opponentId;
    private TextView statusTextView;
    private EditText specificOpponentEditText;
    private Button startNamedMatchButton;
    private Button findRandomOpponentButton;
    private Button findSpecificOpponentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matchmaking);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        statusTextView = findViewById(R.id.statusTextView);
        specificOpponentEditText = findViewById(R.id.specificOpponentEditText);
        startNamedMatchButton = findViewById(R.id.startNamedMatchButton);
        findRandomOpponentButton = findViewById(R.id.findRandomOpponentButton);
        findSpecificOpponentButton = findViewById(R.id.findSpecificOpponentButton);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            playerId = currentUser.getUid();
        } else {
            Log.e(TAG, "User not authenticated");
            finish();
        }

        startNamedMatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNamedMatch();
            }
        });

        findRandomOpponentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findRandomOpponent();
            }
        });

        findSpecificOpponentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findSpecificOpponent();
            }
        });
    }

    private void startNamedMatch() {
        String matchName = MatchNameGenerator.generateRandomMatchName();
        mDatabase.child("matches").child(matchName).child(playerId).setValue(true);
        mDatabase.child("matches").child(matchName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() == 2) {
                    for (DataSnapshot playerSnapshot : snapshot.getChildren()) {
                        String id = playerSnapshot.getKey();
                        if (!id.equals(playerId)) {
                            opponentId = id;
                            mDatabase.child("matches").child(matchName).removeValue();
                            statusTextView.setText("Opponent found: " + opponentId);
                            startGame();
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error finding opponent", error.toException());
            }
        });
    }

    private void findRandomOpponent() {
        mDatabase.child("players").child(playerId).setValue(true);
        mDatabase.child("players").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot playerSnapshot : snapshot.getChildren()) {
                    String id = playerSnapshot.getKey();
                    if (!id.equals(playerId)) {
                        opponentId = id;
                        mDatabase.child("players").child(playerId).removeValue();
                        mDatabase.child("players").child(opponentId).removeValue();
                        statusTextView.setText("Opponent found: " + opponentId);
                        startGame();
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error finding opponent", error.toException());
            }
        });
    }

    private void findSpecificOpponent() {
        String specificOpponentId = specificOpponentEditText.getText().toString();
        if (!specificOpponentId.isEmpty()) {
            mDatabase.child("players").child(specificOpponentId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        opponentId = specificOpponentId;
                        statusTextView.setText("Opponent found: " + opponentId);
                        startGame();
                    } else {
                        statusTextView.setText("Opponent not found");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error finding opponent", error.toException());
                }
            });
        }
    }

    private void startGame() {
        Intent intent = new Intent(MatchmakingActivity.this, MultiPlayerActivity.class);
        intent.putExtra("OPPONENT_ID", opponentId);
        startActivity(intent);
        finish();
    }
}