package honor.entity;

import honor.core.FMath;
import honor.core.IsoMath;
import honor.map.TileMap;
import honor.G2D;
import java.awt.Graphics2D;

public abstract class Entity {
    private static final int[] STEER_X = new int[]{1, 1, 0, -1, -1, -1, 0, 1};
    private static final int[] STEER_Y = new int[]{0, 1, 1, 1, 0, -1, -1, -1};
    private int steerSide;
    private int steerSideLeft;
    public static final int TEAM_BLUE = 0;
    public static final int TEAM_RED = 1;
    public static final int TYPE_HERO = 1;
    public static final int TYPE_MINION = 2;
    public static final int TYPE_TOWER = 3;
    public static final int TYPE_CRYSTAL = 4;
    public static final int TYPE_JUNGLE = 5;
    public static final int TEAM_NEUTRAL = 2;
    public int x;
    public int y;
    public int vx;
    public int vy;
    public int radius = 8;
    public boolean alive = true;
    public int team;
    public int unitType;
    public int hp;
    public int maxHp;
    public int atk;
    public int atkRange;
    public int atkCooldown;
    public int atkCdLeft;
    public Entity target;
    public boolean ranged;
    public int def;
    public int shield;
    public TileMap terrain;

    public Entity(int var1, int var2, int var3, int var4) {
        this.x = var1;
        this.y = var2;
        this.team = var3;
        this.unitType = var4;
        this.ranged = false;
    }

    public abstract void update(int var1);

    public abstract void render(Graphics2D var1, int var2, int var3);

    protected void integratePosition() {
        this.x += this.vx >> 16;
        this.y += this.vy >> 16;
    }

    protected void moveWithCollision(int var1, int var2) {
        int var3 = this.vx >> 16;
        int var4 = this.vy >> 16;
        int var5 = this.x;
        int var6 = this.y;
        if (var3 != 0) {
            this.x += var3;
            if (!this.canNavigateTo(this.x, this.y)) {
                this.x = var5;
            }
        }

        if (var4 != 0) {
            int var7 = this.y;
            this.y += var4;
            if (!this.canNavigateTo(this.x, this.y)) {
                this.y = var7;
            }
        }

        if (var3 != 0 && var4 != 0 && this.x == var5 && this.y == var6) {
            this.y = var6 + var4;
            if (!this.canNavigateTo(var5, this.y)) {
                this.y = var6;
            }

            this.x = var5 + var3;
            if (!this.canNavigateTo(this.x, this.y)) {
                this.x = var5;
            }
        }

        this.clampToWorld(var1, var2);
    }

    protected boolean canStand(int var1, int var2) {
        if (this.terrain == null) {
            return true;
        } else if (!this.terrain.isWalkable(var1, var2)) {
            return false;
        } else {
            int var3 = this.radius > 0 ? this.radius : 0;
            if (var3 > 4) {
                byte var4 = 2;
                if (!this.terrain.isWalkable(var1 - var4, var2) || !this.terrain.isWalkable(var1 + var4, var2) || !this.terrain.isWalkable(var1, var2 - var4) || !this.terrain.isWalkable(var1, var2 + var4)) {
                    return false;
                }
            }

            int var5 = var3 > 4 ? 1 : 0;
            return !this.terrain.hitsSolid(var1, var2, var5);
        }
    }

    protected boolean canNavigateTo(int var1, int var2) {
        return this.canStand(var1, var2);
    }

    protected boolean canNavigateSegment(int var1, int var2, int var3, int var4) {
        int var5 = var3 - var1;
        int var6 = var4 - var2;
        int var7 = FMath.max(FMath.abs(var5), FMath.abs(var6));
        int var8 = var7 / 4;
        if (var8 < 1) {
            var8 = 1;
        }

        for(int var9 = 1; var9 <= var8; ++var9) {
            int var10 = var1 + var5 * var9 / var8;
            int var11 = var2 + var6 * var9 / var8;
            if (!this.canNavigateTo(var10, var11)) {
                return false;
            }
        }

        return true;
    }

