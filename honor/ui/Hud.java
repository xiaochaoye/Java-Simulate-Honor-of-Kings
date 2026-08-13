package honor.ui;

import honor.battle.BattleWorld;
import honor.battle.ShopCatalog;
import honor.entity.Hero;
import honor.entity.Jungle;
import honor.hero.HeroDef;
import honor.skill.SkillDef;
import honor.util.SettingsStore;
import java.awt.Graphics2D;
import java.awt.Color;
import honor.G2D;

public final class Hud {
    public static final int TOP_H = 40;
    private static final int MINIMAP_SIZE = 58;
    private static final String[] SKILL_KEYS = new String[]{"1", "3", "7"};
    private final int[] hitSlot = new int[16];
    private final int[] hitX = new int[16];
    private final int[] hitY = new int[16];
    private final int[] hitR = new int[16];
    private int hitCount;
    private BattleWorld hitWorld;

    public Hud() {
    }

    public void render(Graphics2D var1, int var2, int var3, BattleWorld var4, int var5) {
        this.hitCount = 0;
        this.hitWorld = var4;
        Hero var6 = var4.getLocalHero();
        this.drawTopBar(var1, var2, var4, var6, var5);
        var4.drawMinimap(var1, 4, 44, 58);
        byte var7 = 106;
        this.drawBossStatus(var1, var4, 4, var7);
        int var8 = var4.getResult();
        if (var8 == 0) {
            this.drawAllyList(var1, var4, var6, 4, var7 + 18);
            this.drawItemSlots(var1, 6, var3 - 20, var6);
            this.drawTipBanner(var1, var3, var4);
            if (!SettingsStore.get().isTouchMode()) {
                this.drawSkillCluster(var1, var2, var3, var4, var6);
            }

            if (var4.isShopOpen()) {
                this.drawShopPanel(var1, var2, var3, var4, var6);
            }

            if (var4.isSkillUpgradeMode()) {
                this.drawSkillUpgradePanel(var1, var2, var3, var6);
            }

            if (!var6.alive) {
                this.drawRespawnBanner(var1, var2, var3, var4);
            }
        } else {
            this.drawResultPanel(var1, var2, var3, var4, var6);
        }

    }

    public void drawMatchDetails(Graphics2D var1, int var2, int var3, BattleWorld var4) {
        int var5 = var2 <= 200 ? 3 : 6;
        int var8 = var2 - var5 * 2;
        int var9 = var3 - var5 * 2;
        int var10 = G2D.fontHeight(UiKit.SMALL) + 9;
        UiKit.panel(var1, var5, var5, var8, var9, 15254634);
        var1.setFont(UiKit.SMALL);
        var1.setColor(G2D.color(15254634));
        G2D.drawString(var1, "对局详情", var2 / 2, var5 + 3, 17);
        var1.setColor(G2D.color(8885420));
        G2D.drawString(var1, "I / # 返回", var5 + var8 - 5, var5 + 3, 24);
        int var11 = var5 + var10;
        int var12 = var9 - var10 - 3;
        if (var2 >= var3) {
            byte var13 = 4;
            int var14 = (var8 - var13 - 4) / 2;
            this.drawTeamDetails(var1, var5 + 2, var11, var14, var12, var4, 0);
            this.drawTeamDetails(var1, var5 + 2 + var14 + var13, var11, var14, var12, var4, 1);
        } else {
            byte var15 = 3;
            int var16 = (var12 - var15) / 2;
            this.drawTeamDetails(var1, var5 + 2, var11, var8 - 4, var16, var4, 0);
            this.drawTeamDetails(var1, var5 + 2, var11 + var16 + var15, var8 - 4, var16, var4, 1);
        }

    }

