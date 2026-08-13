package honor.ui;

import honor.core.KeyInput;
import honor.core.MatchConfig;
import honor.core.Scene;
import honor.hero.HeroDef;
import honor.skill.SkillDef;
import honor.util.Sfx;
import java.awt.Graphics2D;
import java.awt.Color;
import honor.G2D;

public final class SelectScene implements Scene {
    private static final String[] SKILL_KEYS = new String[]{"1", "3", "7"};
    private int mode = 1;
    private int cursor;
    private int roleCursor;
    private int heroInRole;
    private MatchConfig preview;
    private MatchConfig locked;
    private int next = -1;
    private int seed;
    private int backFlash;
    private int goFlash;
    private int screenW;
    private int screenH;
    private int goX;
    private int goY;
    private int goW;
    private int goH;
    private int roleBarY;
    private int roleBarH;
    private int rosterBarY;
    private int rosterBarH;
    private boolean pickMode;
    private boolean pickConfirmed;
    private boolean pickCancelled;

    public SelectScene() {
    }

    public void enter(int var1, int var2) {
        this.mode = var1;
        this.seed = var2;
        this.cursor = 0;
        this.roleCursor = HeroDef.roleId(this.cursor);
        this.heroInRole = 0;
        this.locked = null;
        this.next = -1;
        this.backFlash = 0;
        this.goFlash = 0;
        this.pickMode = false;
        this.pickConfirmed = false;
        this.pickCancelled = false;
        this.refreshPreview();
    }

    public void enterForPick(int var1) {
        this.pickMode = true;
        this.pickConfirmed = false;
        this.pickCancelled = false;
        this.locked = null;
        this.next = -1;
        this.backFlash = 0;
        this.goFlash = 0;
        this.mode = 0;
        this.seed = 1;
        if (var1 < 0 || var1 >= 19) {
            var1 = 0;
        }

        this.cursor = var1;
        this.roleCursor = HeroDef.roleId(this.cursor);
        this.heroInRole = indexInRole(this.roleCursor, this.cursor);
        this.refreshPreview();
        Sfx.play(HeroDef.voiceName(this.cursor, 0));
    }

    public int getSelectedHero() {
        return this.cursor;
    }

    public boolean consumePickConfirmed() {
        boolean var1 = this.pickConfirmed;
        this.pickConfirmed = false;
        return var1;
    }

    public boolean consumePickCancelled() {
        boolean var1 = this.pickCancelled;
        this.pickCancelled = false;
        return var1;
    }

    public MatchConfig getConfig() {
        return this.locked;
    }

    public void update(KeyInput var1, int var2) {
        if (this.backFlash > 0) {
            --this.backFlash;
        }

        if (this.goFlash > 0) {
            --this.goFlash;
        }

        if (this.next == -1) {
            if (var1.isPressed(4)) {
                this.moveHero(-1);
            }

            if (var1.isPressed(8)) {
                this.moveHero(1);
            }

            if (var1.isPressed(1)) {
                this.moveRole(-1);
            }

            if (var1.isPressed(2)) {
                this.moveRole(1);
            }

            if (var1.isPressed(2048)) {
                this.backFlash = 6;
                if (this.pickMode) {
                    this.pickCancelled = true;
                } else {
                    this.next = 0;
                }

            } else {
                if (var1.isPressed(16)) {
                    this.confirmLock();
                }

            }
        }
    }

    private void confirmLock() {
        Sfx.play(HeroDef.voiceName(this.cursor, 0));
        this.goFlash = 8;
        if (this.pickMode) {
            this.pickConfirmed = true;
        } else {
            this.locked = MatchConfig.create(this.mode, this.cursor, this.seed);
            this.next = 2;
        }
    }

