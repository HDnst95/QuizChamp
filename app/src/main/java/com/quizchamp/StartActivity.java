package com.quizchamp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.quizchamp.R;

public class StartActivity extends AppCompatActivity {

    private EditText questionCountEditText;
    private EditText namePlayer1;
    private EditText namePlayer2;
    private MaterialButton startGameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        questionCountEditText = findViewById(R.id.questionCountEditText);
        namePlayer1 = findViewById(R.id.namePlayer1);
        namePlayer2 = findViewById(R.id.namePlayer2);
        startGameButton = findViewById(R.id.startGameButton);

        // Check if player names are passed from MainActivity
        Intent intent = getIntent();
        if (intent.hasExtra("NAME_PLAYER_1")) {
            namePlayer1.setText(intent.getStringExtra("NAME_PLAYER_1"));
        }
        if (intent.hasExtra("NAME_PLAYER_2")) {
            namePlayer2.setText(intent.getStringExtra("NAME_PLAYER_2"));
        }

        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String questionCountStr = questionCountEditText.getText().toString();
                if (!questionCountStr.isEmpty()) {
                    int questionCount = Integer.parseInt(questionCountStr);
                    if (questionCount > 0) {
                            if (namePlayer1.getText().toString().isEmpty()) {
                                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                                intent.putExtra("QUESTION_COUNT", questionCount);
                                intent.putExtra("NAME_PLAYER_1", "Spieler 1");
                                intent.putExtra("NAME_PLAYER_2", namePlayer2.getText().toString());
                                startActivity(intent);
                                Toast.makeText(StartActivity.this, R.string.empty_player_names, Toast.LENGTH_LONG).show();
                                finish();
                            } else if (namePlayer2.getText().toString().isEmpty()) {
                                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                                intent.putExtra("QUESTION_COUNT", questionCount);
                                intent.putExtra("NAME_PLAYER_1", namePlayer1.getText().toString());
                                intent.putExtra("NAME_PLAYER_2", "Spieler 2");
                                startActivity(intent);
                                Toast.makeText(StartActivity.this, R.string.empty_player_names, Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                                intent.putExtra("QUESTION_COUNT", questionCount);
                                intent.putExtra("NAME_PLAYER_1", namePlayer1.getText().toString());
                                intent.putExtra("NAME_PLAYER_2", namePlayer2.getText().toString());
                                startActivity(intent);
                                finish();
                            }
                        }
                } else {
                            Toast.makeText(StartActivity.this, R.string.enter_question_count, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
