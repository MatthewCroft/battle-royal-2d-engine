package com.example.battleroyalapi.controller;

import com.example.battleroyalapi.model.GameInstance;
import com.example.battleroyalapi.model.QuadTreeResponse;
import com.example.battleroyalapi.quadtree.QuadTree;
import com.example.battleroyalapi.quadtree.QuadTreeObject;
import com.example.battleroyalapi.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/quadtree")
public class WallController {
    private GameService gameService;

    public WallController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping(value = "/{id}/wall")
    public ResponseEntity<List<QuadTreeObject>> getWallTree(@PathVariable String id) {
        GameInstance gameInstance = gameService.getGameInstance(id);
        List<QuadTreeObject> walls = new ArrayList<>();
        QuadTreeResponse.fromQuadTree(gameInstance.wallTree, walls);
        return ResponseEntity.ok(walls);
    }
}
