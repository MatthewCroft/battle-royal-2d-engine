package com.example.battleroyalapi.quadtree;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Error json
 * {
 *     "bounds": {
 *         "x": 0.0,
 *         "y": 0.0,
 *         "width": 600.0,
 *         "height": 600.0
 *     },
 *     "objects": [
 *         {
 *             "x": 292.0,
 *             "y": 264.0,
 *             "width": 20.0,
 *             "height": 20.0
 *         }
 *     ],
 *     "children": [
 *         {
 *             "bounds": {
 *                 "x": 0.0,
 *                 "y": 0.0,
 *                 "width": 300.0,
 *                 "height": 300.0
 *             },
 *             "objects": [],
 *             "children": []
 *         },
 *         {
 *             "bounds": {
 *                 "x": 300.0,
 *                 "y": 0.0,
 *                 "width": 300.0,
 *                 "height": 300.0
 *             },
 *             "objects": [],
 *             "children": [
 *                 {
 *                     "bounds": {
 *                         "x": 300.0,
 *                         "y": 0.0,
 *                         "width": 150.0,
 *                         "height": 150.0
 *                     },
 *                     "objects": [],
 *                     "children": []
 *                 },
 *                 {
 *                     "bounds": {
 *                         "x": 450.0,
 *                         "y": 0.0,
 *                         "width": 150.0,
 *                         "height": 150.0
 *                     },
 *                     "objects": [],
 *                     "children": []
 *                 },
 *                 {
 *                     "bounds": {
 *                         "x": 300.0,
 *                         "y": 150.0,
 *                         "width": 150.0,
 *                         "height": 150.0
 *                     },
 *                     "objects": [
 *                         {
 *                             "x": 292.0,
 *                             "y": 264.0,
 *                             "width": 20.0,
 *                             "height": 20.0
 *                         },
 *                         {
 *                             "x": 292.0,
 *                             "y": 264.0,
 *                             "width": 20.0,
 *                             "height": 20.0
 *                         },
 *                         {
 *                             "x": 292.0,
 *                             "y": 264.0,
 *                             "width": 20.0,
 *                             "height": 20.0
 *                         },
 *                         {
 *                             "x": 292.0,
 *                             "y": 264.0,
 *                             "width": 20.0,
 *                             "height": 20.0
 *                         }
 *                     ],
 *                     "children": []
 *                 },
 *                 {
 *                     "bounds": {
 *                         "x": 450.0,
 *                         "y": 150.0,
 *                         "width": 150.0,
 *                         "height": 150.0
 *                     },
 *                     "objects": [
 *                         {
 *                             "x": 462.0,
 *                             "y": 170.0,
 *                             "width": 20.0,
 *                             "height": 20.0
 *                         }
 *                     ],
 *                     "children": []
 *                 }
 *             ]
 *         },
 *         {
 *             "bounds": {
 *                 "x": 0.0,
 *                 "y": 300.0,
 *                 "width": 300.0,
 *                 "height": 300.0
 *             },
 *             "objects": [],
 *             "children": []
 *         },
 *         {
 *             "bounds": {
 *                 "x": 300.0,
 *                 "y": 300.0,
 *                 "width": 300.0,
 *                 "height": 300.0
 *             },
 *             "objects": [],
 *             "children": []
 *         }
 *     ]
 * }
 */
public class QuadTreeTest {
//
//    @Test
//    public void testQuadTreeInsert() {
//        QuadTree quadTree = new QuadTree(new Bounds(0, 0, 100, 100));
//        QuadTreeObject quadTreeObject = new QuadTreeObject(new Bounds(3, 3, 20, 20));
//
//        assertTrue(quadTree.insert(quadTreeObject));
//        assertEquals(1, quadTree.getObjects().size());
//        assertEquals(quadTreeObject, quadTree.getObjects().get(0));
//    }
//
//    @Test
//    public void testQuadTreeSubdivides() {
//        QuadTree quadTree = new QuadTree(new Bounds(0, 0, 100, 100));
//        QuadTreeObject quadTreeObject1 = new QuadTreeObject(new Bounds(3, 3, 20, 20));
//        QuadTreeObject quadTreeObject2 = new QuadTreeObject(new Bounds(24, 24, 20, 20));
//        QuadTreeObject quadTreeObject3 = new QuadTreeObject(new Bounds(50, 3, 20, 20));
//        QuadTreeObject quadTreeObject4 = new QuadTreeObject(new Bounds(74, 24, 20, 20));
//        QuadTreeObject quadTreeObject5 = new QuadTreeObject(new Bounds(0, 75, 10, 10));
//
//        assertTrue(quadTree.insert(quadTreeObject1));
//        assertTrue(quadTree.insert(quadTreeObject2));
//        assertTrue(quadTree.insert(quadTreeObject3));
//        assertTrue(quadTree.insert(quadTreeObject4));
//        assertTrue(quadTree.insert(quadTreeObject5));
//        assertEquals(2, quadTree.nw.getObjects().size());
//        assertEquals(2, quadTree.ne.getObjects().size());
//        assertEquals(1, quadTree.sw.getObjects().size());
//    }
//
//    @Test
//    public void testQuadTreeQueryIntersecting() {
//        QuadTree quadTree = new QuadTree(new Bounds(0, 0, 100, 100));
//        QuadTreeObject quadTreeObject1 = new QuadTreeObject(new Bounds(3, 3, 20, 20));
//
//        assertTrue(quadTree.insert(quadTreeObject1));
//
//        List<QuadTreeObject> intersectingObjects = quadTree.queryIntersecting(new QuadTreeObject(new Bounds(5, 5, 10, 10)));
//
//        assertEquals(quadTreeObject1, intersectingObjects.get(0));
//    }
//
//    @Test
//    public void testQuadTreeRemoveObject() {
//        QuadTree quadTree = new QuadTree(new Bounds(0, 0, 100, 100));
//        QuadTreeObject quadTreeObject1 = new QuadTreeObject(new Bounds(3, 3, 20, 20));
//
//        assertTrue(quadTree.insert(quadTreeObject1));
//        assertEquals(1, quadTree.getObjects().size());
//
//        quadTree.remove(quadTreeObject1);
//        assertEquals(0, quadTree.getObjects().size());
//    }
}
