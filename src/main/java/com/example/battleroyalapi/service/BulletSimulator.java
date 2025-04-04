package com.example.battleroyalapi.service;

import com.example.battleroyalapi.model.*;
import com.example.battleroyalapi.quadtree.QuadTree;
import com.example.battleroyalapi.quadtree.QuadTreeObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BulletSimulator {
    private GameManager gameManager;
    private GameWebSocketService gameWebSocketService;
    private CollisionService collisionService;

    public BulletSimulator(GameManager gameManager, GameWebSocketService gameWebSocketService, CollisionService collisionService) {
        this.gameManager = gameManager;
        this.gameWebSocketService = gameWebSocketService;
        this.collisionService = collisionService;
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
                        continue;
                    }
                    List<QuadTreeObject> collisions = tree.queryIntersecting(bullet);
                    for (QuadTreeObject collision : collisions) {
                        switch (collision.type) {
                            case ObjectType.PLAYER -> {
                               Player player = (Player) collision;
                               if (collisionService.isCircleCircleIntersecting(new Circle(player.getCenterX(), player.getCenterY(), player.getRadius()), new Circle(bullet.getCenterX(), bullet.getCenterY(), bullet.getRadius()))) {
                                  player.health -= 10;
                                  gameWebSocketService.sendPlayerHitUpdate(gameInstanceKey, player, bullet.getPlayer());
                                  gameWebSocketService.sendBulletExpired(gameInstanceKey, bullet.id);
                                  gameInstance.bullets.remove(bullet);
                                  tree.remove(bullet);
                               }
                            }
                            case ObjectType.WALL -> {
                                if (collisionService.isCircleRectangleInsecting(new Circle(bullet.getCenterX(), bullet.getCenterY(), bullet.getRadius()), new Rectangle(collision.bounds.getX(), collision.bounds.getY(), collision.bounds.getHeight(), collision.bounds.getWidth()))) {
                                    gameWebSocketService.sendBulletExpired(gameInstanceKey, bullet.id);
                                    gameInstance.bullets.remove(bullet);
                                    tree.remove(bullet);
                                }
                            }
                            default -> System.out.println("Collision not a valid type");
                        }
                    }
                }
            } finally {
                gameInstance.lock.writeLock().unlock();
            }
        }
    }
}
