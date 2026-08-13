package honor.entity;

import honor.core.FMath;
import honor.core.IsoMath;
import honor.G2D;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import honor.Sprite;

public class Jungle extends Entity {
    public static final int KIND_CAMP = 0;
    public static final int KIND_BLUE_BUFF = 1;
    public static final int KIND_RED_BUFF = 2;
    public static final int KIND_TYRANT = 3;
    public static final int KIND_OVERLORD = 4;
    public static final int FRAME_W = 28;
    public static final int FRAME_H = 32;
    public static final int BUFF_FRAMES = 600;
    private static final int[] MAX_HP = new int[]{320, 520, 520, 2600, 4200};
    private static final int[] ATK = new int[]{20, 26, 26, 45, 60};
    private static final int DAMAGE_WAVES_PER_STAGE = 3;
    private static final int[] ATK_PER_STAGE = new int[]{2, 2, 2, 3, 4};
    private static final int DAMAGE_STAGE_CAP = 16;
    private static final int[] GOLD = new int[]{30, 60, 60, 100, 140};
    private static final int[] EXP = new int[]{40, 70, 70, 120, 160};
    private static final int[] RESPAWN = new int[]{600, 900, 900, 1200, 1800};
    private static final int[] SPRITE_FRAME = new int[]{0, 1, 2, 3, 4};
    private static final int[] RADIUS = new int[]{9, 10, 10, 14, 16};
    private static final int AGGRO_R2 = 1600;
    private static final int LEASH_R2 = 14400;
    private static final int REGEN_PER_FRAME = 24;
    private static final int MOVE_SPEED = 98304;
    private final int kind;
    private final int campX;
    private final int campY;
    private final Sprite sprite;
    private int respawnLeft;
    private boolean returning;
    private int battleWave;

    public Jungle(int var1, int var2, int var3, BufferedImage var4) {
        super(var2, var3, 2, 5);
        this.kind = var1;
        this.campX = var2;
        this.campY = var3;
        this.sprite = new Sprite(var4, 28, 32);
        this.sprite.defineReferencePixel(14, 31);
        this.sprite.setFrame(SPRITE_FRAME[var1]);
        this.atkRange = 24;
        this.atkCooldown = 20;
        this.radius = RADIUS[var1];
        this.ranged = false;
        this.reset();
    }

    public void reset() {
        this.alive = true;
        this.maxHp = MAX_HP[this.kind];
        this.hp = this.maxHp;
        this.applyBattleProgress(this.battleWave);
        this.def = 0;
        this.shield = 0;
        this.x = this.campX;
        this.y = this.campY;
        this.vx = 0;
        this.vy = 0;
        this.target = null;
        this.returning = false;
        this.respawnLeft = 0;
        this.atkCdLeft = 0;
    }

    public void applyBattleProgress(int var1) {
        this.battleWave = var1 < 0 ? 0 : var1;
        int var2 = this.battleWave / 3;
        if (var2 > 16) {
            var2 = 16;
        }

        this.atk = ATK[this.kind] + var2 * ATK_PER_STAGE[this.kind];
    }

    public int getKind() {
        return this.kind;
    }

    public int getGoldReward() {
        return GOLD[this.kind];
    }

    public int getExpReward() {
        return EXP[this.kind];
    }

    public boolean isBoss() {
        return this.kind == 3 || this.kind == 4;
    }

    public void update(int var1) {
        if (!this.alive) {
            if (this.respawnLeft > 0) {
                --this.respawnLeft;
                if (this.respawnLeft <= 0) {
                    this.reset();
                }
            }

        } else {
            if (this.atkCdLeft > 0) {
                --this.atkCdLeft;
            }

            if (this.target != null && (!this.target.alive || !this.isEnemy(this.target))) {
                this.target = null;
            }

            if (FMath.dist2(this.x, this.y, this.campX, this.campY) > 14400) {
                this.returning = true;
                this.target = null;
            }

            if (this.returning) {
                if (FMath.dist2(this.x, this.y, this.campX, this.campY) <= 36) {
                    this.x = this.campX;
                    this.y = this.campY;
                    this.vx = 0;
                    this.vy = 0;
                    this.returning = false;
                    this.hp = this.maxHp;
                } else {
                    this.moveToward(this.campX, this.campY);
                    this.hp += 24;
                    if (this.hp > this.maxHp) {
                        this.hp = this.maxHp;
                    }
                }
            } else if (this.target != null) {
                if (this.inRange(this.target)) {
                    this.vx = 0;
                    this.vy = 0;
                } else {
                    this.moveToward(this.target.x, this.target.y);
                }
            } else {
                this.vx = 0;
                this.vy = 0;
            }

            if (this.terrain != null) {
                this.moveWithCollision(this.terrain.getPixelWidth(), this.terrain.getPixelHeight());
            } else {
                this.integratePosition();
            }

        }
    }

    public void considerAggro(Entity var1) {
        if (this.alive && !this.returning && var1 != null && var1.alive) {
            if (this.isEnemy(var1) && this.target == null) {
                if (this.dist2To(var1) <= 1600) {
                    this.target = var1;
                }

            }
        }
    }

    public void onAttackedBy(Entity var1) {
        if (this.alive && !this.returning && var1 != null && this.isEnemy(var1)) {
            this.target = var1;
        }

    }

    public boolean wantsAttack() {
        return this.alive && this.target != null && this.target.alive && this.inRange(this.target) && this.atkCdLeft <= 0;
    }

    protected void onDeath() {
        this.respawnLeft = RESPAWN[this.kind];
        this.target = null;
        this.returning = false;
        this.vx = 0;
        this.vy = 0;
    }

    public int getRespawnSeconds() {
        return this.alive ? 0 : (this.respawnLeft + 14) / 15;
    }

    public void render(Graphics2D var1, int var2, int var3) {
        if (this.alive) {
            int var4 = IsoMath.toScreenX(this.x, this.y) - var2;
            int var5 = IsoMath.toScreenY(this.x, this.y) - var3;
            var1.setColor(G2D.color(0));
            var1.fillArc(var4 - this.radius, var5 - 3, this.radius * 2, 6, 0, 360);
            this.sprite.setRefPixelPosition(var4, var5);
            this.sprite.paint(var1);
            this.drawHpBar(var1, var4, var5, this.isBoss() ? 26 : 16, 30);
        }
    }

    private void moveToward(int var1, int var2) {
        int var3 = var1 - this.x;
        int var4 = var2 - this.y;
        byte var5 = 0;
        byte var6 = 0;
        if (var3 > 0) {
            var5 = 1;
        } else if (var3 < 0) {
            var5 = -1;
        }

        if (var4 > 0) {
            var6 = 1;
        } else if (var4 < 0) {
            var6 = -1;
        }

        this.vx = var5 * 98304;
        this.vy = var6 * 98304;
    }
}
