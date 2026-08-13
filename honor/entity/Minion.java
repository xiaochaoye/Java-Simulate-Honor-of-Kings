package honor.entity;

import honor.core.FMath;
import honor.core.IsoMath;
import honor.G2D;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import honor.Sprite;

public class Minion extends Entity {
    public static final int KIND_MELEE = 0;
    public static final int KIND_RANGED = 1;
    public static final int KIND_SIEGE = 2;
    public static final int KIND_SUPER = 3;
    public static final int FRAME_W = 20;
    public static final int FRAME_H = 26;
    private static final int MOVE_SPEED = 98304;
    private static final int HP_PER_WAVE = 12;
    private static final int ATK_PER_WAVE = 1;
    private Sprite spriteBlue;
    private Sprite spriteRed;
    private Sprite sprite;
    private int[] path;
    private int pathIndex;
    private int animTick;
    private int facing;
    public int kind = 0;
    private int stuckFrames;
    private int unstickDir;
    private static final int LANE_CORRIDOR = 34;
    private static final int SOFT_STUCK_LIMIT = 18;
    private static final int[] UNSTICK_X = new int[]{1, -1, 0, 0, 1, -1, 1, -1, 2, -2, 1, -1};
    private static final int[] UNSTICK_Y = new int[]{0, 0, 1, -1, 1, 1, -1, -1, 1, 1, 2, 2};
    private int navGoalX;
    private int navGoalY;
    private int navBestD2 = Integer.MAX_VALUE;
    private int softStuckFrames;

    public Minion() {
        super(0, 0, 0, 2);
        this.alive = false;
    }

    public void setSheets(BufferedImage var1, BufferedImage var2) {
        this.spriteBlue = new Sprite(var1, 20, 26);
        this.spriteBlue.defineReferencePixel(10, 25);
        this.spriteRed = new Sprite(var2, 20, 26);
        this.spriteRed.defineReferencePixel(10, 25);
        this.sprite = this.spriteBlue;
    }

    public void spawn(int var1, int[] var2, int var3, int var4, int var5, int var6) {
        this.team = var1;
        this.path = var2;
        this.pathIndex = 0;
        this.x = var3;
        this.y = var4;
        this.alive = true;
        this.kind = var6;
        this.shield = 0;
        this.target = null;
        this.animTick = 0;
        this.vx = 0;
        this.vy = 0;
        this.stuckFrames = 0;
        this.softStuckFrames = 0;
        this.navBestD2 = Integer.MAX_VALUE;
        this.applyKindStats(var5);
        this.sprite = var1 == 0 ? this.spriteBlue : this.spriteRed;

    }

    public void spawn(int var1, int[] var2, int var3, int var4, int var5) {
        this.spawn(var1, var2, var3, var4, var5, 0);
    }

    private void applyKindStats(int var1) {
        if (this.kind == 1) {
            this.maxHp = 110 + var1 * 10;
            this.atk = 11 + var1 * 1;
            this.def = 0;
            this.atkRange = 48;
            this.atkCooldown = 20;
            this.radius = 6;
            this.ranged = true;
        } else if (this.kind == 2) {
            this.maxHp = 280 + var1 * 16;
            this.atk = 18 + var1 * 1;
            this.def = 4;
            this.atkRange = 40;
            this.atkCooldown = 24;
            this.radius = 9;
            this.ranged = true;
        } else if (this.kind == 3) {
            this.maxHp = 420 + var1 * 20;
            this.atk = 22 + var1 * 2;
            this.def = 6;
            this.atkRange = 28;
            this.atkCooldown = 16;
            this.radius = 10;
            this.ranged = false;
        } else {
            this.maxHp = 150 + var1 * 12;
            this.atk = 10 + var1 * 1;
            this.def = 0;
            this.atkRange = 22;
            this.atkCooldown = 18;
            this.radius = 7;
            this.ranged = false;
        }

        this.hp = this.maxHp;
        this.atkCdLeft = 0;
    }