    public void onPointer(int var1, int var2, int var3, int var4, KeyInput var5) {
        this.screenW = var3;
        this.screenH = var4;
        this.layoutButtons(var3, var4);
        if (UiKit.hitBackButton(var1, var2, 4, 2)) {
            this.backFlash = 8;
            if (this.pickMode) {
                this.pickCancelled = true;
            } else {
                this.next = 0;
            }

        } else if (UiKit.hitRect(var1, var2, this.goX, this.goY, this.goW, this.goH, 4)) {
            this.confirmLock();
        } else if (var2 >= this.roleBarY && var2 < this.roleBarY + this.roleBarH) {
            int var9 = var1 * 6 / var3;
            if (var9 >= 6) {
                var9 = 5;
            }

            this.selectRole(var9);
        } else if (var2 >= this.rosterBarY && var2 < this.rosterBarY + this.rosterBarH) {
            int var6 = HeroDef.roleHeroCount(this.roleCursor);
            int var7 = var3 / var6;
            if (var7 < 1) {
                var7 = 1;
            }

            int var8 = var1 / var7;
            if (var8 < 0) {
                var8 = 0;
            }

            if (var8 >= var6) {
                var8 = var6 - 1;
            }

            if (var8 != this.heroInRole) {
                this.heroInRole = var8;
                this.cursor = HeroDef.heroAtRole(this.roleCursor, this.heroInRole);
                this.refreshPreview();
                if (this.pickMode) {
                    Sfx.play(HeroDef.voiceName(this.cursor, 0));
                }
            }

        } else {
            if (var1 < var3 / 5) {
                var5.touchAction(4, true);
            } else if (var1 > var3 * 4 / 5) {
                var5.touchAction(8, true);
            }

        }
    }

    private void layoutButtons(int var1, int var2) {
        this.goW = 96;
        this.goH = 28;
        this.goX = (var1 - this.goW) / 2;
        this.goY = var2 - this.goH - 6;
    }

    public void render(Graphics2D var1, int var2, int var3, int var4) {
        this.screenW = var2;
        this.screenH = var3;
        this.layoutButtons(var2, var3);
        UiKit.backdrop(var1, var2, var3);
        int var5 = 2;
        var5 = this.drawHeader(var1, var2, var5);
        var5 = this.drawRoleTabs(var1, var2, var5 + 1);
        var5 = this.drawRoster(var1, var2, var5 + 2, var4);
        var5 = this.drawProfile(var1, var2, var5 + 4);
        var5 = this.drawSkills(var1, var2, var5 + 3);
        if (!this.pickMode) {
            this.drawLineups(var1, var2, var3, var5 + 3);
        }

        UiKit.drawBackButton(var1, 4, 2, this.backFlash > 0);
        UiKit.drawTextButton(var1, this.goX, this.goY, this.goW, this.goH, this.pickMode ? "确定" : "出战", 15254634, this.goFlash > 0);
    }

    public int nextScene() {
        return this.next;
    }

    private void refreshPreview() {
        this.preview = MatchConfig.create(this.mode, this.cursor, this.seed);
    }

    private void moveHero(int var1) {
        int var2 = HeroDef.roleHeroCount(this.roleCursor);
        this.heroInRole = (this.heroInRole + var1 + var2) % var2;
        this.cursor = HeroDef.heroAtRole(this.roleCursor, this.heroInRole);
        this.refreshPreview();
        if (this.pickMode) {
            Sfx.play(HeroDef.voiceName(this.cursor, 0));
        }

    }

    private void moveRole(int var1) {
        this.selectRole((this.roleCursor + var1 + 6) % 6);
    }

    private void selectRole(int var1) {
        this.roleCursor = var1;
        this.heroInRole = 0;
        this.cursor = HeroDef.heroAtRole(this.roleCursor, this.heroInRole);
        this.refreshPreview();
        if (this.pickMode) {
            Sfx.play(HeroDef.voiceName(this.cursor, 0));
        }

    }

    private static int indexInRole(int var0, int var1) {
        int var2 = HeroDef.roleHeroCount(var0);

        for(int var3 = 0; var3 < var2; ++var3) {
            if (HeroDef.heroAtRole(var0, var3) == var1) {
                return var3;
            }
        }

        return 0;
    }

