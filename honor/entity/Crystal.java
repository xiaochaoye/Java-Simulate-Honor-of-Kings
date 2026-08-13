package honor.entity;

import honor.core.IsoMath;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import honor.G2D;

public class Crystal extends Entity {
    public static final int ATTACK_RANGE = 85;
    private static final int BASE_ATK = 52;
    private static final int ATK_CD = 18;
    private static final int DRAW_OFFSET = 6;
    private final BufferedImage image;

    public Crystal(int var1, int var2, int var3, BufferedImage var4) {
        super(var1, var2, var3, 4);
        this.image = var4;
        this.radius = 14;
        this.maxHp = 1800;
        this.hp = this.maxHp;
        this.def = 15;
        this.atk = 52;
        this.atkRange = 85;
        this.atkCooldown = 18;
        this.atkCdLeft = 0;
        this.ranged = true;
    }

    public void update(int var1) {
        if (this.alive) {
            if (this.atkCdLeft > 0) {
                --this.atkCdLeft;
            }

            if (this.target != null && (!this.target.alive || !this.inRange(this.target) || !this.isEnemy(this.target))) {
                this.target = null;
            }

        }
    }

    public boolean wantsAttack() {
        return this.alive && this.target != null && this.target.alive && this.inRange(this.target) && this.atkCdLeft <= 0;
    }

    public void render(Graphics2D var1, int var2, int var3) {
        int var4 = IsoMath.toScreenX(this.x, this.y) - var2;
        int var5 = IsoMath.toScreenY(this.x, this.y) - var3;
        if (!this.alive) {
            var1.setColor(G2D.color(3355443));
            G2D.fillTriangle(var1, var4, var5 - 8, var4 - 10, var5 + 4, var4 + 10, var5 + 4);
        } else {
            var1.setColor(G2D.color(0));
            var1.fillArc(var4 - 12, var5 - 3, 24, 8, 0, 360);
            if (this.image != null) {
                G2D.drawImage(var1, this.image, var4, var5 + 6, 33);
            } else {
                int var6 = this.team == 0 ? 5614335 : 16737877;
                int var7 = this.team == 0 ? 13430527 : 16765120;
                var1.setColor(G2D.color(var6));
                G2D.fillTriangle(var1, var4, var5 - 36, var4 - 12, var5 - 8, var4 + 12, var5 - 8);
                var1.setColor(G2D.color(var7));
                G2D.fillTriangle(var1, var4, var5 - 36, var4 - 4, var5 - 14, var4 + 2, var5 - 10);
                var1.setColor(G2D.color(var6));
                G2D.fillTriangle(var1, var4 - 12, var5 - 8, var4 + 12, var5 - 8, var4, var5 + 2);
            }

            this.drawHpBar(var1, var4, var5, 26, 38);
        }
    }
}
