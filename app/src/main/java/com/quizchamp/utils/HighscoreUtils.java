package com.quizchamp.utils;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quizchamp.model.Highscore;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HighscoreUtils {
    private static final String HIGHSCORES_FILE = "highscore.json";
    private static final int MAX_HIGHSCORES = 10;

    public static List<Highscore> loadHighscores(Context context) {
        List<Highscore> highscores = new ArrayList<>();
        File file = new File(context.getFilesDir(), HIGHSCORES_FILE);

        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Type listType = new TypeToken<List<Highscore>>() {
                }.getType();
                highscores = new Gson().fromJson(reader, listType);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try (InputStreamReader reader = new InputStreamReader(context.getAssets().open(HIGHSCORES_FILE))) {
                Type listType = new TypeToken<List<Highscore>>() {
                }.getType();
                highscores = new Gson().fromJson(reader, listType);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Collections.sort(highscores, (h1, h2) -> Integer.compare(h2.getScore(), h1.getScore()));
        return highscores;
    }

    public static void saveHighscores(Context context, List<Highscore> highscores) {
        File file = new File(context.getFilesDir(), HIGHSCORES_FILE);
        try (FileWriter writer = new FileWriter(file)) {
            new Gson().toJson(highscores, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateHighscores(Context context, Highscore newHighscore) {
        List<Highscore> highscores = loadHighscores(context);
        highscores.add(newHighscore);
        Collections.sort(highscores, (h1, h2) -> Integer.compare(h2.getScore(), h1.getScore()));
        if (highscores.size() > MAX_HIGHSCORES) {
            highscores = highscores.subList(0, MAX_HIGHSCORES);
        }
        saveHighscores(context, highscores);

    }
}