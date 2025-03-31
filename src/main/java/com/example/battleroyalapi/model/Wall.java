package com.example.battleroyalapi.model;

import com.example.battleroyalapi.quadtree.Bounds;
import com.example.battleroyalapi.quadtree.QuadTreeObject;

public class Wall extends QuadTreeObject {
    public Wall(String id, Bounds bounds) {
        super(id, bounds, ObjectType.WALL);
    }
}
