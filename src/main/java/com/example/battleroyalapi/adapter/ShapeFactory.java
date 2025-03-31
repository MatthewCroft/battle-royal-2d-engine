package com.example.battleroyalapi.adapter;

import com.example.battleroyalapi.model.*;
import com.example.battleroyalapi.quadtree.QuadTreeObject;

public class ShapeFactory {
    public static Shape from(QuadTreeObject object) {
        switch (object.type) {
            case ObjectType.PLAYER:
                Player player = (Player) object;
                return new Circle(player.getCenterX(), player.getCenterY(), player.getRadius());
            case ObjectType.BULLET:
                Bullet bullet = (Bullet) object;
                return new Circle(bullet.getCenterX(), bullet.getCenterY(), bullet.getRadius());
            case ObjectType.WALL:
                return new Rectangle(object.bounds.getX(), object.bounds.getY(), object.bounds.getHeight(), object.bounds.getWidth());
            default:
                throw new RuntimeException("this quadtree object does not conform to a shape");
        }
    }
}
