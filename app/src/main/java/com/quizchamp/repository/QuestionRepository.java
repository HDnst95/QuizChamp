// QuestionRepository.java
package com.quizchamp.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.quizchamp.database.DatabaseHelper;
import com.quizchamp.model.Question;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuestionRepository {
    public QuestionRepository(Context context) {
        dbHelper = new DatabaseHelper(context.getApplicationContext());
    }

    private static QuestionRepository instance;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private boolean isOpen;

    public static synchronized QuestionRepository getInstance(Context context) {
        if (instance == null) {
            instance = new QuestionRepository(context);
        }
        return instance;
    }

    public void open() {
        if (!isOpen) {
            db = dbHelper.getWritableDatabase();
            isOpen = true;
        }
    }

    public void close() {
        if (isOpen) {
            dbHelper.close();
            isOpen = false;
        }
    }

    public void clearQuestionsTable() {
        db.execSQL("DELETE FROM " + DatabaseHelper.TABLE_QUESTIONS);
        db.execSQL("DELETE FROM sqlite_sequence WHERE name='" + DatabaseHelper.TABLE_QUESTIONS + "'");
    }

    public void insertQuestionsFromJson(String json) {
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_QUESTION_TEXT, jsonObject.getString("questionText"));
                values.put(DatabaseHelper.COLUMN_OPTION_A, jsonObject.getString("optionA"));
                values.put(DatabaseHelper.COLUMN_OPTION_B, jsonObject.getString("optionB"));
                values.put(DatabaseHelper.COLUMN_OPTION_C, jsonObject.getString("optionC"));
                values.put(DatabaseHelper.COLUMN_OPTION_D, jsonObject.getString("optionD"));
                values.put(DatabaseHelper.COLUMN_CORRECT_ANSWER, jsonObject.getString("correctAnswer"));
                db.insert(DatabaseHelper.TABLE_QUESTIONS, null, values);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Question getQuestionById(int id) {
        Question question = null;
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_QUESTIONS + " WHERE " + DatabaseHelper.COLUMN_ID + " = " + id, null);
        if (cursor != null && cursor.moveToFirst()) {
            int questionIdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
            int questionTextIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_QUESTION_TEXT);
            int optionAIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_OPTION_A);
            int optionBIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_OPTION_B);
            int optionCIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_OPTION_C);
            int optionDIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_OPTION_D);
            int correctAnswerIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CORRECT_ANSWER);

            if (questionTextIndex >= 0 && optionAIndex >= 0 && optionBIndex >= 0 && optionCIndex >= 0 && optionDIndex >= 0 && correctAnswerIndex >= 0) {
                question = new Question();
                question.setId(cursor.getInt(questionIdIndex));
                question.setQuestionText(cursor.getString(questionTextIndex));
                question.setOptionA(cursor.getString(optionAIndex));
                question.setOptionB(cursor.getString(optionBIndex));
                question.setOptionC(cursor.getString(optionCIndex));
                question.setOptionD(cursor.getString(optionDIndex));
                question.setCorrectAnswer(cursor.getString(correctAnswerIndex));
            }
            cursor.close();
        }
        return question;
    }

    public int getQuestionCount() {
        int count = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_QUESTIONS, null);
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public Question getRandomQuestion() {
        Question question = null;
        int questionCount = getQuestionCount();
        if (questionCount > 0) {
            int randomIndex = new Random().nextInt(questionCount);
            Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_QUESTIONS + " LIMIT 1 OFFSET " + randomIndex, null);
            if (cursor != null && cursor.moveToFirst()) {
                int questionIdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
                int questionTextIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_QUESTION_TEXT);
                int optionAIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_OPTION_A);
                int optionBIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_OPTION_B);
                int optionCIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_OPTION_C);
                int optionDIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_OPTION_D);
                int correctAnswerIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CORRECT_ANSWER);

                if (questionTextIndex >= 0 && optionAIndex >= 0 && optionBIndex >= 0 && optionCIndex >= 0 && optionDIndex >= 0 && correctAnswerIndex >= 0) {
                    question = new Question();
                    question.setId(cursor.getInt(questionIdIndex));
                    question.setQuestionText(cursor.getString(questionTextIndex));
                    question.setOptionA(cursor.getString(optionAIndex));
                    question.setOptionB(cursor.getString(optionBIndex));
                    question.setOptionC(cursor.getString(optionCIndex));
                    question.setOptionD(cursor.getString(optionDIndex));
                    question.setCorrectAnswer(cursor.getString(correctAnswerIndex));
                }
                cursor.close();
            }
            return question;
        } else {
            //TODO: Handle no questions in the database
            return null;
        }
    }
}