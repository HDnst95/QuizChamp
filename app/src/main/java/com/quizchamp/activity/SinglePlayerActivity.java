package com.quizchamp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.content.Intent;


import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.quizchamp.R;
import com.quizchamp.model.Question;

import java.util.List;

public class SinglePlayerActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private List<Question> questions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        Intent intent = getIntent();
        String schwierigkeit = intent.getStringExtra("input_data");
        String playerName = intent.getStringExtra("player_name");

        // Implementiere die Logik: Einzelspielermodus gegen einen Bot
        db = FirebaseFirestore.getInstance();
        fetchQuestionsFromDatabase();

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


}