import { BulletManager } from "./BulletManager.js";
import {LocalPlayer} from "./LocalPlayer.js";

export class GameScene extends Phaser.Scene {
    constructor() {
        super({ key: 'GameScene' });
        this.lineLength = 30;
        this.uuid = self.crypto.randomUUID();
        this.pendingBulletDeletes = new Set();
        this.keys = null;
        this.pointer = null;
    }

    //todo: update so that we are using uuid's for querying the correct game instance
    create() {
        this.graphics = this.add.graphics();
        this.playerTreeData = null;
        this.bulletTreeData = null;
        this.wallTreeData = null;
        this.playerManager = null;
        this.bulletManager = null;
        this.isReady = false;
        this.barriers = new Map();
        this.opponents = new Map();

        const socket = new SockJS('/ws');
        const stompClient = Stomp.over(socket);

        stompClient.connect({}, () => {
            stompClient.subscribe(`/topic/${this.uuid}`, (message) => {
                const data = JSON.parse(message.body);

                if (data.type === "bullet_expired" && this.bulletManager.bullets.has(data.id)) {
                    this.bulletManager.pendingDeletes.add(data.id);
                }
                // play sound
                if (data.type === "player_hit") {
                }

                if (data.type === "player_collision" || data.type === "out_of_bounds" || data.type === "wall_collision") {
                    this.playerManager.collisionCorrectionTime = performance.now() + 75;
                }
            });
        });

        fetch(`/api/quadtree?uuid=${this.uuid}`, {
            method: "POST"
        }).then(async () =>
            await fetch(`/api/quadtree/${this.uuid}`))
            .then(res => res.json())
            .then(data => {
                this.treeData = data;
            }).then(async () => {
            const player = {
                type: "PLAYER",
                id: "matt",
                health: 100.0,
                speed: 4.0,
                centerX: 128.0,
                centerY: 1026.0,
                radius: 40.0
            };
            const opponent = {
                type: "PLAYER",
                id: "opponent",
                health: 100.0,
                speed: 2.0,
                centerX: 30.0,
                centerY: 30.0,
                radius: 20.0,
            }

            await fetch(`/api/quadtree/${this.uuid}/start`, {
                method: "PUT",
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(opponent)
            })

            await fetch(`/api/quadtree/${this.uuid}/start`, {
                method: "PUT",
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(player)
            }).then(() => {
                this.playerManager = new LocalPlayer(this, player, this.uuid)
                this.bulletManager = new BulletManager(this.uuid);
                console.log("setup complete")
                this.isReady = true;
            })
        });

        this.input.on("pointerdown", () => {
            const angle = Phaser.Math.Angle.Between(
                this.playerManager.x, this.playerManager.y,
                this.pointer.worldX, this.pointer.worldY
            );
            this.bulletManager.fireBullet(this.playerManager.x, this.playerManager.y, angle, self.crypto.randomUUID(), this.playerManager.id);
        })

        // draw game board boundary (same as QuadTree visualizer size)
        this.graphics.lineStyle(2, 0x007acc);
        this.graphics.strokeRect(0, 0, 600, 600);

        // input keys
        this.keys = this.input.keyboard.addKeys('W,A,S,D');
        // this.input.on("pointermove", async (pointer) => movePlayerPointer(pointer));
        this.pointer = this.input.activePointer;

        setInterval(() => this.syncPlayersServer(this), 50);
        setInterval(() => this.syncWallServer(this), 50);
        setInterval(() => this.syncBulletServer(this), 50);
    }

    async syncPlayersServer() {
        const response = await fetch(`/api/quadtree/${this.uuid}/player`)
        const treeData = await response.json();
        this.playerTreeData = treeData;
        const serverPlayer = treeData.objects.find(object => object && object.id === this.playerManager.id)
        if (serverPlayer) {
            this.playerManager.updateFromServer(serverPlayer);
        }
    }

    async syncBulletServer() {
        const response = await fetch(`/api/quadtree/${this.uuid}/bullet`)
        const treeData = await response.json();
        this.bulletTreeData = treeData;
    }

    async syncWallServer() {
        const response = await fetch(`/api/quadtree/${this.uuid}/wall`)
        const treeData = await response.json();
        this.wallTreeData = treeData;
    }

