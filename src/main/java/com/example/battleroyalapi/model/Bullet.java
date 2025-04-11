package com.example.battleroyalapi.model;

import com.example.battleroyalapi.quadtree.Bounds;
import com.example.battleroyalapi.quadtree.QuadTreeObject;
import com.fasterxml.jackson.annotation.JsonTypeName;

public class Bullet extends QuadTreeObject {
    double velocityX;
    double velocityY;
    double angle;
    Double targetX;
    Double targetY;
    double radius;
    double centerX;
    double centerY;
    String player;

    public Bullet(String id, double velocityX, double velocityY, double angle, Double targetX, Double targetY, double radius, double centerX, double centerY, String player) {
        super(id, new Bounds(centerX - radius, centerY - radius, radius*2, radius*2), ObjectType.BULLET);
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.angle = angle;
        this.targetX = targetX;
        this.targetY = targetY;
        this.centerX = centerX;
        this.radius = radius;
        this.centerY = centerY;
        this.player = player;
    }

    public void update() {
        this.centerX += this.velocityX;
        this.centerY += this.velocityY;
        this.bounds.x = this.centerX - this.radius;
        this.bounds.y = this.centerY - this.radius;
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

    public Double getTargetX() {
        return targetX;
    }

    public Double getTargetY() {
        return targetY;
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

    public String getPlayer() {
        return player;
    }
}
