package honor.battle;

import honor.core.IsoMath;
import honor.entity.Entity;
import java.awt.Color;
import java.awt.Graphics2D;
import honor.G2D;

public final class AttackFx {
    public static final int TYPE_SLASH = 1;
    public static final int TYPE_ARROW = 2;
    public static final int TYPE_RING = 3;
    public static final int TYPE_TRAIL = 4;
    public static final int TYPE_BEAM = 5;
    public static final int TYPE_AURA = 6;
    private static final int ISO_WIDEN = 181;
    public boolean active;
    public int type;
    public int team;
    public int x0;
    public int y0;
    public int x1;
    public int y1;
    public int x;
    public int y;
    public int age;
    public int life;
    public int pendingDamage;
    public Entity victim;
    public Entity attacker;
    public boolean damageApplied;
    public int floaterColor;
    public int radius;
    public Entity anchor;

    public AttackFx() {
    }

    public void spawnSlash(Entity var1, Entity var2, int var3) {
        this.active = true;
        this.type = 1;
        this.team = var1.team;
        this.floaterColor = var3;
        this.x0 = var1.x;
        this.y0 = var1.y;
        this.x1 = var2.x;
        this.y1 = var2.y;
        this.x = this.x0 + this.x1 >> 1;
        this.y = this.y0 + this.y1 >> 1;
        this.age = 0;
        this.life = 8;
        this.pendingDamage = 0;
        this.victim = null;
        Object var4 = null;
        this.anchor = null;
        this.radius = 0;
        this.damageApplied = true;
    }

    public void spawnArrow(Entity var1, Entity var2, int var3, int var4) {
        this.active = true;
        this.type = 2;
        this.team = var1.team;
        this.floaterColor = var4;
        this.x0 = var1.x;
        this.y0 = var1.y;
        this.x1 = var2.x;
        this.y1 = var2.y;
        this.x = this.x0;
        this.y = this.y0;
        this.age = 0;
        int var5 = abs(this.x1 - this.x0) + abs(this.y1 - this.y0);
        this.life = 4 + var5 / 12;
        if (this.life > 12) {
            this.life = 12;
        }

        this.pendingDamage = var3;
        this.victim = var2;
        this.attacker = var1;
        this.anchor = null;
        this.radius = 0;
        this.damageApplied = false;
    }

    public void spawnRing(int var1, int var2, int var3, int var4, int var5) {
        this.resetCommon(3, var4, var5);
        this.x0 = var1;
        this.y0 = var2;
        this.x1 = var1;
        this.y1 = var2;
        this.x = var1;
        this.y = var2;
        this.radius = var3;
        this.life = 10;
    }

    public void spawnTrail(int var1, int var2, int var3, int var4, int var5, int var6) {
        this.resetCommon(4, var5, var6);
        this.x0 = var1;
        this.y0 = var2;
        this.x1 = var3;
        this.y1 = var4;
        this.x = var3;
        this.y = var4;
        this.life = 9;
    }

    public void spawnBeam(int var1, int var2, int var3, int var4, int var5, int var6) {
        this.resetCommon(5, var5, var6);
        this.x0 = var1;
        this.y0 = var2;
        this.x1 = var3;
        this.y1 = var4;
        this.x = var3;
        this.y = var4;
        this.life = 8;
    }

    public void spawnAura(Entity var1, int var2, int var3) {
        this.resetCommon(6, var1.team, var3);
        this.anchor = var1;
        this.x0 = var1.x;
        this.y0 = var1.y;
        this.x = this.x0;
        this.y = this.y0;
        this.radius = var2;
        this.life = 14;
    }

    private void resetCommon(int var1, int var2, int var3) {
        this.active = true;
        this.type = var1;
        this.team = var2;
        this.floaterColor = var3;
        this.age = 0;
        this.pendingDamage = 0;
        this.victim = null;
        this.attacker = null;
        this.anchor = null;
        this.radius = 0;
        this.damageApplied = true;
    }

    public boolean update() {
        if (!this.active) {
            return false;
        } else {
            ++this.age;
            if (this.type == 2) {
                if (this.victim != null && this.victim.alive) {
                    this.x1 = this.victim.x;
                    this.y1 = this.victim.y;
                }

                int var1 = this.age;
                if (var1 > this.life) {
                    var1 = this.life;
                }

                this.x = this.x0 + (this.x1 - this.x0) * var1 / this.life;
                this.y = this.y0 + (this.y1 - this.y0) * var1 / this.life;
                if (this.age >= this.life && !this.damageApplied) {
                    this.x = this.x1;
                    this.y = this.y1;
                    return true;
                }
            } else if (this.type == 6 && this.anchor != null) {
                this.x = this.anchor.x;
                this.y = this.anchor.y;
            }

            if (this.age >= this.life) {
                this.active = false;
                return false;
            } else {
                return true;
            }
        }
    }

