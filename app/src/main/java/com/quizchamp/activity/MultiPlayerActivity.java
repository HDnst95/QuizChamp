// src/main/java/com/quizchamp/activity/MultiPlayerActivity.java
package com.quizchamp.activity;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.quizchamp.R;
import com.quizchamp.model.Question;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MultiPlayerActivity extends AppCompatActivity {

    private TextView questionTextView, player1ScoreTextView, player2ScoreTextView, player1NameTextView, player2NameTextView;
    private GridLayout spielstandAnzeigeScore, spielstandAnzeigeName;
    private MaterialButton buttonAnswer1, buttonAnswer2, buttonAnswer3, buttonAnswer4, nextQuestionButton;
    private List<Question> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int currentPlayer = 1;
    private int player1Score = 0;
    private int player2Score = 0;
    private String playerName, matchID;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference matchRef;
    private Question question;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        questionTextView = findViewById(R.id.questionTextView);

        spielstandAnzeigeScore = findViewById(R.id.spielstandAnzeigeScore);
        spielstandAnzeigeName = findViewById(R.id.spielstandAnzeigeName);
        player1NameTextView = findViewById(R.id.player1NameTextView);
        player2NameTextView = findViewById(R.id.player2NameTextView);
        player1ScoreTextView = findViewById(R.id.player1ScoreTextView);
        player2ScoreTextView = findViewById(R.id.player2ScoreTextView);
        spielstandAnzeigeScore.setVisibility(View.VISIBLE);
        spielstandAnzeigeName.setVisibility(View.VISIBLE);

        buttonAnswer1 = findViewById(R.id.buttonAnswer1);
        buttonAnswer2 = findViewById(R.id.buttonAnswer2);
        buttonAnswer3 = findViewById(R.id.buttonAnswer3);
        buttonAnswer4 = findViewById(R.id.buttonAnswer4);
        nextQuestionButton = findViewById(R.id.nextQuestionButton);

        Intent intent = getIntent();
        playerName = intent.getStringExtra("PLAYER_NAME");
        matchID = intent.getStringExtra("MATCH_ID");

        matchRef = db.collection("matches").document(matchID);

        loadMatchData();
        displayQuestion();

        // Setze Click-Listener für die Antwort-Buttons
        View.OnClickListener answerClickListener = v -> {
            checkAnswer((MaterialButton) v);
        };

        player1NameTextView.setText(playerName);
//        player2NameTextView.setText(opponentName);
        buttonAnswer1.setOnClickListener(answerClickListener);
        buttonAnswer2.setOnClickListener(answerClickListener);
        buttonAnswer3.setOnClickListener(answerClickListener);
        buttonAnswer4.setOnClickListener(answerClickListener);

        nextQuestionButton.setOnClickListener(v -> nextTurn());
    }

    private void loadMatchData() {
        matchRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@NonNull DocumentSnapshot snapshot, @NonNull FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(MultiPlayerActivity.this, "Error loading match data", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (snapshot.exists()) {
                    // Update UI based on match data
                    if (snapshot.getLong("currentPlayer") != null) {
                        currentPlayer = snapshot.getLong("currentPlayer").intValue();
                    } else {
                        Log.e("MultiPlayerActivity", "currentPlayer is null");
                        currentPlayer = 1; // or any default value
                    }
                    if (snapshot.getLong("currentQuestionIndex") != null) {
                        currentQuestionIndex = snapshot.getLong("currentQuestionIndex").intValue();
                    } else {
                        Log.e("MultiPlayerActivity", "currentQuestionIndex is null");
                        currentQuestionIndex = 0; // or any default value
                    }
                    if (snapshot.getLong("player1Score") != null) {
                        player1Score = snapshot.getLong("player1Score").intValue();
                    } else {
                        Log.e("MultiPlayerActivity", "player1Score is null");
                        player1Score = 0; // or any default value
                    }
                    if (snapshot.getLong("player2Score") != null) {
                        player2Score = snapshot.getLong("player2Score").intValue();
                    } else {
                        Log.e("MultiPlayerActivity", "player2Score is null");
                        player2Score = 0; // or any default value
                    }
                    updateUI();
                }
            }
        });
    }

    private void displayQuestion() {
        fetchRandomQuestionFromDatabase(new HighscoreActivity.QuestionFetchCallback() {
            @Override
            public void onQuestionFetched(Question fetchedQuestion) {
                showQuestion(fetchedQuestion);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error fetching question: ", e);
            }
        });
    }

    private void showQuestion(Question currentQuestion) {
        List<String> options = new ArrayList<>();
        options.add(currentQuestion.getOptionA());
        options.add(currentQuestion.getOptionB());
        options.add(currentQuestion.getOptionC());
        options.add(currentQuestion.getOptionD());
        Collections.shuffle(options);

        questionTextView.setText(currentQuestion.getQuestionText());
        buttonAnswer1.setText(options.get(0));
        buttonAnswer2.setText(options.get(1));
        buttonAnswer3.setText(options.get(2));
        buttonAnswer4.setText(options.get(3));

        List<MaterialButton> buttons = new ArrayList<>();
        buttons.add(buttonAnswer1);
        buttons.add(buttonAnswer2);
        buttons.add(buttonAnswer3);
        buttons.add(buttonAnswer4);

        setButtonHeights(buttons);

        resetButtonColors();
        nextQuestionButton.setVisibility(View.INVISIBLE);
    }

    private void fetchRandomQuestionFromDatabase(HighscoreActivity.QuestionFetchCallback callback) {
        db.collection("questions").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int randomIndex = new Random().nextInt(task.getResult().size());
                    DocumentReference randomDocument = task.getResult().getDocuments().get(randomIndex).getReference();
                    randomDocument.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            Question randomQuestion = task.getResult().toObject(Question.class);
                            question = randomQuestion;
                            callback.onQuestionFetched(randomQuestion);
                        }
                    });
                } else {
                    callback.onError(task.getException());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onError(e);
            }
        });
    }

    private int getMaxButtonHeight(List<MaterialButton> buttons) {
        int maxHeight = 0;
        for (MaterialButton button : buttons) {
            int textHeight = measureTextHeight(button.getText().toString());
            if (textHeight > maxHeight) {
                maxHeight = textHeight + 6;
            }
        }
        return maxHeight;
    }

    private int measureTextHeight(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(24); // Ensure consistent text size
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        textView.measure(widthMeasureSpec, heightMeasureSpec);
        return textView.getMeasuredHeight();
    }

    private void resetButtonColors() {
        int buttonColor = getResources().getColor(R.color.buttonColor);
        buttonAnswer1.setBackgroundColor(buttonColor);
        buttonAnswer2.setBackgroundColor(buttonColor);
        buttonAnswer3.setBackgroundColor(buttonColor);
        buttonAnswer4.setBackgroundColor(buttonColor);
    }


    private void checkAnswer(MaterialButton selectedButton) {
        Question currentQuestion = questions.get(currentQuestionIndex);
        if (currentQuestion.getCorrectAnswer().equals(selectedButton.getText().toString())) {
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
            if (currentPlayer == 1) {
                player1Score++;
            } else {
                player2Score++;
            }
        } else {
            Toast.makeText(this, "Wrong!", Toast.LENGTH_SHORT).show();
        }
        currentQuestionIndex++;
        updateMatchData();
    }

    private void updateMatchData() {
        matchRef.update("currentPlayer", currentPlayer == 1 ? 2 : 1,
                        "currentQuestionIndex", currentQuestionIndex,
                        "player1Score", player1Score,
                        "player2Score", player2Score)
                .addOnSuccessListener(aVoid -> displayQuestion())
                .addOnFailureListener(e -> Toast.makeText(MultiPlayerActivity.this, "Error updating match data", Toast.LENGTH_SHORT).show());
    }

    private void updateUI() {
        player1ScoreTextView.setText("Player 1 Score: " + player1Score);
        player2ScoreTextView.setText("Player 2 Score: " + player2Score);
        nextQuestionButton.setVisibility(currentPlayer == 1 ? View.VISIBLE : View.GONE);
    }

    private void nextTurn() {
        currentPlayer = currentPlayer == 1 ? 2 : 1;
        updateMatchData();
    }

    private void endGame() {
        Toast.makeText(this, "Game Over", Toast.LENGTH_SHORT).show();
        // Navigate to results screen or show results
    }

    private void setButtonHeights(List<MaterialButton> buttons) {
        int maxHeight = getMaxButtonHeight(buttons);
        for (MaterialButton button : buttons) {
            ViewGroup.LayoutParams params = button.getLayoutParams();
            params.height = maxHeight + button.getPaddingTop() + button.getPaddingBottom(); // Add padding to ensure text is fully visible
            button.setLayoutParams(params);
        }
    }
}