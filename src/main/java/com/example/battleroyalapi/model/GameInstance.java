package com.example.battleroyalapi.model;

import com.example.battleroyalapi.quadtree.Bounds;
import com.example.battleroyalapi.quadtree.QuadTree;
import com.example.battleroyalapi.utils.LockingSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GameInstance {
    public final LockingSection bulletLock = new LockingSection();
    public final LockingSection playerLock = new LockingSection();
    public final LockingSection wallLock = new LockingSection();
    public QuadTree playerTree = new QuadTree(new Bounds(0, 0, 600, 600));
    public QuadTree bulletTree = new QuadTree(new Bounds(0, 0, 600, 600));
    public QuadTree wallTree = new QuadTree(new Bounds(0, 0, 600, 600));
    public List<Bullet> bullets = new CopyOnWriteArrayList<>();
    public Map<String, Player> players = new HashMap<>();
    public Zone zone = new Zone("zone", 300, 300, 70);
    public GameInstance() {}
}