    public void update(int var1) {
        if (this.alive) {
            if (this.atkCdLeft > 0) {
                --this.atkCdLeft;
            }

            if (this.target != null && (!this.target.alive || !this.isEnemy(this.target))) {
                this.target = null;
            }

            if (this.target != null && !this.isNearLane(this.target.x, this.target.y, 46)) {
                this.target = null;
            }

            if (this.target != null) {
                this.vx = 0;
                this.vy = 0;
                this.faceToward(this.target.x, this.target.y);
                if (!this.inRange(this.target)) {
                    this.moveToward(this.target.x, this.target.y);
                }
            } else {
                this.followPath();
            }

            boolean var2 = this.vx != 0 || this.vy != 0;
            int var3 = this.x;
            int var4 = this.y;
            if (this.terrain != null) {
                this.moveWithCollision(this.terrain.getPixelWidth(), this.terrain.getPixelHeight());
            } else {
                this.integratePosition();
            }

            if (var2 && this.x == var3 && this.y == var4) {
                ++this.stuckFrames;
                if (this.stuckFrames >= 4) {
                    this.tryUnstick();
                    this.stuckFrames = 0;
                }
            } else {
                this.stuckFrames = 0;
            }

            this.updateNavigationProgress(var2);
            ++this.animTick;
            if (this.sprite != null) {
                int var5 = this.animTick / 5 & 1;
                this.sprite.setFrame(this.facing * 2 + var5);
            }

        }
    }

    private void tryUnstick() {
        if (this.terrain != null) {
            int var1 = this.terrain.getPixelWidth();
            int var2 = this.terrain.getPixelHeight();
            int var3 = 98304;
            int var4 = this.x;
            int var5 = this.y;
            if (this.path != null && this.pathIndex * 2 + 1 < this.path.length) {
                var4 = this.path[this.pathIndex * 2];
                var5 = this.path[this.pathIndex * 2 + 1];
            }

            int var6 = var4 - this.x;
            int var7 = var5 - this.y;
            int var8 = this.unstickDir % UNSTICK_X.length;

            for(int var9 = 0; var9 < 2; ++var9) {
                for(int var10 = 0; var10 < UNSTICK_X.length; ++var10) {
                    int var11 = (var8 + var10) % UNSTICK_X.length;
                    int var12 = UNSTICK_X[var11];
                    int var13 = UNSTICK_Y[var11];
                    if (var9 != 0 || var12 * var6 + var13 * var7 > 0) {
                        int var14 = this.x;
                        int var15 = this.y;
                        this.vx = var12 * var3;
                        this.vy = var13 * var3;
                        this.moveWithCollision(var1, var2);
                        if ((this.x != var14 || this.y != var15) && this.isNearLane(this.x, this.y, 34)) {
                            this.unstickDir = var8 + var10 + 1;
                            this.faceToward(var4, var5);
                            return;
                        }

                        this.x = var14;
                        this.y = var15;
                    }
                }
            }

            for(int var16 = 4; var16 <= 32; var16 += 4) {
                for(int var17 = 0; var17 < 8; ++var17) {
                    int var18 = var17 * 45;
                    int var19 = this.x + (FMath.cosDeg(var18) * var16 >> 16);
                    int var20 = this.y + (FMath.sinDeg(var18) * var16 >> 16);
                    if (this.canStand(var19, var20) && this.isNearLane(var19, var20, 34)) {
                        this.x = var19;
                        this.y = var20;
                        ++this.unstickDir;
                        this.vx = 0;
                        this.vy = 0;
                        return;
                    }
                }
            }

            ++this.unstickDir;
            this.vx = 0;
            this.vy = 0;
        }
    }

    private void updateNavigationProgress(boolean var1) {
        if (!var1) {
            this.navBestD2 = Integer.MAX_VALUE;
            this.softStuckFrames = 0;
        } else {
            int var2 = FMath.dist2(this.x, this.y, this.navGoalX, this.navGoalY);
            if (this.navBestD2 == Integer.MAX_VALUE) {
                this.navBestD2 = var2;
                this.softStuckFrames = 0;
            } else if (var2 + 8 < this.navBestD2) {
                this.navBestD2 = var2;
                this.softStuckFrames = 0;
            } else {
                ++this.softStuckFrames;
                if (this.softStuckFrames >= 18) {
                    if (this.target != null) {
                        this.target = null;
                    }

                    this.tryUnstick();
                    this.navBestD2 = Integer.MAX_VALUE;
                    this.softStuckFrames = 0;
                }
            }

        }
    }

    public boolean isLaneTarget(Entity var1) {
        return var1 != null && this.isNearLane(var1.x, var1.y, 46);
    }

