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
    private final double TICK = 0.016;

    public ZoneListener(GameManager gameManager, CollisionService collisionService) {
        this.gameManager = gameManager;
        this.collisionService = collisionService;
    }

    /**
     * Currently this is the only wall write log the system should have
     */
    @Scheduled(fixedRate = 16)
    public void zoneTick() {
        for (String key : gameManager.map.keySet()) {
            GameInstance gameInstance = gameManager.map.get(key);
            List<QuadTreeObject> intersectingObjects =
                    gameInstance.playerLock.withRead(zone -> {
                        return gameInstance.playerTree.queryIntersecting(zone);
                    }, gameInstance.zone);

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
}
