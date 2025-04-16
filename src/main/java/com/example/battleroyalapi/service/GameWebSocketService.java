package com.example.battleroyalapi.service;

import com.example.battleroyalapi.model.Bullet;
import com.example.battleroyalapi.model.Player;
import com.example.battleroyalapi.model.Wall;
import com.example.battleroyalapi.model.Zone;
import com.example.battleroyalapi.quadtree.QuadTreeObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GameWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private Logger logger = LoggerFactory.getLogger(GameWebSocketService.class);

    public GameWebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendBulletExpired(String gameId, String bulletId) {
        logger.info("[BulletExpired] QuadTree={} Bullet={}", gameId, bulletId);

        messagingTemplate.convertAndSend(
                String.format("/topic/%s/events", gameId), // Target game UUID topic
                Map.of(
                        "type", "bullet_expired",
                        "id", bulletId
                )
        );
    }

    public void sendWallCollision(String gameId, Player player, Wall wall) {
        logger.info("[PlayerWallCollision] QuadTree={}, Player={} collided with wall={} at x={} y={} width={} height={}, setting player to x={} y={}",
                gameId, player.id, wall.id, wall.bounds.getX(), wall.bounds.getY(), wall.bounds.getWidth(), wall.bounds.getHeight(), player.getCenterX(), player.getCenterY());

        messagingTemplate.convertAndSend(
                String.format("/topic/%s/events", gameId),
                Map.of("type", "wall_collision",
                        "player", player)
        );
    }

    public void sendPlayerCollision(String gameId, Player player, Player opponent) {
        logger.info("[PlayerPlayerCollision] QuadTree={}, Player={} collided with opponent={}, setting player={} to x={},y={}",
                gameId, player.id, opponent.id, player.id, player.getCenterX(), player.getCenterY());

        messagingTemplate.convertAndSend(
                String.format("/topic/%s/events", gameId),
                Map.of("type", "player_collision",
                        "player", player)
        );
    }

    public void sendPlayerHitUpdate(String gameId, Player victim, String shooter) {
        logger.info("[PlayerHit] QuadTree={}, player={} shot victim={}, victim={} now has {} health", gameId, shooter, victim.id, victim.id, victim.health);

        messagingTemplate.convertAndSend(
                String.format("/topic/%s/events", gameId),
                Map.of("type", "player_hit",
                        "victim", String.format("%s", victim.id),
                        "shooter", String.format("%s", shooter))
        );
    }

    public void sendOutOfBounds(String gameId, Player previousPlayerPosition) {
        logger.info("[OutOfBounds] QuadTree={}, Player={} attempted to move out of bounds, repositioning at x={}, y={}", gameId, previousPlayerPosition.id, previousPlayerPosition.getCenterX(), previousPlayerPosition.getCenterY());

        messagingTemplate.convertAndSend(
                String.format("/topic/%s/events", gameId),
                Map.of("type", "out_of_bounds",
                        "player", previousPlayerPosition)
        );
    }

    public void sendBulletsUpdate(String gameId, List<Bullet> bullets) {
        messagingTemplate.convertAndSend(String.format("/topic/%s/bullets", gameId), bullets);
    }

    public void sendZoneUpdate(String gameId, Zone zone) {
        messagingTemplate.convertAndSend(String.format("/topic/%s/zone", gameId), zone);
    }

    public void sendPlayerPositionUpdate(String id, Player player) {
        messagingTemplate.convertAndSend(String.format("/topic/%s/player", id), player);
    }

    public void sendPlayerCorrectionPositionUpdate(String id, Player player) {
        messagingTemplate.convertAndSend(String.format("/topic/%s/player/correction", id), player);
    }
}
