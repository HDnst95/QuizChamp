package com.quizchamp;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
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
}
