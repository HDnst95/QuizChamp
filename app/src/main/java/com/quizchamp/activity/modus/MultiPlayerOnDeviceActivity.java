// app/src/main/java/com/quizchamp/MultiPlayerActivity.java
package com.quizchamp.activity.modus;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.quizchamp.R;
import com.quizchamp.activity.menu.MainMenuActivity;
import com.quizchamp.model.Question;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MultiPlayerOnDeviceActivity extends AppCompatActivity {

    private static final String TAG = "HighscoreActivity";
    FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private GridLayout spielstandAnzeigeName, spielstandAnzeigeScore;
    private TextView questionTextView, textViewFrage, textViewPlayer1, textViewPlayer2, textViewPlayer1Score, textViewPlayer2Score;
    private MaterialButton buttonAnswer1, buttonAnswer2, buttonAnswer3, buttonAnswer4, nextQuestionButton, backToMainMenuButton;
    private List<Question> questions = new ArrayList<>();
    private int currentQuestionIndex = 1;
    private int questionCount;

    private int currentPlayer = 1;
    private String playerName1;
    private String playerName2;
    private int player1Score = 0;
    private int player2Score = 0;
    private Question question;
    private int anzahlFragen = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        Intent intent = getIntent();
        playerName1 = intent.getStringExtra("PLAYER_1");
        playerName2 = intent.getStringExtra("PLAYER_2");
        questionCount = intent.getIntExtra("QUESTION_COUNT", 10);
        anzahlFragen = intent.getIntExtra("ANZAHL_DB", 0);


        questionTextView = findViewById(R.id.questionTextView); // Ensure this line is correct
        textViewFrage = findViewById(R.id.textViewFrage);
        buttonAnswer1 = findViewById(R.id.buttonAnswer1);
        buttonAnswer2 = findViewById(R.id.buttonAnswer2);
        buttonAnswer3 = findViewById(R.id.buttonAnswer3);
        buttonAnswer4 = findViewById(R.id.buttonAnswer4);
        nextQuestionButton = findViewById(R.id.nextQuestionButton);
        backToMainMenuButton = findViewById(R.id.backToMainMenuButton);

        spielstandAnzeigeName = findViewById(R.id.spielstandAnzeigeName);
        spielstandAnzeigeScore = findViewById(R.id.spielstandAnzeigeScore);
        textViewPlayer1 = findViewById(R.id.player1NameTextView);
        textViewPlayer2 = findViewById(R.id.player2NameTextView);
        textViewPlayer1Score = findViewById(R.id.player1ScoreTextView);
        textViewPlayer2Score = findViewById(R.id.player2ScoreTextView);

        if (playerName1.isBlank()) {
            playerName1 = "Spieler 1";
        }
        if (playerName2.isBlank()) {
            playerName2 = "Spieler 2";
        }
        textViewPlayer1.setText(playerName1);
        textViewPlayer2.setText(playerName2);
        textViewPlayer1Score.setText(String.valueOf(player1Score));
        textViewPlayer2Score.setText(String.valueOf(player2Score));

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        elementeAusblenden();

        erstesFragenLaden();

        // Setze Click-Listener für die Antwort-Buttons
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

    private void elementeAusblenden() {
        textViewFrage.setVisibility(View.GONE);
        questionTextView.setVisibility(View.GONE);
        buttonAnswer1.setVisibility(View.GONE);
        buttonAnswer2.setVisibility(View.GONE);
        buttonAnswer3.setVisibility(View.GONE);
        buttonAnswer4.setVisibility(View.GONE);
        nextQuestionButton.setVisibility(View.INVISIBLE);
        backToMainMenuButton.setVisibility(View.INVISIBLE);
        spielstandAnzeigeName.setVisibility(View.GONE);
        spielstandAnzeigeScore.setVisibility(View.GONE);
    }

    private void elementeEinblenden() {
        textViewFrage.setVisibility(View.VISIBLE);
        questionTextView.setVisibility(View.VISIBLE);
        buttonAnswer1.setVisibility(View.VISIBLE);
        buttonAnswer2.setVisibility(View.VISIBLE);
        buttonAnswer3.setVisibility(View.VISIBLE);
        buttonAnswer4.setVisibility(View.VISIBLE);
        nextQuestionButton.setVisibility(View.VISIBLE);
        backToMainMenuButton.setVisibility(View.VISIBLE);
        spielstandAnzeigeName.setVisibility(View.VISIBLE);
        spielstandAnzeigeScore.setVisibility(View.VISIBLE);
    }

    private void erstesFragenLaden() {
        fetchRandomQuestionFromDatabase(new MultiPlayerOnDeviceActivity.QuestionFetchCallback() {
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

    private void displayQuestion() {
        fetchRandomQuestionFromDatabase(new MultiPlayerOnDeviceActivity.QuestionFetchCallback() {
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

    private void fetchRandomQuestionFromDatabase(MultiPlayerOnDeviceActivity.QuestionFetchCallback callback) {
        int randomIndex = new Random().nextInt(anzahlFragen);
        db.collection("questions").document(String.valueOf(randomIndex)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Question randomQuestion = task.getResult().toObject(Question.class);
                question = randomQuestion;
                callback.onQuestionFetched(randomQuestion);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onError(e);
            }
        });
    }

    private void showQuestion(Question currentQuestion) {
        textViewFrage.setText("Frage " + (currentQuestionIndex));
        textViewPlayer1.setText(playerName1);
        textViewPlayer2.setText(playerName2);
        textViewPlayer1Score.setText(String.valueOf(player1Score));
        textViewPlayer2Score.setText(String.valueOf(player2Score));


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
        selectedButton.setBackgroundColor(getResources().getColor(R.color.colorOnBackground));
        if (question.getCorrectAnswer().equals(selectedButton.getText().toString())) {
            if (currentPlayer == 1) player1Score++;
            else player2Score++;
        }
        textViewPlayer1Score.setText(String.valueOf(player1Score));
        textViewPlayer2Score.setText(String.valueOf(player2Score));
        nextQuestionButton.setVisibility(View.VISIBLE);
    }

    private void nextTurn() {
        if (currentQuestionIndex < questionCount) {
            if (currentPlayer == 1) {
                currentPlayer = 2;
                resetButtonColors();
                Toast.makeText(this, currentPlayer + " ist an der Reihe", Toast.LENGTH_SHORT).show();
            } else {
                currentPlayer = 1;
                resetButtonColors();
                Toast.makeText(this, currentPlayer + " ist an der Reihe", Toast.LENGTH_SHORT).show();
                currentQuestionIndex++;
                displayQuestion();
            }
        } else if (currentQuestionIndex == questionCount && currentPlayer == 1) {
            currentPlayer = 2;
            resetButtonColors();
            Toast.makeText(this, currentPlayer + " ist an der Reihe", Toast.LENGTH_SHORT).show();
        } else if (currentQuestionIndex == questionCount && currentPlayer == 2) {
            endGame();
            nextQuestionButton.setText(R.string.end_game);
            nextQuestionButton.setVisibility(View.VISIBLE);
        }

    }

    private void endGame() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.game_over);
        builder.setMessage(String.format(getString(R.string.player_1_score), player1Score) + "\n" + String.format(getString(R.string.player_2_score), player2Score));
        builder.setPositiveButton(R.string.restart_game, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                backToMainMenu();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void backToMainMenu() {
        Intent intent = new Intent(MultiPlayerOnDeviceActivity.this, MainMenuActivity.class);
        startActivity(intent);
        finish();
    }

    private void setButtonHeights(List<MaterialButton> buttons) {
        int maxHeight = getMaxButtonHeight(buttons);
        for (MaterialButton button : buttons) {
            ViewGroup.LayoutParams params = button.getLayoutParams();
            params.height = maxHeight + button.getPaddingTop() + button.getPaddingBottom(); // Add padding to ensure text is fully visible
            button.setLayoutParams(params);
        }
    }

    private interface QuestionFetchCallback {
        void onQuestionFetched(Question fetchedQuestion);

        void onError(Exception e);
    }
}