    update() {
        if (!this.isReady) return;
        this.playerManager.updateInput(this.keys, this.pointer);

        // Redraw player and line
        this.graphics.clear();

        if (this.playerTreeData) {
            this.drawPlayerTree(this.graphics, this.playerTreeData);
        }

        if (this.bulletTreeData) {
            this.drawBulletTree(this.graphics, this.bulletTreeData);
        }

        if (this.wallTreeData) {
            this.drawWallTree(this.graphics, this.wallTreeData);
        }

        this.bulletManager.update(this.barriers, this.opponents);
        this.bulletManager.draw(this.graphics);
        this.playerManager.draw();
    }

    drawPlayerTree(g, tree) {
        if (tree.objects) {
            for (let obj of tree.objects) {
                if (!obj) continue;
                if (obj.type === "PLAYER" && obj.id === this.playerManager.id) {
                    continue;
                }
                // todo: draw player with health updates
                if (obj.type === "PLAYER") {
                    const playerCircle = new Phaser.Geom.Circle(obj.centerX, obj.centerY, obj.radius);
                    this.opponents.set(obj.id, playerCircle);
                    this.drawPlayer(obj, g);
                }
            }

            if (tree.children) {
                for (let child of tree.children) {
                    this.drawPlayerTree(g, child, this);
                }
            }
        }
    }

    drawWallTree(g, tree) {
        if (tree.objects) {
            for (let obj of tree.objects) {
                if (!obj) continue;
                if (obj.type === "ZONE") {
                    this.drawZone(obj, g);
                }

                if (obj.type === "WALL") {
                    const wallBounds = new Phaser.Geom.Rectangle(obj.bounds.x, obj.bounds.y, obj.bounds.width, obj.bounds.height);
                    this.barriers.set(obj.id, wallBounds);
                    this.drawWall(obj, g);
                }
            }

            if (tree.children) {
                for (let child of tree.children) {
                    this.drawWallTree(g, child, this);
                }
            }
        }
    }

    //todo: update to draw each tree (player, wall, bullet)
    drawBulletTree(g, tree) {
        if (tree.objects) {
            for (let obj of tree.objects) {
                if (!obj) continue;
                // existing bullet set targetX and targetY, signifies the server position of the bullet
                if (obj.type === "BULLET" && this.bulletManager.bullets.has(obj.id)) {
                    let bullet = this.bulletManager.bullets.get(obj.id);
                    bullet.targetX = obj.centerX;
                    bullet.targetY = obj.centerY;
                }

                // new bullet
                if (obj.type === "BULLET" && !this.bulletManager.bullets.has(obj.id)) {
                    this.bulletManager.bullets.set(obj.id, {
                        ...obj,
                        targetX: null,
                        targetY: null
                    })
                }
            }

            if (tree.children) {
                for (let child of tree.children) {
                    this.drawBulletTree(g, child, this);
                }
            }
        }
    }

    drawZone(zone, g) {
        g.lineStyle(2, 0xffffff); // thickness = 2, color = white
        g.strokeCircle(zone.centerX, zone.centerY, zone.radius);
        const progress = zone.time / 10;

        g.lineStyle(4, 0x00ff00);
        g.beginPath();
        g.arc(zone.centerX, zone.centerY, zone.radius, -Math.PI/2, -Math.PI/2 + Math.PI * 2 * progress, false);
        g.strokePath();
    }

    drawWall(wall, g) {
        const { x, y, width, height } = wall.bounds;

        g.fillStyle(0x888888); // grey fill color
        g.fillRect(x, y, width, height); // draw the wall
    }

    drawPlayer(player, g) {
        const x = player.centerX;
        const y = player.centerY;
        const radius = player.radius;

        const healthRatio = player.health / 100;

        const fillColor = Phaser.Display.Color.Interpolate.ColorWithColor(
            new Phaser.Display.Color(255, 0, 0),
            new Phaser.Display.Color(0, 255, 0),
            100,
            healthRatio * 100
        );
        const finalColor = Phaser.Display.Color.GetColor(fillColor.r, fillColor.g, fillColor.b);

        g.fillStyle(finalColor, 1);
        g.fillCircle(x, y, radius);

        g.lineStyle(2, 0xffffff);
        g.strokeCircle(x, y, radius);

        // Draw direction line
        g.lineStyle(2, 0xffffff);
        g.beginPath();
        g.moveTo(player.centerX, player.centerY);
        g.lineTo(player.pointerX, player.pointerY);
        g.strokePath();
    }
}