    private int drawRoleTabs(Graphics2D var1, int var2, int var3) {
        this.roleBarY = var3;
        this.roleBarH = G2D.fontHeight(UiKit.SMALL) + 5;
        int var4 = var2 / 6;
        var1.setFont(UiKit.SMALL);

        for(int var5 = 0; var5 < 6; ++var5) {
            int var6 = var5 * var4;
            boolean var7 = var5 == this.roleCursor;
            var1.setColor(G2D.color(var7 ? HeroDef.theme(HeroDef.heroAtRole(var5, 0)) : 2108226));
            var1.fillRect(var6 + 1, var3, var4 - 2, this.roleBarH);
            var1.setColor(G2D.color(var7 ? 14476530 : 8885420));
            G2D.drawString(var1, HeroDef.roleName(var5), var6 + var4 / 2, var3 + 2, 17);
        }

        return var3 + this.roleBarH;
    }

    private int drawHeader(Graphics2D var1, int var2, int var3) {
        int var4 = G2D.fontHeight(UiKit.SMALL) + 4;
        UiKit.gradientV(var1, 0, var3, var2, var4, 2306642, 1120812);
        var1.setFont(UiKit.SMALL);
        var1.setColor(G2D.color(15254634));
        G2D.drawString(var1, this.pickMode ? "选择联机英雄" : "选择英雄", 62, var3 + 2, 20);
        var1.setColor(G2D.color(14476530));
        if (!this.pickMode) {
            G2D.drawString(var1, MatchConfig.modeName(this.mode), var2 - 6, var3 + 2, 24);
        }

        return var3 + var4;
    }

    private int drawRoster(Graphics2D var1, int var2, int var3, int var4) {
        this.rosterBarY = var3;
        int var5 = HeroDef.roleHeroCount(this.roleCursor);
        int var6 = var2 / var5;
        int var7 = var6 / 2 - 3;
        if (var7 > 15) {
            var7 = 15;
        }

        int var8 = var3 + var7 + 3;

        for(int var9 = 0; var9 < var5; ++var9) {
            int var10 = var6 / 2 + var9 * var6;
            int var11 = HeroDef.heroAtRole(this.roleCursor, var9);
            boolean var12 = var9 == this.heroInRole;
            UiKit.avatar(var1, var10, var8, var12 ? var7 : var7 - 2, var11, 0, true, false);
            if (var12) {
                var1.setColor(G2D.color((var4 >> 2 & 1) == 0 ? 15254634 : 8018974));
                var1.drawArc(var10 - var7 - 2, var8 - var7 - 2, (var7 + 2) * 2, (var7 + 2) * 2, 0, 360);
                G2D.fillTriangle(var1, var10 - 4, var8 + var7 + 4, var10 + 4, var8 + var7 + 4, var10, var8 + var7 + 9);
            }
        }

        this.rosterBarH = var8 + var7 + 10 - var3;
        return var3 + this.rosterBarH;
    }

    private int drawProfile(Graphics2D var1, int var2, int var3) {
        var1.setFont(UiKit.MEDIUM);
        var1.setColor(G2D.color(14476530));
        G2D.drawString(var1, HeroDef.name(this.cursor), 8, var3, 20);
        var1.setFont(UiKit.SMALL);
        int var4 = G2D.stringWidth(UiKit.SMALL,HeroDef.role(this.cursor)) + 8;
        int var5 = 12 + G2D.stringWidth(UiKit.MEDIUM,HeroDef.name(this.cursor));
        var1.setColor(G2D.color(HeroDef.theme(this.cursor)));
        var1.fillRoundRect(var5, var3 + 2, var4, G2D.fontHeight(UiKit.SMALL) + 2, 5, 5);
        var1.setColor(G2D.color(14476530));
        G2D.drawString(var1, HeroDef.role(this.cursor), var5 + var4 / 2, var3 + 3, 17);
        var1.setColor(G2D.color(8885420));
        G2D.drawString(var1, HeroDef.ranged(this.cursor) ? "远程" : "近战", var2 - 8, var3 + 3, 24);
        int var6 = var3 + G2D.fontHeight(UiKit.MEDIUM) + 2;
        int var7 = G2D.fontHeight(UiKit.SMALL) + 1;
        this.drawStat(var1, 8, var6, "生存", HeroDef.statTough(this.cursor), 4177759);
        this.drawStat(var1, 8 + (var2 - 16) / 3, var6, "输出", HeroDef.statPower(this.cursor), 14174280);
        this.drawStat(var1, 8 + (var2 - 16) * 2 / 3, var6, "机动", HeroDef.statMobile(this.cursor), 4161496);
        return var6 + var7 + 2;
    }