    private void drawTeamDetails(Graphics2D var1, int var2, int var3, int var4, int var5, BattleWorld var6, int var7) {
        int var8 = var6.getTeamSize();
        int var9 = G2D.fontHeight(UiKit.SMALL) + 2;
        int var10 = (var5 - var9) / var8;
        if (var10 < 12) {
            var10 = 12;
        }

        int var11 = 0;

        for(int var12 = 0; var12 < var8; ++var12) {
            var11 += ShopCatalog.economy(var6.getTeamHero(var7, var12));
        }

        Hero var29 = var6.getLocalHero();
        int var13 = var7 == 0 ? 7385343 : 16744576;
        var1.setFont(UiKit.SMALL);
        var1.setColor(G2D.color(var13));
        String var14 = var29 != null && var7 == var29.team ? "我方" : "敌方";
        String var15 = var7 == 0 ? "蓝方" : "红方";
        G2D.drawString(var1, var14 + " / " + var15, var2 + 2, var3, 20);
        var1.setColor(G2D.color(15254634));
        G2D.drawString(var1, "总经济 " + var11, var2 + var4 - 2, var3, 24);
        int var16 = var3 + var9;

        for(int var17 = 0; var17 < var8; ++var17) {
            Hero var18 = var6.getTeamHero(var7, var17);
            int var19 = var16 + var17 * var10;
            int var20 = var19 + var10 - 1;
            if (var18 == var29) {
                var1.setColor(G2D.color(2374744));
                var1.fillRect(var2, var19, var4, var10 - 1);
            }

            var1.setColor(G2D.color(3686480));
            var1.drawLine(var2, var20, var2 + var4 - 1, var20);
            int var21 = var10 >= 20 ? 7 : 5;
            int var22 = var19 + var10 / 2;
            UiKit.avatar(var1, var2 + var21 + 2, var22, var21, var18.getHeroId(), 0, var18.alive, var18 == var29);
            int var23 = var2 + var21 * 2 + 6;
            int var24 = ShopCatalog.economy(var18);
            String var25 = var18.heroKills + "/" + var18.deaths + "/兵" + var18.minionKills;
            String var26 = var24 + "g";
            boolean var27 = var8 >= 5 && var4 >= 200 && var10 >= 20;
            var1.setColor(G2D.color(var18.alive ? 14476530 : 8885420));
            G2D.drawString(var1, HeroDef.name(var18.getHeroId()) + " Lv" + var18.level, var23, var19, 20);
            var1.setColor(G2D.color(15254634));
            G2D.drawString(var1, var26, var2 + var4 - 2, var19, 24);
            if (var27) {
                int var28 = G2D.stringWidth(UiKit.SMALL,var26);
                var1.setColor(G2D.color(8885420));
                G2D.drawString(var1, var25, var2 + var4 - var28 - 7, var19, 24);
            }

            if (var10 >= 20) {
                if (!var27) {
                    var1.setColor(G2D.color(8885420));
                    G2D.drawString(var1, var25, var23, var19 + G2D.fontHeight(UiKit.SMALL), 20);
                }

                this.drawDetailItems(var1, var2 + var4 - 2, var19 + G2D.fontHeight(UiKit.SMALL) + 2, var18, 5);
            } else {
                int var30 = G2D.stringWidth(UiKit.SMALL,HeroDef.name(var18.getHeroId()) + " Lv" + var18.level);
                var1.setColor(G2D.color(8885420));
                G2D.drawString(var1, var25, var23 + var30 + 3, var19, 20);
                this.drawDetailItems(var1, var2 + var4 - 2, var20 - 3, var18, 3);
            }
        }

    }

    private void drawDetailItems(Graphics2D var1, int var2, int var3, Hero var4, int var5) {
        int var6 = var5 + 2;
        int var7 = var2 - var6 * 6;
        int var8 = 0;

        for(int var9 = 0; var9 < 12 && var8 < 6; ++var9) {
            if ((var4.equipMask & 1 << var9) != 0) {
                int var10 = var7 + var8 * var6;
                var1.setColor(G2D.color(itemColor(var9)));
                var1.fillRect(var10, var3, var5, var5);
                var1.setColor(G2D.color(8018974));
                var1.drawRect(var10, var3, var5, var5);
                ++var8;
            }
        }

        while(var8 < 6) {
            int var11 = var7 + var8 * var6;
            var1.setColor(G2D.color(4343896));
            var1.drawRect(var11, var3, var5, var5);
            ++var8;
        }

    }

    private void drawTopBar(Graphics2D var1, int var2, BattleWorld var3, Hero var4, int var5) {
        UiKit.gradientV(var1, 0, 0, var2, 40, 1845570, 725024);
        var1.setColor(G2D.color(8018974));
        var1.fillRect(0, 39, var2, 1);
        var1.setFont(UiKit.SMALL);
        this.drawPlayerBlock(var1, var4);
        this.drawCenterColumn(var1, var2, var3, var5);
        this.drawEnemyRow(var1, var2, var3, var4);
    }

