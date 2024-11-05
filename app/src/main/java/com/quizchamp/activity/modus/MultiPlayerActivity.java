package com.quizchamp.activity.modus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.quizchamp.R;
import com.quizchamp.activity.menu.MainMenuActivity;
import com.quizchamp.model.Match;
import com.quizchamp.model.Question;
import com.quizchamp.repository.QuestionRepository;
import com.quizchamp.utils.JsonUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MultiPlayerActivity extends AppCompatActivity {

    private TextView textViewFrage, questionTextView, player1ScoreTextView, player2ScoreTextView, player1NameTextView, player2NameTextView;
    private GridLayout spielstandAnzeigeScore, spielstandAnzeigeName;
    private MaterialButton buttonAnswer1, buttonAnswer2, buttonAnswer3, buttonAnswer4, nextQuestionButton, backToMainMenuButton;
    private Question question;
    private int playerScore = 0;
    private String playerTurn = "player1";
    private int anzahlFragenDB = 0;
    private int questionNumber = 1;
    private String playerName, opponentName, matchID;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference matchRef;
    private boolean isHost;
    private boolean isFirstQuestion = true;
    private String TAG;
    private Match match;
    private QuestionRepository questionRepository;
    private int currentQuestionIndex = 1;
    private List<Question> gespielteFragen = new ArrayList<Question>();

    private AlertDialog.Builder waitGameDialogBuilder;
    private AlertDialog.Builder waitStartDialogBuilder;
    private AlertDialog waitGameDialog;
    private AlertDialog waitStartDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        questionRepository = QuestionRepository.getInstance(this);
        questionRepository.open(); // Open the database
        // Clear the questions table and insert questions from JSON
        questionRepository.clearQuestionsTable();
        String json = JsonUtils.loadJSONFromAsset(this, "questions.json");
        questionRepository.insertQuestionsFromJson(json);
        anzahlFragenDB = questionRepository.getQuestionCount();

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
        isHost = getIntent().getBooleanExtra("IS_HOST", false);
        playerName = getIntent().getStringExtra("PLAYER_NAME");

        if (isHost) {
            createMatch();
        } else {
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

        waitStartDialogBuilder = new AlertDialog.Builder(this)
                .setTitle("Warte auf Start des Spiels")
                .setMessage("Warte auf den Start des Spiels...")
                .setCancelable(false);
        waitGameDialogBuilder = new AlertDialog.Builder(this)
                .setTitle("Warte auf Gegner")
                .setMessage("Warte auf die Antwort des Gegners...")
                .setCancelable(false);

        waitStartDialog = waitStartDialogBuilder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        notifyOpponent();
        if (matchRef != null) {
            matchRef.delete().addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Match successfully deleted!");
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error deleting match", e);
            });
        }
    }

    private void createMatch() {
        Match newMatch = new Match();
        newMatch.setPlayer1(mAuth.getCurrentUser().getUid());
        newMatch.setPlayerName1(playerName);
        newMatch.setTurn(mAuth.getCurrentUser().getUid());
        newMatch.setMatchID(matchID);
        newMatch.setQuestionNumber(1);
        newMatch.setNewQuestionNeeded(false);
        matchRef = db.collection("matches").document(newMatch.getMatchID());
        matchRef.set(newMatch).addOnSuccessListener(aVoid -> {
            match = newMatch;
            listenForMatchUpdatesHost();
        }).addOnFailureListener(e -> Log.e(TAG, "Error creating match", e));
    }

    private void joinMatch(String matchID) {
        matchRef = db.collection("matches").document(matchID);
        matchRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                match = documentSnapshot.toObject(Match.class);
                match.setPlayer2(mAuth.getCurrentUser().getUid());
                match.setPlayerName2(playerName);
                match.setNewQuestionNeeded(true);
                matchRef.set(match).addOnSuccessListener(aVoid -> {
                    waitStartDialog.dismiss();
                    listenForMatchUpdatesJoin();
                }).addOnFailureListener(e -> Log.e(TAG, "Error joining match", e));
            }
        });
    }

    private void listenForMatchUpdatesHost() {
        matchRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }
            if (documentSnapshot != null && documentSnapshot.exists()) {
                match = documentSnapshot.toObject(Match.class);
                if (match.getPlayer2() != null) {
                    if (waitStartDialog != null && waitStartDialog.isShowing()) {
                        waitStartDialog.dismiss();
                    }
                    if (waitGameDialog == null || !waitGameDialog.isShowing()) {
                        waitGameDialog = waitGameDialogBuilder.show();
                    }
                    if (match.getTurn().equals(mAuth.getCurrentUser().getUid())) {
                        if (waitGameDialog != null && waitGameDialog.isShowing()) {
                            waitGameDialog.dismiss();
                        }
                        elementeEinblenden();
                        if (match.isNewQuestionNeeded()) {
                            loadQuestion();
                        }
                    } else {
                        elementeAusblenden();
                        waitGameDialog.show();
                    }
                }
                updateUI();
            } else {
                showOpponentLeftDialog();
            }
        });
    }

    private void listenForMatchUpdatesJoin() {
        matchRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }
            if (documentSnapshot != null && documentSnapshot.exists()) {
                match = documentSnapshot.toObject(Match.class);
                if (waitGameDialog == null || !waitGameDialog.isShowing()) {
                    waitGameDialog = waitGameDialogBuilder.show();
                }
                if (match.getTurn().equals(mAuth.getCurrentUser().getUid())) {
                    if (waitGameDialog != null && waitGameDialog.isShowing()) {
                        waitGameDialog.dismiss();
                    }
                    elementeEinblenden();
                    loadQuestion();
                } else {
                    elementeAusblenden();
                }
                updateUI();
            } else {
                showOpponentLeftDialog();
            }
        });
    }

    private void updateUI() {
        player1ScoreTextView.setText(String.valueOf(match.getPlayer1Score()));
        player2ScoreTextView.setText(String.valueOf(match.getPlayer2Score()));
        player1NameTextView.setText(playerName.equals(match.getPlayer1()) ? playerName : opponentName);
        player2NameTextView.setText(playerName.equals(match.getPlayer2()) ? playerName : opponentName);
    }

    private void updateScore(String playerId, int score) {
        DocumentReference matchRef = db.collection("matches").document(match.getMatchID());

        matchRef.update(playerId.equals(match.getPlayer1()) ? "player1Score" : "player2Score", score)
                .addOnFailureListener(e -> Log.e(TAG, "Error updating score", e));
    }

    private void switchTurn() {
        DocumentReference matchRef = db.collection("matches").document(match.getMatchID());
        String nextTurn = match.getTurn().equals(match.getPlayer1()) ? match.getPlayer2() : match.getPlayer1();
        matchRef.update("turn", nextTurn)
                .addOnFailureListener(e -> Log.e(TAG, "Error switching turn", e));
    }

    private void loadQuestion() {
        if (isHost) {
            prüfeFragendoppelung();
        } else {
            question = questionRepository.getQuestionById(match.getQuestionID());
        }
        if (question != null) {
            displayQuestion();
        } else {
            Log.e(TAG, "No questions available in the database.");
        }
    }

    private void prüfeFragendoppelung() {
        question = questionRepository.getRandomQuestion();
        for (Question q : gespielteFragen) {
            if (q.getId() == question.getId()) {
                question = questionRepository.getRandomQuestion();
                if (gespielteFragen.size() == anzahlFragenDB) {
                    endGame();
                }
            }
        }
        gespielteFragen.add(question);
        matchRef.update("questionID", question.getId());
        matchRef.update("newQuestionNeeded", false);
        matchRef.get().addOnSuccessListener(documentSnapshot -> {
            match = documentSnapshot.toObject(Match.class);
        });
    }

    private void displayQuestion() {
        questionNumber = match.getQuestionNumber();
        textViewFrage.setText("Frage " + (questionNumber));

        List<String> options = new ArrayList<>();
        options.add(question.getOptionA());
        options.add(question.getOptionB());
        options.add(question.getOptionC());
        options.add(question.getOptionD());
        Collections.shuffle(options);

        questionTextView.setText(question.getQuestionText());
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

        resetButtons();
        nextQuestionButton.setVisibility(View.INVISIBLE);
    }

    private void checkAnswer(MaterialButton button) {
        if (question.getCorrectAnswer().equals(button.getText().toString())) {
            button.setBackgroundColor(getResources().getColor(R.color.correctAnswerColor));
            nextQuestionButton.setVisibility(View.VISIBLE);
            playerScore++;
        } else {
            button.setBackgroundColor(getResources().getColor(R.color.wrongAnswerColor));
            nextQuestionButton.setVisibility(View.VISIBLE);
            highlightCorrectAnswer();
        }
    }

    private void highlightCorrectAnswer() {
        if (buttonAnswer1.getText().toString().equals(question.getCorrectAnswer())) {
            buttonAnswer1.setBackgroundColor(getResources().getColor(R.color.correctAnswerColor));
        } else if (buttonAnswer2.getText().toString().equals(question.getCorrectAnswer())) {
            buttonAnswer2.setBackgroundColor(getResources().getColor(R.color.correctAnswerColor));
        } else if (buttonAnswer3.getText().toString().equals(question.getCorrectAnswer())) {
            buttonAnswer3.setBackgroundColor(getResources().getColor(R.color.correctAnswerColor));
        } else if (buttonAnswer4.getText().toString().equals(question.getCorrectAnswer())) {
            buttonAnswer4.setBackgroundColor(getResources().getColor(R.color.correctAnswerColor));
        }
    }

    private void nextTurn() {
        elementeAusblenden();
        updateScore(mAuth.getCurrentUser().getUid(), playerScore);
        matchRef.update(mAuth.getCurrentUser().getUid().equals(match.getPlayer1()) ? "player1Answered" : "player2Answered", true);
        if (waitGameDialog == null || !waitGameDialog.isShowing()) {
            waitGameDialog = waitGameDialogBuilder.show();
        }
        if (mAuth.getCurrentUser().getUid().equals(match.getPlayer2())) {
            matchRef.update("newQuestionNeeded", true);
            matchRef.update("questionNumber", ++questionNumber);
        }
        switchTurn();
    }

    private void endGame() {
        playerName = match.getPlayerName1();
        opponentName = match.getPlayerName2();
        String winner;
        if (match.getPlayer1Score() > match.getPlayer2Score()) {
            winner = playerName.equals(match.getPlayer1()) ? playerName : opponentName;
        } else if (match.getPlayer1Score() < match.getPlayer2Score()) {
            winner = playerName.equals(match.getPlayer2()) ? playerName : opponentName;
        } else {
            winner = "Unentschieden";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Spielende");
        builder.setMessage("Gewinner: " + winner + "\n" +
                playerName + " : " + (playerName.equals(match.getPlayer1()) ? match.getPlayer1Score() : match.getPlayer2Score()) + "\n" +
                opponentName + " : " + (playerName.equals(match.getPlayer1()) ? match.getPlayer2Score() : match.getPlayer1Score()));
        builder.setPositiveButton("OK", (dialog, which) -> backToMainMenu());
        builder.show();
    }

    private void backToMainMenu() {

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

    private void setButtonHeights(List<MaterialButton> buttons) {
        int maxHeight = getMaxButtonHeight(buttons);
        for (MaterialButton button : buttons) {
            ViewGroup.LayoutParams params = button.getLayoutParams();
            params.height = maxHeight + button.getPaddingTop() + button.getPaddingBottom(); // Add padding to ensure text is fully visible
            button.setLayoutParams(params);
        }
    }

    private void resetButtons() {
        int buttonColor = getResources().getColor(R.color.buttonColor);
        buttonAnswer1.setBackgroundColor(buttonColor);
        buttonAnswer2.setBackgroundColor(buttonColor);
        buttonAnswer3.setBackgroundColor(buttonColor);
        buttonAnswer4.setBackgroundColor(buttonColor);

        buttonAnswer1.setClickable(true);
        buttonAnswer2.setClickable(true);
        buttonAnswer3.setClickable(true);
        buttonAnswer4.setClickable(true);
    }

    private void notifyOpponent() {
        if (match != null) {
            String opponentId = mAuth.getCurrentUser().getUid().equals(match.getPlayer1()) ? match.getPlayer2() : match.getPlayer1();
            if (opponentId != null) {
                DocumentReference opponentRef = db.collection("users").document(opponentId);
                opponentRef.update("opponentLeft", true).addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Opponent notified successfully!");
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error notifying opponent", e);
                });
            }
        }
    }

    private void showOpponentLeftDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Opponent Left");
        builder.setMessage("Your opponent has left the game.");
        builder.setPositiveButton("OK", (dialog, which) -> backToMainMenu());
        builder.setCancelable(false);
        builder.show();
    }
}