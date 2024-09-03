package com.quizchamp.UUID;

import java.util.UUID;

public class MatchNameGenerator {
    public static String generateRandomMatchName() {
        return "Match-" + UUID.randomUUID().toString();
    }
}