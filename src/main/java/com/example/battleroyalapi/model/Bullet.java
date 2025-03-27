package com.example.battleroyalapi.model;

import com.example.battleroyalapi.quadtree.Bounds;
import com.example.battleroyalapi.quadtree.QuadTreeObject;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("bullet")
public class Bullet extends QuadTreeObject {
    double velocityX;
    double velocityY;
    double angle;
    Double targetX;
    Double targetY;
    double radius;

    public Bullet(String id, Bounds bounds, double velocityX, double velocityY, double angle, Double targetX, Double targetY, double radius) {
        super(id, bounds);
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.angle = angle;
        this.targetX = targetX;
        this.targetY = targetY;
    }

    public void update() {
        this.bounds.x += this.velocityX;
        this.bounds.y += this.velocityY;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public double getAngle() {
        return angle;
    }
}
