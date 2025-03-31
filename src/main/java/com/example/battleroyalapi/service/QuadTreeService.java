package com.example.battleroyalapi.service;

import com.example.battleroyalapi.model.*;
import com.example.battleroyalapi.quadtree.Bounds;
import com.example.battleroyalapi.quadtree.QuadTree;
import com.example.battleroyalapi.quadtree.QuadTreeObject;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QuadTreeService {
    private GameManager gameManager;
    private GameWebSocketService gameWebSocketService;
    private CollisionService collisionService;

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

    public void insertQuadTreeObject(String id, QuadTreeObject quadTreeObject) {
        GameInstance gameInstance = gameManager.getOrCreate(id);
        gameInstance.lock.writeLock().lock();
        try {
            gameInstance.tree.insert(quadTreeObject);
        } finally {
            gameInstance.lock.writeLock().unlock();
        }
    }

    public void moveQuadTreePlayer(String id, QuadTreeObject quadTreeObject) {
        GameInstance gameInstance = gameManager.getOrCreate(id);
        gameInstance.lock.writeLock().lock();
        try {
            QuadTreeObject previousPlayerPosition = gameInstance.tree.remove(quadTreeObject);
            if (previousPlayerPosition == null) return;
           boolean inserted = gameInstance.tree.insert(quadTreeObject);
            if (!inserted) {
                gameInstance.tree.insert(previousPlayerPosition);
                quadTreeObject = previousPlayerPosition;
            }
            List<QuadTreeObject> collisions = gameInstance.tree.queryIntersecting(quadTreeObject);
            for (QuadTreeObject collision : collisions) {
                System.out.println("Player collisions: " + collision.type);
                switch(collision.type) {
                    case ObjectType.WALL -> {
                        Player player = (Player) quadTreeObject;
                        if (collisionService.isCircleRectangleInsecting(new Circle(player.getCenterX(), player.getCenterY(), player.getRadius()), new Rectangle(collision.bounds.getX(), collision.bounds.getY(), collision.bounds.getHeight(), collision.bounds.getWidth()))) {
                            gameWebSocketService.sendWallCollision(id);
                            gameInstance.tree.insert(previousPlayerPosition);
                        }
                    }
                    case ObjectType.PLAYER -> {
                        Player player = (Player) quadTreeObject;
                        Player opponent = (Player) collision;

                        if (collisionService.isCircleCircleIntersecting(new Circle(player.getCenterX(), player.getCenterY(), player.getRadius()), new Circle(opponent.getCenterX(), opponent.getCenterY(), opponent.getRadius()))) {
                            gameWebSocketService.sendPlayerCollision(id);
                            gameInstance.tree.insert(previousPlayerPosition);
                        }
                    }
                    default -> System.out.println("collision not a type");
                }
            }
        } finally {
            gameInstance.lock.writeLock().unlock();
        }
    }

    public void createSimpleQuadTree(String uuid) {
        gameManager.getOrCreate(uuid);
    }
}
