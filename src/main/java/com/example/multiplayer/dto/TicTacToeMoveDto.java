package com.example.multiplayer.dto;

import lombok.Data;

@Data
public class TicTacToeMoveDto {
    private String sessionId;
    private String playerName;
    private int row;
    private int col;
    
    // New fields for game state
    private String[][] board;
    private boolean gameOver;
    private String winner; // Player name, or "DRAW"
}