    private void drawPlayerBlock(Graphics2D var1, Hero var2) {
        UiKit.avatar(var1, 12, 14, 10, var2.getHeroId(), var2.level, var2.alive, false);
        this.rememberAvatarHit(this.slotOf(var2), 12, 14, 10);
        int var3 = var2.maxHp > 0 ? var2.hp * 100 / var2.maxHp : 0;
        UiKit.bar(var1, 26, 4, 62, 7, var3, 4177759, 8842148);
        int var4 = var2.maxMp > 0 ? var2.mp * 100 / var2.maxMp : 0;
        UiKit.bar(var1, 26, 13, 62, 4, var4, 4161496, 9224447);
        UiKit.coin(var1, 30, 24);
        var1.setFont(UiKit.SMALL);
        var1.setColor(G2D.color(15254634));
        G2D.drawString(var1, String.valueOf(var2.gold), 36, 19, 20);
        var1.setColor(G2D.color(8885420));
        G2D.drawString(var1, var2.heroKills + "/" + var2.deaths + "/" + var2.minionKills, 72, 19, 20);
        this.drawBuffIcons(var1, 4, 31, var2);
    }

    private void drawBuffIcons(Graphics2D var1, int var2, int var3, Hero var4) {
        int var5 = this.drawBuffIcon(var1, var2, var3, var4.redBuffLeft > 0, 16742972, "红");
        var5 = this.drawBuffIcon(var1, var5, var3, var4.blueBuffLeft > 0, 7317759, "蓝");
        var5 = this.drawBuffIcon(var1, var5, var3, var4.speedBuffLeft > 0, 9240480, "速");
        var5 = this.drawBuffIcon(var1, var5, var3, var4.shield > 0, 15266047, "盾");
        this.drawBuffIcon(var1, var5, var3, var4.slowLeft > 0, 11575488, "缓");
    }

    private int drawBuffIcon(Graphics2D var1, int var2, int var3, boolean var4, int var5, String var6) {
        if (!var4) {
            return var2;
        } else {
            var1.setColor(G2D.color(var5));
            var1.fillRoundRect(var2, var3, 8, 8, 2, 2);
            var1.setColor(G2D.color(329486));
            var1.drawRoundRect(var2, var3, 8, 8, 2, 2);
            return var2 + 10;
        }
    }

    private void drawCenterColumn(Graphics2D var1, int var2, BattleWorld var3, int var4) {
        int var5 = var2 / 2;
        int var6 = var4 / 15;
        var1.setFont(UiKit.SMALL);
        var1.setColor(G2D.color(14476530));
        G2D.drawString(var1, UiKit.pad2(var6 / 60) + ":" + UiKit.pad2(var6 % 60), var5, 1, 17);
        String var7 = String.valueOf(var3.getTeamKills(0));
        String var8 = String.valueOf(var3.getTeamKills(1));
        int var9 = G2D.stringWidth(UiKit.SMALL,var7);
        var1.setColor(G2D.color(14476530));
        G2D.drawString(var1, ":", var5, 13, 17);
        var1.setColor(G2D.color(8175871));
        G2D.drawString(var1, var7, var5 - 3 - var9, 13, 20);
        var1.setColor(G2D.color(16748688));
        G2D.drawString(var1, var8, var5 + 4, 13, 20);
        var1.setColor(G2D.color(8885420));
        G2D.drawString(var1, "波" + var3.getWave(), var5, 25, 17);
    }

