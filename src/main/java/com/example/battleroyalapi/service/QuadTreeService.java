package com.example.battleroyalapi.service;

import com.example.battleroyalapi.model.GameInstance;
import com.example.battleroyalapi.model.GameManager;
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

    public QuadTreeService(GameManager gameManager) {
        this.gameManager = gameManager;
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
            List<QuadTreeObject> intersecting = gameInstance.tree.queryIntersecting(quadTreeObject);
            // walls (barriers),
            if (!intersecting.isEmpty()) {
                System.out.println("intersecting nodes size: " + intersecting.size());
            }
        } finally {
            gameInstance.lock.writeLock().unlock();
        }
    }

    public void createSimpleQuadTree(String uuid) {
        gameManager.getOrCreate(uuid);
    }
}
