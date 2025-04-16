package com.example.battleroyalapi.service;

import com.example.battleroyalapi.model.GameInstance;
import com.example.battleroyalapi.quadtree.QuadTreeObject;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WallService {
    public List<QuadTreeObject> intersectingWalls(GameInstance gameInstance, QuadTreeObject quadTreeObject) {
        return gameInstance.wallLock.withRead(treeObject -> {
            return gameInstance.wallTree.queryIntersecting(treeObject);
        }, quadTreeObject);
    }
}
