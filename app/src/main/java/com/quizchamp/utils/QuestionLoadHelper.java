package com.quizchamp.utils;

import com.quizchamp.activity.modus.HighscoreActivity;
import com.quizchamp.activity.modus.MultiPlayerOnDeviceActivity;
import com.quizchamp.activity.modus.MultiPlayerActivity;
import com.quizchamp.activity.modus.SinglePlayerActivity;


public class QuestionLoadHelper {
    public QuestionLoadHelper(SinglePlayerActivity singlePlayerGameActivity) {
        this.singlePlayerGameActivity = singlePlayerGameActivity;
    }
    public QuestionLoadHelper(HighscoreActivity highScoreActivity) {
        this.highScoreActivity = highScoreActivity;
    }
    public QuestionLoadHelper(MultiPlayerActivity multiPlayerGameActivity) {
        this.multiPlayerGameActivity = multiPlayerGameActivity;
    }
    public QuestionLoadHelper(MultiPlayerOnDeviceActivity multiPlayerOnDeviceActivity) {
        this.multiPlayerOnDeviceActivity = multiPlayerOnDeviceActivity;
    }
    private SinglePlayerActivity singlePlayerGameActivity;
    private HighscoreActivity highScoreActivity;
    private MultiPlayerActivity multiPlayerGameActivity;
    private MultiPlayerOnDeviceActivity multiPlayerOnDeviceActivity;

}
