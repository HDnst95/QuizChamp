package com.quizchamp.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.quizchamp.R;
import com.quizchamp.model.Question;

import java.util.ArrayList;
import java.util.List;

public class MainMenuActivity extends AppCompatActivity {

    private Button singlePlayerButton;
    private Button highscoreButton;
    private Button multiPlayerButton;
    private Button viewHighscoreButton;
    private EditText playerNameEditText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<Question> questionsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        singlePlayerButton = findViewById(R.id.singlePlayerButton);
        highscoreButton = findViewById(R.id.highscoreButton);
        multiPlayerButton = findViewById(R.id.multiPlayerButton);
        viewHighscoreButton = findViewById(R.id.viewHighscoreButton);
        playerNameEditText = findViewById(R.id.playerNameEditText);
        mAuth = FirebaseAuth.getInstance();


        singlePlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog("SinglePlayer");
            }
        });

        multiPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog("Multiplayer");
            }
        });

        highscoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog("Highscore");
            }
        });

        viewHighscoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, ViewHighscoresActivity.class);
                startActivity(intent);
            }
        });


        singlePlayerButton.setEnabled(false);
        singlePlayerButton.setTextColor(getResources().getColor(R.color.disabledButtonTextColor));
        multiPlayerButton.setEnabled(false);
        multiPlayerButton.setTextColor(getResources().getColor(R.color.disabledButtonTextColor));

        checkUser();
    }

    private void checkUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(MainMenuActivity.this, LoginRegisterActivity.class);
            startActivity(intent);
            finish();
        }
    }

    // MainMenuActivity.java
    private void showInputDialog(final String mode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_input, null);
        builder.setView(dialogView);

        final SeekBar comStrengthSeekBar = dialogView.findViewById(R.id.comStrengthSeekBar);
        final TextView comStrengthValue = dialogView.findViewById(R.id.comStrengthValue);
        final EditText questionsField = dialogView.findViewById(R.id.questionsField);
        final EditText playerNameField = dialogView.findViewById(R.id.playerNameField);
        playerNameField.setText(playerNameEditText.getText().toString());
        final TextView seekBarDescription = dialogView.findViewById(R.id.seekBarDescription);

        if (mode.equals("SinglePlayer")) {
            comStrengthSeekBar.setVisibility(View.VISIBLE);
            comStrengthValue.setVisibility(View.VISIBLE);
            seekBarDescription.setVisibility(View.VISIBLE);
            comStrengthSeekBar.setMax(4);
            comStrengthValue.setText("sehr einfach");
            questionsField.setVisibility(View.GONE);
            comStrengthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    String[] difficultyLevels = {"sehr einfach", "einfach", "mittel", "schwer", "sehr schwer"};
                    comStrengthValue.setText(difficultyLevels[progress]);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        } else if (mode.equals("Multiplayer")) {
            comStrengthSeekBar.setVisibility(View.GONE);
            comStrengthValue.setVisibility(View.GONE);
            seekBarDescription.setVisibility(View.GONE);
            questionsField.setVisibility(View.VISIBLE);
            playerNameField.setVisibility(View.GONE);
        } else if (mode.equals("Highscore")) {
            comStrengthSeekBar.setVisibility(View.GONE);
            comStrengthValue.setVisibility(View.GONE);
            seekBarDescription.setVisibility(View.GONE);
            questionsField.setVisibility(View.GONE);
            playerNameField.setVisibility(View.VISIBLE);
        }

        builder.setTitle(mode)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String inputData = "";
                        if (mode.equals("SinglePlayer")) {
                            inputData = comStrengthValue.getText().toString();
                        } else if (mode.equals("Multiplayer")) {
                            inputData = questionsField.getText().toString();
                        } else if (mode.equals("Highscore")) {
                            inputData = playerNameField.getText().toString();
                        }
                        startGameActivity(mode, inputData);
                    }
                })
                .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void startGameActivity(String mode, String inputData) {
        Intent intent;
        switch (mode) {
            case "SinglePlayer":
                intent = new Intent(MainMenuActivity.this, SinglePlayerActivity.class);
                intent.putExtra("input_data", inputData);
                break;
            case "Multiplayer":
                intent = new Intent(MainMenuActivity.this, MultiPlayerActivity.class);
                intent.putExtra("input_data", inputData);
                break;
            case "Highscore":
                intent = new Intent(MainMenuActivity.this, HighscoreActivity.class);
                intent.putExtra("input_data", inputData);
                break;
            default:
                return;
        }
        startActivity(intent);
        finish();
    }
}