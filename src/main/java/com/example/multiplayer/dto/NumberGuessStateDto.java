package com.example.multiplayer.dto;

import lombok.Data;
import java.util.List;

@Data
public class NumberGuessStateDto {
    private String sessionId;
    
    // Game phases: SETUP, PICKING, PLAYING, FINISHED
    private String phase;
    
    private Integer maxRange;
    
    // Which players have picked their secret numbers
    private List<String> playersPicked;
    
    // Whose turn is it to guess?
    private String currentTurnPlayer;
    
    // Pending guess that needs a response from the opponent
    private Integer pendingGuess;
    
    // List of history objects: { "guesser": "A", "guess": 50, "responder": "B", "response": "GREATER" }
    // Just represent it as a string for simplicity: "A guessed 50 - B says GREATER"
    private List<String> history;
    
    private String winner;
    private String errorMsg; // e.g. "You must tell the truth!"
}
