package com.quizchamp.model;

public class Match {
    public Match() {
        // Default constructor required for calls to DataSnapshot.getValue(Match.class)
    }

    private String matchID;
    private String player1;
    private String player2;
    private String playerName1;
    private String playerName2;
    private int player1Score;
    private int player2Score;
    private int questionID;
    private int questionNumber;
    private boolean player1Answered;
    private boolean player2Answered;
    private boolean newQuestionNeeded;
    private String turn;

    public String getMatchID() {
        return matchID;
    }

    public String getPlayerName1() {
        return playerName1;
    }

    public void setPlayerName1(String playerName1) {
        this.playerName1 = playerName1;
    }

    public void setMatchID(String matchID) {
        this.matchID = matchID;
    }

    // Getters and setters
    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public int getPlayer1Score() {
        return player1Score;
    }

    public void setPlayer1Score(int player1Score) {
        this.player1Score = player1Score;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public int getPlayer2Score() {
        return player2Score;
    }

    public void setPlayer2Score(int player2Score) {
        this.player2Score = player2Score;
    }

    public int getQuestionID() {
        return questionID;
    }

    public String getPlayerName2() {
        return playerName2;
    }

    public void setPlayerName2(String playerName2) {
        this.playerName2 = playerName2;
    }

    public void setQuestionID(int questionID) {
        this.questionID = questionID;
    }

    public int getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(int questionNumber) {
        this.questionNumber = questionNumber;
    }

    public String getTurn() {
        return turn;
    }

    public void setTurn(String turn) {
        this.turn = turn;
    }

    public boolean isNewQuestionNeeded() {
        return newQuestionNeeded;
    }

    public void setNewQuestionNeeded(boolean newQuestionNeeded) {
        this.newQuestionNeeded = newQuestionNeeded;
    }

    public boolean isPlayer1Answered() {
        return player1Answered;
    }

    public void setPlayer1Answered(boolean player1Answered) {
        this.player1Answered = player1Answered;
    }

    public boolean isPlayer2Answered() {
        return player2Answered;
    }

    public void setPlayer2Answered(boolean player2Answered) {
        this.player2Answered = player2Answered;
    }
}