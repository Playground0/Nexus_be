package com.example.multiplayer.controller;

import com.example.multiplayer.config.WebSocketEventListener;
import com.example.multiplayer.dto.TicTacToeMoveDto;
import com.example.multiplayer.service.GameSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
public class GameWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private GameSessionService sessionService;

    private final Map<String, String[][]> ticTacToeBoards = new HashMap<>();
    private final Map<String, Map<String, Integer>> triviaScores = new HashMap<>();

    @MessageMapping("/join")
    public void handleJoin(@Payload Map<String, String> payload, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = payload.get("sessionId");
        String simpSessionId = headerAccessor.getSessionId();
        if (simpSessionId != null && sessionId != null) {
            WebSocketEventListener.socketToSessionMap.put(simpSessionId, sessionId);
        }
    }

    @MessageMapping("/tictactoe/move")
    public void handleTicTacToeMove(@Payload TicTacToeMoveDto move, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = move.getSessionId();
        
        // Also map connection here just in case they didn't send /join
        String simpSessionId = headerAccessor.getSessionId();
        if (simpSessionId != null) {
            WebSocketEventListener.socketToSessionMap.put(simpSessionId, sessionId);
        }

        ticTacToeBoards.putIfAbsent(sessionId, new String[3][3]);
        String[][] board = ticTacToeBoards.get(sessionId);

        if (!move.isGameOver()) {
            board[move.getRow()][move.getCol()] = move.getPlayerName();
        }

        move.setBoard(board);

        // Check win logic
        String winner = checkWin(board);
        if (winner != null) {
            move.setGameOver(true);
            move.setWinner(winner);
            endGame(sessionId);
        } else if (isDraw(board)) {
            move.setGameOver(true);
            move.setWinner("DRAW");
            endGame(sessionId);
        }

        messagingTemplate.convertAndSend("/topic/game/" + sessionId, move);
    }

    private void endGame(String sessionId) {
        try {
            sessionService.endSession(sessionId);
        } catch (Exception e) {
            // Ignore if already ended
        }
        // Cleanup board
        ticTacToeBoards.remove(sessionId);
    }

    private String checkWin(String[][] board) {
        for (int i = 0; i < 3; i++) {
            if (board[i][0] != null && board[i][0].equals(board[i][1]) && board[i][1].equals(board[i][2])) return board[i][0];
            if (board[0][i] != null && board[0][i].equals(board[1][i]) && board[1][i].equals(board[2][i])) return board[0][i];
        }
        if (board[0][0] != null && board[0][0].equals(board[1][1]) && board[1][1].equals(board[2][2])) return board[0][0];
        if (board[0][2] != null && board[0][2].equals(board[1][1]) && board[1][1].equals(board[2][0])) return board[0][2];
        return null;
    }

    private boolean isDraw(String[][] board) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == null) return false;
            }
        }
        return true;
    }

    @MessageMapping("/trivia/answer")
    public void handleTriviaAnswer(@Payload com.example.multiplayer.dto.TriviaAnswerDto answer, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = answer.getSessionId();
        
        String simpSessionId = headerAccessor.getSessionId();
        if (simpSessionId != null) {
            WebSocketEventListener.socketToSessionMap.put(simpSessionId, sessionId);
        }

        triviaScores.putIfAbsent(sessionId, new HashMap<>());
        Map<String, Integer> scores = triviaScores.get(sessionId);
        
        scores.put(answer.getPlayerName(), scores.getOrDefault(answer.getPlayerName(), 0) + 1);

        com.example.multiplayer.dto.TriviaStateDto state = new com.example.multiplayer.dto.TriviaStateDto();
        state.setSessionId(sessionId);
        state.setScores(scores);
        
        messagingTemplate.convertAndSend("/topic/trivia/" + sessionId, state);
    }

    // NUMBER GUESS STATE
    private final Map<String, com.example.multiplayer.dto.NumberGuessStateDto> numberGuessStates = new HashMap<>();
    private final Map<String, Map<String, Integer>> numberGuessSecrets = new HashMap<>();

    @MessageMapping("/numberguess/action")
    public void handleNumberGuessAction(@Payload com.example.multiplayer.dto.NumberGuessActionDto action, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = action.getSessionId();
        
        String simpSessionId = headerAccessor.getSessionId();
        if (simpSessionId != null) {
            WebSocketEventListener.socketToSessionMap.put(simpSessionId, sessionId);
        }

        numberGuessStates.putIfAbsent(sessionId, new com.example.multiplayer.dto.NumberGuessStateDto());
        com.example.multiplayer.dto.NumberGuessStateDto state = numberGuessStates.get(sessionId);
        numberGuessSecrets.putIfAbsent(sessionId, new HashMap<>());
        Map<String, Integer> secrets = numberGuessSecrets.get(sessionId);
        
        try {
            com.example.multiplayer.model.GameSession dbSession = sessionService.getSession(sessionId);
            
            if (state.getPhase() == null) {
                state.setSessionId(sessionId);
                state.setPhase("SETUP");
                state.setPlayersPicked(new java.util.ArrayList<>());
                state.setHistory(new java.util.ArrayList<>());
            }

            state.setErrorMsg(null); // reset error

            if ("SET_RANGE".equals(action.getActionType())) {
                // Only host should set range ideally, but we'll accept
                state.setMaxRange(action.getMaxRange());
                state.setPhase("PICKING");
            } 
            else if ("PICK".equals(action.getActionType())) {
                secrets.put(action.getPlayerName(), action.getNumber());
                if (!state.getPlayersPicked().contains(action.getPlayerName())) {
                    state.getPlayersPicked().add(action.getPlayerName());
                }
                
                if (state.getPlayersPicked().size() == 2) {
                    state.setPhase("PLAYING");
                    // P1 starts
                    state.setCurrentTurnPlayer(dbSession.getPlayers().get(0));
                }
            }
            else if ("GUESS".equals(action.getActionType())) {
                if (action.getPlayerName().equals(state.getCurrentTurnPlayer())) {
                    state.setPendingGuess(action.getNumber());
                }
            }
            else if ("RESPOND".equals(action.getActionType())) {
                // The responder is the one who is NOT the current turn player
                String opponentName = state.getCurrentTurnPlayer();
                Integer mySecret = secrets.get(action.getPlayerName());
                Integer guess = state.getPendingGuess();
                
                if (mySecret != null && guess != null) {
                    // Enforce honesty
                    String actualRelation;
                    if (mySecret > guess) actualRelation = "GREATER";
                    else if (mySecret < guess) actualRelation = "LESS";
                    else actualRelation = "EQUAL";
                    
                    if (!actualRelation.equals(action.getResponse())) {
                        state.setErrorMsg("You must tell the truth! The secret number is " + actualRelation + " than " + guess);
                    } else {
                        state.getHistory().add(opponentName + " guessed " + guess + " - " + action.getPlayerName() + " says " + actualRelation);
                        state.setPendingGuess(null);
                        
                        if ("EQUAL".equals(actualRelation)) {
                            state.setPhase("FINISHED");
                            state.setWinner(opponentName);
                            endGame(sessionId);
                        } else {
                            // Switch turn
                            state.setCurrentTurnPlayer(action.getPlayerName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            state.setErrorMsg("Server Error: " + e.getMessage());
        }

        messagingTemplate.convertAndSend("/topic/numberguess/" + sessionId, state);
    }

    // ======================== DIGIT GUESS STATE ========================
    private final Map<String, com.example.multiplayer.dto.DigitGuessStateDto> digitGuessStates = new HashMap<>();
    private final Map<String, Map<String, String>> digitGuessSecrets = new HashMap<>();

    @MessageMapping("/digitguess/action")
    public void handleDigitGuessAction(@Payload com.example.multiplayer.dto.DigitGuessActionDto action, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = action.getSessionId();

        String simpSessionId = headerAccessor.getSessionId();
        if (simpSessionId != null) {
            WebSocketEventListener.socketToSessionMap.put(simpSessionId, sessionId);
        }

        digitGuessStates.putIfAbsent(sessionId, new com.example.multiplayer.dto.DigitGuessStateDto());
        com.example.multiplayer.dto.DigitGuessStateDto state = digitGuessStates.get(sessionId);
        digitGuessSecrets.putIfAbsent(sessionId, new HashMap<>());
        Map<String, String> secrets = digitGuessSecrets.get(sessionId);

        try {
            com.example.multiplayer.model.GameSession dbSession = sessionService.getSession(sessionId);

            if (state.getPhase() == null) {
                state.setSessionId(sessionId);
                state.setPhase("SETUP");
                state.setPlayersPicked(new java.util.ArrayList<>());
                state.setRevealedDigits(new HashMap<>());
                state.setGuessHistory(new HashMap<>());
            }

            state.setErrorMsg(null);

            if ("SET_DIGITS".equals(action.getActionType())) {
                int dc = action.getDigitCount();
                state.setDigitCount(dc);
                state.setPhase("PICKING");
            }
            else if ("PICK".equals(action.getActionType())) {
                String secret = action.getSecretNumber();
                if (secret == null || secret.length() != state.getDigitCount()) {
                    state.setErrorMsg("Secret number must be exactly " + state.getDigitCount() + " digits.");
                } else {
                    secrets.put(action.getPlayerName(), secret);
                    if (!state.getPlayersPicked().contains(action.getPlayerName())) {
                        state.getPlayersPicked().add(action.getPlayerName());
                    }

                    java.util.List<String> blanks = new java.util.ArrayList<>();
                    for (int i = 0; i < state.getDigitCount(); i++) blanks.add("_");
                    state.getRevealedDigits().put(action.getPlayerName(), blanks);
                    state.getGuessHistory().put(action.getPlayerName(), new java.util.ArrayList<>());

                    if (state.getPlayersPicked().size() == 2) {
                        state.setPhase("PLAYING");
                        state.setCurrentTurnPlayer(dbSession.getPlayers().get(0));
                    }
                }
            }
            else if ("GUESS".equals(action.getActionType())) {
                if (!action.getPlayerName().equals(state.getCurrentTurnPlayer())) {
                    state.setErrorMsg("It's not your turn!");
                } else {
                    String guess = action.getGuess();
                    if (guess == null || guess.length() != state.getDigitCount()) {
                        state.setErrorMsg("Guess must be exactly " + state.getDigitCount() + " digits.");
                    } else {
                        String opponent = null;
                        for (String p : dbSession.getPlayers()) {
                            if (!p.equals(action.getPlayerName())) {
                                opponent = p;
                                break;
                            }
                        }

                        String opponentSecret = secrets.get(opponent);
                        java.util.List<String> revealed = state.getRevealedDigits().get(opponent);

                        StringBuilder feedback = new StringBuilder();
                        boolean allCorrect = true;
                        for (int i = 0; i < state.getDigitCount(); i++) {
                            if (guess.charAt(i) == opponentSecret.charAt(i)) {
                                revealed.set(i, String.valueOf(opponentSecret.charAt(i)));
                                feedback.append(opponentSecret.charAt(i));
                            } else {
                                allCorrect = false;
                                feedback.append("_");
                            }
                        }

                        state.getGuessHistory().get(opponent).add(
                            action.getPlayerName() + " guessed " + guess + " → " + feedback.toString()
                        );

                        if (allCorrect) {
                            state.setPhase("FINISHED");
                            state.setWinner(action.getPlayerName());
                            endGame(sessionId);
                        } else {
                            state.setCurrentTurnPlayer(opponent);
                        }
                    }
                }
            }
        } catch (Exception e) {
            state.setErrorMsg("Server Error: " + e.getMessage());
        }

        messagingTemplate.convertAndSend("/topic/digitguess/" + sessionId, state);
    }
}
