package com.example.battleroyalapi.model;

import com.example.battleroyalapi.quadtree.Bounds;
import com.example.battleroyalapi.quadtree.QuadTreeObject;

public class Zone extends QuadTreeObject {
    double centerX;
    double centerY;
    double radius;

    public Zone(String id, double centerX, double centerY, double radius) {
        super(id, new Bounds(centerX - radius, centerY - radius, radius * 2, radius * 2), ObjectType.ZONE);
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
    }

    public double getCenterX() {
        return centerX;
    }

    public double getCenterY() {
        return centerY;
    }

    public double getRadius() {
        return radius;
    }
}
