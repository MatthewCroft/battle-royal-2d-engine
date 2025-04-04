package com.example.battleroyalapi.quadtree;

import java.util.ArrayList;
import java.util.List;

public class QuadTree {
    public Bounds bounds;
    private int capacity = 4;
    public List<QuadTreeObject> objects = new ArrayList<>();
    public QuadTree ne, se, nw, sw;
    public boolean subdivide = false;

    public QuadTree(Bounds bounds) {
        this.bounds = bounds;
    }

    public boolean insert(QuadTreeObject object) {
        if (!bounds.contains(object.bounds)) {
            return false;
        }

        if (!subdivide && objects.size() < capacity) {
            objects.add(object);
            return true;
        }

        if (!subdivide) {
            subdivide();
        }

        if (this.ne.insert(object) || this.nw.insert(object) ||
            this.se.insert(object) || this.sw.insert(object)) {
            return true;
        }

        objects.add(object);
        return true;
    }

    public void subdivide() {
        double height = this.bounds.height / 2;
        double width = this.bounds.width / 2;
        double x = this.bounds.x;
        double y = this.bounds.y;

        this.ne = new QuadTree(new Bounds(x + width, y, width, height));
        this.nw = new QuadTree(new Bounds(x, y, width, height));
        this.se = new QuadTree(new Bounds(x + width, y + height, width, height));
        this.sw = new QuadTree(new Bounds(x, y + height, width, height));

        this.subdivide = true;

        List<QuadTreeObject> remaining = List.copyOf(objects);
        objects.clear();

        for (QuadTreeObject object : remaining) {
            this.insert(object);
        }
    }

    public List<QuadTreeObject> queryIntersecting(QuadTreeObject range) {
        List<QuadTreeObject> intersectingNodes = new ArrayList<>();
        queryIntersecting(range, intersectingNodes);
        return intersectingNodes;
    }

    private void queryIntersecting(QuadTreeObject range, List<QuadTreeObject> intersectingNodes) {
        if (!this.bounds.intersects(range.bounds)) return;

        for (QuadTreeObject object : this.objects) {
            if (!range.equals(object) && object.bounds.intersects(range.bounds)) {
                intersectingNodes.add(object);
            }
        }

        if (this.subdivide) {
            this.ne.queryIntersecting(range, intersectingNodes);
            this.nw.queryIntersecting(range, intersectingNodes);
            this.se.queryIntersecting(range, intersectingNodes);
            this.sw.queryIntersecting(range, intersectingNodes);
        }
    }

    public QuadTreeObject query(QuadTreeObject object) {
        int index;
        if ((index = objects.indexOf(object)) != -1) {
            return objects.get(index);
        }

        if (this.subdivide) {
            QuadTreeObject found = null;
            return (found = this.ne.query(object)) != null ||
                    (found = this.nw.query(object)) != null ||
                    (found = this.se.query(object)) != null ||
                    (found = this.sw.query(object)) != null ? found : null;
        }

        return null;
    }

    public QuadTreeObject remove(QuadTreeObject object) {
        int index;
        if (!this.bounds.intersects(object.bounds)) return null;
        if ((index = objects.indexOf(object)) != -1) {
            QuadTreeObject removed = objects.get(index);
            objects.remove(index);
            return removed;
        }

        if (this.subdivide) {
            QuadTreeObject removedObject = null;
            return (removedObject = this.ne.remove(object)) != null ||
                    (removedObject = this.nw.remove(object)) != null ||
                    (removedObject = this.se.remove(object)) != null ||
                    (removedObject = this.sw.remove(object)) != null ? removedObject : null;
        }

        return null;
    }

    public List<QuadTreeObject> getObjects() {
        return this.objects;
    }

    public void print(String prefix) {
        System.out.println(prefix + "Node " + this.bounds + " contains:");
        for (QuadTreeObject obj : objects) {
            System.out.println(prefix + "  ↳ " + obj);
        }

        if (this.subdivide) {
            ne.print(prefix + "  ");
            nw.print(prefix + "  ");
            se.print(prefix + "  ");
            sw.print(prefix + "  ");
        }
    }
}
