package com.example.battleroyalapi.model;

import com.example.battleroyalapi.quadtree.Bounds;
import com.example.battleroyalapi.quadtree.QuadTree;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameManager {
    public Map<String, GameInstance> map = new ConcurrentHashMap<>();

    public GameInstance getOrCreate(String name) {
        return map.computeIfAbsent(name, id -> new GameInstance());
    }

    public Collection<GameInstance> getGames() {
        return map.values();
    }
}
