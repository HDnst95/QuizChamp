// DifficultyLevel.java
package com.quizchamp.model;

public enum DifficultyLevel {
    VERY_EASY(0.50),
    EASY(0.75),
    MEDIUM(0.83),
    HARD(0.90),
    VERY_HARD(0.98);

    DifficultyLevel(double accuracy) {
        this.accuracy = accuracy;
    }
    private final double accuracy;

    public double getAccuracy() {
        return accuracy;
    }
}