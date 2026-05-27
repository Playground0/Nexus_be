package com.example.multiplayer.dto;

import lombok.Data;

@Data
public class DigitGuessActionDto {
    private String sessionId;
    private String playerName;

    private String actionType; // SET_DIGITS, PICK, GUESS

    private Integer digitCount;  // used in SET_DIGITS
    private String secretNumber; // used in PICK (e.g. "7291")
    private String guess;        // used in GUESS (e.g. "7350")
}
