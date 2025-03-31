package com.example.battleroyalapi.service;

import com.example.battleroyalapi.model.Circle;
import com.example.battleroyalapi.model.Rectangle;
import com.example.battleroyalapi.quadtree.QuadTreeObject;
import org.springframework.stereotype.Service;

@Service
public class CollisionService {
   public boolean isCircleRectangleInsecting(Circle circle, Rectangle rectangle) {
       double closestX = clamp(circle.getX(), rectangle.getX(), rectangle.getX() + rectangle.getWidth());
       double closestY = clamp(circle.getY(), rectangle.getY(), rectangle.getY() + rectangle.getHeight());

       double dx = circle.getX() - closestX;
       double dy = circle.getY() - closestY;
       return (dx * dx + dy * dy) <= (circle.getRadius() * circle.getRadius());
    }

    public boolean isCircleCircleIntersecting(Circle c1, Circle c2) {
       double dx = c1.getX() - c2.getX();
       double dy = c1.getY() - c2.getY();
       double radius = c1.getRadius() + c2.getRadius();
       return dx * dx + dy * dy <= radius * radius;
    }

    private double clamp (double value, double min, double max) {
       return Math.max(min, Math.min(max, value));
    }
}
