package com.example.battleroyalapi.service;

import com.example.battleroyalapi.model.*;
import com.example.battleroyalapi.quadtree.Bounds;
import com.example.battleroyalapi.quadtree.QuadTree;
import com.example.battleroyalapi.quadtree.QuadTreeObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QuadTreeService {
    private GameManager gameManager;
    private GameWebSocketService gameWebSocketService;
    private CollisionService collisionService;
    private Logger logger = LoggerFactory.getLogger(QuadTreeService.class);

    public QuadTreeService(GameManager gameManager, GameWebSocketService gameWebSocketService, CollisionService collisionService) {
        this.gameManager = gameManager;
        this.gameWebSocketService = gameWebSocketService;
        this.collisionService = collisionService;
    }

    public QuadTree getQuadTree(String game) {
        GameInstance gameInstance = gameManager.getOrCreate(game);
        //todo: throw exception here
        if (gameInstance == null) return null;
        return gameInstance.tree;
    }

    public GameInstance getGameInstance(String game) {
        return gameManager.getOrCreate(game);
    }

    public void insertQuadTreeObject(String id, Player quadTreeObject) {
        GameInstance gameInstance = gameManager.getOrCreate(id);
        gameInstance.lock.writeLock().lock();
    //    logger.info("[InsertQuadTree] QuadTree={} objectId={}", id, quadTreeObject.id);
        try {
            gameInstance.players.put(quadTreeObject.id, quadTreeObject);
            gameInstance.tree.insert(quadTreeObject);
        } finally {
            gameInstance.lock.writeLock().unlock();
        }
    }

    public void moveQuadTreePlayer(String id, Player player) {
        GameInstance gameInstance = gameManager.getOrCreate(id);
        gameInstance.lock.writeLock().lock();
        //logger.info("[MovePlayer] QuadTree={} player={} x={} y={}", id, player.id, player.getCenterX(), player.getCenterY());
        try {
            Player previousPlayer = gameInstance.players.get(player.id);
            QuadTreeObject previousPlayerPosition = gameInstance.tree.remove(previousPlayer);
            if (previousPlayerPosition == null) {
                logger.info("[MovePlayer] QuadTree={} player={} does not exist in the current tree to be moved", id, player.id);
                return;
            }

            boolean inserted = gameInstance.tree.insert(player);
            if (!inserted) {
                gameWebSocketService.sendOutOfBounds(id, (Player) previousPlayerPosition);
                gameInstance.tree.insert(previousPlayerPosition);
                gameInstance.players.put(previousPlayerPosition.id, (Player) previousPlayerPosition);
                return;
            }

            List<QuadTreeObject> collisions = gameInstance.tree.queryIntersecting(player);

            if (collisions.isEmpty()) {
                gameInstance.players.put(player.id, player);
            }

            for (QuadTreeObject collision : collisions) {
                switch(collision.type) {
                    case ObjectType.WALL -> {
                        if (collisionService.isCircleRectangleInsecting(new Circle(player.getCenterX(), player.getCenterY(), player.getRadius()), new Rectangle(collision.bounds.getX(), collision.bounds.getY(), collision.bounds.getHeight(), collision.bounds.getWidth()))) {
                            gameInstance.tree.remove(player);
                            gameInstance.tree.insert(previousPlayerPosition);
                            gameInstance.players.put(previousPlayerPosition.id, (Player) previousPlayerPosition);
                            gameWebSocketService.sendWallCollision(id, (Player)previousPlayerPosition, (Wall) collision);
                        }
                    }
                    case ObjectType.PLAYER -> {
                        Player opponent = (Player) collision;
                        if (collisionService.isCircleCircleIntersecting(new Circle(player.getCenterX(), player.getCenterY(), player.getRadius()), new Circle(opponent.getCenterX(), opponent.getCenterY(), opponent.getRadius()))) {
                            gameWebSocketService.sendPlayerCollision(id, (Player)previousPlayerPosition, opponent);
                            gameInstance.tree.remove(player);
                            gameInstance.tree.insert(previousPlayerPosition);
                            gameInstance.players.put(previousPlayerPosition.id, (Player) previousPlayerPosition);
                        }
                    }
                    default -> gameInstance.players.put(player.id, player);
                }
            }
        } finally {
            gameInstance.lock.writeLock().unlock();
        }
    }

    public void createSimpleQuadTree(String uuid) {
        GameInstance instance = gameManager.getOrCreate(uuid);
        instance.lock.writeLock().lock();
        try {
            instance.tree.insert(new Wall("left-corner-horizontal", new Bounds(100, 430, 70, 20)));
            instance.tree.insert(new Wall("left-corner-vertical", new Bounds(150, 450, 20, 50)));

            // 600, 0
            // 400, 130
            // 450, 150 center
            instance.tree.insert(new Wall("right-corner-vertical", new Bounds(430, 100, 20, 70)));
            instance.tree.insert(new Wall("right-corner-horizontal", new Bounds(450, 150, 50, 20)));
            instance.tree.insert(new Wall("middle-protection", new Bounds(285, 285, 30, 30)));

            instance.tree.insert(instance.zone);
        } finally {
            instance.lock.writeLock().unlock();
        }

    }
}
