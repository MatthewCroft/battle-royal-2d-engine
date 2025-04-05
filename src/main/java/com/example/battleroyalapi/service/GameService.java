package com.example.battleroyalapi.service;

import com.example.battleroyalapi.model.GameInstance;
import com.example.battleroyalapi.model.GameManager;
import com.example.battleroyalapi.model.Wall;
import com.example.battleroyalapi.quadtree.Bounds;
import org.springframework.stereotype.Service;

@Service
public class GameService {
    private GameManager gameManager;

    public GameService(GameManager gameManager) {
       this.gameManager = gameManager;
    }

    public void createSimpleQuadTree(String uuid) {
        GameInstance instance = gameManager.getOrCreate(uuid);
        instance.wallLock.withWrite(() -> {
            instance.wallTree.insert(new Wall("left-corner-horizontal", new Bounds(100, 430, 70, 20)));
            instance.wallTree.insert(new Wall("left-corner-vertical", new Bounds(150, 450, 20, 50)));

            // 600, 0
            // 400, 130
            // 450, 150 center
            instance.wallTree.insert(new Wall("right-corner-vertical", new Bounds(430, 100, 20, 70)));
            instance.wallTree.insert(new Wall("right-corner-horizontal", new Bounds(450, 150, 50, 20)));
            instance.wallTree.insert(new Wall("middle-protection", new Bounds(285, 285, 30, 30)));

            instance.wallTree.insert(instance.zone);
        });
    }

    public GameInstance getGameInstance(String game) {
        return gameManager.map.get(game);
    }
}
