package com.example.battleroyalapi.service;

import com.example.battleroyalapi.model.*;
import com.example.battleroyalapi.quadtree.QuadTree;
import com.example.battleroyalapi.quadtree.QuadTreeObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
            List<Bullet> bulletsSnapshot = gameInstance.bulletLock.withRead(() -> {
                return new ArrayList<>(gameInstance.bullets);
            });
            for (Bullet bullet : bulletsSnapshot) {

                bullet.update();

                gameInstance.bulletLock.withWrite(b -> {
                    gameInstance.bulletTree.remove(b);
                    if (!gameInstance.bulletTree.insert(b)) {
                        gameWebSocketService.sendBulletExpired(gameInstanceKey, b.id);
                        gameInstance.bullets.remove(b);
                    }
                }, bullet);


                List<QuadTreeObject> collisions = gameInstance.wallLock.withRead(movingBullet -> {
                    return gameInstance.wallTree.queryIntersecting(movingBullet);
                }, bullet);

                collisions.addAll(gameInstance.playerLock.withRead(movingBullet -> {
                    return gameInstance.playerTree.queryIntersecting(movingBullet);
                }, bullet));

                for (QuadTreeObject collision : collisions) {
                    switch (collision.type) {
                        case ObjectType.PLAYER -> {
                            Player player = (Player) collision;
                            if (collisionService.isCircleCircleIntersecting(new Circle(player.getCenterX(), player.getCenterY(), player.getRadius()), new Circle(bullet.getCenterX(), bullet.getCenterY(), bullet.getRadius()))) {
                                gameInstance.playerLock.withWrite(playerHit -> {
                                    playerHit.health -= 10;
                                }, player);

                                gameInstance.bulletLock.withWrite(b -> {
                                    gameInstance.bullets.remove(bullet);
                                    gameInstance.bulletTree.remove(bullet);
                                }, bullet);

                                gameWebSocketService.sendPlayerHitUpdate(gameInstanceKey, player, bullet.getPlayer());
                                gameWebSocketService.sendBulletExpired(gameInstanceKey, bullet.id);
                            }
                        }
                        case ObjectType.WALL -> {
                            if (collisionService.isCircleRectangleInsecting(new Circle(bullet.getCenterX(), bullet.getCenterY(), bullet.getRadius()), new Rectangle(collision.bounds.getX(), collision.bounds.getY(), collision.bounds.getHeight(), collision.bounds.getWidth()))) {
                                gameInstance.bulletLock.withWrite(b -> {
                                    gameInstance.bullets.remove(bullet);
                                    gameInstance.bulletTree.remove(bullet);
                                }, bullet);

                                gameWebSocketService.sendBulletExpired(gameInstanceKey, bullet.id);
                            }
                        }
                        default -> System.out.println("Collision not a valid type");
                    }
                }
            }
    }
}
}
