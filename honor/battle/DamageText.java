package honor.battle;

import honor.core.IsoMath;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import honor.G2D;

public final class DamageText {
    public boolean active;
    public int x;
    public int y;
    public String text;
    public int life;
    public int color;
    private static final Font FONT = G2D.decodeJ2meFont(0, 1, 8);

    public DamageText() {
    }

    public void spawn(int var1, int var2, int var3, int var4) {
        this.active = true;
        this.x = var1;
        this.y = var2;
        this.text = Integer.toString(var3);
        this.life = 24;
        this.color = var4;
    }

    public void spawnLabel(int var1, int var2, String var3, int var4) {
        this.active = true;
        this.x = var1;
        this.y = var2;
        this.text = var3;
        this.life = 32;
        this.color = var4;
    }

    public boolean update() {
        if (!this.active) {
            return false;
        } else {
            --this.life;
            --this.y;
            if (this.life <= 0) {
                this.active = false;
                return false;
            } else {
                return true;
            }
        }
    }

    public void render(Graphics2D var1, int var2, int var3) {
        if (this.active) {
            int var4 = IsoMath.toScreenX(this.x, this.y) - var2;
            int var5 = IsoMath.toScreenY(this.x, this.y) - var3 - 28;
            var1.setFont(FONT);
            var1.setColor(G2D.color(0));
            G2D.drawString(var1, this.text, var4 + 1, var5 + 1, 17);
            var1.setColor(G2D.color(this.color));
            G2D.drawString(var1, this.text, var4, var5, 17);
        }
    }
}
