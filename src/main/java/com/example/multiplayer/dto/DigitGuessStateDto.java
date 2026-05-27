package com.example.multiplayer.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DigitGuessStateDto {
    private String sessionId;

    // Game phases: SETUP, PICKING, PLAYING, FINISHED
    private String phase;

    private int digitCount;

    // Which players have picked their secret numbers
    private List<String> playersPicked;

    // Whose turn is it to guess?
    private String currentTurnPlayer;

    // Per-player revealed masks: e.g. { "Alice": ["7", "_", "_", "_"], "Bob": ["_", "_", "9", "_"] }
    // This shows what the GUESSER has cracked of the OPPONENT's number
    private Map<String, List<String>> revealedDigits;

    // Per-player guess history: { "Alice": [ { guess: "7350", feedback: "7 _ _ _" }, ... ] }
    private Map<String, List<String>> guessHistory;

    private String winner;
    private String errorMsg;
}
