package com.example.battleroyalapi.service;

import com.example.battleroyalapi.model.*;
import com.example.battleroyalapi.quadtree.Bounds;
import com.example.battleroyalapi.quadtree.QuadTree;
import com.example.battleroyalapi.quadtree.QuadTreeObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlayerService {
    private GameManager gameManager;
    private GameWebSocketService gameWebSocketService;
    private CollisionService collisionService;
    private Logger logger = LoggerFactory.getLogger(PlayerService.class);

    public PlayerService(GameManager gameManager, GameWebSocketService gameWebSocketService, CollisionService collisionService) {
        this.gameManager = gameManager;
        this.gameWebSocketService = gameWebSocketService;
        this.collisionService = collisionService;
    }

//
//    public QuadTree getQuadTree(String game) {
//        GameInstance gameInstance = gameManager.getOrCreate(game);
//        //todo: throw exception here
//        if (gameInstance == null) return null;
//        return gameInstance.tree;
//    }

    public void insertPlayer(String id, Player quadTreeObject) {
        GameInstance gameInstance = gameManager.getOrCreate(id);

        gameInstance.playerLock.withWrite(player -> {
            gameInstance.players.put(quadTreeObject.id, quadTreeObject);
            gameInstance.playerTree.insert(quadTreeObject);
        }, quadTreeObject);
    }

    public void movePlayer(String id, Player player) {
        GameInstance gameInstance = gameManager.getOrCreate(id);
        List<QuadTreeObject> collisions = gameInstance.wallLock.withRead(p -> {
            return gameInstance.wallTree.queryIntersecting(p);
        }, player);

        gameInstance.playerLock.withRead(p -> {
           collisions.addAll(gameInstance.playerTree.queryIntersecting(p));
        }, player);

        for (QuadTreeObject collision : collisions) {
            switch(collision.type) {
                case ObjectType.WALL -> {
                    if (collisionService.isCircleRectangleInsecting(new Circle(player.getCenterX(), player.getCenterY(), player.getRadius()), new Rectangle(collision.bounds.getX(), collision.bounds.getY(), collision.bounds.getHeight(), collision.bounds.getWidth()))) {
                        gameInstance.players.put(player.id, player);
                        gameWebSocketService.sendWallCollision(id, player, (Wall) collision);
                        return;
                    }
                }
                case ObjectType.PLAYER -> {
                    Player opponent = (Player) collision;
                    if (collisionService.isCircleCircleIntersecting(new Circle(player.getCenterX(), player.getCenterY(), player.getRadius()), new Circle(opponent.getCenterX(), opponent.getCenterY(), opponent.getRadius()))) {
                        gameWebSocketService.sendPlayerCollision(id, player, opponent);
                        gameInstance.players.put(player.id, player);
                        return;
                    }
                }
                default -> System.out.println("Player collisions not a valid type");
            }
        }

        gameInstance.playerLock.withWrite((quadTreeId, movingPlayer) -> {
            Player previousPlayer = gameInstance.players.get(movingPlayer.id);
            QuadTreeObject previousPlayerPosition = gameInstance.playerTree.remove(previousPlayer);
            if (previousPlayerPosition == null) {
                logger.info("[MovePlayer] QuadTree={} player={} does not exist in the current tree to be moved", id, movingPlayer.id);
                return;
            }

            boolean inserted = gameInstance.playerTree.insert(movingPlayer);
            if (!inserted) {
                gameWebSocketService.sendOutOfBounds(quadTreeId, (Player) previousPlayerPosition);
                gameInstance.playerTree.insert(previousPlayerPosition);
                gameInstance.players.put(previousPlayerPosition.id, (Player) previousPlayerPosition);
                return;
            }

            gameInstance.players.put(movingPlayer.id, movingPlayer);
        }, id, player);
    }

    public void playerHitDamage(GameInstance gameInstance, Player player) {
        gameInstance.playerLock.withWrite(playerHit -> {
            playerHit.health -= 10;
        }, player);
    }

    public QuadTree getPlayerQuadTree(String id) {
        GameInstance gameInstance = gameManager.map.get(id);
        return gameInstance.playerTree;
    }

    public List<QuadTreeObject> playersIntersecting(GameInstance gameInstance, QuadTreeObject quadTreeObject) {
        return gameInstance.playerLock.withRead(treeObject -> {
            return gameInstance.playerTree.queryIntersecting(treeObject);
        }, quadTreeObject);
    }
}
