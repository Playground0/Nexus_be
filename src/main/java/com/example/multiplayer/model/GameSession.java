package com.example.multiplayer.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameSession {

    @Id
    private String id; // Session ID (e.g., UUID)

    private String gameType; // e.g., "TIC_TAC_TOE", "TRIVIA"

    private String hostName;

    private int minPlayers;
    private int maxPlayers;

    private String status; // "WAITING", "IN_PROGRESS", "FINISHED"

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "game_session_players", joinColumns = @JoinColumn(name = "game_session_id"))
    @Column(name = "player_name")
    private List<String> players = new ArrayList<>(); // List of player names

    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }
}
