
export class BulletManager {
    constructor(treeUUID) {
        this.speed = 6;
        this.radius = 7;
        this.treeUUID = treeUUID;
        this.bullets = new Map();
        this.pendingDeletes = new Set();
    }
    async fireBullet(x, y, angle, bulletId, playerId) {
        const offset = 50;
        const bulletX = x + Math.cos(angle) * offset;
        const bulletY = y + Math.sin(angle) * offset;

        const bullet = {
            type: "BULLET",
            angle,
            velocityX: Math.cos(angle) * this.speed,
            velocityY: Math.sin(angle) * this.speed,
            id: bulletId,
            targetX: null,
            targetY: null,
            centerX: bulletX,
            centerY: bulletY,
            radius: this.radius,
            player: playerId
        };

        this.bullets.set(bulletId, bullet);

        await fetch(`/api/quadtree/${this.treeUUID}/bullet`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(bullet)
        })
    }

    update(barriers, opponents) {
        for (let bullet of this.bullets.values()) {
            if (this.pendingDeletes.has(bullet.id)) {
                this.bullets.delete(bullet.id);
                continue;
            }

            //todo: update so that server can respawn bullets if intersecting from phaser does not agree with server
            const bulletCircle = new Phaser.Geom.Circle(bullet.centerX, bullet.centerY, bullet.radius);
            for (const barrier of barriers.values()) {
                if (Phaser.Geom.Intersects.CircleToRectangle(bulletCircle, barrier)) {
                    this.bullets.delete(bullet.id);
                    break;
                }
            }

            for (const opponent of opponents.values()) {
                if (Phaser.Geom.Intersects.CircleToCircle(bulletCircle, opponent)) {
                    this.bullets.delete(bullet.id);
                    break;
                }
            }

            if (!this.bullets.has(bullet.id)) continue;

            bullet.centerX += bullet.velocityX;
            bullet.centerY += bullet.velocityY;

            if (bullet.targetX !== null && bullet.targetY !== null
                && this.distance(bullet.targetX, bullet.centerX, bullet.targetY, bullet.centerY) > 5) {
                bullet.centerX = Phaser.Math.Linear(bullet.centerX, bullet.targetX, 0.1);
                bullet.centerY = Phaser.Math.Linear(bullet.centerY, bullet.targetY, 0.1);
            }
        }
    }

    distance(targetX, x, targetY, y) {
        const dx = targetX - x;
        const dy = targetY - y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    draw(graphics) {
        for (let bullet of this.bullets.values()) {
            if (this.pendingDeletes.has(bullet.id)) continue;
            graphics.fillStyle(0xffff00);
            graphics.fillCircle(bullet.centerX, bullet.centerY, this.radius);
        }
    }
}