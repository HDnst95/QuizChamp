package com.quizchamp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;

public class StartActivity extends AppCompatActivity {

    private EditText questionCountEditText;
    private MaterialButton startGameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        questionCountEditText = findViewById(R.id.questionCountEditText);
        startGameButton = findViewById(R.id.startGameButton);

        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String questionCountStr = questionCountEditText.getText().toString();
                if (!questionCountStr.isEmpty()) {
                    int questionCount = Integer.parseInt(questionCountStr);
                    if (questionCount > 0) {
                        Intent intent = new Intent(StartActivity.this, MainActivity.class);
                        intent.putExtra("QUESTION_COUNT", questionCount);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(StartActivity.this, "Please enter a positive number", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(StartActivity.this, "Please enter the number of questions", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
