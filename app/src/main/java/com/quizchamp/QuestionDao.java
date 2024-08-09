package com.quizchamp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface QuestionDao {
    @Query("SELECT * FROM question")
    List<Question> getAllQuestions();

    @Insert
    void insertAll(Question... questions);
}
