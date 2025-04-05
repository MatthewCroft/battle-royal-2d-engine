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

            List<QuadTreeObject> collisions = gameInstance.wallLock.withRead(p -> {
                return gameInstance.wallTree.queryIntersecting(p);
            }, movingPlayer);
            collisions.addAll(gameInstance.playerTree.queryIntersecting(movingPlayer));

            if (collisions.isEmpty()) {
                gameInstance.players.put(player.id, player);
                return;
            }

            for (QuadTreeObject collision : collisions) {
                switch(collision.type) {
                    case ObjectType.WALL -> {
                        if (collisionService.isCircleRectangleInsecting(new Circle(movingPlayer.getCenterX(), movingPlayer.getCenterY(), movingPlayer.getRadius()), new Rectangle(collision.bounds.getX(), collision.bounds.getY(), collision.bounds.getHeight(), collision.bounds.getWidth()))) {
                            gameInstance.playerTree.remove(movingPlayer);
                            gameInstance.playerTree.insert(previousPlayerPosition);
                            gameInstance.players.put(previousPlayerPosition.id, (Player) previousPlayerPosition);
                            gameWebSocketService.sendWallCollision(quadTreeId, (Player)previousPlayerPosition, (Wall) collision);
                        }
                    }
                    case ObjectType.PLAYER -> {
                        Player opponent = (Player) collision;
                        if (collisionService.isCircleCircleIntersecting(new Circle(movingPlayer.getCenterX(), movingPlayer.getCenterY(), movingPlayer.getRadius()), new Circle(opponent.getCenterX(), opponent.getCenterY(), opponent.getRadius()))) {
                            gameWebSocketService.sendPlayerCollision(quadTreeId, (Player)previousPlayerPosition, opponent);
                            gameInstance.playerTree.remove(movingPlayer);
                            gameInstance.playerTree.insert(previousPlayerPosition);
                            gameInstance.players.put(previousPlayerPosition.id, (Player) previousPlayerPosition);
                        }
                    }
                    default -> gameInstance.players.put(movingPlayer.id, movingPlayer);
                }
            }
        }, id, player);
    }

    public QuadTree getPlayerQuadTree(String id) {
        GameInstance gameInstance = gameManager.map.get(id);
        return gameInstance.playerTree;
    }
}
