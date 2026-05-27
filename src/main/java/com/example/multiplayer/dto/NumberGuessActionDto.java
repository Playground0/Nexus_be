package com.example.multiplayer.dto;

import lombok.Data;

@Data
public class NumberGuessActionDto {
    private String sessionId;
    private String playerName;
    
    private String actionType; // SET_RANGE, PICK, GUESS, RESPOND
    
    private Integer maxRange; // used in SET_RANGE
    private Integer number; // used in PICK and GUESS
    
    private String response; // "GREATER", "LESS", "EQUAL" - used in RESPOND
}
