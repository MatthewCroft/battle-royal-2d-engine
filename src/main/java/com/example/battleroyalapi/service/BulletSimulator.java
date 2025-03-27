package com.example.battleroyalapi.service;

import com.example.battleroyalapi.model.Bullet;
import com.example.battleroyalapi.model.GameInstance;
import com.example.battleroyalapi.model.GameManager;
import com.example.battleroyalapi.quadtree.QuadTree;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BulletSimulator {
    private GameManager gameManager;
    private GameWebSocketService gameWebSocketService;

    public BulletSimulator(GameManager gameManager, GameWebSocketService gameWebSocketService) {
        this.gameManager = gameManager;
        this.gameWebSocketService = gameWebSocketService;
    }

    @Scheduled(fixedRate = 16)
    public void simulateBullets() {
        for (String gameInstanceKey : gameManager.map.keySet()) {
            GameInstance gameInstance = gameManager.map.get(gameInstanceKey);
            gameInstance.lock.writeLock().lock();
            try {
                QuadTree tree = gameInstance.tree;
                for (Bullet bullet : gameInstance.bullets) {
                    bullet.update();
                    tree.remove(bullet);
                    if(!tree.insert(bullet)) {
                        gameWebSocketService.sendBulletExpired(gameInstanceKey, bullet.id);
                        gameInstance.bullets.remove(bullet);
                    }
                }
            } finally {
                gameInstance.lock.writeLock().unlock();
            }
        }
    }
}
