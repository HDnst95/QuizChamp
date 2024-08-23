package com.quizchamp;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

public class JsonLoader {

    public static List<Question> loadQuestionsFromJson(Context context) {
        try {
            InputStream inputStream = context.getAssets().open("questions.json");
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();

            String json = new String(buffer, "UTF-8");
            Gson gson = new Gson();
            Type questionListType = new TypeToken<List<Question>>() {}.getType();
            return gson.fromJson(json, questionListType);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Highscore> loadHighscoresFromJson(Context context) {
        try (InputStreamReader reader = new InputStreamReader(context.getAssets().open("highscore.json"))) {
            Type highscoreListType = new TypeToken<List<Highscore>>() {
            }.getType();
            return new Gson().fromJson(reader, highscoreListType);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void saveHighscoresToJson(Context context, List<Highscore> highscores) {
        try (FileWriter writer = new FileWriter(context.getAssets().openFd("highscore.json").getFileDescriptor())) {
            new Gson().toJson(highscores, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}