    private void drawStat(Graphics2D var1, int var2, int var3, String var4, int var5, int var6) {
        var1.setFont(UiKit.SMALL);
        var1.setColor(G2D.color(8885420));
        G2D.drawString(var1, var4, var2, var3, 20);
        int var7 = var2 + G2D.stringWidth(UiKit.SMALL,var4) + 3;
        UiKit.pips(var1, var7, var3 + 3, 2, G2D.fontHeight(UiKit.SMALL) - 5, var5, 10, var6);
    }

    private int drawSkills(Graphics2D var1, int var2, int var3) {
        var1.setFont(UiKit.SMALL);
        int var4 = G2D.fontHeight(UiKit.SMALL) + 5;
        var1.setColor(G2D.color(10537215));
        G2D.drawString(var1, "被动 " + HeroDef.passiveName(this.cursor), 8, var3, 20);
        var1.setColor(G2D.color(8885420));
        G2D.drawString(var1, HeroDef.passiveDesc(this.cursor), var2 - 6, var3, 24);
        var3 += var4;

        for(int var5 = 0; var5 < 3; ++var5) {
            int var6 = HeroDef.skill(this.cursor, var5);
            int var7 = var3 + var5 * var4;
            boolean var8 = var5 == 2;
            var1.setColor(G2D.color(var8 ? 8018974 : 2766160));
            var1.fillRoundRect(6, var7, 13, G2D.fontHeight(UiKit.SMALL) + 2, 4, 4);
            var1.setColor(G2D.color(var8 ? 15254634 : 8885420));
            var1.drawRoundRect(6, var7, 13, G2D.fontHeight(UiKit.SMALL) + 2, 4, 4);
            var1.setColor(G2D.color(14476530));
            G2D.drawString(var1, SKILL_KEYS[var5], 13, var7, 17);
            var1.setColor(G2D.color(SkillDef.color(var6)));
            G2D.drawString(var1, SkillDef.name(var6), 24, var7, 20);
            var1.setColor(G2D.color(8885420));
            G2D.drawString(var1, SkillDef.desc(var6), var2 - 6, var7, 24);
            if (var8) {
                var1.setColor(G2D.color(8018974));
                G2D.drawString(var1, "Lv4", 24, var7 + var4 - 2, 20);
            }
        }

        return var3 + var4 * 3;
    }

    private void drawLineups(Graphics2D var1, int var2, int var3, int var4) {
        if (this.preview != null) {
            var1.setFont(UiKit.SMALL);
            int var5 = G2D.fontHeight(UiKit.SMALL) + 2;
            if (var5 < 16) {
                var5 = 16;
            }

            if (var4 + var5 * 2 <= this.goY - 2) {
                this.drawLineup(var1, var2, var4, "我方", this.preview.blueHeroIds, 4029408);
                this.drawLineup(var1, var2, var4 + var5, "敌方", this.preview.redHeroIds, 14174280);
            }
        }
    }

    private void drawLineup(Graphics2D var1, int var2, int var3, String var4, int[] var5, int var6) {
        var1.setColor(G2D.color(var6));
        G2D.drawString(var1, var4, 6, var3, 20);
        int var7 = 6 + G2D.stringWidth(UiKit.SMALL,var4) + 6;
        byte var8 = 6;

        for(int var9 = 0; var9 < var5.length; ++var9) {
            String var10 = HeroDef.name(var5[var9]);
            int var11 = var8 * 2 + 4 + G2D.stringWidth(UiKit.SMALL,var10);
            if (var7 + var11 > var2 - 4) {
                break;
            }

            UiKit.avatar(var1, var7 + var8, var3 + G2D.fontHeight(UiKit.SMALL) / 2, var8, var5[var9], 0, true, false);
            var1.setColor(G2D.color(var9 == 0 && var6 == 4029408 ? 15254634 : 8885420));
            G2D.drawString(var1, var10, var7 + var8 * 2 + 3, var3, 20);
            var7 += var11 + 6;
        }

    }
}
