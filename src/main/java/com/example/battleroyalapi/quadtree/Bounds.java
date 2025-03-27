package com.example.battleroyalapi.quadtree;

public class Bounds {
    public double x;
    public double y;
    double width;
    double height;

    public Bounds(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean contains(Bounds bounds) {
        return bounds.x >= x &&
                bounds.y >= y &&
                bounds.x + bounds.width <= x + width &&
                bounds.y + bounds.height <= y + height;
    }

    public boolean intersects(Bounds bounds) {
        return !(bounds.x > x + width ||
                bounds.x + bounds.width < x ||
                bounds.y + bounds.height < y ||
                bounds.y > y + height);
    }

    @Override
    public String toString() {
        return String.format("[x=%.1f, y=%.1f, w=%.1f, h=%.1f]", x, y, width, height);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}
