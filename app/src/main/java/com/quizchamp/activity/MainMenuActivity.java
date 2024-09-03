package com.quizchamp.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.quizchamp.R;
import com.quizchamp.model.Question;

import java.util.ArrayList;
import java.util.List;

public class MainMenuActivity extends AppCompatActivity {

    private Button savePlayerNameButton;
    private Button singlePlayerButton;
    private Button highscoreButton;
    private Button multiPlayerButton;
    private Button multiPlayerOnDeviceButton;
    private Button viewHighscoreButton;
    private EditText playerNameEditText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<Question> questionsList = new ArrayList<>();
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        savePlayerNameButton = findViewById(R.id.savePlayerNameButton);
        playerNameEditText = findViewById(R.id.playerNameEditText);


        singlePlayerButton = findViewById(R.id.singlePlayerButton);

        multiPlayerButton = findViewById(R.id.multiPlayerButton);
        multiPlayerOnDeviceButton = findViewById(R.id.multiPlayerOnScreenButton);

        highscoreButton = findViewById(R.id.highscoreButton);
        viewHighscoreButton = findViewById(R.id.viewHighscoreButton);

        sharedPreferences = getSharedPreferences("QuizChampPrefs", MODE_PRIVATE);
        // Load player name from SharedPreferences
        String savedPlayerName = sharedPreferences.getString("PLAYER_NAME", "");
        playerNameEditText.setText(savedPlayerName);


        savePlayerNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("PLAYER_NAME", playerNameEditText.getText().toString());
                editor.apply();
            }
        });
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
        multiPlayerOnDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog("MultiplayerOnDevice");
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
        multiPlayerOnDeviceButton.setEnabled(false);
        multiPlayerOnDeviceButton.setTextColor(getResources().getColor(R.color.disabledButtonTextColor));

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
        final EditText playerNameMulti1Field = dialogView.findViewById(R.id.playerNameMulti1Field);
        final EditText playerNameMulti2Field = dialogView.findViewById(R.id.playerNameMulti2Field);
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
            questionsField.setVisibility(View.GONE);
            playerNameField.setVisibility(View.VISIBLE);
        } else if (mode.equals("MultiplayerOnDevice")) {
            comStrengthSeekBar.setVisibility(View.GONE);
            comStrengthValue.setVisibility(View.GONE);
            seekBarDescription.setVisibility(View.GONE);
            questionsField.setVisibility(View.VISIBLE);
            playerNameField.setVisibility(View.VISIBLE);
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
                        String playerName = "";
                        String playerMultiName1 = "";
                        String playerMultiName2 = "";
                        int questionCount = 0;
                        int comStrength = 0;
                        if (mode.equals("SinglePlayer")) {
                            comStrength = comStrengthSeekBar.getProgress();
                        } else if (mode.equals("Multiplayer")) {
                            questionCount = questionsField.getText().toString().isEmpty() ? 0 : Integer.parseInt(questionsField.getText().toString());
                        } else if (mode.equals("MultiplayerOnDevice")) {
                            playerMultiName1 = playerNameMulti1Field.getText().toString();
                            playerMultiName2 = playerNameMulti2Field.getText().toString();
                            questionCount = questionsField.getText().toString().isEmpty() ? 0 : Integer.parseInt(questionsField.getText().toString());
                        } else if (mode.equals("Highscore")) {
                            playerName = playerNameField.getText().toString();
                        }
                        startGameActivity(mode, comStrength, playerName, questionCount, playerMultiName1, playerMultiName2);
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

    private void startGameActivity(String mode, int comStrength, String playerName, int questionCount, String playerMultiName1, String playerMultiName2) {
        Intent intent;
        switch (mode) {
            case "SinglePlayer":
                intent = new Intent(MainMenuActivity.this, SinglePlayerActivity.class);
                intent.putExtra("DIFFICULTY", comStrength);
                intent.putExtra("PLAYER_NAME", playerName);
                break;
            case "Multiplayer":
                intent = new Intent(MainMenuActivity.this, MatchmakingActivity.class);
                intent.putExtra("PLAYER_NAME", playerName);
                break;
            case "MultiplayerOnDevice":
                intent = new Intent(MainMenuActivity.this, MultiPlayerOnDeviceActivity.class);
                intent.putExtra("PLAYER_1", playerMultiName1);
                intent.putExtra("PLAYER_2", playerMultiName2);
                intent.putExtra("QUESTION_COUNT", questionCount);
                break;
            case "Highscore":
                intent = new Intent(MainMenuActivity.this, HighscoreActivity.class);
                intent.putExtra("PLAYER_NAME", playerName);
                break;
            default:
                return;
        }
        startActivity(intent);
        finish();
    }
}