package com.example.battleroyalapi.controller;

import com.example.battleroyalapi.model.Bullet;
import com.example.battleroyalapi.model.GameInstance;
import com.example.battleroyalapi.model.Player;
import com.example.battleroyalapi.model.QuadTreeResponse;
import com.example.battleroyalapi.quadtree.QuadTree;
import com.example.battleroyalapi.quadtree.QuadTreeObject;
import com.example.battleroyalapi.service.QuadTreeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Game board
 * 300, 300
 * 150,150
 *
 */
@RestController
@RequestMapping("/api/quadtree")
public class QuadTreeController {
    private QuadTreeService quadTreeService;
    private Logger logger = LoggerFactory.getLogger(QuadTreeController.class);

    public QuadTreeController(QuadTreeService quadTreeService) {
        this.quadTreeService = quadTreeService;
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<QuadTreeResponse> getQuadTree(@PathVariable String id) {
        QuadTree quadTree = quadTreeService.getQuadTree(id);
        QuadTreeResponse response = QuadTreeResponse.fromQuadTree(quadTree);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/player")
    public void moveQuadTreePlayer(@PathVariable String id, @RequestBody Player player) {
        quadTreeService.moveQuadTreePlayer(id, player);
    }

    /**
     * currently only using this to insert players when setting up the game, the service holds the logic for putting
     * the player in the gameInstance map
     */
    @PutMapping("/{id}/start")
    public void setupGameTree(@PathVariable String id, @RequestBody Player quadTreeObject) {
        quadTreeService.insertQuadTreeObject(id, quadTreeObject);
    }

    @PostMapping("/{id}/bullet")
    public void fireBullet(@PathVariable String id, @RequestBody Bullet bullet) {
        GameInstance gameInstance = quadTreeService.getGameInstance(id);
        logger.info("[BulletFired] player={} fired bullet={} at x={} y={} at angle={}",
                bullet.getPlayer(), bullet.id, bullet.getCenterX(), bullet.getCenterY(), bullet.getAngle());

        gameInstance.lock.writeLock().lock();
        try {
            gameInstance.bullets.add(bullet);
        } finally {
            gameInstance.lock.writeLock().unlock();
        }
    }

    @PostMapping
    public ResponseEntity<String> createSimpleQuadTree(@RequestParam String uuid) {
        quadTreeService.createSimpleQuadTree(uuid);
        return ResponseEntity.ok("Created");
    }
}