    private void drawEnemyRow(Graphics2D var1, int var2, BattleWorld var3, Hero var4) {
        int var5 = var3.getTeamSize();
        int var6 = 1 - var4.team;
        int var7 = (var2 / 2 - 14) / var5;
        if (var7 > 22) {
            var7 = 22;
        }

        int var8 = var7 / 2 - 2;
        if (var8 > 9) {
            var8 = 9;
        }

        for(int var9 = 0; var9 < var5; ++var9) {
            Hero var10 = var3.getTeamHero(var6, var9);
            int var11 = var2 - 6 - var8 - (var5 - 1 - var9) * var7;
            UiKit.avatar(var1, var11, 4 + var8, var8, var10.getHeroId(), 0, var10.alive, false);
            this.rememberAvatarHit(this.slotOf(var10), var11, 4 + var8, var8);
            int var12 = var10.alive && var10.maxHp > 0 ? var10.hp * 100 / var10.maxHp : 0;
            var1.setColor(G2D.color(329486));
            var1.fillRect(var11 - var8, 6 + var8 * 2, var8 * 2, 4);
            var1.setColor(G2D.color(var10.alive ? 14174280 : 4868693));
            var1.fillRect(var11 - var8, 6 + var8 * 2, var8 * 2 * var12 / 100, 4);
            var1.setFont(UiKit.SMALL);
            var1.setColor(G2D.color(8885420));
            G2D.drawString(var1, String.valueOf(var10.level), var11, 11 + var8 * 2, 17);
        }

    }

    private void drawAllyList(Graphics2D var1, BattleWorld var2, Hero var3, int var4, int var5) {
        int var6 = var2.getTeamSize();
        var1.setFont(UiKit.SMALL);
        int var7 = 0;

        for(int var8 = 0; var8 < var6; ++var8) {
            Hero var9 = var2.getTeamHero(var3.team, var8);
            if (var9 != var3) {
                int var10 = var5 + var7 * 14;
                UiKit.avatar(var1, var4 + 6, var10 + 6, 6, var9.getHeroId(), 0, var9.alive, false);
                this.rememberAvatarHit(this.slotOf(var9), var4 + 6, var10 + 6, 6);
                int var11 = var9.alive && var9.maxHp > 0 ? var9.hp * 100 / var9.maxHp : 0;
                var1.setColor(G2D.color(329486));
                var1.fillRect(var4 + 14, var10 + 3, 28, 6);
                var1.setColor(G2D.color(var9.alive ? 4177759 : 4868693));
                var1.fillRect(var4 + 14, var10 + 3, 28 * var11 / 100, 6);
                var1.setColor(G2D.color(8885420));
                G2D.drawString(var1, String.valueOf(var9.level), var4 + 44, var10 + 1, 20);
                ++var7;
            }
        }

    }

    private void drawBossStatus(Graphics2D var1, BattleWorld var2, int var3, int var4) {
        var1.setFont(UiKit.SMALL);
        this.drawBossLine(var1, var2.getBoss(3), var3, var4, "暴", 16751178);
        this.drawBossLine(var1, var2.getBoss(4), var3 + 32, var4, "主", 12618495);
    }

    private void drawBossLine(Graphics2D var1, Jungle var2, int var3, int var4, String var5, int var6) {
        if (var2 != null) {
            var1.setColor(G2D.color(var2.alive ? var6 : 3817552));
            var1.fillRoundRect(var3, var4, 10, 10, 3, 3);
            var1.setColor(G2D.color(var2.alive ? var6 : 8885420));
            G2D.drawString(var1, var2.alive ? "在" : String.valueOf(var2.getRespawnSeconds()), var3 + 13, var4 - 1, 20);
        }
    }

    private void drawSkillCluster(Graphics2D var1, int var2, int var3, BattleWorld var4, Hero var5) {
        boolean var6 = var5.alive;
        this.drawSkillButton(var1, var2 - 32, var3 - 40, 21, "普攻", 4029408, var5.atkCdLeft, var5.atkCooldown, var6 && var5.atkCdLeft <= 0 && var5.stunLeft <= 0, false);
        int var7 = var5.attackSpeed10();
        var1.setFont(UiKit.SMALL);
        var1.setColor(G2D.color(14215423));
        G2D.drawString(var1, var7 / 10 + "." + var7 % 10 + "/秒", var2 - 32, var3 - 18, 17);
        this.drawSkillSlot(var1, var2 - 76, var3 - 32, 15, var5, 0, var6);
        this.drawSkillSlot(var1, var2 - 64, var3 - 72, 15, var5, 1, var6);
        this.drawSkillSlot(var1, var2 - 26, var3 - 88, 17, var5, 2, var6);
        this.drawSkillButton(var1, var2 - 104, var3 - 60, 11, "9", 3055247, var5.recallLeft, 45, var6, false);
        boolean var8 = var4.isLocalInFountain() && var6;
        this.drawSkillButton(var1, var2 - 110, var3 - 22, 11, "0", var8 ? 8018974 : 3817292, 0, 1, var8, false);
    }

