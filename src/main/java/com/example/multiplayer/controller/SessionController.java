package com.example.multiplayer.controller;

import com.example.multiplayer.dto.JoinRequestDto;
import com.example.multiplayer.dto.SessionRequestDto;
import com.example.multiplayer.model.GameSession;
import com.example.multiplayer.service.GameSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    @Autowired
    private GameSessionService sessionService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/create")
    public ResponseEntity<?> createSession(@RequestBody SessionRequestDto request) {
        try {
            int minPlayers = 2;
            int maxPlayers = 2;

            if ("TRIVIA".equals(request.getGameType())) {
                maxPlayers = 10;
            } else if ("NUMBER_GUESS".equals(request.getGameType())) {
                minPlayers = 2;
                maxPlayers = 2;
            } else if ("DIGIT_GUESS".equals(request.getGameType())) {
                minPlayers = 2;
                maxPlayers = 2;
            }

            GameSession session = sessionService.createSession(request.getGameType(), request.getPlayerName(), minPlayers, maxPlayers);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{sessionId}/join")
    public ResponseEntity<?> joinSession(@PathVariable String sessionId, @RequestBody JoinRequestDto request) {
        try {
            GameSession session = sessionService.joinSession(sessionId, request.getPlayerName());
            // Broadcast lobby update
            messagingTemplate.convertAndSend("/topic/lobby/" + sessionId, session);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{sessionId}/start")
    public ResponseEntity<?> startGame(@PathVariable String sessionId) {
        try {
            GameSession session = sessionService.startGame(sessionId);
            // Broadcast game start
            messagingTemplate.convertAndSend("/topic/lobby/" + sessionId, session);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{sessionId}/end")
    public ResponseEntity<?> endSession(@PathVariable String sessionId) {
        try {
            GameSession session = sessionService.endSession(sessionId);
            // Broadcast game end
            messagingTemplate.convertAndSend("/topic/lobby/" + sessionId, session);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<?> getSession(@PathVariable String sessionId) {
        try {
            return ResponseEntity.ok(sessionService.getSession(sessionId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
