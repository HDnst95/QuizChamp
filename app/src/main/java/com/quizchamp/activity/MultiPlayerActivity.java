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
        player1ScoreTextView = findViewById(R.id.player1ScoreTextView);
        player2ScoreTextView = findViewById(R.id.player2ScoreTextView);
        player1NameTextView = findViewById(R.id.player1NameTextView);
        player2NameTextView = findViewById(R.id.player2NameTextView);

    }

}