    public boolean shouldApplyDamage() {
        return this.active && this.type == 2 && !this.damageApplied && this.age >= this.life;
    }

    public void markDamageApplied() {
        this.damageApplied = true;
        this.pendingDamage = 0;
        this.active = false;
    }

    public void render(Graphics2D var1, int var2, int var3) {
        if (this.active) {
            if (this.type == 1) {
                this.renderSlash(var1, var2, var3);
            } else if (this.type == 2) {
                this.renderArrow(var1, var2, var3);
            } else if (this.type == 3) {
                this.renderRing(var1, var2, var3);
            } else if (this.type == 4) {
                this.renderTrail(var1, var2, var3);
            } else if (this.type == 5) {
                this.renderBeam(var1, var2, var3);
            } else if (this.type == 6) {
                this.renderAura(var1, var2, var3);
            }

        }
    }

    private void renderRing(Graphics2D var1, int var2, int var3) {
        int var4 = IsoMath.toScreenX(this.x, this.y) - var2;
        int var5 = IsoMath.toScreenY(this.x, this.y) - var3;
        int var6 = this.radius * this.age / this.life;
        drawIsoEllipse(var1, var4, var5, var6, this.floaterColor);
        if (this.age > 2) {
            drawIsoEllipse(var1, var4, var5, this.radius * (this.age - 2) / this.life, 16777215);
        }

    }

    private void renderTrail(Graphics2D var1, int var2, int var3) {
        int var4 = IsoMath.toScreenX(this.x0, this.y0) - var2;
        int var5 = IsoMath.toScreenY(this.x0, this.y0) - var3 - 12;
        int var6 = IsoMath.toScreenX(this.x1, this.y1) - var2;
        int var7 = IsoMath.toScreenY(this.x1, this.y1) - var3 - 12;
        byte var8 = 5;

        for(int var9 = 0; var9 <= var8; ++var9) {
            int var10 = var4 + (var6 - var4) * var9 / var8;
            int var11 = var5 + (var7 - var5) * var9 / var8;
            int var12 = 5 - (this.age >> 1) + (var9 >> 1);
            if (var12 < 1) {
                var12 = 1;
            }

            var1.setColor(G2D.color(var9 >= var8 - 1 ? 16777215 : this.floaterColor));
            var1.fillArc(var10 - var12, var11 - var12, var12 * 2, var12 * 2, 0, 360);
        }

    }

    private void renderBeam(Graphics2D var1, int var2, int var3) {
        int var4 = IsoMath.toScreenX(this.x0, this.y0) - var2;
        int var5 = IsoMath.toScreenY(this.x0, this.y0) - var3 - 14;
        int var6 = IsoMath.toScreenX(this.x1, this.y1) - var2;
        int var7 = IsoMath.toScreenY(this.x1, this.y1) - var3 - 14;
        int var8 = 3 - (this.age >> 2);
        if (var8 < 1) {
            var8 = 1;
        }

        var1.setColor(G2D.color(this.floaterColor));

        for(int var9 = -var8; var9 <= var8; ++var9) {
            var1.drawLine(var4, var5 + var9, var6, var7 + var9);
        }

        var1.setColor(G2D.color(16777215));
        var1.drawLine(var4, var5, var6, var7);
    }

    private void renderAura(Graphics2D var1, int var2, int var3) {
        int var4 = IsoMath.toScreenX(this.x, this.y) - var2;
        int var5 = IsoMath.toScreenY(this.x, this.y) - var3;
        int var6 = this.radius - (this.age & 3) * 2;
        if (var6 < 4) {
            var6 = 4;
        }

        drawIsoEllipse(var1, var4, var5, var6, this.floaterColor);
        int var7 = this.age * 2;
        var1.setColor(G2D.color(16777215));
        var1.drawLine(var4 - 6, var5 - var7, var4 - 6, var5 - var7 - 4);
        var1.drawLine(var4 + 6, var5 - var7, var4 + 6, var5 - var7 - 4);
    }

    private static void drawIsoEllipse(Graphics2D var0, int var1, int var2, int var3, int var4) {
        if (var3 > 0) {
            int var5 = var3 * 181 >> 7;
            int var6 = var5 >> 1;
            if (var6 < 1) {
                var6 = 1;
            }

            var0.setColor(G2D.color(var4));
            var0.drawArc(var1 - var5, var2 - var6, var5 * 2, var6 * 2, 0, 360);
        }
    }

