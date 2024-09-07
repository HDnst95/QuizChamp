package com.quizchamp.activity.menu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.FirebaseFirestore;
import com.quizchamp.R;
import com.quizchamp.activity.view.ViewHighscoresActivity;
import com.quizchamp.activity.modus.HighscoreActivity;
import com.quizchamp.activity.modus.MultiPlayerActivity;
import com.quizchamp.activity.modus.MultiPlayerOnDeviceActivity;
import com.quizchamp.activity.modus.SinglePlayerActivity;
import com.quizchamp.model.DifficultyLevel;
import com.quizchamp.model.Question;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainMenuActivity extends AppCompatActivity {

    private Button savePlayerNameButton;
    private Button singlePlayerButton;
    private Button highscoreButton;
    private Button multiPlayerButton;
    private Button multiPlayerOnDeviceButton;
    private Button viewHighscoreButton;
    private Button logoutButton;
    private EditText playerNameEditText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<Question> questionsList = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private String playerName;
    private int anzahlFragen;
    private boolean hostOrJoin = false;

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

        logoutButton = findViewById(R.id.logoutButton);

        sharedPreferences = getSharedPreferences("QuizChampPrefs", MODE_PRIVATE);
        // Load player name from SharedPreferences
        playerName = sharedPreferences.getString("PLAYER_NAME", "");
        if (playerName.isEmpty()) {
            showInfoDialog();
        }
        playerNameEditText.setText(playerName);



        savePlayerNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (playerNameEditText.getText().toString().isEmpty()) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    Toast.makeText(MainMenuActivity.this, "Spielername ist leer bzw. wurde gelöscht", Toast.LENGTH_SHORT).show();
                } else {
                    playerName = playerNameEditText.getText().toString();
                    editor.putString("PLAYER_NAME", playerNameEditText.getText().toString());
                    editor.apply();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    Toast.makeText(MainMenuActivity.this, "Spielername gespeichert", Toast.LENGTH_SHORT).show();
                }
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
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        multiPlayerButton.setBackgroundColor(getResources().getColor(R.color.disabledButtonTextColor));
        multiPlayerButton.setEnabled(false);

        checkUser();
        zaehleFragen();
    }

    private void zaehleFragen() {
        db.collection("questions").count().get(AggregateSource.SERVER).addOnCompleteListener(new OnCompleteListener<AggregateQuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<AggregateQuerySnapshot> task) {
                if (task.isSuccessful()) {
                    AggregateQuerySnapshot snapshot = task.getResult();
                    anzahlFragen = (int) snapshot.getCount();
                } else {
                    Toast.makeText(MainMenuActivity.this, "Fehler beim Zählen der Fragen", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Wichtiger Hinweis");
        builder.setMessage("Hallo! Schön, dass du QuizChamp nutzt. Bitte beachte, dass QuizChamp noch in der Entwicklung ist und es zu Fehlern kommen kann. Falls du Fehler findest oder Verbesserungsvorschläge hast, gib mir bitte Bescheid! " + "\n\n" + "Für einige Spielmodi wird dein Spielername verwendet. Bitte trage diesen ein, um personalisierte Inhalte ordnungsgemäß anzuzeigen. " + "\n\n" + "Viel Spaß beim Spielen!");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        Identity.getSignInClient(this).signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // Update UI after sign out
                Toast.makeText(MainMenuActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainMenuActivity.this, LoginRegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
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
        final Button hostButton = dialogView.findViewById(R.id.hostButton);
        final Button joinButton = dialogView.findViewById(R.id.joinButton);
        final TextView gameIdTextView = dialogView.findViewById(R.id.gameIdTextView);
        final Button copyGameIdButton = dialogView.findViewById(R.id.copyGameIdButton);
        final EditText matchIdField = dialogView.findViewById(R.id.matchIdField);

        if (mode.equals("SinglePlayer")) {
            comStrengthSeekBar.setVisibility(View.VISIBLE);
            comStrengthValue.setVisibility(View.VISIBLE);
            seekBarDescription.setVisibility(View.VISIBLE);
            comStrengthSeekBar.setMax(4);
            comStrengthSeekBar.setProgress(0);
            comStrengthValue.setText(DifficultyLevel.VERY_EASY.toString());
            questionsField.setVisibility(View.VISIBLE);
            questionsField.setText("10");
            comStrengthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    DifficultyLevel[] difficultyLevels = {DifficultyLevel.VERY_EASY, DifficultyLevel.EASY, DifficultyLevel.MEDIUM, DifficultyLevel.HARD, DifficultyLevel.VERY_HARD};
                    comStrengthValue.setText(difficultyLevels[progress].toString());
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        } else if (mode.equals("Multiplayer")) {
            playerNameField.setVisibility(View.VISIBLE);
            hostButton.setVisibility(View.VISIBLE);
            joinButton.setVisibility(View.VISIBLE);
            hostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gameIdTextView.setVisibility(View.VISIBLE);
                    copyGameIdButton.setVisibility(View.VISIBLE);
                    matchIdField.setVisibility(View.GONE);
                    hostOrJoin = true;
                    // Generate Game ID and display it
                    String gameId = generateGameId();
                    gameIdTextView.setText(gameId);
                    copyGameIdButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            copyToClipboard(gameId);
                        }
                    });
                }
            });
            joinButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gameIdTextView.setVisibility(View.GONE);
                    copyGameIdButton.setVisibility(View.GONE);
                    matchIdField.setVisibility(View.VISIBLE);
                    hostOrJoin = true;
                }
            });
        } else if (mode.equals("MultiplayerOnDevice")) {
            comStrengthSeekBar.setVisibility(View.GONE);
            comStrengthValue.setVisibility(View.GONE);
            seekBarDescription.setVisibility(View.GONE);
            questionsField.setVisibility(View.VISIBLE);
            questionsField.setText("10");
            playerNameField.setVisibility(View.GONE);
            playerNameMulti1Field.setVisibility(View.VISIBLE);
            playerNameMulti1Field.setText(playerNameEditText.getText().toString());
            playerNameMulti2Field.setVisibility(View.VISIBLE);
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
                        String matchId = "";
                        boolean isHost = false;
                        DifficultyLevel comStrength = DifficultyLevel.VERY_EASY;
                        if (mode.equals("SinglePlayer")) {
                            questionCount = questionsField.getText().toString().isEmpty() ? 0 : Integer.parseInt(questionsField.getText().toString());
                            comStrength = DifficultyLevel.values()[comStrengthSeekBar.getProgress()];
                            playerName = playerNameField.getText().toString();
                        } else if (mode.equals("Multiplayer")) {
                            if (hostOrJoin) {
                                if (gameIdTextView.getVisibility() == View.VISIBLE && copyGameIdButton.getVisibility() == View.VISIBLE) {
                                    if (!gameIdTextView.getText().toString().isEmpty() && !playerNameField.getText().toString().isEmpty()) {
                                        copyToClipboard(gameIdTextView.getText().toString());
                                        Toast.makeText(MainMenuActivity.this, "Match ID wurde kopiert", Toast.LENGTH_SHORT).show();
                                        playerName = playerNameField.getText().toString();
                                        matchId = gameIdTextView.getText().toString();
                                        isHost = true;
                                    } else {
                                        Toast.makeText(MainMenuActivity.this, "Bitte fülle alle Felder aus", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                } else if (matchIdField.getVisibility() == View.VISIBLE && playerNameField.getVisibility() == View.VISIBLE) {
                                    if (!matchIdField.getText().toString().isEmpty() && !playerNameField.getText().toString().isEmpty()) {
                                        playerName = playerNameField.getText().toString();
                                        matchId = matchIdField.getText().toString();
                                        isHost = false;
                                    } else {
                                        Toast.makeText(MainMenuActivity.this, "Bitte fülle alle Felder aus", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                            } else {
                                Toast.makeText(MainMenuActivity.this, "Bitte wähle aus, ob du ein Spiel hosten oder einem Spiel beitreten möchtest", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } else if (mode.equals("MultiplayerOnDevice")) {
                            playerMultiName1 = playerNameMulti1Field.getText().toString();
                            playerMultiName2 = playerNameMulti2Field.getText().toString();
                            questionCount = questionsField.getText().toString().isEmpty() ? 0 : Integer.parseInt(questionsField.getText().toString());
                        } else if (mode.equals("Highscore")) {
                            playerName = playerNameField.getText().toString();
                        }
                        startGameActivity(mode, comStrength, playerName, questionCount, playerMultiName1, playerMultiName2, matchId, isHost);
                    }
                })
                .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private String generateGameId() {
        // Generate a unique Game ID
        return UUID.randomUUID().toString();
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Game ID", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Game ID copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private void startGameActivity(String mode, DifficultyLevel comStrength, String playerName, int questionCount, String playerMultiName1, String playerMultiName2, String matchId, boolean isHost) {
        Intent intent;
        switch (mode) {
            case "SinglePlayer":
                intent = new Intent(MainMenuActivity.this, SinglePlayerActivity.class);
                intent.putExtra("QUESTION_COUNT", questionCount);
                intent.putExtra("DIFFICULTY", comStrength);
                intent.putExtra("PLAYER_NAME", playerName);
                intent.putExtra("ANZAHL_DB", anzahlFragen);
                break;
            case "Multiplayer":
                intent = new Intent(MainMenuActivity.this, MultiPlayerActivity.class);
                intent.putExtra("PLAYER_NAME", playerName);
                intent.putExtra("ANZAHL_DB", anzahlFragen);
                intent.putExtra("MATCH_ID", matchId);
                intent.putExtra("IS_HOST", isHost);
                break;
            case "MultiplayerOnDevice":
                intent = new Intent(MainMenuActivity.this, MultiPlayerOnDeviceActivity.class);
                intent.putExtra("PLAYER_1", playerMultiName1);
                intent.putExtra("PLAYER_2", playerMultiName2);
                intent.putExtra("QUESTION_COUNT", questionCount);
                intent.putExtra("ANZAHL_DB", anzahlFragen);
                break;
            case "Highscore":
                intent = new Intent(MainMenuActivity.this, HighscoreActivity.class);
                intent.putExtra("PLAYER_NAME", playerName);
                intent.putExtra("ANZAHL_DB", anzahlFragen);
                break;
            default:
                return;
        }
        startActivity(intent);
        finish();
    }
}