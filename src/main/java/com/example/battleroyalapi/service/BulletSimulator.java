package com.example.battleroyalapi.service;

import com.example.battleroyalapi.model.*;
import com.example.battleroyalapi.quadtree.QuadTree;
import com.example.battleroyalapi.quadtree.QuadTreeObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Component
public class BulletSimulator {
    private GameManager gameManager;
    private GameWebSocketService gameWebSocketService;
    private CollisionService collisionService;
    private PlayerService playerService;
    private WallService wallService;

    public BulletSimulator(GameManager gameManager, GameWebSocketService gameWebSocketService, CollisionService collisionService, PlayerService playerService, WallService wallService) {
        this.gameManager = gameManager;
        this.gameWebSocketService = gameWebSocketService;
        this.collisionService = collisionService;
        this.playerService = playerService;
        this.wallService = wallService;
    }

    @Scheduled(fixedRate = 100)
    private void pushBullets() {
        for (String gameId : gameManager.map.keySet()) {
            GameInstance game = gameManager.map.get(gameId);
            List<Bullet> bullets = bulletSnapshot(game);
            gameWebSocketService.sendBulletsUpdate(gameId, bullets);
        }
    }

    @Scheduled(fixedRate = 16)
    private void simulateBullets() {
        for (String gameInstanceKey : gameManager.map.keySet()) {
            GameInstance gameInstance = gameManager.map.get(gameInstanceKey);
            for (Bullet bullet : bulletSnapshot(gameInstance)) {
                bullet.update();

                boolean isInserted = reinsertBullet(gameInstance, bullet);
                if (!isInserted || handleCollisions(gameInstanceKey, gameInstance, bullet)) {
                    removeBullet(gameInstance, bullet);
                    gameWebSocketService.sendBulletExpired(gameInstanceKey, bullet.id);
                }
            }
        }
    }

    private boolean handleCollisions(String gameId, GameInstance gameInstance, Bullet bullet) {
        boolean isCollision = false;
        List<QuadTreeObject> collisions = new ArrayList<>();

        collisions.addAll(playerService.playersIntersecting(gameInstance, bullet));
        collisions.addAll(wallService.intersectingWalls(gameInstance, bullet));
        for (QuadTreeObject collision : collisions) {
            isCollision = switch (collision.type) {
                case ObjectType.PLAYER -> {
                    Player player = (Player) collision;
                    if (collisionService.isCircleCircleIntersecting(
                            new Circle(player.getCenterX(), player.getCenterY(), player.getRadius()),
                            new Circle(bullet.getCenterX(), bullet.getCenterY(), bullet.getRadius()))) {
                        playerService.playerHitDamage(gameInstance, player);
                        gameWebSocketService.sendPlayerHitUpdate(gameId, player, bullet.getPlayer());
                        yield true;
                    }
                    yield false;
                }
                case ObjectType.WALL -> {
                    yield collisionService.
                            isCircleRectangleInsecting(
                                    new Circle(bullet.getCenterX(), bullet.getCenterY(), bullet.getRadius()),
                                    new Rectangle(collision.bounds.getX(), collision.bounds.getY(), collision.bounds.getHeight(), collision.bounds.getWidth())
                            );
                }
                default -> false;
            };
            if (isCollision) break;
        }

        return isCollision;
    }

    private List<Bullet> bulletSnapshot(GameInstance gameInstance) {
        return gameInstance.bulletLock.withRead(() ->
                new ArrayList<>(gameInstance.bullets));
    }

    private boolean reinsertBullet(GameInstance gameInstance, Bullet bullet) {
        return gameInstance.bulletLock.withWrite(b -> {
            gameInstance.bulletTree.remove(b);
            return gameInstance.bulletTree.insert(b);
        }, bullet);
    }

    private void removeBullet(GameInstance gameInstance, Bullet bullet) {
        gameInstance.bulletLock.withWrite(removingBullet -> {
            gameInstance.bullets.remove(removingBullet);
            gameInstance.bulletTree.remove(removingBullet);
        }, bullet);
    }
}
