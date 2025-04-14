package com.example.battleroyalapi.model;

import com.example.battleroyalapi.quadtree.Bounds;
import com.example.battleroyalapi.quadtree.QuadTree;
import com.example.battleroyalapi.quadtree.QuadTreeObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QuadTreeResponse {
    Bounds bounds;
    List<QuadTreeObject> objects = new ArrayList<>();
    List<QuadTreeResponse> children = new ArrayList<>();

    public QuadTreeResponse(){}

    public static QuadTreeResponse fromQuadTree(QuadTree tree) {
        QuadTreeResponse quadTreeResponse = new QuadTreeResponse();
        quadTreeResponse.bounds = tree.bounds;

        quadTreeResponse.objects.addAll(tree.objects);

        if (tree.subdivide) {
            quadTreeResponse.children.add(fromQuadTree(tree.nw));
            quadTreeResponse.children.add(fromQuadTree(tree.ne));
            quadTreeResponse.children.add(fromQuadTree(tree.sw));
            quadTreeResponse.children.add(fromQuadTree(tree.se));
        }

        return quadTreeResponse;
    }

    public static void fromQuadTree(QuadTree tree, List<QuadTreeObject> objects) {
        if (!tree.objects.isEmpty()) {
            objects.addAll(tree.objects);
        }

        if (tree.subdivide) {
            fromQuadTree(tree.nw, objects);
            fromQuadTree(tree.ne, objects);
            fromQuadTree(tree.sw, objects);
            fromQuadTree(tree.se, objects);
        }
    }

    public Bounds getBounds() {
        return bounds;
    }

    public List<QuadTreeObject> getObjects() {
        return objects;
    }

    public List<QuadTreeResponse> getChildren() {
        return children;
    }
}
