package com.example.multiplayer.service;

import com.example.multiplayer.model.GameSession;
import com.example.multiplayer.repository.GameSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class GameSessionService {

    @Autowired
    private GameSessionRepository repository;

    @Transactional
    public GameSession createSession(String gameType, String hostName, int minPlayers, int maxPlayers) {
        GameSession session = new GameSession();
        session.setGameType(gameType);
        session.setHostName(hostName);
        session.setMinPlayers(minPlayers);
        session.setMaxPlayers(maxPlayers);
        session.setStatus("WAITING");
        session.getPlayers().add(hostName); // Host is the first player
        return repository.save(session);
    }

    @Transactional
    public GameSession joinSession(String sessionId, String playerName) throws Exception {
        Optional<GameSession> sessionOpt = repository.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            throw new Exception("Session not found");
        }

        GameSession session = sessionOpt.get();

        if (!"WAITING".equals(session.getStatus())) {
            throw new Exception("Game is already in progress or finished");
        }

        if (session.getPlayers().size() >= session.getMaxPlayers()) {
            throw new Exception("Lobby is full");
        }

        if (session.getPlayers().contains(playerName)) {
            throw new Exception("Player name already exists in this session");
        }

        session.getPlayers().add(playerName);
        return repository.save(session);
    }

    @Transactional
    public GameSession startGame(String sessionId) throws Exception {
        Optional<GameSession> sessionOpt = repository.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            throw new Exception("Session not found");
        }
        
        GameSession session = sessionOpt.get();
        if (session.getPlayers().size() < session.getMinPlayers()) {
            throw new Exception("Not enough players to start the game");
        }

        session.setStatus("IN_PROGRESS");
        return repository.save(session);
    }

    @Transactional
    public GameSession endSession(String sessionId) throws Exception {
        Optional<GameSession> sessionOpt = repository.findById(sessionId);
        if (sessionOpt.isPresent()) {
            GameSession session = sessionOpt.get();
            session.setStatus("FINISHED");
            return repository.save(session);
        }
        throw new Exception("Session not found");
    }

    public GameSession getSession(String sessionId) throws Exception {
        return repository.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
    }
}
