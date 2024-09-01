package com.quizchamp.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Highscore {
    public Highscore(String playerName, int score, String dateTime) {
        this.playerName = playerName;
        this.score = score;
        this.dateTime = dateTime != null ? dateTime : LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private String playerName;
    private int score;
    private String dateTime;

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}