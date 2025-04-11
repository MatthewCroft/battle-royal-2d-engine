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
            // Bottom Barriers
            instance.wallTree.insert(new Wall("bottom-barrier-left", new Bounds(600, 880, 160, 36)));
            instance.wallTree.insert(new Wall("bottom-barrier-right", new Bounds(1160, 880, 160, 36)));

            // Top Barriers
            instance.wallTree.insert(new Wall("top-barrier-left", new Bounds(600, 200, 160, 36)));
            instance.wallTree.insert(new Wall("top-barrier-right", new Bounds(1160, 200, 160, 36)));

            // Side Barriers
            instance.wallTree.insert(new Wall("left-barrier", new Bounds(200, 460, 36, 160)));
            instance.wallTree.insert(new Wall("right-barrier", new Bounds(1684, 460, 36, 160)));

            instance.wallTree.insert(new Wall("middle-protection", new Bounds(912, 513, 96, 54)));
            instance.wallTree.insert(instance.zone);
        });
    }

    public GameInstance getGameInstance(String game) {
        return gameManager.map.get(game);
    }
}
