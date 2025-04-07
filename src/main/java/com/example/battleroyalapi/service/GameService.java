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
            instance.wallTree.insert(new Wall("left-corner-horizontal", new Bounds(320, 774, 224, 36)));
            instance.wallTree.insert(new Wall("left-corner-vertical", new Bounds(480, 810, 64, 90)));

            instance.wallTree.insert(new Wall("right-corner-vertical", new Bounds(1376, 180, 64, 126)));
            instance.wallTree.insert(new Wall("right-corner-horizontal", new Bounds(1440, 270, 160, 36)));

            instance.wallTree.insert(new Wall("middle-protection", new Bounds(912, 513, 96, 54)));

            instance.wallTree.insert(instance.zone);
        });
    }

    public GameInstance getGameInstance(String game) {
        return gameManager.map.get(game);
    }
}
