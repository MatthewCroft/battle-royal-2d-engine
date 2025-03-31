
export class BulletManager {
    constructor(treeUUID) {
        this.speed = 3;
        this.radius = 4;
        this.treeUUID = treeUUID;
        this.bullets = new Map();
        this.pendingDeletes = new Set();
    }
    async fireBullet(x, y, angle, bulletId) {
        const offset = 30;
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
            radius: this.radius
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
            graphics.fillStyle(0xffff00);
            graphics.fillCircle(bullet.centerX, bullet.centerY, this.radius);
        }
    }
}