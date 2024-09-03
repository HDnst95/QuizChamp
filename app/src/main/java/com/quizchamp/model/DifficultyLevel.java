// DifficultyLevel.java
package com.quizchamp.model;

import java.io.Serializable;

public enum DifficultyLevel implements Serializable {
    VERY_EASY("sehr einfach", 0.65),
    EASY("einfach", 0.80),
    MEDIUM("mittel", 0.88),
    HARD("schwer", 0.92),
    VERY_HARD("sehr schwer", 0.98);

    DifficultyLevel(String definition, double accuracy) {
        this.definition = definition;
        this.accuracy = accuracy;
    }
    private final double accuracy;
    private final String definition;

    public String toString() {
        return definition;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public String getDefinition() {
        return definition;
    }
}