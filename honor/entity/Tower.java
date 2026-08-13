package honor.entity;

import honor.core.IsoMath;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import honor.G2D;
import honor.Img;

public class Tower extends Entity {
    public static final int ATTACK_RANGE = 90;
    public static final int TIER_OUTER = 0;
    public static final int TIER_INNER = 1;
    public static final int TIER_HIGH = 2;
    private static final int[] TIER_HP = new int[]{620, 780, 940};
    private static final int[] TIER_ATK = new int[]{36, 42, 48};
    private static final int DAMAGE_WAVES_PER_STAGE = 3;
    private static final int DAMAGE_STAGE_CAP = 16;
    private static final int HERO_DAMAGE_BASE = 15;
    private static final int HERO_DAMAGE_STEP = 12;
    private static final int HERO_FOCUS_MAX = 5;
    private final BufferedImage image;
    private static final int DRAW_OFFSET = 40;
    public static final int RANGE_SAFE = 0;
    public static final int RANGE_ENTERED = 1;
    public static final int RANGE_TARGETED = 2;
    private static final BufferedImage[] RANGE_IMAGE = new BufferedImage[3];
    private static final int RANGE_HALF_W = 143;
    private static final int RANGE_HALF_H = 70;
    public final int tier;
    private Entity warnedTarget;
    private int warningLeft;
    private Entity focusHero;
    private int focusStacks;

    public Tower(int var1, int var2, int var3, BufferedImage var4, int var5) {
        super(var1, var2, var3, 3);
        this.image = var4;
        this.tier = var5;
        this.radius = 12;
        this.maxHp = TIER_HP[var5];
        this.hp = this.maxHp;
        this.atk = TIER_ATK[var5];
        this.def = 12;
        this.atkRange = 90;
        this.atkCooldown = 20;
        this.atkCdLeft = 0;
        this.ranged = true;
    }

    public void update(int var1) {
        if (this.alive) {
            if (this.atkCdLeft > 0) {
                --this.atkCdLeft;
            }

            if (this.warningLeft > 0) {
                --this.warningLeft;
                if (this.warningLeft <= 0) {
                    this.warnedTarget = null;
                }
            }

            if (this.target != null && (!this.target.alive || !this.inRange(this.target) || !this.isEnemy(this.target))) {
                this.target = null;
            }

            if (this.target == null && this.focusHero != null) {
                this.resetHeroFocus();
            }

        }
    }

    public boolean wantsAttack() {
        return this.alive && this.target != null && this.target.alive && this.inRange(this.target) && this.atkCdLeft <= 0;
    }

    public void renderAttackRange(Graphics2D var1, int var2, int var3, int var4) {
        if (this.alive) {
            int var5 = IsoMath.toScreenX(this.x, this.y) - var2;
            int var6 = IsoMath.toScreenY(this.x, this.y) - var3;
            byte var7 = 20;
            Rectangle clip = var1.getClipBounds();
            if (clip == null) {
                return;
            }
            if (var5 >= clip.x - var7 && var5 <= clip.x + clip.width + var7 && var6 >= clip.y - var7 && var6 <= clip.y + clip.height + var7) {
                int var8 = var5 - 143;
                int var9 = var6 - 70;
                short var10 = 287;
                short var11 = 141;
                if (var8 < clip.x + clip.width && var9 < clip.y + clip.height && var8 + var10 >= clip.x && var9 + var11 >= clip.y) {
                    BufferedImage var12 = rangeImage(var4);
                    if (var12 != null) {
                        G2D.drawImage(var1, var12, var8, var9, 20);
                    }

                }
            }
        }
    }

    private static BufferedImage rangeImage(int var0) {
        int var1 = var0;
        if (var0 < 0 || var0 > 2) {
            var1 = 0;
        }

        if (RANGE_IMAGE[var1] != null) {
            return RANGE_IMAGE[var1];
        } else {
            short var2 = 287;
            short var3 = 141;
            int[] var4 = new int[var2 * var3];
            int var5 = var1 == 2 ? 16728128 : (var1 == 1 ? 16765503 : 3968255);
            short var6 = 8100;
            byte var7 = 86;
            int var8 = var7 * var7;

            for(int var9 = -70; var9 <= 70; ++var9) {
                for(int var10 = -143; var10 <= 143; ++var10) {
                    int var11 = 16 * (5 * var10 + 22 * var9) / 275;
                    int var12 = 16 * (-10 * var10 + 11 * var9) / 275;
                    int var13 = var11 * var11 + var12 * var12;
                    if (var13 <= var6) {
                        int var14 = var13 >= var8 ? 96 : 22;
                        var4[(var9 + 70) * var2 + var10 + 143] = var14 << 24 | var5;
                    }
                }
            }

            RANGE_IMAGE[var1] = Img.createRgb(var4, var2, var3, true);
            return RANGE_IMAGE[var1];
        }
    }

    public void markAttacking(Entity var1) {
        this.warnedTarget = var1;
        this.warningLeft = 12;
        if (var1 != null && var1.unitType == 1) {
            if (this.focusHero == var1) {
                if (this.focusStacks < 5) {
                    ++this.focusStacks;
                }
            } else {
                this.focusHero = var1;
                this.focusStacks = 1;
            }
        } else {
            this.resetHeroFocus();
        }

    }

    public int heroDamageBonus(Entity var1) {
        if (var1 != null && var1.unitType == 1 && this.focusHero == var1) {
            int var2 = this.focusStacks > 0 ? this.focusStacks : 1;
            return 15 + (var2 - 1) * 12;
        } else {
            return 15;
        }
    }

    public void applyBattleProgress(int var1) {
        int var2 = var1 / 3;
        if (var2 < 0) {
            var2 = 0;
        } else if (var2 > 16) {
            var2 = 16;
        }

        this.atk = TIER_ATK[this.tier] + var2 * (this.tier + 2);
    }

    private void resetHeroFocus() {
        this.focusHero = null;
        this.focusStacks = 0;
    }

    public boolean isThreatening(Entity var1) {
        return var1 != null && (this.target == var1 || this.warningLeft > 0 && this.warnedTarget == var1);
    }

    public void render(Graphics2D var1, int var2, int var3) {
        if (!this.alive) {
            int var6 = IsoMath.toScreenX(this.x, this.y) - var2;
            int var7 = IsoMath.toScreenY(this.x, this.y) - var3;
            var1.setColor(G2D.color(4473924));
            var1.fillArc(var6 - 10, var7 - 4, 20, 8, 0, 360);
        } else {
            int var4 = IsoMath.toScreenX(this.x, this.y) - var2;
            int var5 = IsoMath.toScreenY(this.x, this.y) - var3;
            var1.setColor(G2D.color(0));
            var1.fillArc(var4 - 10, var5 - 3, 20, 6, 0, 360);
            if (this.image != null) {
                G2D.drawImage(var1, this.image, var4, var5 - 40, 17);
            }

            this.drawHpBar(var1, var4, var5, 22, 40);
        }
    }
}
