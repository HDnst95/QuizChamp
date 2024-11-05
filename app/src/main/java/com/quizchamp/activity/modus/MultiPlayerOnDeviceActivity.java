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
import com.quizchamp.repository.QuestionRepository;
import com.quizchamp.utils.JsonUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MultiPlayerOnDeviceActivity extends AppCompatActivity {

    private static final String TAG = "HighscoreActivity";
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
    private List<Question> gespielteFragen = new ArrayList<Question>();
    private int anzahlFragen = 0;
    private QuestionRepository questionRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        mAuth = FirebaseAuth.getInstance();

        questionRepository = QuestionRepository.getInstance(this);
        questionRepository.open(); // Open the database
        // Clear the questions table and insert questions from JSON
        questionRepository.clearQuestionsTable();
        String json = JsonUtils.loadJSONFromAsset(this, "questions.json");
        questionRepository.insertQuestionsFromJson(json);

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

        loadQuestion();
    }

    private void loadQuestion() {
        prüfeFragendoppelung();
        if (question != null) {
            textViewFrage.setText("Frage " + (currentQuestionIndex));

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
        } else {
            Log.e(TAG, "No questions available in the database.");
        }
    }

    private void prüfeFragendoppelung() {
        question = questionRepository.getRandomQuestion();
        for (Question q : gespielteFragen) {
            if (q.getId() == question.getId()) {
                question = questionRepository.getRandomQuestion();
                if (gespielteFragen.size() == anzahlFragen) {
                    endGameButton();
                }
            }
        }
        gespielteFragen.add(question);
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

    private void resetButtons() {
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
                resetButtons();
                Toast.makeText(this, currentPlayer + " ist an der Reihe", Toast.LENGTH_SHORT).show();
            } else {
                currentPlayer = 1;
                resetButtons();
                Toast.makeText(this, currentPlayer + " ist an der Reihe", Toast.LENGTH_SHORT).show();
                currentQuestionIndex++;
                loadQuestion();
            }
        } else if (currentQuestionIndex == questionCount && currentPlayer == 1) {

        } else if (currentQuestionIndex == questionCount && currentPlayer == 2) {
            endGameButton();
        }

    }

    private void endGameButton() {
        nextQuestionButton.setText(R.string.end_game);
        nextQuestionButton.setVisibility(View.VISIBLE);
        nextQuestionButton.setOnClickListener(v -> endGame());
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

}
