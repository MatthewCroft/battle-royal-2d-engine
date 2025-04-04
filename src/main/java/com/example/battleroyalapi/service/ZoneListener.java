package com.example.battleroyalapi.service;

import com.example.battleroyalapi.model.GameInstance;
import com.example.battleroyalapi.model.GameManager;
import com.example.battleroyalapi.model.Player;
import com.example.battleroyalapi.model.Zone;
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
    private GameWebSocketService gameWebSocketService;
    private final double TICK = 0.016;

    public ZoneListener(GameManager gameManager, GameWebSocketService gameWebSocketService) {
        this.gameManager = gameManager;
        this.gameWebSocketService = gameWebSocketService;
    }

    @Scheduled(fixedRate = 16)
    public void zoneTick() {
        for (String key : gameManager.map.keySet()) {
            GameInstance gameInstance = gameManager.map.get(key);
            gameInstance.lock.readLock().lock();
            try {
                List<QuadTreeObject> intersectingObjects = gameInstance.tree.queryIntersecting(new Zone("zone", 300, 300, 70));
                List<Player> playersCurrentlyInZone = new ArrayList<>();
                for (QuadTreeObject object : intersectingObjects) {
                    if (object instanceof Player p) {
                        playersCurrentlyInZone.add(p);
                    }
                }

                if (playersCurrentlyInZone.size() == 1) {
                    playersCurrentlyInZone.get(0).zoneTime += TICK;
                }

                for (Player player : gameInstance.players.values()) {
                    if (!playersCurrentlyInZone.contains(player)) {
                        player.zoneTime = 0;
                    }
                }
            } finally {
                gameInstance.lock.readLock().unlock();
            }
        }
    }
}