    private void drawSkillSlot(Graphics2D var1, int var2, int var3, int var4, Hero var5, int var6, boolean var7) {
        int var8 = var5.skillId(var6);
        int var9 = var5.skillLv[var6];
        boolean var10 = var9 > 0;
        boolean var11 = var7 && var10 && var5.canCast(var6);
        int var12 = var10 ? SkillDef.cooldown(var8, var9) : SkillDef.cooldown(var8, 1);
        this.drawSkillButton(var1, var2, var3, var4, SKILL_KEYS[var6], SkillDef.color(var8), var5.skillCd[var6], var12, var11, !var10);
        var1.setFont(UiKit.SMALL);
        if (!var10) {
            var1.setColor(G2D.color(8018974));
            G2D.drawString(var1, "Lv" + SkillDef.unlockLevel(var6), var2, var3 + var4 - 2, 17);
        } else {
            var1.setColor(G2D.color(15254634));
            G2D.drawString(var1, String.valueOf(var9), var2 + var4 - 2, var3 - var4 - 1, 17);
        }

        if (var5.canUpgradeSkill(var6)) {
            var1.setColor(G2D.color(6750088));
            G2D.drawString(var1, "+", var2 - var4 + 2, var3 - var4 - 1, 20);
        }

    }

    private void drawSkillButton(Graphics2D var1, int var2, int var3, int var4, String var5, int var6, int var7, int var8, boolean var9, boolean var10) {
        int var11 = var9 ? var6 : UiKit.shade(var6, -60);
        if (var10) {
            var11 = 2764604;
        }

        var1.setColor(G2D.color(329486));
        var1.fillArc(var2 - var4 - 2, var3 - var4 - 2, (var4 + 2) * 2, (var4 + 2) * 2, 0, 360);
        var1.setColor(G2D.color(var11));
        var1.fillArc(var2 - var4, var3 - var4, var4 * 2, var4 * 2, 0, 360);
        var1.setColor(G2D.color(UiKit.shade(var11, 40)));
        var1.drawArc(var2 - var4 + 2, var3 - var4 + 2, (var4 - 2) * 2, (var4 - 2) * 2, 40, 120);
        if (var7 > 0 && var8 > 0) {
            int var12 = 360 * var7 / var8;
            if (var12 > 360) {
                var12 = 360;
            }

            var1.setColor(G2D.color(1054760));
            var1.fillArc(var2 - var4, var3 - var4, var4 * 2, var4 * 2, 90, -var12);
        }

        var1.setColor(G2D.color(var9 ? 15254634 : 8018974));
        var1.drawArc(var2 - var4 - 1, var3 - var4 - 1, (var4 + 1) * 2, (var4 + 1) * 2, 0, 360);
        var1.setFont(UiKit.SMALL);
        if (var7 > 0) {
            var1.setColor(G2D.color(16777215));
            G2D.drawString(var1, this.cooldownText(var7), var2, var3 - G2D.fontHeight(UiKit.SMALL) / 2 - 1, 17);
            var1.setColor(G2D.color(8885420));
            G2D.drawString(var1, var5, var2, var3 + var4 - G2D.fontHeight(UiKit.SMALL), 17);
        } else {
            var1.setColor(G2D.color(var9 ? 14476530 : 8885420));
            G2D.drawString(var1, var5, var2, var3 - G2D.fontHeight(UiKit.SMALL) / 2, 17);
        }

    }

    private String cooldownText(int var1) {
        if (var1 >= 15) {
            return String.valueOf((var1 + 14) / 15);
        } else {
            int var2 = (var1 * 10 + 14) / 15;
            return var2 >= 10 ? "1.0" : "0." + var2;
        }
    }

