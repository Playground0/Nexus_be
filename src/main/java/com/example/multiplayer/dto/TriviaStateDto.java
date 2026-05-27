package com.example.multiplayer.dto;

import lombok.Data;
import java.util.Map;

@Data
public class TriviaStateDto {
    private String sessionId;
    private Map<String, Integer> scores;
    private int currentQuestionIndex;
}
