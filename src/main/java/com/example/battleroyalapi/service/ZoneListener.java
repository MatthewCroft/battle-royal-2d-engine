package com.example.battleroyalapi.service;

import com.example.battleroyalapi.model.*;
import com.example.battleroyalapi.quadtree.QuadTreeObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ZoneListener {
    private GameManager gameManager;
    private CollisionService collisionService;
    private PlayerService playerService;
    private GameWebSocketService gameWebSocketService;
    private final double TICK = 0.016;

    public ZoneListener(GameManager gameManager, CollisionService collisionService, PlayerService playerService, GameWebSocketService gameWebSocketService) {
        this.gameManager = gameManager;
        this.collisionService = collisionService;
        this.playerService = playerService;
        this.gameWebSocketService = gameWebSocketService;
    }

    /**
     * Currently this is the only wall write log the system should have
     */
    @Scheduled(fixedRate = 16)
    public void zoneTick() {
        for (String key : gameManager.map.keySet()) {
            GameInstance gameInstance = gameManager.map.get(key);
            List<QuadTreeObject> intersectingObjects = playerService.playersIntersecting(gameInstance, gameInstance.zone);

            if (intersectingObjects.isEmpty()) continue;

            gameInstance.wallLock.withWrite(intersecting -> {
                List<Player> playersCurrentlyInZone = new ArrayList<>();
                for (QuadTreeObject object : intersecting) {
                    if (object instanceof Player player &&
                            collisionService.isCircleCircleIntersecting(new Circle(player.getCenterX(), player.getCenterY(), player.getRadius()), new Circle(gameInstance.zone.getCenterX(), gameInstance.zone.getCenterY(), gameInstance.zone.getRadius()))) {
                        playersCurrentlyInZone.add(player);
                    }
                }

                if (playersCurrentlyInZone.isEmpty()) {
                    gameInstance.zone.time = 0;
                    gameInstance.zone.player = "";
                }

                if (playersCurrentlyInZone.size() == 1) {
                    gameInstance.zone.time += TICK;
                    gameInstance.zone.player = playersCurrentlyInZone.get(0).id;
                }
            }, intersectingObjects);
        }
    }

    @Scheduled(fixedRate = 50)
    public void zoneUpdateToClient() {
        for (String key : gameManager.map.keySet()) {
            GameInstance gameInstance = gameManager.map.get(key);
            Zone zone = gameInstance.wallLock.withRead(() -> gameInstance.zone);
            gameWebSocketService.sendZoneUpdate(key, zone);
        }
    }
}