    private void drawItemSlots(Graphics2D var1, int var2, int var3, Hero var4) {
        int var5 = 0;

        for(int var6 = 0; var6 < 12; ++var6) {
            if (ShopCatalog.owned(var4, var6) && var5 < 6) {
                int var7 = var2 + var5 * 17;
                var1.setColor(G2D.color(329486));
                var1.fillRoundRect(var7 - 1, var3 - 1, 16, 16, 4, 4);
                var1.setColor(G2D.color(itemColor(var6)));
                var1.fillRoundRect(var7, var3, 14, 14, 3, 3);
                var1.setColor(G2D.color(UiKit.shade(itemColor(var6), 70)));
                var1.fillRect(var7 + 2, var3 + 2, 10, 2);
                var1.setColor(G2D.color(ShopCatalog.isAdvanced(var6) ? 15254634 : 8018974));
                var1.drawRoundRect(var7 - 1, var3 - 1, 16, 16, 4, 4);
                ++var5;
            }
        }

        while(var5 < 6) {
            int var8 = var2 + var5 * 17;
            var1.setColor(G2D.color(329486));
            var1.fillRoundRect(var8 - 1, var3 - 1, 16, 16, 4, 4);
            var1.setColor(G2D.color(2304570));
            var1.fillRoundRect(var8, var3, 14, 14, 3, 3);
            var1.setColor(G2D.color(8018974));
            var1.drawRoundRect(var8 - 1, var3 - 1, 16, 16, 4, 4);
            ++var5;
        }

    }

    private void drawTipBanner(Graphics2D var1, int var2, BattleWorld var3) {
        if (!var3.isShopOpen() && !var3.isSkillUpgradeMode()) {
            Hero var4 = var3.getLocalHero();
            String var5;
            int var6;
            if (var4 != null && var4.skillPoints > 0) {
                var5 = SettingsStore.get().isTouchMode() ? "技能点 " + var4.skillPoints + "  点击技能+加点" : "技能点 " + var4.skillPoints + "  按1/3/7加点";
                var6 = 15254634;
            } else if (var3.isLocalInFountain()) {
                var5 = "泉水恢复中   0 开商店";
                var6 = 8842148;
            } else if (var3.isEnemyCrystalShielded()) {
                var5 = "拆掉高地塔才能破水晶";
                var6 = 8885420;
            } else {
                var5 = "水晶护盾已破，进攻！";
                var6 = 15254634;
            }

            var1.setFont(UiKit.SMALL);
            int var7 = G2D.stringWidth(UiKit.SMALL,var5) + 10;
            int var8 = G2D.fontHeight(UiKit.SMALL) + 4;
            byte var9 = 4;
            int var10 = var2 - 24 - var8;
            var1.setColor(G2D.color(725024));
            var1.fillRoundRect(var9, var10, var7, var8, 6, 6);
            var1.setColor(G2D.color(8018974));
            var1.drawRoundRect(var9, var10, var7, var8, 6, 6);
            var1.setColor(G2D.color(var6));
            G2D.drawString(var1, var5, var9 + 5, var10 + 2, 20);
        }
    }

    private void drawSkillUpgradePanel(Graphics2D var1, int var2, int var3, Hero var4) {
        int var5 = var2 - 40;
        byte var6 = 78;
        byte var7 = 20;
        int var8 = var3 / 2 - var6 / 2;
        var1.setColor(G2D.color(725024));
        var1.fillRoundRect(var7, var8, var5, var6, 8, 8);
        var1.setColor(G2D.color(15254634));
        var1.drawRoundRect(var7, var8, var5, var6, 8, 8);
        var1.setFont(UiKit.SMALL);
        var1.setColor(G2D.color(15254634));
        G2D.drawString(var1, "技能加点  剩余" + var4.skillPoints, var2 / 2, var8 + 6, 17);

        for(int var9 = 0; var9 < 3; ++var9) {
            int var10 = var4.skillId(var9);
            int var11 = var8 + 22 + var9 * 16;
            boolean var12 = var4.canUpgradeSkill(var9);
            var1.setColor(G2D.color(var12 ? SkillDef.color(var10) : 8885420));
            String var13 = SKILL_KEYS[var9] + " " + SkillDef.name(var10) + " Lv" + var4.skillLv[var9] + "/" + SkillDef.maxRank(var9) + (var12 ? " +" : "");
            G2D.drawString(var1, var13, var7 + 10, var11, 20);
        }

    }

