package com.example.battleroyalapi.service;

import com.example.battleroyalapi.model.*;
import com.example.battleroyalapi.quadtree.Bounds;
import com.example.battleroyalapi.quadtree.QuadTree;
import com.example.battleroyalapi.quadtree.QuadTreeObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlayerService {
    private GameManager gameManager;
    private GameWebSocketService gameWebSocketService;
    private CollisionService collisionService;
    private WallService wallService;
    private Logger logger = LoggerFactory.getLogger(PlayerService.class);

    public PlayerService(GameManager gameManager, GameWebSocketService gameWebSocketService, CollisionService collisionService, WallService wallService) {
        this.gameManager = gameManager;
        this.gameWebSocketService = gameWebSocketService;
        this.collisionService = collisionService;
        this.wallService = wallService;
    }

    public void insertPlayer(String id, Player quadTreeObject) {
        GameInstance gameInstance = gameManager.getOrCreate(id);

        gameInstance.playerLock.withWrite(player -> {
            gameInstance.players.put(quadTreeObject.id, quadTreeObject);
            gameInstance.playerTree.insert(quadTreeObject);
        }, quadTreeObject);
    }

    public void movePlayer(String id, Player player) {
        GameInstance gameInstance = gameManager.getOrCreate(id);

        Player previousPlayer = gameInstance.players.get(player.id);
        if (checkCollisions(id, gameInstance, player)) {
            gameWebSocketService.sendPlayerCorrectionPositionUpdate(id, previousPlayer);
            return;
        }

        if (!reinsertPlayer(id, gameInstance, player)) {
            gameWebSocketService.sendPlayerCorrectionPositionUpdate(id, previousPlayer);
            return;
        }

        gameWebSocketService.sendPlayerPositionUpdate(id, player);
    }

    private boolean reinsertPlayer(String id, GameInstance gameInstance, Player player) {
        return gameInstance.playerLock.withWrite((quadTreeId, movingPlayer) -> {
            Player previousPlayerState = gameInstance.players.get(movingPlayer.id);
            QuadTreeObject previousPlayerPosition = gameInstance.playerTree.remove(previousPlayerState);
            if (previousPlayerPosition == null) {
                logger.info("[MovePlayer] QuadTree={} player={} does not exist in the current tree to be moved", id, movingPlayer.id);
                return false;
            }

            boolean inserted = gameInstance.playerTree.insert(movingPlayer);
            if (!inserted) {
                gameWebSocketService.sendOutOfBounds(quadTreeId, (Player) previousPlayerPosition);
                gameInstance.playerTree.insert(previousPlayerPosition);
                gameInstance.players.put(previousPlayerPosition.id, (Player) previousPlayerPosition);
                return false;
            }

            gameInstance.players.put(movingPlayer.id, movingPlayer);
            return true;
        }, id, player);
    }

    private boolean checkCollisions(String id, GameInstance gameInstance, Player player) {
        boolean isCollision = false;
        List<QuadTreeObject> collisions = new ArrayList<>();

        collisions.addAll(wallService.intersectingWalls(gameInstance, player));
        collisions.addAll(playersIntersecting(gameInstance, player));

        for (QuadTreeObject collision : collisions) {
            isCollision = switch(collision.type) {
                case ObjectType.WALL -> {
                    if (collisionService.isCircleRectangleInsecting(new Circle(player.getCenterX(), player.getCenterY(), player.getRadius()), new Rectangle(collision.bounds.getX(), collision.bounds.getY(), collision.bounds.getHeight(), collision.bounds.getWidth()))) {
                        //gameInstance.players.put(player.id, player);
                        gameWebSocketService.sendWallCollision(id, player, (Wall) collision);
                        yield true;
                    }
                    yield false;
                }
                case ObjectType.PLAYER -> {
                    Player opponent = (Player) collision;
                    if (collisionService.isCircleCircleIntersecting(new Circle(player.getCenterX(), player.getCenterY(), player.getRadius()), new Circle(opponent.getCenterX(), opponent.getCenterY(), opponent.getRadius()))) {
                        gameWebSocketService.sendPlayerCollision(id, player, opponent);
                        //gameInstance.players.put(player.id, player);
                        yield true;
                    }
                    yield false;
                }
                default -> false;
            };
            if (isCollision) break;
        }
        return isCollision;
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
