package com.example.multiplayer.config;

import com.example.multiplayer.model.GameSession;
import com.example.multiplayer.service.GameSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketEventListener {

    @Autowired
    private GameSessionService sessionService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Maps STOMP simpSessionId -> GameSessionId
    public static final Map<String, String> socketToSessionMap = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String simpSessionId = headers.getSessionId();
        
        if (simpSessionId != null && socketToSessionMap.containsKey(simpSessionId)) {
            String gameSessionId = socketToSessionMap.get(simpSessionId);
            socketToSessionMap.remove(simpSessionId);
            
            try {
                // If a user disconnects forcefully, we can mark the session as FINISHED
                GameSession session = sessionService.endSession(gameSessionId);
                
                // Broadcast to anyone remaining in the lobby or game topic
                messagingTemplate.convertAndSend("/topic/lobby/" + gameSessionId, session);
                
            } catch (Exception e) {
                // Session might already be ended or not found
            }
        }
    }
}