    private void renderSlash(Graphics2D var1, int var2, int var3) {
        int var4 = IsoMath.toScreenX(this.x0, this.y0) - var2;
        int var5 = IsoMath.toScreenY(this.x0, this.y0) - var3 - 14;
        int var6 = IsoMath.toScreenX(this.x1, this.y1) - var2;
        int var7 = IsoMath.toScreenY(this.x1, this.y1) - var3 - 14;
        int var8 = var6 - var4;
        int var9 = var7 - var5;
        if (var8 == 0 && var9 == 0) {
            var8 = 0;
            var9 = -12;
        }

        int var10 = var4 + var8 * 2 / 3;
        int var11 = var5 + var9 * 2 / 3;
        int var12 = -var9;
        int var14 = 1 + absSqrtApprox(var12 * var12 + var8 * var8);
        var12 = var12 * 16 / var14;
        int var13 = var8 * 16 / var14;
        int var15 = this.age;
        if (var15 > 6) {
            var15 = 6;
        }

        int var16 = var12 * (var15 - 3) / 3;
        int var17 = var13 * (var15 - 3) / 3;
        int var18 = var10 - var12 + var16 / 2;
        int var19 = var11 - var13 + var17 / 2;
        int var20 = var10 + var12 + var16 / 2;
        int var21 = var11 + var13 + var17 / 2;
        int var22 = this.team == 0 ? 10541311 : 16756880;
        int var23 = 16777215;
        var1.setColor(G2D.color(var22));
        var1.drawLine(var18, var19, var20, var21);
        var1.drawLine(var18, var19 - 1, var20, var21 - 1);
        var1.setColor(G2D.color(var23));
        var1.drawLine(var4 + var8 / 4, var5 + var9 / 4, var10 + var16, var11 + var17);
        int var24 = 5 - (this.age > 4 ? 4 : this.age);
        if (var24 < 2) {
            var24 = 2;
        }

        var1.setColor(G2D.color(var23));
        var1.fillArc(var10 - var24, var11 - var24, var24 * 2, var24 * 2, 0, 360);
        var1.setColor(G2D.color(var22));
        var1.drawArc(var10 - var24 - 2, var11 - var24 - 2, (var24 + 2) * 2, (var24 + 2) * 2, 20, 140);
    }

    private void renderArrow(Graphics2D var1, int var2, int var3) {
        int var4 = IsoMath.toScreenX(this.x, this.y) - var2;
        int var5 = IsoMath.toScreenY(this.x, this.y) - var3 - 18;
        int var6 = IsoMath.toScreenX(this.x1, this.y1) - var2;
        int var7 = IsoMath.toScreenY(this.x1, this.y1) - var3 - 18;
        int var8 = var6 - var4;
        int var9 = var7 - var5;
        if (var8 == 0 && var9 == 0) {
            var8 = 1;
        }

        int var10 = 1 + absSqrtApprox(var8 * var8 + var9 * var9);
        int var11 = var8 * 8 / var10;
        int var12 = var9 * 8 / var10;
        int var13 = -var12 / 2;
        int var14 = var11 / 2;
        int var15 = var4 + var11;
        int var16 = var5 + var12;
        int var17 = var4 - var11;
        int var18 = var5 - var12;
        int var19 = this.team == 0 ? 15257760 : 13676688;
        int var20 = this.team == 0 ? 14741759 : 16765120;
        var1.setColor(G2D.color(var19));
        var1.drawLine(var17, var18, var15, var16);
        var1.setColor(G2D.color(var20));
        G2D.fillTriangle(var1, var15, var16, var15 - var11 / 2 + var13, var16 - var12 / 2 + var14, var15 - var11 / 2 - var13, var16 - var12 / 2 - var14);
        var1.setColor(G2D.color(this.floaterColor));
        var1.drawLine(var17, var18, var17 + var13, var18 + var14);
        var1.drawLine(var17, var18, var17 - var13, var18 - var14);
    }

    private static int abs(int var0) {
        return var0 < 0 ? -var0 : var0;
    }

    private static int absSqrtApprox(int var0) {
        if (var0 <= 0) {
            return 0;
        } else {
            int var1 = var0;
            if (var0 > 1) {
                var1 = var0 / 2 + 1;

                for(int var2 = 0; var2 < 6; ++var2) {
                    var1 = (var1 + var0 / var1) / 2;
                    if (var1 <= 0) {
                        return 1;
                    }
                }
            }

            return var1;
        }
    }
}
