package com.example.battleroyalapi.model;

import com.example.battleroyalapi.quadtree.Bounds;
import com.example.battleroyalapi.quadtree.QuadTreeObject;
import com.fasterxml.jackson.annotation.JsonTypeName;

public class Player extends QuadTreeObject {
    public double health;
    double speed;
    double radius;
    public double centerX;
    public double centerY;
    public double angle;
    public Player(String id, double health, double speed, double centerX, double centerY, double radius, double angle) {
        super(id, new Bounds(centerX - radius, centerY - radius, radius * 2, radius * 2), ObjectType.PLAYER);
        this.health = health;
        this.speed = speed;
        this.radius = radius;
        this.centerX = centerX;
        this.centerY  = centerY;
        this.angle = angle;
    }

    public double getAngle() {
        return angle;
    }

    public double getHealth() {
        return health;
    }

    public double getSpeed() {
        return speed;
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
