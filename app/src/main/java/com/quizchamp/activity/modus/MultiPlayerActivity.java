package com.quizchamp.activity.modus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.quizchamp.R;
import com.quizchamp.activity.menu.MainMenuActivity;
import com.quizchamp.model.Match;
import com.quizchamp.model.Question;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MultiPlayerActivity extends AppCompatActivity {

    private TextView textViewFrage, questionTextView, player1ScoreTextView, player2ScoreTextView, player1NameTextView, player2NameTextView;
    private GridLayout spielstandAnzeigeScore, spielstandAnzeigeName;
    private MaterialButton buttonAnswer1, buttonAnswer2, buttonAnswer3, buttonAnswer4, nextQuestionButton, backToMainMenuButton;
    private Question question;
    private int currentPlayer = 1;
    private int player1Score = 0;
    private int player2Score = 0;
    private int anzahlFragen;
    private int questionNumber = 0;
    private String player1Name, player2Name, matchID;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference matchRef;
    private boolean isHost;
    private String TAG;
    private boolean playerOne;
    private Match match;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        textViewFrage = findViewById(R.id.textViewFrage);
        questionTextView = findViewById(R.id.questionTextView);
        buttonAnswer1 = findViewById(R.id.buttonAnswer1);
        buttonAnswer2 = findViewById(R.id.buttonAnswer2);
        buttonAnswer3 = findViewById(R.id.buttonAnswer3);
        buttonAnswer4 = findViewById(R.id.buttonAnswer4);

        spielstandAnzeigeScore = findViewById(R.id.spielstandAnzeigeScore);
        spielstandAnzeigeName = findViewById(R.id.spielstandAnzeigeName);
        player1ScoreTextView = findViewById(R.id.player1ScoreTextView);
        player2ScoreTextView = findViewById(R.id.player2ScoreTextView);
        player1NameTextView = findViewById(R.id.player1NameTextView);
        player2NameTextView = findViewById(R.id.player2NameTextView);

        nextQuestionButton = findViewById(R.id.nextQuestionButton);
        backToMainMenuButton = findViewById(R.id.backToMainMenuButton);

        // Matchmaking
        matchID = getIntent().getStringExtra("MATCH_ID");
        anzahlFragen = getIntent().getIntExtra("ANZAHL_DB", 0);
        isHost = getIntent().getBooleanExtra("IS_HOST", false);
        String playerName = getIntent().getStringExtra("PLAYER_NAME");

        if (isHost) {
            player1Name = playerName;
            playerOne = true;
            createMatch();
        } else {
            player2Name = playerName;
            playerOne = false;
            joinMatch(matchID);
        }

        elementeAusblenden();

        View.OnClickListener answerClickListener = v -> {
            checkAnswer((MaterialButton) v);
        };

        buttonAnswer1.setOnClickListener(answerClickListener);
        buttonAnswer2.setOnClickListener(answerClickListener);
        buttonAnswer3.setOnClickListener(answerClickListener);
        buttonAnswer4.setOnClickListener(answerClickListener);

        nextQuestionButton.setOnClickListener(v -> nextTurn());
        backToMainMenuButton.setOnClickListener(v -> backToMainMenu());
    }

    // MultiPlayerActivity.java

    private void createMatch() {
        matchRef = db.collection("matches").document();
        Match match = new Match();
        match.setPlayer1(player1Name);
        match.setPlayer1Score(0);
        match.setPlayer2("");
        match.setPlayer2Score(0);
        match.setMatchID(matchID);
        match.setTurn(player1Name);
        match.setQuestionID(0);
        match.setQuestionNumber(0);
        matchRef.set(match)
                .addOnSuccessListener(aVoid -> {
                    addSnapshotListener();
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    private void joinMatch(String matchID) {
        matchRef = db.collection("matches").document(matchID);
        matchRef.update("player2", player2Name)
                .addOnSuccessListener(aVoid -> {
                    addSnapshotListener();
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    private void deleteMatch() {
        matchRef.delete()
                .addOnSuccessListener(aVoid -> {
                    // Handle success
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    private void addSnapshotListener() {
        matchRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                // Handle error
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                Match match = snapshot.toObject(Match.class);
                if (match != null) {
                    updateGameState(match);
                    if (match.getPlayer2() != null && !match.getPlayer2().isEmpty()) {
                        if (match.getTurn().equals(player1Name)) {
                            fragenLaden();
                        }
                    }
                }
            }
        });
    }

    private void updateGameState(Match match) {
        player1NameTextView.setText(match.getPlayer1());
        player2NameTextView.setText(match.getPlayer2());
        player1ScoreTextView.setText(String.valueOf(match.getPlayer1Score()));
        player2ScoreTextView.setText(String.valueOf(match.getPlayer2Score()));
        currentPlayer = match.getTurn().equals(player1Name) ? 1 : 2;
        matchID = match.getMatchID();
        questionNumber = match.getQuestionNumber();
    }

    private void fragenLaden() {
        fetchRandomQuestionFromDatabase(new QuestionFetchCallback() {
            @Override
            public void onQuestionFetched(Question fetchedQuestion) {
                showQuestion(fetchedQuestion);
                elementeEinblenden();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error fetching question: ", e);
            }
        });
    }

    private void fetchRandomQuestionFromDatabase(QuestionFetchCallback callback) {
        if (playerOne) {
            Random random = new Random();
            int randomIndex = random.nextInt(anzahlFragen);
            db.collection("matches").document(matchRef.getId()).update("questionID", randomIndex);
            db.collection("questions").document(String.valueOf(randomIndex)).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    question = task.getResult().toObject(Question.class);
                    callback.onQuestionFetched(question);
                }
            }).addOnFailureListener(callback::onError);
        } else {
            db.collection("questions").document(String.valueOf(match.getQuestionID())).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    question = task.getResult().toObject(Question.class);
                    callback.onQuestionFetched(question);
                }
            }).addOnFailureListener(callback::onError);
        }
    }

    private void showQuestion(Question currentQuestion) {
        textViewFrage.setText("Frage " + (match.getQuestionNumber() + 1));

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

    private void checkAnswer(MaterialButton button) {
        String selectedAnswer = button.getText().toString();
        String correctAnswer = question.getCorrectAnswer();
        if (selectedAnswer.equals(correctAnswer)) {
            button.setBackgroundColor(getResources().getColor(R.color.correctAnswerColor));
            if (currentPlayer == 1) {
                matchRef.update("player1Score", player1Score + 1);
            } else {
                matchRef.update("player2Score", player2Score + 1);
            }
        } else {
            button.setBackgroundColor(getResources().getColor(R.color.wrongAnswerColor));
            endGame();
        }
        matchRef.update(currentPlayer == 1 ? "player1Score" : "player2Score", currentPlayer == 1 ? player1Score : player2Score);
        nextQuestionButton.setVisibility(View.VISIBLE);
    }

    private void nextTurn() {
        matchRef.update("turn", currentPlayer == 1 ? player2Name : player1Name);
        fragenLaden();
    }

    private void endGame() {
        matchRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    match = document.toObject(Match.class);
                    showMatchResult();
                    backToMainMenu();
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    private void showMatchResult() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Match beendet")
                .setMessage(player1Name + ": " + player1Score + " Punkte" + "\n" + player2Name + ": " + player2Score + " Punkte")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void backToMainMenu() {
        deleteMatch();
        Intent intent = new Intent(MultiPlayerActivity.this, MainMenuActivity.class);
        startActivity(intent);
        finish();
    }

    private void elementeAusblenden() {
        textViewFrage.setVisibility(View.GONE);
        questionTextView.setVisibility(View.GONE);
        buttonAnswer1.setVisibility(View.GONE);
        buttonAnswer2.setVisibility(View.GONE);
        buttonAnswer3.setVisibility(View.GONE);
        buttonAnswer4.setVisibility(View.GONE);
        nextQuestionButton.setVisibility(View.INVISIBLE);
        backToMainMenuButton.setVisibility(View.INVISIBLE);
    }

    private void elementeEinblenden() {
        textViewFrage.setVisibility(View.VISIBLE);
        questionTextView.setVisibility(View.VISIBLE);
        buttonAnswer1.setVisibility(View.VISIBLE);
        buttonAnswer2.setVisibility(View.VISIBLE);
        buttonAnswer3.setVisibility(View.VISIBLE);
        buttonAnswer4.setVisibility(View.VISIBLE);
        backToMainMenuButton.setVisibility(View.VISIBLE);
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

    public interface QuestionFetchCallback {
        void onQuestionFetched(Question fetchedQuestion);

        void onError(Exception e);
    }

    public interface MatchFetchCallback {
        void onMatchFetched(Match fetchedMatch);

        void onError(Exception e);
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