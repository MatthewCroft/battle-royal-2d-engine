package com.example.battleroyalapi.model;

import com.example.battleroyalapi.quadtree.Bounds;
import com.example.battleroyalapi.quadtree.QuadTreeObject;
import com.fasterxml.jackson.annotation.JsonTypeName;

public class Player extends QuadTreeObject {
    public double health;
    double speed;
    double pointerX;
    double pointerY;
    double radius;
    public double centerX;
    public double centerY;
    public double zoneTime;
    public Player(String id, double health, double speed, double pointerX, double pointerY, double centerX, double centerY, double radius, double zoneTime) {
        super(id, new Bounds(centerX - radius, centerY - radius, radius * 2, radius * 2), ObjectType.PLAYER);
        this.health = health;
        this.speed = speed;
        this.pointerX = pointerX;
        this.pointerY = pointerY;
        this.radius = radius;
        this.centerX = centerX;
        this.centerY  = centerY;
        this.zoneTime = zoneTime;
    }

    public double getHealth() {
        return health;
    }

    public double getSpeed() {
        return speed;
    }

    public double getPointerX() {
        return pointerX;
    }

    public double getPointerY() {
        return pointerY;
    }

    public double getRadius() {
        return radius;
    }

    public double getCenterX() {
        return centerX;
    }

    public double getCenterY() {
        return centerY;
    }
}