    private void drawRespawnBanner(Graphics2D var1, int var2, int var3, BattleWorld var4) {
        var1.setFont(UiKit.SMALL);
        int var5 = var4.getLocalReviveSeconds();
        String var6 = var4.getDeathKillerName();
        if (var6 == null || var6.length() == 0) {
            var6 = "敌人";
        }

        String var7 = "你被击败了";
        String var8 = "击杀者  " + var6;
        String var9 = "致死伤害 " + var4.getDeathBlowDamage() + "   承伤 " + var4.getDeathDamageTaken();
        String var10 = var5 + " 秒后复活";
        byte var11 = 10;
        int var12 = G2D.fontHeight(UiKit.SMALL);
        int var13 = var2 - 36;
        if (var13 < 160) {
            var13 = var2 - 16;
        }

        int var14 = var12 * 4 + var11 * 2 + 12;
        int var15 = (var2 - var13) / 2;
        int var16 = var3 / 2 - var14 / 2 - 8;
        var1.setColor(G2D.color(1052696));
        var1.fillRoundRect(var15, var16, var13, var14, 10, 10);
        var1.setColor(G2D.color(14174280));
        var1.drawRoundRect(var15, var16, var13, var14, 10, 10);
        var1.setColor(G2D.color(16740464));
        G2D.drawString(var1, var7, var2 / 2, var16 + var11, 17);
        var1.setColor(G2D.color(15254634));
        G2D.drawString(var1, var8, var2 / 2, var16 + var11 + var12 + 4, 17);
        var1.setColor(G2D.color(8885420));
        G2D.drawString(var1, var9, var2 / 2, var16 + var11 + (var12 + 4) * 2, 17);
        var1.setColor(G2D.color(16777215));
        G2D.drawString(var1, var10, var2 / 2, var16 + var11 + (var12 + 4) * 3, 17);
    }

    private void drawShopPanel(Graphics2D var1, int var2, int var3, BattleWorld var4, Hero var5) {
        var1.setFont(UiKit.SMALL);
        int var6 = G2D.fontHeight(UiKit.SMALL) + 1;
        int var7 = var6 * 3;
        int var8 = var6 * 2 + 8;
        int var9 = var2 - (var2 <= 200 ? 12 : 24);
        int var10 = var8 + var7 * 3 + 7;
        int var11 = (var2 - var9) / 2;
        int var12 = (var3 - var10) / 2;
        int var13 = var4.getShopPage();
        UiKit.panel(var1, var11, var12, var9, var10, 15254634);
        var1.setColor(G2D.color(15254634));
        G2D.drawString(var1, "装备商店 " + (var13 + 1) + "/" + 4, var11 + var9 / 2, var12 + 4, 17);
        UiKit.coin(var1, var11 + 10, var12 + 5 + var6);
        var1.setColor(G2D.color(15254634));
        G2D.drawString(var1, String.valueOf(var5.gold), var11 + 18, var12 + var6, 20);
        var1.setColor(G2D.color(8885420));
        G2D.drawString(var1, "5翻页  0关闭", var11 + var9 - 7, var12 + var6, 24);

        for(int var14 = 0; var14 < 3; ++var14) {
            int var15 = ShopCatalog.itemAt(var13, var14);
            if (var15 >= 0) {
                int var16 = var12 + var8 + var14 * var7;
                boolean var17 = ShopCatalog.owned(var5, var15);
                boolean var18 = ShopCatalog.hasRecipe(var5, var15);
                boolean var19 = ShopCatalog.canBuy(var5, var15);
                int var20 = ShopCatalog.price(var15);
                var1.setColor(G2D.color(var17 ? 2764864 : (var19 ? itemColor(var15) : 2764864)));
                var1.fillRoundRect(var11 + 7, var16 + 1, 16, 16, 4, 4);
                var1.setColor(G2D.color(var17 ? 8018974 : 15254634));
                var1.drawRoundRect(var11 + 7, var16 + 1, 16, 16, 4, 4);
                var1.setColor(G2D.color(14476530));
                G2D.drawString(var1, SKILL_KEYS[var14], var11 + 15, var16 + 1, 17);
                var1.setColor(G2D.color(var17 ? 8885420 : 14476530));
                G2D.drawString(var1, ShopCatalog.name(var15), var11 + 29, var16, 20);
                var1.setColor(G2D.color(var17 ? 8885420 : (var19 ? 15254634 : 10117216)));
                String var21 = var17 ? "已购" : (!var18 ? "缺配件" : var20 + "g");
                G2D.drawString(var1, var21, var11 + var9 - 7, var16, 24);
                var1.setColor(G2D.color(var17 ? 8885420 : 11520232));
                G2D.drawString(var1, ShopCatalog.statText(var15), var11 + 29, var16 + var6, 20);
                var1.setColor(G2D.color(var18 ? 8885420 : 13668448));
                String var22 = ShopCatalog.isAdvanced(var15) ? "配:" + ShopCatalog.recipeText(var15) : "基础装备";
                G2D.drawString(var1, var22, var11 + 29, var16 + var6 * 2, 20);
            }
        }

    }