    protected void steerToward(int var1, int var2, int var3) {
        int var4 = var1 - this.x;
        int var5 = var2 - this.y;
        int var6 = this.vx < 0 ? -1 : (this.vx > 0 ? 1 : 0);
        int var7 = this.vy < 0 ? -1 : (this.vy > 0 ? 1 : 0);
        int var8 = var4 < 0 ? -1 : (var4 > 0 ? 1 : 0);
        int var9 = var5 < 0 ? -1 : (var5 > 0 ? 1 : 0);
        if (var8 == 0 && var9 == 0) {
            this.vx = 0;
            this.vy = 0;
        } else {
            int var10 = var8 != 0 && var9 != 0 ? var3 * 3 / 4 : var3;
            int var11 = var8 * var10;
            int var12 = var9 * var10;
            int var13 = var11 >> 16;
            int var14 = var12 >> 16;
            int var15 = this.x + var13;
            int var16 = this.y + var14;
            boolean var17 = this.canNavigateTo(var15, var16) && this.canNavigateTo(this.x + var13 * 3, this.y + var14 * 3);
            if (var17) {
                this.vx = var11;
                this.vy = var12;
                if (this.steerSideLeft > 0) {
                    --this.steerSideLeft;
                } else {
                    this.steerSide = 0;
                }

            } else {
                int var18 = -1;
                int var19 = Integer.MAX_VALUE;

                for(int var20 = 0; var20 < STEER_X.length; ++var20) {
                    int var21 = STEER_X[var20];
                    int var22 = STEER_Y[var20];
                    int var23 = var21 != 0 && var22 != 0 ? var3 * 3 / 4 : var3;
                    int var24 = var21 * var23 >> 16;
                    int var25 = var22 * var23 >> 16;
                    int var26 = this.x + var24;
                    int var27 = this.y + var25;
                    if (this.canNavigateTo(var26, var27)) {
                        int var28 = var1 - var26;
                        int var29 = var2 - var27;
                        int var30 = var28 * var28 + var29 * var29;
                        int var31 = var21 * var4 + var22 * var5;
                        if (var31 < 0) {
                            var30 += 4096;
                        } else if (var31 == 0) {
                            var30 += 512;
                        }

                        int var32 = var21 * var6 + var22 * var7;
                        if (var32 < 0) {
                            var30 += 2048;
                        } else if (var32 == 0 && (var6 != 0 || var7 != 0)) {
                            var30 += 64;
                        }

                        if (!this.canNavigateTo(this.x + var24 * 3, this.y + var25 * 3)) {
                            var30 += 4096;
                        }

                        int var33 = var21 * var9 - var22 * var8;
                        if (this.steerSideLeft > 0 && this.steerSide != 0 && var33 != 0 && var33 != this.steerSide) {
                            var30 += 3072;
                        }

                        if (var30 < var19) {
                            var19 = var30;
                            var18 = var20;
                        }
                    }
                }

                if (var18 >= 0) {
                    int var34 = STEER_X[var18];
                    int var35 = STEER_Y[var18];
                    int var36 = var34 != 0 && var35 != 0 ? var3 * 3 / 4 : var3;
                    this.vx = var34 * var36;
                    this.vy = var35 * var36;
                    int var37 = var34 * var9 - var35 * var8;
                    if (var37 != 0) {
                        if (this.steerSideLeft <= 0 || this.steerSide == 0) {
                            this.steerSide = var37 < 0 ? -1 : 1;
                        }

                        this.steerSideLeft = 18;
                    }
                } else {
                    this.vx = 0;
                    this.vy = 0;
                }

            }
        }
    }

    protected void clampToWorld(int var1, int var2) {
        byte var3 = 8;
        this.x = FMath.clamp(this.x, var3, var1 - var3);
        this.y = FMath.clamp(this.y, var3, var2 - var3);
    }

    public int takeDamage(int var1) {
        if (this.alive && var1 > 0) {
            int var2 = var1;
            if (this.shield > 0) {
                int var3 = this.shield < var1 ? this.shield : var1;
                this.shield -= var3;
                var2 = var1 - var3;
            }

            this.hp -= var2;
            if (this.hp <= 0) {
                this.hp = 0;
                this.alive = false;
                this.onDeath();
            }

            return var1;
        } else {
            return 0;
        }
    }

    protected void onDeath() {
    }

    public boolean isEnemy(Entity var1) {
        return var1 != null && var1.alive && var1.team != this.team;
    }

    public int dist2To(Entity var1) {
        return FMath.dist2(this.x, this.y, var1.x, var1.y);
    }

    public boolean inRange(Entity var1) {
        int var2 = this.atkRange + var1.radius;
        return this.dist2To(var1) <= var2 * var2;
    }

    public boolean tryAttackReady() {
        if (this.atkCdLeft > 0) {
            --this.atkCdLeft;
            return false;
        } else {
            return true;
        }
    }

    public void resetAttackCd() {
        this.atkCdLeft = this.atkCooldown;
    }

    public int getDepth() {
        return IsoMath.depth(this.x, this.y);
    }

    protected void drawHpBar(Graphics2D var1, int var2, int var3, int var4, int var5) {
        if (this.maxHp > 0) {
            int var6 = var2 - (var4 >> 1);
            int var7 = var3 - var5;
            int var8 = this.unitType == 1 ? 4 : 3;
            var1.setColor(G2D.color(0));
            var1.fillRect(var6 - 1, var7 - 1, var4 + 2, var8 + 2);
            var1.setColor(G2D.color(3350570));
            var1.fillRect(var6, var7, var4, var8);
            int var9 = this.hp * var4 / this.maxHp;
            if (var9 < 0) {
                var9 = 0;
            }

            if (var9 > 0) {
                var1.setColor(G2D.color(this.team == 0 ? 3049192 : 15220800));
                var1.fillRect(var6, var7, var9, var8);
                var1.setColor(G2D.color(this.team == 0 ? 9226495 : 16752800));
                var1.fillRect(var6, var7, var9, 1);
            }

            if (this.shield > 0) {
                int var10 = this.shield * var4 / this.maxHp;
                if (var10 > var4 - var9) {
                    var10 = var4 - var9;
                }

                if (var10 > 0) {
                    var1.setColor(G2D.color(15266047));
                    var1.fillRect(var6 + var9, var7, var10, var8);
                }
            }

            if (this.unitType == 1 && var4 >= 16) {
                var1.setColor(G2D.color(0));

                for(int var11 = 1; var11 < 4; ++var11) {
                    var1.fillRect(var6 + var4 * var11 / 4, var7, 1, var8);
                }
            }

        }
    }
}
