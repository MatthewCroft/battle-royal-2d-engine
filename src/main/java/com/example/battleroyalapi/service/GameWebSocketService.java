package com.example.battleroyalapi.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class GameWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public GameWebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendBulletExpired(String gameId, String bulletId) {
        messagingTemplate.convertAndSend(
                "/topic/" + gameId, // Target game UUID topic
                Map.of(
                        "type", "bullet_expired",
                        "id", bulletId
                )
        );
    }

    public void sendWallCollision(String gameId) {
        messagingTemplate.convertAndSend(
                "/topic/" + gameId,
                Map.of("type", "wall_collision")
        );
    }

    public void sendPlayerCollision(String gameId) {
        messagingTemplate.convertAndSend(
                "/topic/" + gameId,
                Map.of("type", "player_collision")
        );
    }
}
