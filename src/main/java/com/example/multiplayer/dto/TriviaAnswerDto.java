package com.example.multiplayer.dto;

import lombok.Data;

@Data
public class TriviaAnswerDto {
    private String sessionId;
    private String playerName;
    private int answerIndex;
}