    private boolean isNearLane(int var1, int var2, int var3) {
        if (this.path != null && this.path.length >= 4) {
            int var4 = var3 * var3;

            for(int var5 = 0; var5 + 3 < this.path.length; var5 += 2) {
                int var6 = this.path[var5];
                int var7 = this.path[var5 + 1];
                int var8 = this.path[var5 + 2];
                int var9 = this.path[var5 + 3];
                int var10 = var8 - var6;
                int var11 = var9 - var7;
                int var12 = var1 - var6;
                int var13 = var2 - var7;
                int var14 = var10 * var10 + var11 * var11;
                int var15 = var6;
                int var16 = var7;
                if (var14 > 0) {
                    int var17 = var12 * var10 + var13 * var11;
                    if (var17 >= var14) {
                        var15 = var8;
                        var16 = var9;
                    } else if (var17 > 0) {
                        var15 = var6 + var10 * var17 / var14;
                        var16 = var7 + var11 * var17 / var14;
                    }
                }

                int var19 = var1 - var15;
                int var18 = var2 - var16;
                if (var19 * var19 + var18 * var18 <= var4) {
                    return true;
                }
            }

            return false;
        } else {
            return true;
        }
    }

    protected boolean canNavigateTo(int var1, int var2) {
        return this.canStand(var1, var2) && this.isNearLane(var1, var2, 34);
    }

    public boolean wantsAttack() {
        return this.alive && this.target != null && this.target.alive && this.inRange(this.target) && this.atkCdLeft <= 0;
    }

    public boolean inRange(Entity var1) {
        if (var1 == null) {
            return false;
        } else {
            int var2 = this.atkRange;
            if (var1.unitType == 3 || var1.unitType == 4) {
                var2 = 42;
            }

            int var3 = var2 + var1.radius;
            return this.dist2To(var1) <= var3 * var3;
        }
    }

    public void render(Graphics2D var1, int var2, int var3) {
        if (this.alive && this.sprite != null) {
            int var4 = IsoMath.toScreenX(this.x, this.y) - var2;
            int var5 = IsoMath.toScreenY(this.x, this.y) - var3;
            var1.setColor(G2D.color(0));
            int var6 = this.kind != 2 && this.kind != 3 ? 7 : 10;
            var1.fillArc(var4 - var6, var5 - 2, var6 * 2, 4, 0, 360);
            this.sprite.setRefPixelPosition(var4, var5);
            this.sprite.paint(var1);
            if (this.kind == 1) {
                var1.setColor(G2D.color(16769120));
                var1.fillRect(var4 - 3, var5 - 26 - 2, 6, 2);
            } else if (this.kind == 2) {
                var1.setColor(G2D.color(16747056));
                var1.fillRect(var4 - 4, var5 - 26 - 2, 8, 2);
            } else if (this.kind == 3) {
                var1.setColor(G2D.color(12607743));
                var1.fillRect(var4 - 4, var5 - 26 - 2, 8, 2);
            }

            this.drawHpBar(var1, var4, var5, this.kind == 3 ? 18 : 14, 24);
        }
    }

    private void followPath() {
        if (this.path != null && this.pathIndex * 2 + 1 < this.path.length) {
            int var1 = this.path[this.pathIndex * 2];
            int var2 = this.path[this.pathIndex * 2 + 1];
            int var3 = FMath.dist2(this.x, this.y, var1, var2);
            short var4 = 576;
            if (this.terrain != null && this.terrain.hitsSolid(var1, var2, 12)) {
                var4 = 1600;
            }

            if (var3 <= var4) {
                ++this.pathIndex;
                this.vx = 0;
                this.vy = 0;
            } else {
                this.moveToward(var1, var2);
                this.faceToward(var1, var2);
            }
        } else {
            this.vx = 0;
            this.vy = 0;
        }
    }

    private void moveToward(int var1, int var2) {
        if (FMath.dist2(this.navGoalX, this.navGoalY, var1, var2) > 144) {
            this.navBestD2 = Integer.MAX_VALUE;
            this.softStuckFrames = 0;
        }

        this.navGoalX = var1;
        this.navGoalY = var2;
        this.steerToward(var1, var2, 98304);
    }

    private void faceToward(int var1, int var2) {
        int var3 = var1 - this.x;
        int var4 = var2 - this.y;
        if (var3 != 0 || var4 != 0) {
            if (FMath.abs(var3) >= FMath.abs(var4)) {
                this.facing = var3 < 0 ? 1 : 2;
            } else {
                this.facing = var4 < 0 ? 3 : 0;
            }

        }
    }
}
