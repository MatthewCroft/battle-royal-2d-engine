package com.example.battleroyalapi.model;

import com.example.battleroyalapi.quadtree.QuadTree;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GameInstance {
    public QuadTree tree;
    public final ReadWriteLock lock = new ReentrantReadWriteLock();
    public List<Bullet> bullets = new CopyOnWriteArrayList<>();

    public GameInstance(QuadTree tree) {
        this.tree = tree;
    }
}
