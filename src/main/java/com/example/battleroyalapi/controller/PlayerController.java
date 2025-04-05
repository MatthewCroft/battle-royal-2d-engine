package com.example.battleroyalapi.controller;

import com.example.battleroyalapi.model.Bullet;
import com.example.battleroyalapi.model.GameInstance;
import com.example.battleroyalapi.model.Player;
import com.example.battleroyalapi.model.QuadTreeResponse;
import com.example.battleroyalapi.quadtree.QuadTree;
import com.example.battleroyalapi.service.PlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Game board
 * 300, 300
 * 150,150
 */
@RestController
@RequestMapping("/api/quadtree")
public class PlayerController {
    private PlayerService playerService;
    private Logger logger = LoggerFactory.getLogger(PlayerController.class);

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping(value = "/{id}/player")
    public ResponseEntity<QuadTreeResponse> getQuadTree(@PathVariable String id) {
        QuadTree quadTree = playerService.getPlayerQuadTree(id);
        QuadTreeResponse response = QuadTreeResponse.fromQuadTree(quadTree);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/player")
    public void moveQuadTreePlayer(@PathVariable String id, @RequestBody Player player) {
        playerService.movePlayer(id, player);
    }

    /**
     * currently only using this to insert players when setting up the game, the service holds the logic for putting
     * the player in the gameInstance map
     */
    @PutMapping("/{id}/start")
    public void setupGameTree(@PathVariable String id, @RequestBody Player quadTreeObject) {
        playerService.insertPlayer(id, quadTreeObject);
    }
}
