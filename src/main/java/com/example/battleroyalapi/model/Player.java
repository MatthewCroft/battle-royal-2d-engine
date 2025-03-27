package com.example.battleroyalapi.model;

import com.example.battleroyalapi.quadtree.Bounds;
import com.example.battleroyalapi.quadtree.QuadTreeObject;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("player")
public class Player extends QuadTreeObject {
    double health;
    double speed;
    double pointerX;
    double pointerY;
    double radius;
    public Player(String id, double health, double speed, double pointerX, double pointerY, Bounds bounds, double radius) {
        super(id, bounds);
        this.health = health;
        this.speed = speed;
        this.pointerX = pointerX;
        this.pointerY = pointerY;
        this.radius = radius;
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
}