    private void drawResultPanel(Graphics2D var1, int var2, int var3, BattleWorld var4, Hero var5) {
        boolean var6 = var4.getResult() == 3;
        boolean var7 = !var6 && var4.getResult() == 1 == (var5.team == 0);
        int var8 = var6 ? 8885420 : (var7 ? 15254634 : 14174280);
        int var9 = var2 - 32;
        int var10 = SettingsStore.get().isTouchMode() ? 132 : 116;
        byte var11 = 16;
        int var12 = (var3 - var10) / 2;
        UiKit.panel(var1, var11, var12, var9, var10, var8);
        var1.setFont(UiKit.LARGE);
        var1.setColor(G2D.color(var8));
        G2D.drawString(var1, var6 ? "DRAW" : (var7 ? "VICTORY" : "DEFEAT"), var2 / 2, var12 + 10, 17);
        var1.setFont(UiKit.SMALL);
        int var13 = G2D.fontHeight(UiKit.SMALL) + 2;
        int var14 = var12 + 14 + G2D.fontHeight(UiKit.LARGE);
        var1.setColor(G2D.color(14476530));
        G2D.drawString(var1, var6 ? "对局超时，双方未分胜负" : (var7 ? "敌方水晶已摧毁" : "己方水晶被摧毁"), var2 / 2, var14, 17);
        var1.setColor(G2D.color(8885420));
        G2D.drawString(var1, "团队比分 " + var4.getTeamKills(0) + " : " + var4.getTeamKills(1), var2 / 2, var14 + var13, 17);
        G2D.drawString(var1, HeroDef.name(var5.getHeroId()) + "  " + var5.heroKills + "/" + var5.deaths + "/" + var5.minionKills + "   Lv" + var5.level, var2 / 2, var14 + var13 * 2, 17);
        if (SettingsStore.get().isTouchMode()) {
            int var15 = var2 / 2 - 27;
            int var16 = var12 + var10 - 24 - 10;
            UiKit.drawBackButton(var1, var15, var16, false);
        } else {
            var1.setColor(G2D.color(15254634));
            G2D.drawString(var1, "5 返回主菜单    ESC 退出", var2 / 2, var14 + var13 * 3, 17);
        }

    }

    public int heroAvatarAt(int var1, int var2) {
        for(int var3 = 0; var3 < this.hitCount; ++var3) {
            int var4 = var1 - this.hitX[var3];
            int var5 = var2 - this.hitY[var3];
            int var6 = this.hitR[var3] + 4;
            if (var4 * var4 + var5 * var5 <= var6 * var6) {
                return this.hitSlot[var3];
            }
        }

        return -1;
    }

    private void rememberAvatarHit(int var1, int var2, int var3, int var4) {
        if (var1 >= 0 && this.hitCount < this.hitSlot.length) {
            this.hitSlot[this.hitCount] = var1;
            this.hitX[this.hitCount] = var2;
            this.hitY[this.hitCount] = var3;
            this.hitR[this.hitCount] = var4;
            ++this.hitCount;
        }
    }

    private int slotOf(Hero var1) {
        if (this.hitWorld != null && var1 != null) {
            int var2 = this.hitWorld.getHeroCount();

            for(int var3 = 0; var3 < var2; ++var3) {
                if (this.hitWorld.getHeroAt(var3) == var1) {
                    return var3;
                }
            }

            return -1;
        } else {
            return -1;
        }
    }

    private static int itemColor(int var0) {
        if (var0 != 0 && var0 != 4 && var0 != 6 && var0 != 7) {
            if (var0 != 2 && var0 != 8 && var0 != 11) {
                return var0 != 3 && var0 != 10 ? 10119848 : 4892778;
            } else {
                return 4881088;
            }
        } else {
            return 12603466;
        }
    }
}
