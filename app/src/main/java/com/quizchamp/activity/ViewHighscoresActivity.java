package com.quizchamp.activity;

import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.quizchamp.R;
import com.quizchamp.model.Highscore;
import com.quizchamp.utils.HighscoreUtils;

import java.util.List;

public class ViewHighscoresActivity extends AppCompatActivity {

    private TableLayout highscoresTableLayout;
    private List<Highscore> highscores;
    private int selectedRowIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_highscores);

        highscoresTableLayout = findViewById(R.id.highscoresTableLayout);
        MaterialButton buttonBack = findViewById(R.id.buttonBack);
        MaterialButton buttonDelete = findViewById(R.id.buttonDelete);
        MaterialButton buttonDeleteAll = findViewById(R.id.buttonDeleteAll);

        loadHighscores();

        buttonBack.setOnClickListener(v -> finish());

        buttonDelete.setOnClickListener(v -> {
            if (selectedRowIndex != -1) {
                highscores.remove(selectedRowIndex);
                HighscoreUtils.saveHighscores(this, highscores);
                loadHighscores();
                selectedRowIndex = -1;
            }
        });

        buttonDeleteAll.setOnClickListener(v -> {
            highscores.clear();
            HighscoreUtils.saveHighscores(this, highscores);
            loadHighscores();
        });
    }

    private void loadHighscores() {
        highscoresTableLayout.removeViews(1, highscoresTableLayout.getChildCount() - 1);
        highscores = HighscoreUtils.loadHighscores(this);
        for (int i = 0; i < highscores.size(); i++) {
            Highscore highscore = highscores.get(i);
            TableRow row = new TableRow(this);
            row.setOnClickListener(v -> {
                selectedRowIndex = highscoresTableLayout.indexOfChild(v) - 1;
                highlightSelectedRow();
            });

            TextView nameTextView = new TextView(this);
            nameTextView.setText(highscore.getPlayerName());
            nameTextView.setPadding(16, 16, 16, 16);
            nameTextView.setTextSize(18);
            nameTextView.setTextColor(getResources().getColor(android.R.color.black));

            TextView scoreTextView = new TextView(this);
            scoreTextView.setText(String.valueOf(highscore.getScore()));
            scoreTextView.setPadding(16, 16, 16, 16);
            scoreTextView.setTextSize(18);
            scoreTextView.setTextColor(getResources().getColor(android.R.color.black));


            TextView dateTimeTextView = new TextView(this);
            dateTimeTextView.setText(highscore.getDateTime());
            dateTimeTextView.setPadding(16, 16, 16, 16);
            dateTimeTextView.setTextSize(18);
            dateTimeTextView.setTextColor(getResources().getColor(android.R.color.black));

            row.addView(nameTextView);
            row.addView(scoreTextView);
            row.addView(dateTimeTextView);

            highscoresTableLayout.addView(row);
        }
    }

    private void highlightSelectedRow() {
        for (int i = 1; i < highscoresTableLayout.getChildCount(); i++) {
            TableRow row = (TableRow) highscoresTableLayout.getChildAt(i);
            if (i == selectedRowIndex + 1) {
                row.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
            } else {
                row.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            }
        }
    }
}