package com.example.battleroyalapi.model;

import com.example.battleroyalapi.quadtree.QuadTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GameInstance {
    public QuadTree tree;
    public final ReadWriteLock lock = new ReentrantReadWriteLock();
    public List<Bullet> bullets = new CopyOnWriteArrayList<>();
    public Map<String, Player> players = new HashMap<>();
    public Zone zone = new Zone("zone", 300, 300, 70);

    public GameInstance(QuadTree tree) {
        this.tree = tree;
    }
}
