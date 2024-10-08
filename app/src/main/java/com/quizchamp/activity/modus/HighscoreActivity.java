package com.quizchamp.activity.modus;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
import com.quizchamp.model.Highscore;
import com.quizchamp.model.Question;
import com.quizchamp.utils.HighscoreUtils;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class HighscoreActivity extends AppCompatActivity {

    private static final String TAG = "HighscoreActivity";
    private Question question = new Question();
    private int currentQuestionIndex = 1;
    private MaterialButton buttonAnswer1, buttonAnswer2, buttonAnswer3, buttonAnswer4, nextQuestionButton, backToMainMenuButton;
    private TextView questionTextView, textViewFrage;
    private String playerName;
    private int playerScore = 0;
    private FirebaseAuth mAuth;
    FirebaseFirestore db;
    private int anzahlFragen = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        Intent intent = getIntent();
        playerName = intent.getStringExtra("PLAYER_NAME");
        anzahlFragen = intent.getIntExtra("ANZAHL_DB", 0);

        textViewFrage = findViewById(R.id.textViewFrage);
        questionTextView = findViewById(R.id.questionTextView);
        buttonAnswer1 = findViewById(R.id.buttonAnswer1);
        buttonAnswer2 = findViewById(R.id.buttonAnswer2);
        buttonAnswer3 = findViewById(R.id.buttonAnswer3);
        buttonAnswer4 = findViewById(R.id.buttonAnswer4);
        nextQuestionButton = findViewById(R.id.nextQuestionButton);
        backToMainMenuButton = findViewById(R.id.backToMainMenuButton);


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

    private void displayQuestion() {
        fetchRandomQuestionFromDatabase(new QuestionFetchCallback() {
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

    private void erstesFragenLaden() {
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

    private void checkAnswer(MaterialButton selectedButton) {
        if (question.getCorrectAnswer().equals(selectedButton.getText().toString())) {
            selectedButton.setBackgroundColor(getResources().getColor(R.color.correctAnswerColor));
            playerScore++;
            nextQuestionButton.setVisibility(View.VISIBLE);
        } else {
            selectedButton.setBackgroundColor(getResources().getColor(R.color.wrongAnswerColor));
            if (buttonAnswer1.getText().toString().equals(question.getCorrectAnswer())) {
                buttonAnswer1.setBackgroundColor(getResources().getColor(R.color.correctAnswerColor));
            } else if (buttonAnswer2.getText().toString().equals(question.getCorrectAnswer())) {
                buttonAnswer2.setBackgroundColor(getResources().getColor(R.color.correctAnswerColor));
            } else if (buttonAnswer3.getText().toString().equals(question.getCorrectAnswer())) {
                buttonAnswer3.setBackgroundColor(getResources().getColor(R.color.correctAnswerColor));
            } else if (buttonAnswer4.getText().toString().equals(question.getCorrectAnswer())) {
                buttonAnswer4.setBackgroundColor(getResources().getColor(R.color.correctAnswerColor));
            }
            nextQuestionButton.setText(R.string.end_game);
            nextQuestionButton.setVisibility(View.VISIBLE);
            endGame();
        }
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

    private void endGame() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.game_over);
        builder.setMessage(String.format(getString(R.string.player_score), playerName, playerScore));
        builder.setPositiveButton(R.string.restart_game, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    updateHighscore();
                    backToMainMenu();
                }
            });
        builder.setCancelable(false);
        builder.show();

    }

    private void nextTurn() {
        currentQuestionIndex++;
        displayQuestion();
    }

    private void updateHighscore() {
        // Update high scores
        if (playerScore > 0) {
            LocalDateTime now = LocalDateTime.now();
            String zeit;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            zeit = now.format(formatter).toString() + " Uhr";
            HighscoreUtils.updateHighscores(this, new Highscore(playerName, playerScore, zeit));
            Toast.makeText(this, R.string.highscore_updated, Toast.LENGTH_SHORT).show();
        }
    }

    private void backToMainMenu() {
        Intent intent = new Intent(HighscoreActivity.this, MainMenuActivity.class);
        intent.putExtra("PLAYER_NAME", playerName);
        startActivity(intent);
        finish();
    }

    // Callback-Interface zur Übergabe der Ergebnisse
    public interface QuestionFetchCallback {
        void onQuestionFetched(Question fetchedQuestion);

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