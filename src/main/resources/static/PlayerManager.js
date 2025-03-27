export class PlayerManager {
    constructor(player, treeUUID) {
        this.treeUUID = treeUUID;
        this.type = "player";
        this.id = player.id;
        this.health = player.health;
        this.speed = player.speed;
        this.pointerX = player.pointerX;
        this.pointerY = player.pointerY;
        this.bounds = {
            x: player.bounds.x,
            y: player.bounds.y,
            width: player.bounds.width,
            height: player.bounds.height
        }
    }

    updateInput(keys, pointer) {
        const prevX = this.bounds.x;
        const prevY = this.bounds.y;
        let currentX = this.bounds.x;
        let currentY = this.bounds.y;
        let currentPointerX;
        let currentPointerY;
        const prevPointerX = this.pointerX;
        const prevPointerY = this.pointerY;

        // the client player will update when the server responds
        if (keys.W.isDown) currentY = this.bounds.y - this.speed;
        if (keys.S.isDown) currentY = this.bounds.y + this.speed;
        if (keys.A.isDown) currentX = this.bounds.x - this.speed;
        if (keys.D.isDown) currentX = this.bounds.x + this.speed;

        const angle = Phaser.Math.Angle.Between(this.bounds.x, this.bounds.y, pointer.worldX, pointer.worldY);
        currentPointerX = this.bounds.x + Math.cos(angle) * 25;
        currentPointerY = this.bounds.y + Math.sin(angle) * 25;
        const withinBounds = currentX >= 0 && currentX <= 600 &&
            currentY >= 0 && currentY <= 600;
        if (withinBounds && (parseInt(currentX) !== parseInt(prevX) || parseInt(currentY) !== parseInt(prevY) ||
            parseInt(prevPointerY) !== parseInt(currentPointerY) || parseInt(prevPointerX) !== parseInt(currentPointerX))) {
            this.sendPlayerMoveUpdate(currentX, currentY,
                currentPointerX, currentPointerY);
            this.bounds.x = currentX;
            this.bounds.y = currentY;
            this.pointerY = currentPointerY;
            this.pointerX = currentPointerX;
        }
    }

    async sendPlayerMoveUpdate(currentX, currentY, pointerX, pointerY) {
        const playerObj = {
            id: this.id,
            type: "player",
            health: this.health,
            speed: this.speed,
            pointerX: pointerX,
            pointerY: pointerY,
            bounds: {
                x: currentX,
                y: currentY,
                width: this.bounds.width,
                height: this.bounds.height
            }
        };

        await fetch(`/api/quadtree/${this.treeUUID}/player`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(playerObj)
        });
    }

    updateFromServer(serverPlayer) {
        const dx = serverPlayer.bounds.x - this.bounds.x;
        const dy = serverPlayer.bounds.y - this.bounds.y;
        const distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > 5) {
            this.bounds.x = Phaser.Math.Linear(this.bounds.x, serverPlayer.bounds.x, 0.2);
            this.bounds.y = Phaser.Math.Linear(this.bounds.y, serverPlayer.bounds.y, 0.2);
        } else {
            this.bounds.x = serverPlayer.bounds.x;
            this.bounds.y = serverPlayer.bounds.y;
        }

        const pointerDX = serverPlayer.pointerX - this.pointerX;
        const pointerDY = serverPlayer.pointerY - this.pointerY;
        const pointerDistance = Math.sqrt(pointerDX * pointerDX + pointerDY * pointerDY);

        if (pointerDistance > 5) {
            this.pointerX = Phaser.Math.Linear(this.pointerX, serverPlayer.pointerX, 0.2);
            this.pointerY = Phaser.Math.Linear(this.pointerY, serverPlayer.pointerY, 0.2);
        } else {
            this.pointerX = serverPlayer.pointerX;
            this.pointerY = serverPlayer.pointerY;
        }
    }



    draw(graphics) {
        graphics.fillStyle(0x44ff44);
        graphics.fillCircle(this.bounds.x, this.bounds.y, 20);

        // Draw direction line
        graphics.lineStyle(2, 0xffffff);
        graphics.beginPath();
        graphics.moveTo(this.bounds.x, this.bounds.y);
        graphics.lineTo(this.pointerX, this.pointerY);
        graphics.strokePath();
    }
}