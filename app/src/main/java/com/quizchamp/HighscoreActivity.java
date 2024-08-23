package com.quizchamp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HighscoreActivity extends AppCompatActivity {

    private TextView questionTextView, textViewFrage;
    private MaterialButton buttonAnswer1, buttonAnswer2, buttonAnswer3, buttonAnswer4, nextQuestionButton;
    private List<Question> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private String playerName;
    private int playerScore = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscore);
        playerName = getIntent().getStringExtra("PLAYER_NAME");

        textViewFrage = findViewById(R.id.textViewFrage);
        questionTextView = findViewById(R.id.questionTextView);
        buttonAnswer1 = findViewById(R.id.buttonAnswer1);
        buttonAnswer2 = findViewById(R.id.buttonAnswer2);
        buttonAnswer3 = findViewById(R.id.buttonAnswer3);
        buttonAnswer4 = findViewById(R.id.buttonAnswer4);
        nextQuestionButton = findViewById(R.id.nextQuestionButton);

        questions = JsonLoader.loadQuestionsFromJson(this);
        if (questions != null) {
            Collections.shuffle(questions);
            displayQuestion();
        } else {
            Toast.makeText(this, R.string.error_loading_questions, Toast.LENGTH_SHORT).show();
        }

        // Setze Click-Listener für die Antwort-Buttons
        View.OnClickListener answerClickListener = v -> {
            checkAnswer((MaterialButton) v);
        };

        buttonAnswer1.setOnClickListener(answerClickListener);
        buttonAnswer2.setOnClickListener(answerClickListener);
        buttonAnswer3.setOnClickListener(answerClickListener);
        buttonAnswer4.setOnClickListener(answerClickListener);

        nextQuestionButton.setOnClickListener(v -> nextTurn());
    }

    private void displayQuestion() {
        textViewFrage.setText("Frage " + (currentQuestionIndex + 1));
        Question currentQuestion = questions.get(currentQuestionIndex);

        List<String> options = new ArrayList<>();
        options.add(currentQuestion.getOption1());
        options.add(currentQuestion.getOption2());
        options.add(currentQuestion.getOption3());
        options.add(currentQuestion.getOption4());
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
        nextQuestionButton.setVisibility(View.GONE);
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
            selectedButton.setBackgroundColor(getResources().getColor(R.color.correctAnswerColor));
            currentQuestionIndex++;
            playerScore++;
            nextQuestionButton.setVisibility(View.VISIBLE);
        } else {
            selectedButton.setBackgroundColor(getResources().getColor(R.color.wrongAnswerColor));
            if (buttonAnswer1.getText().toString().equals(currentQuestion.getCorrectAnswer())) {
                buttonAnswer1.setBackgroundColor(getResources().getColor(R.color.correctAnswerColor));
            } else if (buttonAnswer2.getText().toString().equals(currentQuestion.getCorrectAnswer())) {
                buttonAnswer2.setBackgroundColor(getResources().getColor(R.color.correctAnswerColor));
            } else if (buttonAnswer3.getText().toString().equals(currentQuestion.getCorrectAnswer())) {
                buttonAnswer3.setBackgroundColor(getResources().getColor(R.color.correctAnswerColor));
            } else if (buttonAnswer4.getText().toString().equals(currentQuestion.getCorrectAnswer())) {
                buttonAnswer4.setBackgroundColor(getResources().getColor(R.color.correctAnswerColor));
            }
            currentQuestionIndex++;
            nextQuestionButton.setText(R.string.end_game);
            nextQuestionButton.setVisibility(View.VISIBLE);
            nextQuestionButton.setOnClickListener(v -> endGame(false));
            endGame(false);
        }
        if (questions.size() <= currentQuestionIndex + 1) {
            nextQuestionButton.setText(R.string.end_game);
            nextQuestionButton.setVisibility(View.VISIBLE);
            endGame(true);
        }
    }

    private void nextTurn() {
        displayQuestion();
    }

    private void endGame(boolean gewonnen) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (gewonnen) {
            builder.setTitle(R.string.game_won);
            builder.setMessage(String.format(getString(R.string.player_score), playerName, playerScore));
            builder.setPositiveButton(R.string.restart_game, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    restartGame();
                }
            });
        } else {
            builder.setTitle(R.string.game_over);
            builder.setMessage(String.format(getString(R.string.player_score), playerName, playerScore));
            builder.setPositiveButton(R.string.restart_game, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    restartGame();
                }
            });
        }
        builder.show();

    }

    private void restartGame() {
        // Update high scores
        if (playerScore > 0) {
            LocalDateTime now = LocalDateTime.now();
            String zeit;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            zeit = now.format(formatter).toString() + " Uhr";
            HighscoreUtils.updateHighscores(this, new Highscore(playerName, playerScore, zeit));
            Toast.makeText(this, R.string.highscore_updated, Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(HighscoreActivity.this, StartActivity.class);
        intent.putExtra("PLAYER_NAME", playerName);
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