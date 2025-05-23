package com.example.battleroyalapi.controller;

import com.example.battleroyalapi.model.Bullet;
import com.example.battleroyalapi.model.GameInstance;
import com.example.battleroyalapi.model.QuadTreeResponse;
import com.example.battleroyalapi.quadtree.QuadTreeObject;
import com.example.battleroyalapi.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/api/quadtree")
public class BulletController {

    private GameService gameService;

    public BulletController(GameService gameService) {
        this.gameService = gameService;
    }

    Logger logger = LoggerFactory.getLogger(BulletController.class);

    @PostMapping("/{id}/bullet")
    public void fireBullet(@PathVariable String id, @RequestBody Bullet bullet) {
        GameInstance gameInstance = gameService.getGameInstance(id);
        logger.info("[BulletFired] player={} fired bullet={} at x={} y={} at angle={}",
                bullet.getPlayer(), bullet.id, bullet.getCenterX(), bullet.getCenterY(), bullet.getAngle());

        gameInstance.bulletLock.withWrite(b -> {
            gameInstance.bullets.add(b);
        }, bullet);
    }

    @GetMapping("{id}/bullet")
    public ResponseEntity<List<QuadTreeObject>> getBulletTree(@PathVariable String id) {
        GameInstance gameInstance = gameService.getGameInstance(id);
        List<QuadTreeObject> bullets = new ArrayList<>();
        QuadTreeResponse.fromQuadTree(gameInstance.bulletTree, bullets);
        return ResponseEntity.ok(bullets);
    }
}
