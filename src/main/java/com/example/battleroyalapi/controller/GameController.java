package com.example.battleroyalapi.controller;

import com.example.battleroyalapi.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quadtree")
public class GameController {
    private GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<String> createSimpleQuadTree(@RequestParam String uuid) {
        gameService.createSimpleQuadTree(uuid);
        return ResponseEntity.ok("Created");
    }
}
