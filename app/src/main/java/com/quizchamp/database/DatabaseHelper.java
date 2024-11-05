// DatabaseHelper.java
package com.quizchamp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static final String TABLE_QUESTIONS = "questions";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_QUESTION_TEXT = "questionText";
    public static final String COLUMN_OPTION_A = "optionA";
    public static final String COLUMN_OPTION_B = "optionB";
    public static final String COLUMN_OPTION_C = "optionC";
    public static final String COLUMN_OPTION_D = "optionD";
    public static final String COLUMN_CORRECT_ANSWER = "correctAnswer";
    private static final String DATABASE_NAME = "quizchamp.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_QUESTIONS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_QUESTION_TEXT + " TEXT, " +
                    COLUMN_OPTION_A + " TEXT, " +
                    COLUMN_OPTION_B + " TEXT, " +
                    COLUMN_OPTION_C + " TEXT, " +
                    COLUMN_OPTION_D + " TEXT, " +
                    COLUMN_CORRECT_ANSWER + " TEXT" +
                    ");";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUESTIONS);
        onCreate(db);
    }
}