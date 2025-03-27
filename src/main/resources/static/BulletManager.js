
export class BulletManager {
    constructor(treeUUID) {
        this.speed = 3;
        this.radius = 4;
        this.treeUUID = treeUUID;
        this.bullets = new Map();
        this.pendingDeletes = new Set();
    }
    async fireBullet(x, y, angle, bulletId) {
        const bullet = {
            type: "bullet",
            angle,
            velocityX: Math.cos(angle) * this.speed,
            velocityY: Math.sin(angle) * this.speed,
            id: bulletId,
            targetX: null,
            targetY: null,
            bounds: {
                x,
                y,
                width: 5,
                height: 5
            }
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

    update() {
        for (let bullet of this.bullets.values()) {
            if (this.pendingDeletes.has(bullet.id)) {
                this.bullets.delete(bullet.id);
            }

            bullet.bounds.x += bullet.velocityX;
            bullet.bounds.y += bullet.velocityY;

            if (bullet.targetX !== null && bullet.targetY !== null
                && this.distance(bullet.targetX, bullet.bounds.x, bullet.targetY, bullet.bounds.y) > 5) {
                bullet.bounds.x = Phaser.Math.Linear(bullet.bounds.x, bullet.targetX, 0.1);
                bullet.bounds.y = Phaser.Math.Linear(bullet.bounds.y, bullet.targetY, 0.1);
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
            graphics.fillStyle(0xffff00);
            graphics.fillCircle(bullet.bounds.x, bullet.bounds.y, this.radius);
        }
    }
}