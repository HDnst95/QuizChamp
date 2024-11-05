package com.quizchamp.activity.modus;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.quizchamp.R;
import com.quizchamp.activity.menu.MainMenuActivity;
import com.quizchamp.model.DifficultyLevel;
import com.quizchamp.model.Question;
import com.quizchamp.repository.QuestionRepository;
import com.quizchamp.utils.JsonUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SinglePlayerActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private QuestionRepository questionRepository;
    private static final String TAG = "HighscoreActivity";

    private MaterialButton buttonAnswer1, buttonAnswer2, buttonAnswer3, buttonAnswer4, nextQuestionButton, backToMainMenuButton;
    private TextView questionTextView, textViewFrage;

    private Question question = new Question();
    private List<Question> gespielteFragen = new ArrayList<Question>();
    private int currentQuestionIndex = 1;
    private DifficultyLevel difficultyLevel;
    private String playerName;
    private int playerScore = 0;
    private int comScore = 0;
    private int questionCount;
    private int anzahlFragen;

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
        difficultyLevel = (DifficultyLevel) intent.getSerializableExtra("DIFFICULTY");
        questionCount = intent.getIntExtra("QUESTION_COUNT", 10);
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

        anzahlFragen = questionRepository.getQuestionCount();
        if (anzahlFragen < questionCount) {
            questionCount = anzahlFragen;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Nicht genügend Fragen");
            String message = "Achtung: Es sind nur " + anzahlFragen + " Fragen in der Datenbank vorhanden. Daher wurden die zu spielenden Fragen auf " + anzahlFragen + " heruntergesetzt.";
            builder.setMessage(message);
            builder.setPositiveButton("Bestätigen", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            builder.setCancelable(false);
            builder.show();
        }
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
            }
        }
        gespielteFragen.add(question);
    }

    private void checkAnswer(MaterialButton selectedButton) {
        buttonAnswer1.setClickable(false);
        buttonAnswer2.setClickable(false);
        buttonAnswer3.setClickable(false);
        buttonAnswer4.setClickable(false);
        if (question.getCorrectAnswer().equals(selectedButton.getText().toString())) {
            selectedButton.setBackgroundColor(getResources().getColor(R.color.correctAnswerColor));
            playerScore++;
            simulateAIAnswer();
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
            simulateAIAnswer();
        }
        nextQuestionButton.setVisibility(View.VISIBLE);
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

        buttonAnswer1.setClickable(true);
        buttonAnswer2.setClickable(true);
        buttonAnswer3.setClickable(true);
        buttonAnswer4.setClickable(true);

    }

    private void simulateAIAnswer() {
        Random random = new Random();
        boolean aiCorrect = false;
        if (difficultyLevel.equals(DifficultyLevel.VERY_EASY)) {
            if (random.nextDouble() < 0.5) {
                aiCorrect = true;
                comScore++;
            } else {
                aiCorrect = false;
            }
        } else if (difficultyLevel.equals(DifficultyLevel.EASY)) {
            if (random.nextDouble() < 0.75) {
                aiCorrect = true;
                comScore++;
            } else {
                aiCorrect = false;
            }
        } else if (difficultyLevel.equals(DifficultyLevel.MEDIUM)) {
            if (random.nextDouble() < 0.83) {
                aiCorrect = true;
                comScore++;
            } else {
                aiCorrect = false;
            }
        } else if (difficultyLevel.equals(DifficultyLevel.HARD)) {
            if (random.nextDouble() < 0.90) {
                aiCorrect = true;
                comScore++;
            } else {
                aiCorrect = false;
            }
        } else if (difficultyLevel.equals(DifficultyLevel.VERY_HARD)) {
            if (random.nextDouble() < 0.98) {
                aiCorrect = true;
                comScore++;
            } else {
                aiCorrect = false;
            }
        }
        if (aiCorrect) {
            Toast.makeText(this, "Die AI hat richtig geantwortet!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Die AI hat falsch geantwortet!", Toast.LENGTH_SHORT).show();
        }
        nextQuestionButton.setVisibility(View.VISIBLE);
    }

    private void endGame() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.game_over);
        String message = String.format(getString(R.string.player_score), playerName, playerScore) + "\n" +
                String.format(getString(R.string.com_score), comScore);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.end_game, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                backToMainMenu();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void nextTurn() {
        if (currentQuestionIndex == questionCount) {
            endGame();
        } else {
            if (currentQuestionIndex == questionCount - 1) {
                nextQuestionButton.setText(R.string.end_game);
                nextQuestionButton.setVisibility(View.VISIBLE);
            }
            currentQuestionIndex++;
            loadQuestion();
        }
    }

    private void backToMainMenu() {
        Intent intent = new Intent(SinglePlayerActivity.this, MainMenuActivity.class);
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