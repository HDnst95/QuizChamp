// app/src/main/java/com/quizchamp/MultiPlayerActivity.java
package com.quizchamp.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.quizchamp.R;
import com.quizchamp.model.Question;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class MultiPlayerActivity extends AppCompatActivity {

    private TextView questionTextView, frageTextView;
    private MaterialButton buttonAnswer1, buttonAnswer2, buttonAnswer3, buttonAnswer4, nextQuestionButton;
    private List<Question> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int questionCount;

    private TextView playerTurnTextView;
    private int currentPlayer = 1;
    String player1Name;
    String player2Name;
    private int player1Score = 0;
    private int player2Score = 0;
    private FirebaseFirestore db;
    private String opponentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        questionTextView = findViewById(R.id.questionTextView); // Ensure this line is correct
        frageTextView = findViewById(R.id.textViewFrage);
        buttonAnswer1 = findViewById(R.id.buttonAnswer1);
        buttonAnswer2 = findViewById(R.id.buttonAnswer2);
        buttonAnswer3 = findViewById(R.id.buttonAnswer3);
        buttonAnswer4 = findViewById(R.id.buttonAnswer4);
        nextQuestionButton = findViewById(R.id.nextQuestionButton);

        Intent intent = getIntent();
        opponentId = intent.getStringExtra("OPPONENT_ID");

        db = FirebaseFirestore.getInstance();
        fetchQuestionsFromDatabase();

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

    private void fetchQuestionsFromDatabase() {
        db.collection("questions")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        questions.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Question question = document.toObject(Question.class);
                            questions.add(question);
                        }
                    } else {
                        // Handle error
                    }
                });
    }

    private void displayQuestion() {
        if (currentQuestionIndex < questions.size()) {
            frageTextView.setText("Frage " + (currentQuestionIndex + 1) + " von " + questions.size());
            Question currentQuestion = questions.get(currentQuestionIndex);

            // Create a list of options and shuffle them
            List<String> options = new ArrayList<>();
            options.add(currentQuestion.getOptionA());
            options.add(currentQuestion.getOptionB());
            options.add(currentQuestion.getOptionC());
            options.add(currentQuestion.getOptionD());
            Collections.shuffle(options);

            // Set the shuffled options to the buttons
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

            // Adjust button heights
            setButtonHeights(buttons);

            resetButtonColors();
            nextQuestionButton.setVisibility(View.GONE);
        } else {
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
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24); // Ensure consistent text size
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
            if (currentPlayer == 1) {
                player1Score++;
            } else {
                player2Score++;
            }
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
        }
        nextQuestionButton.setVisibility(View.VISIBLE);
    }

    private void nextTurn() {
        if (currentPlayer == 1) {
            currentPlayer = 2;
        } else {
            currentPlayer = 1;
            currentQuestionIndex++;
        }
        displayQuestion();
    }


    private void endGame() {
        nextQuestionButton.setText(R.string.end_game);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.game_over);
        builder.setMessage(String.format(getString(R.string.player_1_score), player1Score) + "\n" + String.format(getString(R.string.player_2_score), player2Score));
        builder.setPositiveButton(R.string.restart_game, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                restartGame();
            }
        });
        builder.show();
    }

    private void restartGame() {
        Intent intent = new Intent(MultiPlayerActivity.this, MainMenuActivity.class);
        startActivity(intent);
        finish();
    }
}
