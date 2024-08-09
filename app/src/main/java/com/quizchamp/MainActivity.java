package com.quizchamp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView questionTextView;
    private MaterialButton buttonAnswer1, buttonAnswer2, buttonAnswer3, buttonAnswer4, nextQuestionButton;
    private TextView playerTurnTextView;
    private int currentPlayer = 1;
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int player1Score = 0;
    private int player2Score = 0;
    private int questionCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        questionTextView = findViewById(R.id.questionTextView);
        buttonAnswer1 = findViewById(R.id.buttonAnswer1);
        buttonAnswer2 = findViewById(R.id.buttonAnswer2);
        buttonAnswer3 = findViewById(R.id.buttonAnswer3);
        buttonAnswer4 = findViewById(R.id.buttonAnswer4);
        nextQuestionButton = findViewById(R.id.nextQuestionButton);
        playerTurnTextView = findViewById(R.id.playerTurnTextView);

        Intent intent = getIntent();
        questionCount = intent.getIntExtra("QUESTION_COUNT", 10);

        QuizDatabase db = QuizDatabase.getInstance(this);

        // Prepopulate the database if empty
        prepopulateDatabase(db);

        loadQuestions();
        displayQuestion();

        View.OnClickListener answerClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialButton selectedButton = (MaterialButton) v;
                String selectedAnswer = selectedButton.getText().toString();
                checkAnswer(selectedAnswer);
                if (currentPlayer == 2) {
                    showCorrectAnswer();
                } else {
                    showNextPlayerToast();
                }
            }
        };

        buttonAnswer1.setOnClickListener(answerClickListener);
        buttonAnswer2.setOnClickListener(answerClickListener);
        buttonAnswer3.setOnClickListener(answerClickListener);
        buttonAnswer4.setOnClickListener(answerClickListener);

        nextQuestionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextTurn();
            }
        });
    }

    private void loadQuestions() {
        QuizDatabase db = QuizDatabase.getInstance(this);
        List<Question> allQuestions = db.questionDao().getAllQuestions();
        Collections.shuffle(allQuestions);
        questions = allQuestions.subList(0, Math.min(questionCount, allQuestions.size()));
    }

    private void prepopulateDatabase(QuizDatabase db) {
        Question[] questionsArray = new Question[] {
                new Question("What is the capital of France?", "Paris", "Berlin", "Madrid", "Rome", "Paris"),
                new Question("What is the largest planet in our solar system?", "Earth", "Mars", "Jupiter", "Saturn", "Jupiter"),
                new Question("Who wrote 'Hamlet'?", "Charles Dickens", "William Shakespeare", "Jane Austen", "Mark Twain", "William Shakespeare"),
                new Question("What is the smallest prime number?", "0", "1", "2", "3", "2"),
                new Question("What is the square root of 64?", "6", "7", "8", "9", "8"),
                new Question("What is the chemical symbol for water?", "HO", "OH", "H2O", "O2H", "H2O"),
                new Question("What is the capital of Italy?", "Paris", "Berlin", "Rome", "Madrid", "Rome"),
                new Question("What is the speed of light?", "299,792 km/s", "300,000 km/s", "150,000 km/s", "1,000 km/s", "299,792 km/s"),
                new Question("Who painted the Mona Lisa?", "Vincent van Gogh", "Claude Monet", "Leonardo da Vinci", "Pablo Picasso", "Leonardo da Vinci"),
                new Question("What is the tallest mountain in the world?", "K2", "Kangchenjunga", "Lhotse", "Mount Everest", "Mount Everest"),
                new Question("What is the capital of Japan?", "Beijing", "Seoul", "Tokyo", "Bangkok", "Tokyo"),
                new Question("Who developed the theory of relativity?", "Isaac Newton", "Galileo Galilei", "Nikola Tesla", "Albert Einstein", "Albert Einstein"),
                new Question("What is the hardest natural substance?", "Gold", "Iron", "Diamond", "Platinum", "Diamond"),
                new Question("What is the chemical symbol for gold?", "Au", "Ag", "Pb", "Pt", "Au"),
                new Question("What is the capital of Australia?", "Sydney", "Melbourne", "Canberra", "Perth", "Canberra"),
                new Question("What is the longest river in the world?", "Amazon River", "Nile River", "Yangtze River", "Mississippi River", "Nile River"),
                new Question("Who wrote 'Pride and Prejudice'?", "Emily Bronte", "Charlotte Bronte", "Jane Austen", "Mary Shelley", "Jane Austen"),
                new Question("What is the smallest country in the world?", "Monaco", "San Marino", "Liechtenstein", "Vatican City", "Vatican City"),
                new Question("What is the largest ocean in the world?", "Atlantic Ocean", "Indian Ocean", "Southern Ocean", "Pacific Ocean", "Pacific Ocean"),
                new Question("What is the capital of Canada?", "Toronto", "Vancouver", "Montreal", "Ottawa", "Ottawa"),
                new Question("Who discovered penicillin?", "Marie Curie", "Alexander Fleming", "Isaac Newton", "Louis Pasteur", "Alexander Fleming"),
                new Question("What is the chemical symbol for sodium?", "Na", "S", "Cl", "K", "Na"),
                new Question("What is the largest desert in the world?", "Sahara Desert", "Gobi Desert", "Arabian Desert", "Kalahari Desert", "Sahara Desert"),
                new Question("What is the capital of Russia?", "Moscow", "Saint Petersburg", "Novosibirsk", "Yekaterinburg", "Moscow"),
                new Question("Who wrote '1984'?", "Aldous Huxley", "George Orwell", "Ray Bradbury", "Philip K. Dick", "George Orwell"),
                new Question("What is the boiling point of water?", "50°C", "100°C", "200°C", "300°C", "100°C"),
                new Question("What is the capital of China?", "Seoul", "Tokyo", "Beijing", "Bangkok", "Beijing"),
                new Question("Who developed the first successful airplane?", "Thomas Edison", "Nikola Tesla", "Wright Brothers", "Henry Ford", "Wright Brothers"),
                new Question("What is the chemical symbol for iron?", "Fe", "Ir", "In", "I", "Fe"),
                new Question("What is the capital of Germany?", "Vienna", "Berlin", "Zurich", "Brussels", "Berlin"),
                new Question("What is the largest mammal in the world?", "Elephant", "Blue Whale", "Giraffe", "Hippopotamus", "Blue Whale"),
                new Question("Who wrote 'The Odyssey'?", "Socrates", "Plato", "Homer", "Aristotle", "Homer"),
                new Question("What is the speed of sound?", "343 m/s", "299,792 m/s", "150 m/s", "1,000 m/s", "343 m/s"),
                new Question("What is the capital of India?", "Islamabad", "Colombo", "Kathmandu", "New Delhi", "New Delhi"),
                new Question("Who developed the telephone?", "Alexander Graham Bell", "Thomas Edison", "Nikola Tesla", "Samuel Morse", "Alexander Graham Bell"),
                new Question("What is the chemical symbol for carbon?", "Ca", "C", "Cr", "Co", "C"),
                new Question("What is the capital of Brazil?", "São Paulo", "Rio de Janeiro", "Brasília", "Salvador", "Brasília"),
                new Question("What is the smallest planet in our solar system?", "Earth", "Mars", "Mercury", "Venus", "Mercury"),
                new Question("Who wrote 'To Kill a Mockingbird'?", "Harper Lee", "J.D. Salinger", "F. Scott Fitzgerald", "Ernest Hemingway", "Harper Lee"),
                new Question("What is the chemical symbol for oxygen?", "Ox", "O", "On", "Og", "O"),
                new Question("What is the capital of Spain?", "Madrid", "Barcelona", "Valencia", "Seville", "Madrid"),
                new Question("Who discovered gravity?", "Albert Einstein", "Isaac Newton", "Galileo Galilei", "Nikola Tesla", "Isaac Newton"),
                new Question("What is the largest island in the world?", "Madagascar", "Greenland", "Borneo", "New Guinea", "Greenland"),
                new Question("What is the chemical symbol for hydrogen?", "Hy", "H", "Hg", "Hd", "H"),
                new Question("What is the capital of the United States?", "New York", "Los Angeles", "Chicago", "Washington D.C.", "Washington D.C."),
                new Question("Who painted the Sistine Chapel?", "Leonardo da Vinci", "Raphael", "Michelangelo", "Donatello", "Michelangelo"),
                new Question("What is the deepest ocean in the world?", "Atlantic Ocean", "Indian Ocean", "Arctic Ocean", "Pacific Ocean", "Pacific Ocean"),
                new Question("What is the chemical symbol for nitrogen?", "N", "Ni", "Ne", "Nn", "N"),
                new Question("What is the capital of Argentina?", "Santiago", "Lima", "Buenos Aires", "Bogotá", "Buenos Aires")
        };

        db.questionDao().insertAll(questionsArray);
    }

    private void displayQuestion() {
        if (currentQuestionIndex < questions.size()) {
            Question currentQuestion = questions.get(currentQuestionIndex);
            questionTextView.setText(currentQuestion.getQuestionText());
            buttonAnswer1.setText(currentQuestion.getOption1());
            buttonAnswer2.setText(currentQuestion.getOption2());
            buttonAnswer3.setText(currentQuestion.getOption3());
            buttonAnswer4.setText(currentQuestion.getOption4());
            resetButtonColors();
            nextQuestionButton.setVisibility(View.GONE);
        } else {
            endGame();
        }
    }

    private void resetButtonColors() {
        int buttonColor = getResources().getColor(R.color.buttonColor);
        buttonAnswer1.setBackgroundColor(buttonColor);
        buttonAnswer2.setBackgroundColor(buttonColor);
        buttonAnswer3.setBackgroundColor(buttonColor);
        buttonAnswer4.setBackgroundColor(buttonColor);
    }

    private void checkAnswer(String selectedAnswer) {
        Question currentQuestion = questions.get(currentQuestionIndex);
        if (currentQuestion.getCorrectAnswer().equals(selectedAnswer)) {
            if (currentPlayer == 1) {
                player1Score++;
            } else {
                player2Score++;
            }
        }
    }

    private void showNextPlayerToast() {
        currentPlayer = 2;
        Toast.makeText(this, "Player 2, it's your turn!", Toast.LENGTH_SHORT).show();
        playerTurnTextView.setText(String.format("Player %d's Turn", currentPlayer));
    }

    private void showCorrectAnswer() {
        Question currentQuestion = questions.get(currentQuestionIndex);
        int correctAnswerColor = getResources().getColor(R.color.correctAnswerColor);
        if (buttonAnswer1.getText().toString().equals(currentQuestion.getCorrectAnswer())) {
            buttonAnswer1.setBackgroundColor(correctAnswerColor);
        } else if (buttonAnswer2.getText().toString().equals(currentQuestion.getCorrectAnswer())) {
            buttonAnswer2.setBackgroundColor(correctAnswerColor);
        } else if (buttonAnswer3.getText().toString().equals(currentQuestion.getCorrectAnswer())) {
            buttonAnswer3.setBackgroundColor(correctAnswerColor);
        } else if (buttonAnswer4.getText().toString().equals(currentQuestion.getCorrectAnswer())) {
            buttonAnswer4.setBackgroundColor(correctAnswerColor);
        }
        nextQuestionButton.setVisibility(View.VISIBLE);
    }

    private void nextTurn() {
        currentPlayer = 1;
        currentQuestionIndex++;
        displayQuestion();
        playerTurnTextView.setText(String.format("Player %d's Turn", currentPlayer));
    }

    private void endGame() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Game Over");
        builder.setMessage(String.format("Player 1: %d points\nPlayer 2: %d points", player1Score, player2Score));
        builder.setPositiveButton("Restart", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                restartGame();
            }
        });
        builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void restartGame() {
        player1Score = 0;
        player2Score = 0;
        currentQuestionIndex = 0;
        currentPlayer = 1;
        displayQuestion();
    }
}
